package it.govpay.portal.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.govpay.pendenze.client.api.PendenzeApi;
import it.govpay.pendenze.client.model.NuovaPendenza;
import it.govpay.pendenze.client.model.PendenzaCreata;
import it.govpay.portal.config.SpidUserDetails;
import it.govpay.portal.entity.TipoVersamentoDominio;
import it.govpay.portal.exception.BadRequestException;
import it.govpay.portal.exception.NotFoundException;
import it.govpay.portal.exception.UnprocessableEntityException;
import it.govpay.portal.mapper.PendenzeMapper;
import it.govpay.portal.model.Pendenza;
import it.govpay.portal.repository.TipoVersamentoDominioRepository;
import it.govpay.portal.utils.trasformazioni.Costanti;
import it.govpay.portal.utils.trasformazioni.JsonPathExtractor;
import it.govpay.portal.utils.trasformazioni.RegExpExtractor;
import it.govpay.portal.utils.trasformazioni.TransformationContext;
import it.govpay.portal.utils.trasformazioni.TrasformazioniUtils;
import it.govpay.portal.utils.trasformazioni.exception.TrasformazioneException;

/**
 * Service per la creazione di pendenze tramite form spontaneo.
 * Implementa la logica di trasformazione FreeMarker basata su GovPay.
 */
@Service
@Transactional(readOnly = true)
public class CreaPendenzaService {

    private static final Logger log = LoggerFactory.getLogger(CreaPendenzaService.class);

    private static final String FREEMARKER = "freemarker";

    private final TipoVersamentoDominioRepository tipoVersamentoDominioRepository;
    private final PendenzeApi pendenzeApi;
    private final PendenzeMapper pendenzeMapper;
    private final ObjectMapper objectMapper;

