package it.govpay.portal.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.govpay.pendenze.client.api.PendenzeApi;
import it.govpay.pendenze.client.model.NuovaPendenza;
import it.govpay.pendenze.client.model.PendenzaCreata;
import it.govpay.portal.config.SpidUserDetails;
import it.govpay.portal.entity.Dominio;
import it.govpay.portal.entity.TipoVersamento;
import it.govpay.portal.entity.TipoVersamentoDominio;
import it.govpay.portal.exception.BadRequestException;
import it.govpay.portal.exception.NotFoundException;
import it.govpay.portal.exception.UnprocessableEntityException;
import it.govpay.portal.exception.ValidationException;
import it.govpay.portal.mapper.PendenzaPostMapper;
import it.govpay.portal.mapper.PendenzeMapper;
import it.govpay.portal.model.Pendenza;
import it.govpay.portal.repository.TipoVersamentoDominioRepository;

@ExtendWith(MockitoExtension.class)
class CreaPendenzaServiceTest {

    @Mock
    private TipoVersamentoDominioRepository tipoVersamentoDominioRepository;

    @Mock
    private PendenzeApi pendenzeApi;

    @Mock
    private PendenzeMapper pendenzeMapper;

    private PendenzaPostMapper pendenzaPostMapper;

    @InjectMocks
    private CreaPendenzaService creaPendenzaService;

    private ObjectMapper objectMapper;

    private TipoVersamentoDominio tipoVersamentoDominio;
    private Dominio dominio;
    private TipoVersamento tipoVersamento;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        pendenzaPostMapper = new PendenzaPostMapper();
        creaPendenzaService = new CreaPendenzaService(
                tipoVersamentoDominioRepository,
                pendenzeApi,
                pendenzeMapper,
                pendenzaPostMapper,
                objectMapper);

        // Setup test entities
        dominio = new Dominio();
        dominio.setId(1L);
        dominio.setCodDominio("01234567890");
        dominio.setRagioneSociale("Ente Test");

        tipoVersamento = new TipoVersamento();
        tipoVersamento.setId(1L);
        tipoVersamento.setCodTipoVersamento("TIPO_TEST");
        tipoVersamento.setDescrizione("Tipo Test");

        tipoVersamentoDominio = new TipoVersamentoDominio();
        tipoVersamentoDominio.setId(1L);
        tipoVersamentoDominio.setDominio(dominio);
        tipoVersamentoDominio.setTipoVersamento(tipoVersamento);
        tipoVersamentoDominio.setAbilitato(true);
        tipoVersamentoDominio.setPagAbilitato(true);
        tipoVersamentoDominio.setPagCodApplicazione("APP_TEST");

