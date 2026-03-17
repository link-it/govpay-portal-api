package it.govpay.portal.security.hardening.matcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;

import it.govpay.portal.repository.VersamentoRepository;
import it.govpay.common.configurazione.model.Hardening;
import it.govpay.portal.service.ConfigurazioneService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * RequestMatcher specializzato per gli avvisi.
 * Applica controlli in cascata:
 * 1. Se utente autenticato -> accesso consentito
 * 2. Se presente UUID valido (verificato su DB) -> accesso consentito
 * 3. Altrimenti -> verifica ReCaptcha
 */
public class AvvisiRequestMatcher extends HardeningRequestMatcher {

    private static final Logger log = LoggerFactory.getLogger(AvvisiRequestMatcher.class);

    private static final String PARAMETER_UUID = "UUID";

    // Pattern per estrarre idDominio e numeroAvviso/IUV dal path
    // es: /pendenze/{idDominio}/{numeroAvviso}/avviso
    // Usa find() invece di matches() per evitare ReDoS con .*
    private static final Pattern AVVISI_PATH_PATTERN = Pattern.compile("/pendenze/([^/]+)/([^/]+)/avviso");

    private final VersamentoRepository versamentoRepository;

    public AvvisiRequestMatcher(String pattern, ConfigurazioneService configurazioneService,
            VersamentoRepository versamentoRepository) {
        super(pattern, configurazioneService);
        this.versamentoRepository = versamentoRepository;
    }

    public AvvisiRequestMatcher(String pattern, HttpMethod method, ConfigurazioneService configurazioneService,
            VersamentoRepository versamentoRepository) {
        super(pattern, method, configurazioneService);
        this.versamentoRepository = versamentoRepository;
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        // Prima verifica se il path corrisponde
        if (!getPathMatcher().matches(request)) {
            return false;
        }

        // Se l'utente è autenticato, consenti l'accesso
        if (isAuthenticated()) {
            log.debug("Utente autenticato, accesso consentito a {}", request.getRequestURI());
            return true;
        }

        // Estrai idDominio e numeroAvviso dal path
        String[] avvisoParams = extractAvvisoParams(request);
        if (avvisoParams == null) {
            log.warn("Impossibile estrarre parametri avviso dal path: {}", request.getPathInfo());
            return false;
        }

        String idDominio = avvisoParams[0];
        String numeroAvvisoOrIuv = avvisoParams[1];

        // Applica controlli hardening
        return applyAvvisoHardening(request, idDominio, numeroAvvisoOrIuv);
    }

    private String[] extractAvvisoParams(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = request.getRequestURI();
        }

        Matcher matcher = AVVISI_PATH_PATTERN.matcher(pathInfo);
        if (matcher.find()) {
            return new String[]{matcher.group(1), matcher.group(2)};
        }
        return null;
    }

    private boolean applyAvvisoHardening(HttpServletRequest request, String idDominio, String numeroAvvisoOrIuv) {
        try {
            Hardening hardening = getConfigurazioneService().getHardening();

            if (!hardening.isAbilitato()) {
                log.debug("Hardening disabilitato, accesso consentito");
                return true;
            }

            log.debug("Applico controlli hardening per avviso [{}/{}]", idDominio, numeroAvvisoOrIuv);

            // 1. Controlla UUID
            String uuid = request.getParameter(PARAMETER_UUID);
            if (StringUtils.hasText(uuid)) {
                log.debug("Parametro UUID trovato, verifico diritti avviso nel database...");
                if (checkAvvisoByUuid(idDominio, numeroAvvisoOrIuv, uuid)) {
                    log.debug("Accesso consentito tramite UUID");
                    return true;
                }
            }

            // 2. Se UUID non presente o non valido, verifica ReCaptcha
            log.debug("UUID non presente o non valido, applico controllo ReCaptcha");
            return validateReCaptcha(request, hardening);

        } catch (Exception e) {
            log.error("Errore durante i controlli di hardening avviso: {}", e.getMessage(), e);
            return false;
        }
    }

    private boolean checkAvvisoByUuid(String idDominio, String numeroAvvisoOrIuv, String uuid) {
        try {
            boolean exists;
            if (numeroAvvisoOrIuv.length() == 18) {
                // È un numero avviso
                exists = versamentoRepository.findByDominioCodDominioAndNumeroAvvisoAndIdSessione(
                        idDominio, numeroAvvisoOrIuv, uuid).isPresent();
            } else {
                // È uno IUV
                exists = versamentoRepository.findByDominioCodDominioAndIuvVersamentoAndIdSessione(
                        idDominio, numeroAvvisoOrIuv, uuid).isPresent();
            }

            if (exists) {
                log.debug("Avviso trovato nel database con UUID corrispondente");
                return true;
            }
        } catch (Exception e) {
            log.error("Errore durante la verifica avviso nel database: {}", e.getMessage());
        }

        return false;
    }
}