    public CreaPendenzaService(
            TipoVersamentoDominioRepository tipoVersamentoDominioRepository,
            PendenzeApi pendenzeApi,
            PendenzeMapper pendenzeMapper,
            ObjectMapper objectMapper) {
        this.tipoVersamentoDominioRepository = tipoVersamentoDominioRepository;
        this.pendenzeApi = pendenzeApi;
        this.pendenzeMapper = pendenzeMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * Crea una nuova pendenza a partire dai dati del form.
     *
     * @param idDominio identificativo del dominio
     * @param idTipoPendenza identificativo del tipo pendenza
     * @param requestBody dati del form (struttura dinamica)
     * @param idA2A identificativo applicazione (opzionale)
     * @param idPendenza identificativo pendenza (opzionale)
     * @param headers headers HTTP della richiesta
     * @param queryParams parametri query della richiesta
     * @param pathParams parametri path della richiesta
     * @return la pendenza creata
     */
    public Pendenza creaPendenza(
            String idDominio,
            String idTipoPendenza,
            Map<String, Object> requestBody,
            String idA2A,
            String idPendenza,
            Map<String, String> headers,
            Map<String, String> queryParams,
            Map<String, String> pathParams) {

        log.debug("Creazione pendenza: idDominio={}, idTipoPendenza={}", idDominio, idTipoPendenza);

        // 1. Carica la configurazione del tipo versamento dominio
        TipoVersamentoDominio tipoVersamentoDominio = tipoVersamentoDominioRepository
                .findByDominioCodDominioAndTipoVersamentoCodTipoVersamento(idDominio, idTipoPendenza)
                .orElseThrow(() -> new NotFoundException(
                        "Tipo pendenza " + idTipoPendenza + " non trovato per dominio " + idDominio));

        // 2. Verifica che il pagamento spontaneo sia abilitato
        if (!Boolean.TRUE.equals(tipoVersamentoDominio.getPagAbilitato())) {
            throw new BadRequestException(
                    "Pagamento spontaneo non abilitato per tipo pendenza " + idTipoPendenza);
        }

        // 3. Converti il body in JSON string
        String inputJson;
        try {
            inputJson = objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Errore nella serializzazione del body: " + e.getMessage());
        }

        // 4. Applica la trasformazione FreeMarker se configurata
        String trasformatoJson = inputJson;
        if (StringUtils.hasText(tipoVersamentoDominio.getPagTrasformazioneDef())) {
            trasformatoJson = applicaTrasformazione(
                    tipoVersamentoDominio,
                    idDominio,
                    idTipoPendenza,
                    inputJson,
                    headers,
                    queryParams,
                    pathParams);
        }

        // 5. Parse il JSON trasformato in NuovaPendenza
        NuovaPendenza nuovaPendenza;
        try {
            nuovaPendenza = objectMapper.readValue(trasformatoJson, NuovaPendenza.class);
        } catch (JsonProcessingException e) {
            log.error("Errore nel parsing del JSON trasformato: {}", e.getMessage());
            throw new UnprocessableEntityException(
                    "Errore nel parsing del risultato della trasformazione: " + e.getMessage());
        }

        // 6. Imposta valori obbligatori
        nuovaPendenza.setIdDominio(idDominio);
        nuovaPendenza.setIdTipoPendenza(idTipoPendenza);

        // Imposta soggetto pagatore da SPID se autenticato
        impostaSoggettoPagatoreDaSpid(nuovaPendenza);

        // 7. Genera idA2A e idPendenza se non forniti
        String codApplicazione = determinaCodApplicazione(tipoVersamentoDominio, idA2A);
        String codPendenza = determinaCodPendenza(idPendenza);

        // 8. Chiama l'API GovPay per creare la pendenza
        PendenzaCreata pendenzaCreata = chiamaApiGovPay(codApplicazione, codPendenza, nuovaPendenza);

        // 9. Converti la risposta nel modello portal
        return pendenzeMapper.toPendenzaFromCreata(pendenzaCreata, nuovaPendenza);
    }

    /**
     * Applica la trasformazione FreeMarker al body della richiesta.
     */
    private String applicaTrasformazione(
            TipoVersamentoDominio tipoVersamentoDominio,
            String idDominio,
            String idTipoPendenza,
            String inputJson,
            Map<String, String> headers,
            Map<String, String> queryParams,
            Map<String, String> pathParams) {

        String tipoTrasformazione = tipoVersamentoDominio.getPagTrasformazioneTipo();
        if (!FREEMARKER.equalsIgnoreCase(tipoTrasformazione)) {
            log.warn("Tipo trasformazione non supportato: {}, salto trasformazione", tipoTrasformazione);
            return inputJson;
        }

        String templateBase64 = tipoVersamentoDominio.getPagTrasformazioneDef();

        // Rimuovi eventuali virgolette iniziali/finali
        if (templateBase64.startsWith("\"")) {
            templateBase64 = templateBase64.substring(1);
        }
        if (templateBase64.endsWith("\"")) {
            templateBase64 = templateBase64.substring(0, templateBase64.length() - 1);
        }

        // Decodifica il template Base64
        byte[] templateBytes;
        try {
            templateBytes = Base64.getDecoder().decode(templateBase64);
        } catch (IllegalArgumentException e) {
            log.error("Errore nella decodifica Base64 del template: {}", e.getMessage());
            throw new UnprocessableEntityException("Template di trasformazione non valido");
        }

        String templateContent = new String(templateBytes, StandardCharsets.UTF_8);

        // Costruisci il contesto della trasformazione
        Map<String, Object> context = buildTransformationContext(
                idDominio, idTipoPendenza, inputJson, headers, queryParams, pathParams);

        // Applica la trasformazione
        try {
            String result = TrasformazioniUtils.transform(
                    "TrasformazionePendenza_" + idTipoPendenza,
                    templateContent,
                    context);

            log.debug("Trasformazione completata con successo");
            return result;

        } catch (TrasformazioneException e) {
            log.error("Errore durante la trasformazione FreeMarker: {}", e.getMessage());
            throw new UnprocessableEntityException(
                    "Errore durante la trasformazione: " + e.getMessage());
        }
    }

    /**
     * Costruisce il contesto per la trasformazione FreeMarker.
     */
    private Map<String, Object> buildTransformationContext(
            String idDominio,
            String idTipoPendenza,
            String inputJson,
            Map<String, String> headers,
            Map<String, String> queryParams,
            Map<String, String> pathParams) {

        TransformationContext builder = TransformationContext.builder()
                .withDate()
                .withRandomTransactionId()
                .withIdDominio(idDominio)
                .withIdTipoVersamento(idTipoPendenza)
                .withRequestBody(inputJson)
                .withJson(inputJson);

        if (headers != null && !headers.isEmpty()) {
            builder.withHeaders(headers);
        }

        if (queryParams != null && !queryParams.isEmpty()) {
            builder.withQueryParams(queryParams);
        }

        if (pathParams != null && !pathParams.isEmpty()) {
            builder.withPathParams(pathParams);
        }

        // Aggiungi utente SPID se autenticato
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof SpidUserDetails spidUser) {
            Map<String, Object> utenteMap = new HashMap<>();
            utenteMap.put("codiceFiscale", spidUser.getFiscalNumber());
            utenteMap.put("nome", spidUser.getName());
            utenteMap.put("cognome", spidUser.getFamilyName());
            utenteMap.put("email", spidUser.getEmail());
            builder.withUtente(utenteMap);
        }

        return builder.build();
    }