        // Clear security context
        SecurityContextHolder.clearContext();
    }

    @Test
    void testCreaPendenza_Success() {
        // Given
        String idDominio = "01234567890";
        String idTipoPendenza = "TIPO_TEST";
        Map<String, Object> requestBody = createValidRequestBody();

        when(tipoVersamentoDominioRepository.findByDominioCodDominioAndTipoVersamentoCodTipoVersamento(
                idDominio, idTipoPendenza))
                .thenReturn(Optional.of(tipoVersamentoDominio));

        PendenzaCreata pendenzaCreata = new PendenzaCreata();
        pendenzaCreata.setIdDominio(idDominio);
        pendenzaCreata.setNumeroAvviso("301000000000000001");

        when(pendenzeApi.addPendenza(anyString(), anyString(), anyBoolean(), any(), any(NuovaPendenza.class)))
                .thenReturn(pendenzaCreata);

        Pendenza expectedPendenza = new Pendenza();
        expectedPendenza.setNumeroAvviso("301000000000000001");

        when(pendenzeMapper.toPendenzaFromCreata(any(PendenzaCreata.class), any(NuovaPendenza.class)))
                .thenReturn(expectedPendenza);

        // When
        Pendenza result = creaPendenzaService.creaPendenza(
                idDominio,
                idTipoPendenza,
                requestBody,
                null,
                null,
                new HashMap<>(),
                new HashMap<>(),
                Map.of("idDominio", idDominio, "idTipoPendenza", idTipoPendenza));

        // Then
        assertNotNull(result);
        assertEquals("301000000000000001", result.getNumeroAvviso());

        verify(tipoVersamentoDominioRepository).findByDominioCodDominioAndTipoVersamentoCodTipoVersamento(
                idDominio, idTipoPendenza);
        verify(pendenzeApi).addPendenza(anyString(), anyString(), anyBoolean(), any(), any(NuovaPendenza.class));
    }

    @Test
    void testCreaPendenza_TipoPendenzaNotFound() {
        // Given
        String idDominio = "01234567890";
        String idTipoPendenza = "TIPO_INESISTENTE";
        Map<String, Object> requestBody = new HashMap<>();

        when(tipoVersamentoDominioRepository.findByDominioCodDominioAndTipoVersamentoCodTipoVersamento(
                idDominio, idTipoPendenza))
                .thenReturn(Optional.empty());

        // When/Then
        assertThrows(NotFoundException.class, () ->
                creaPendenzaService.creaPendenza(
                        idDominio,
                        idTipoPendenza,
                        requestBody,
                        null,
                        null,
                        new HashMap<>(),
                        new HashMap<>(),
                        new HashMap<>()));
    }

    @Test
    void testCreaPendenza_PagamentoNonAbilitato() {
        // Given
        String idDominio = "01234567890";
        String idTipoPendenza = "TIPO_TEST";
        Map<String, Object> requestBody = new HashMap<>();

        tipoVersamentoDominio.setPagAbilitato(false);

        when(tipoVersamentoDominioRepository.findByDominioCodDominioAndTipoVersamentoCodTipoVersamento(
                idDominio, idTipoPendenza))
                .thenReturn(Optional.of(tipoVersamentoDominio));

        // When/Then
        assertThrows(BadRequestException.class, () ->
                creaPendenzaService.creaPendenza(
                        idDominio,
                        idTipoPendenza,
                        requestBody,
                        null,
                        null,
                        new HashMap<>(),
                        new HashMap<>(),
                        new HashMap<>()));
    }

    @Test
    void testCreaPendenza_WithFreeMarkerTransformation() {
        // Given
        String idDominio = "01234567890";
        String idTipoPendenza = "TIPO_TEST";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("nome", "Mario");
        requestBody.put("cognome", "Rossi");
        requestBody.put("importo", 50.0);

        // Template FreeMarker semplice
        String template = """
            {
                "idA2A": "APP_TEST",
                "idPendenza": "${transactionId}",
                "idDominio": "${idDominio}",
                "idTipoPendenza": "${idTipoVersamento}",
                "causale": "Pagamento per ${jsonPath.read("$.nome")} ${jsonPath.read("$.cognome")}",
                "importo": ${jsonPath.read("$.importo")},
                "soggettoPagatore": {
                    "tipo": "F",
                    "identificativo": "RSSMRA80A01H501U",
                    "anagrafica": "${jsonPath.read("$.nome")} ${jsonPath.read("$.cognome")}"
                },
                "voci": [{
                    "idVocePendenza": "VOCE001",
                    "importo": ${jsonPath.read("$.importo")},
                    "descrizione": "Voce di test",
                    "codEntrata": "ENTRATA_TEST"
                }]
            }
            """;
        String templateBase64 = Base64.getEncoder().encodeToString(template.getBytes());

        tipoVersamentoDominio.setPagTrasformazioneTipo("freemarker");
        tipoVersamentoDominio.setPagTrasformazioneDef(templateBase64);

        when(tipoVersamentoDominioRepository.findByDominioCodDominioAndTipoVersamentoCodTipoVersamento(
                idDominio, idTipoPendenza))
                .thenReturn(Optional.of(tipoVersamentoDominio));

        PendenzaCreata pendenzaCreata = new PendenzaCreata();
        pendenzaCreata.setIdDominio(idDominio);
        pendenzaCreata.setNumeroAvviso("301000000000000002");

        when(pendenzeApi.addPendenza(anyString(), anyString(), anyBoolean(), any(), any(NuovaPendenza.class)))
                .thenReturn(pendenzaCreata);

        Pendenza expectedPendenza = new Pendenza();
        expectedPendenza.setNumeroAvviso("301000000000000002");
        expectedPendenza.setCausale("Pagamento per Mario Rossi");

        when(pendenzeMapper.toPendenzaFromCreata(any(PendenzaCreata.class), any(NuovaPendenza.class)))
                .thenReturn(expectedPendenza);

        // When
        Pendenza result = creaPendenzaService.creaPendenza(
                idDominio,
                idTipoPendenza,
                requestBody,
                null,
                null,
                new HashMap<>(),
                new HashMap<>(),
                Map.of("idDominio", idDominio, "idTipoPendenza", idTipoPendenza));

        // Then
        assertNotNull(result);
        assertEquals("301000000000000002", result.getNumeroAvviso());
    }

    @Test
    void testCreaPendenza_WithSpidUser() {
        // Given
        String idDominio = "01234567890";
        String idTipoPendenza = "TIPO_TEST";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("idA2A", "APP_TEST");
        requestBody.put("idPendenza", "PEND_SPID_001");
        requestBody.put("importo", 100.50);
        requestBody.put("causale", "Test causale");
        // Aggiungo le voci necessarie per la validazione
        List<Map<String, Object>> voci = new ArrayList<>();
        Map<String, Object> voce = new HashMap<>();
        voce.put("idVocePendenza", "VOCE001");
        voce.put("importo", 100.50);
        voce.put("descrizione", "Voce di test");
        voce.put("codEntrata", "ENTRATA_TEST");
        voci.add(voce);
        requestBody.put("voci", voci);

        // Setup SPID user
        SpidUserDetails spidUser = mock(SpidUserDetails.class);
        when(spidUser.getFiscalNumber()).thenReturn("RSSMRA80A01H501U");
        when(spidUser.getName()).thenReturn("Mario");
        when(spidUser.getFamilyName()).thenReturn("Rossi");
        when(spidUser.getEmail()).thenReturn("mario.rossi@email.it");

        TestingAuthenticationToken auth = new TestingAuthenticationToken(spidUser, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(tipoVersamentoDominioRepository.findByDominioCodDominioAndTipoVersamentoCodTipoVersamento(
                idDominio, idTipoPendenza))
                .thenReturn(Optional.of(tipoVersamentoDominio));

        PendenzaCreata pendenzaCreata = new PendenzaCreata();
        pendenzaCreata.setIdDominio(idDominio);
        pendenzaCreata.setNumeroAvviso("301000000000000003");

        when(pendenzeApi.addPendenza(anyString(), anyString(), anyBoolean(), any(), any(NuovaPendenza.class)))
                .thenAnswer(invocation -> {
                    NuovaPendenza np = invocation.getArgument(4);
                    // Verify soggetto pagatore from SPID
                    assertNotNull(np.getSoggettoPagatore());
                    assertEquals("RSSMRA80A01H501U", np.getSoggettoPagatore().getIdentificativo());
                    assertEquals("Mario Rossi", np.getSoggettoPagatore().getAnagrafica());
                    return pendenzaCreata;
                });

        Pendenza expectedPendenza = new Pendenza();
        expectedPendenza.setNumeroAvviso("301000000000000003");

        when(pendenzeMapper.toPendenzaFromCreata(any(PendenzaCreata.class), any(NuovaPendenza.class)))
                .thenReturn(expectedPendenza);

        // When
        Pendenza result = creaPendenzaService.creaPendenza(
                idDominio,
                idTipoPendenza,
                requestBody,
                null,
                null,
                new HashMap<>(),
                new HashMap<>(),
                Map.of("idDominio", idDominio, "idTipoPendenza", idTipoPendenza));

        // Then
        assertNotNull(result);
    }

    @Test
    void testCreaPendenza_InvalidTransformationTemplate() {
        // Given
        String idDominio = "01234567890";
        String idTipoPendenza = "TIPO_TEST";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("importo", 100.50);

        // Template non valido (Base64 invalido)
        tipoVersamentoDominio.setPagTrasformazioneTipo("freemarker");
        tipoVersamentoDominio.setPagTrasformazioneDef("!!!INVALID_BASE64!!!");

        when(tipoVersamentoDominioRepository.findByDominioCodDominioAndTipoVersamentoCodTipoVersamento(
                idDominio, idTipoPendenza))
                .thenReturn(Optional.of(tipoVersamentoDominio));

        // When/Then
        assertThrows(UnprocessableEntityException.class, () ->
                creaPendenzaService.creaPendenza(
                        idDominio,
                        idTipoPendenza,
                        requestBody,
                        null,
                        null,
                        new HashMap<>(),
                        new HashMap<>(),
                        new HashMap<>()));
    }

    @Test
    void testCreaPendenza_ValidationFailure_MancaIdA2A() {
        // Given - request body senza idA2A
        String idDominio = "01234567890";
        String idTipoPendenza = "TIPO_TEST";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("idPendenza", "PEND_001");
        requestBody.put("causale", "Test causale");
        requestBody.put("importo", 100.50);
        // Manca idA2A

        List<Map<String, Object>> voci = new ArrayList<>();
        Map<String, Object> voce = new HashMap<>();
        voce.put("idVocePendenza", "VOCE001");
        voce.put("importo", 100.50);
        voce.put("descrizione", "Voce di test");
        voce.put("codEntrata", "ENTRATA_TEST");
        voci.add(voce);
        requestBody.put("voci", voci);

        when(tipoVersamentoDominioRepository.findByDominioCodDominioAndTipoVersamentoCodTipoVersamento(
                idDominio, idTipoPendenza))
                .thenReturn(Optional.of(tipoVersamentoDominio));

        // When/Then
        ValidationException exception = assertThrows(ValidationException.class, () ->
                creaPendenzaService.creaPendenza(
                        idDominio,
                        idTipoPendenza,
                        requestBody,
                        null,
                        null,
                        new HashMap<>(),
                        new HashMap<>(),
                        new HashMap<>()));

        assertTrue(exception.getMessage().contains("idA2A"));
    }

    @Test
    void testCreaPendenza_WithProvidedIdA2A() {
        // Given
        String idDominio = "01234567890";
        String idTipoPendenza = "TIPO_TEST";
        String idA2A = "APP_CUSTOM";
        Map<String, Object> requestBody = createValidRequestBody();

        tipoVersamentoDominio.setPagCodApplicazione(null); // non configurato

        when(tipoVersamentoDominioRepository.findByDominioCodDominioAndTipoVersamentoCodTipoVersamento(
                idDominio, idTipoPendenza))
                .thenReturn(Optional.of(tipoVersamentoDominio));

        PendenzaCreata pendenzaCreata = new PendenzaCreata();
        pendenzaCreata.setIdDominio(idDominio);
        pendenzaCreata.setNumeroAvviso("301000000000000004");

        when(pendenzeApi.addPendenza(eq(idA2A), anyString(), anyBoolean(), any(), any(NuovaPendenza.class)))
                .thenReturn(pendenzaCreata);

        Pendenza expectedPendenza = new Pendenza();
        expectedPendenza.setNumeroAvviso("301000000000000004");

        when(pendenzeMapper.toPendenzaFromCreata(any(PendenzaCreata.class), any(NuovaPendenza.class)))
                .thenReturn(expectedPendenza);

        // When
        Pendenza result = creaPendenzaService.creaPendenza(
                idDominio,
                idTipoPendenza,
                requestBody,
                idA2A,
                null,
                new HashMap<>(),
                new HashMap<>(),
                Map.of("idDominio", idDominio, "idTipoPendenza", idTipoPendenza));

        // Then
        assertNotNull(result);
        verify(pendenzeApi).addPendenza(eq(idA2A), anyString(), anyBoolean(), any(), any(NuovaPendenza.class));
    }

    @Test
    void testCreaPendenza_ValidationFailure_MancaCausale() {
        // Given - request body senza causale
        String idDominio = "01234567890";
        String idTipoPendenza = "TIPO_TEST";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("idA2A", "APP_TEST");
        requestBody.put("idPendenza", "PEND_001");
        requestBody.put("importo", 100.50);
        // Manca causale

        List<Map<String, Object>> voci = new ArrayList<>();
        Map<String, Object> voce = new HashMap<>();
        voce.put("idVocePendenza", "VOCE001");
        voce.put("importo", 100.50);
        voce.put("descrizione", "Voce di test");
        voce.put("codEntrata", "ENTRATA_TEST");
        voci.add(voce);
        requestBody.put("voci", voci);

        when(tipoVersamentoDominioRepository.findByDominioCodDominioAndTipoVersamentoCodTipoVersamento(
                idDominio, idTipoPendenza))
                .thenReturn(Optional.of(tipoVersamentoDominio));

        // When/Then
        ValidationException exception = assertThrows(ValidationException.class, () ->
                creaPendenzaService.creaPendenza(
                        idDominio,
                        idTipoPendenza,
                        requestBody,
                        null,
                        null,
                        new HashMap<>(),
                        new HashMap<>(),
                        new HashMap<>()));

        // Verifica che l'eccezione contenga il messaggio sulla causale
        assertTrue(exception.getMessage().contains("causale"));
    }

    @Test
    void testCreaPendenza_ValidationFailure_MancaVoci() {
        // Given - request body senza voci
        String idDominio = "01234567890";
        String idTipoPendenza = "TIPO_TEST";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("idA2A", "APP_TEST");
        requestBody.put("idPendenza", "PEND_001");
        requestBody.put("importo", 100.50);
        requestBody.put("causale", "Test causale");
        // Manca voci

        when(tipoVersamentoDominioRepository.findByDominioCodDominioAndTipoVersamentoCodTipoVersamento(
                idDominio, idTipoPendenza))
                .thenReturn(Optional.of(tipoVersamentoDominio));

        // When/Then
        ValidationException exception = assertThrows(ValidationException.class, () ->
                creaPendenzaService.creaPendenza(
                        idDominio,
                        idTipoPendenza,
                        requestBody,
                        null,
                        null,
                        new HashMap<>(),
                        new HashMap<>(),
                        new HashMap<>()));

        // Verifica che l'eccezione contenga il messaggio sulle voci
        assertTrue(exception.getMessage().contains("voci"));
    }

    /**
     * Crea un requestBody valido per la validazione della pendenza.
     */
    private Map<String, Object> createValidRequestBody() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("idA2A", "APP_TEST");
        requestBody.put("idPendenza", "PEND_001");
        requestBody.put("causale", "Test causale");
        requestBody.put("importo", 100.50);

        Map<String, Object> soggettoPagatore = new HashMap<>();
        soggettoPagatore.put("tipo", "F");
        soggettoPagatore.put("identificativo", "RSSMRA80A01H501U");
        soggettoPagatore.put("anagrafica", "Mario Rossi");
        requestBody.put("soggettoPagatore", soggettoPagatore);

        List<Map<String, Object>> voci = new ArrayList<>();
        Map<String, Object> voce = new HashMap<>();
        voce.put("idVocePendenza", "VOCE001");
        voce.put("importo", 100.50);
        voce.put("descrizione", "Voce di test");
        voce.put("codEntrata", "ENTRATA_TEST");
        voci.add(voce);
        requestBody.put("voci", voci);

        return requestBody;
    }
}