    /**
     * Imposta il soggetto pagatore dai dati SPID se l'utente è autenticato.
     */
    private void impostaSoggettoPagatoreDaSpid(NuovaPendenza nuovaPendenza) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof SpidUserDetails spidUser) {
            // Se non è già impostato un soggetto pagatore, usa i dati SPID
            if (nuovaPendenza.getSoggettoPagatore() == null) {
                it.govpay.pendenze.client.model.Soggetto soggetto =
                        new it.govpay.pendenze.client.model.Soggetto();
                soggetto.setTipo(it.govpay.pendenze.client.model.TipoSoggetto.F);
                soggetto.setIdentificativo(spidUser.getFiscalNumber());

                String anagrafica = spidUser.getName();
                if (StringUtils.hasText(spidUser.getFamilyName())) {
                    anagrafica = anagrafica + " " + spidUser.getFamilyName();
                }
                soggetto.setAnagrafica(anagrafica);
                soggetto.setEmail(spidUser.getEmail());

                nuovaPendenza.setSoggettoPagatore(soggetto);

                log.debug("Soggetto pagatore impostato da SPID: {}", spidUser.getFiscalNumber());
            }
        }
    }

    /**
     * Determina il codice applicazione da usare.
     */
    private String determinaCodApplicazione(TipoVersamentoDominio tipoVersamentoDominio, String idA2A) {
        if (StringUtils.hasText(idA2A)) {
            return idA2A;
        }
        if (StringUtils.hasText(tipoVersamentoDominio.getPagCodApplicazione())) {
            return tipoVersamentoDominio.getPagCodApplicazione();
        }
        throw new BadRequestException(
                "Codice applicazione non specificato e non configurato per il tipo pendenza");
    }

    /**
     * Determina il codice pendenza da usare.
     */
    private String determinaCodPendenza(String idPendenza) {
        if (StringUtils.hasText(idPendenza)) {
            return idPendenza;
        }
        // Genera un ID univoco
        return UUID.randomUUID().toString().replace("-", "").substring(0, 20);
    }

    /**
     * Chiama l'API GovPay per creare la pendenza.
     */
    private PendenzaCreata chiamaApiGovPay(String idA2A, String idPendenza, NuovaPendenza nuovaPendenza) {
        log.debug("Chiamata API GovPay: idA2A={}, idPendenza={}", idA2A, idPendenza);

        try {
            PendenzaCreata result = pendenzeApi.addPendenza(idA2A, idPendenza, false, null, nuovaPendenza);

            log.info("Pendenza creata con successo: idDominio={}, numeroAvviso={}",
                    result.getIdDominio(), result.getNumeroAvviso());

            return result;

        } catch (HttpClientErrorException.BadRequest e) {
            log.error("Errore BadRequest da GovPay: {}", e.getResponseBodyAsString());
            throw new BadRequestException("Errore nella creazione della pendenza: " + e.getMessage());

        } catch (HttpClientErrorException.UnprocessableEntity e) {
            log.error("Errore UnprocessableEntity da GovPay: {}", e.getResponseBodyAsString());
            throw new UnprocessableEntityException(
                    "Dati pendenza non validi: " + e.getResponseBodyAsString());

        } catch (RestClientException e) {
            log.error("Errore nella chiamata API GovPay: {}", e.getMessage(), e);
            throw new RuntimeException("Errore nella comunicazione con GovPay: " + e.getMessage(), e);
        }
    }
}
