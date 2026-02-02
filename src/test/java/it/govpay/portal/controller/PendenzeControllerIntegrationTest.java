package it.govpay.portal.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.govpay.portal.config.SpidUserDetails;

import it.govpay.pendenze.client.api.PendenzeApi;
import it.govpay.pendenze.client.model.NuovaPendenza;
import it.govpay.pendenze.client.model.PendenzaCreata;
import it.govpay.portal.entity.Dominio;
import it.govpay.portal.entity.TipoVersamento;
import it.govpay.portal.entity.TipoVersamentoDominio;
import it.govpay.portal.repository.TipoVersamentoDominioRepository;
import it.govpay.portal.security.hardening.model.Hardening;
import it.govpay.portal.service.ConfigurazioneService;
import it.govpay.portal.test.TemplateTestUtils;

@SpringBootTest
@AutoConfigureMockMvc
class PendenzeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TipoVersamentoDominioRepository tipoVersamentoDominioRepository;

    @MockBean
    private PendenzeApi pendenzeApi;

    @MockBean
    private ConfigurazioneService configurazioneService;

    private TipoVersamentoDominio tipoVersamentoDominio;
    private Dominio dominio;
    private TipoVersamento tipoVersamento;

    private static final String ID_DOMINIO = "01234567890";
    private static final String ID_TIPO_PENDENZA = "TASSA_RIFIUTI";
    private static final String COD_APPLICAZIONE = "APP_PORTAL";

    @BeforeEach
    void setUp() {
        // Reset mocks per garantire isolamento tra i test
        reset(pendenzeApi, tipoVersamentoDominioRepository, configurazioneService);

        // Setup dominio
        dominio = new Dominio();
        dominio.setId(1L);
        dominio.setCodDominio(ID_DOMINIO);
        dominio.setRagioneSociale("Comune di Test");

        // Setup tipo versamento
        tipoVersamento = new TipoVersamento();
        tipoVersamento.setId(1L);
        tipoVersamento.setCodTipoVersamento(ID_TIPO_PENDENZA);
        tipoVersamento.setDescrizione("Tassa Rifiuti");

        // Setup tipo versamento dominio
        tipoVersamentoDominio = new TipoVersamentoDominio();
        tipoVersamentoDominio.setId(1L);
        tipoVersamentoDominio.setDominio(dominio);
        tipoVersamentoDominio.setTipoVersamento(tipoVersamento);
        tipoVersamentoDominio.setAbilitato(true);
        tipoVersamentoDominio.setPagAbilitato(true);
        tipoVersamentoDominio.setPagCodApplicazione(COD_APPLICAZIONE);

        // Disabilita reCAPTCHA per i test
        when(configurazioneService.getHardening())
                .thenReturn(Hardening.builder().abilitato(false).build());
    }

    /**
     * Crea un'autenticazione SPID per i test.
     */
    private UsernamePasswordAuthenticationToken createSpidAuthentication(
            String fiscalNumber, String name, String familyName, String email) {
        SpidUserDetails spidUserDetails = new SpidUserDetails(fiscalNumber, name, familyName, email, null, null);
        return new UsernamePasswordAuthenticationToken(
                spidUserDetails, null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    @DisplayName("POST pendenze - Creazione senza trasformazione")
    void testCreaPendenza_SenzaTrasformazione() throws Exception {
        // Given - Body JSON diretto (senza trasformazione)
        String requestBody = """
            {
                "idA2A": "APP_PORTAL",
                "idPendenza": "PEND-TEST-001",
                "causale": "Tassa rifiuti 2024",
                "importo": 150.50,
                "soggettoPagatore": {
                    "tipo": "F",
                    "identificativo": "RSSMRA80A01H501U",
                    "anagrafica": "Mario Rossi",
                    "email": "mario.rossi@email.it"
                },
                "voci": [{
                    "idVocePendenza": "VOCE001",
                    "importo": 150.50,
                    "descrizione": "Tassa rifiuti anno 2024",
                    "codEntrata": "TARI"
                }]
            }
            """;

        when(tipoVersamentoDominioRepository.findByDominioCodDominioAndTipoVersamentoCodTipoVersamento(
                ID_DOMINIO, ID_TIPO_PENDENZA))
                .thenReturn(Optional.of(tipoVersamentoDominio));

        PendenzaCreata pendenzaCreata = new PendenzaCreata();
        pendenzaCreata.setIdDominio(ID_DOMINIO);
        pendenzaCreata.setNumeroAvviso("301000000000000001");
        pendenzaCreata.setUUID("uuid-test-12345");

        when(pendenzeApi.addPendenza(eq(COD_APPLICAZIONE), anyString(), eq(false), isNull(), any(NuovaPendenza.class)))
                .thenReturn(pendenzaCreata);

        // When & Then
        mockMvc.perform(post("/pendenze/{idDominio}/{idTipoPendenza}", ID_DOMINIO, ID_TIPO_PENDENZA)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroAvviso").value("301000000000000001"))
                .andExpect(jsonPath("$.dominio.idDominio").value(ID_DOMINIO));

        // Verifica che la chiamata a GovPay sia stata effettuata
        verify(pendenzeApi).addPendenza(eq(COD_APPLICAZIONE), anyString(), eq(false), isNull(), any(NuovaPendenza.class));
    }

    @Test
    @DisplayName("POST pendenze - Creazione con trasformazione FreeMarker TARI")
    void testCreaPendenza_ConTrasformazioneFreeMarkerTari() throws Exception {
        // Given - Carica template da file
        String templateBase64 = TemplateTestUtils.getTariTemplateBase64();

        tipoVersamentoDominio.setPagTrasformazioneTipo("freemarker");
        tipoVersamentoDominio.setPagTrasformazioneDef(templateBase64);

        // Body del form compilato dall'utente
        String requestBody = """
            {
                "codiceFiscale": "RSSMRA80A01H501U",
                "nome": "Mario",
                "cognome": "Rossi",
                "email": "mario.rossi@email.it",
                "importo": 250.75,
                "annoRiferimento": "2024",
                "dataScadenza": "2024-12-31"
            }
            """;

        when(tipoVersamentoDominioRepository.findByDominioCodDominioAndTipoVersamentoCodTipoVersamento(
                ID_DOMINIO, ID_TIPO_PENDENZA))
                .thenReturn(Optional.of(tipoVersamentoDominio));

        PendenzaCreata pendenzaCreata = new PendenzaCreata();
        pendenzaCreata.setIdDominio(ID_DOMINIO);
        pendenzaCreata.setNumeroAvviso("301000000000000002");
        pendenzaCreata.setUUID("uuid-trasformazione-12345");

        ArgumentCaptor<NuovaPendenza> pendenzaCaptor = ArgumentCaptor.forClass(NuovaPendenza.class);

        when(pendenzeApi.addPendenza(eq(COD_APPLICAZIONE), anyString(), eq(false), isNull(), pendenzaCaptor.capture()))
                .thenReturn(pendenzaCreata);

        // When
        mockMvc.perform(post("/pendenze/{idDominio}/{idTipoPendenza}", ID_DOMINIO, ID_TIPO_PENDENZA)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroAvviso").value("301000000000000002"));

        // Then - Verifica che la trasformazione sia stata applicata correttamente
        NuovaPendenza pendenzaInviata = pendenzaCaptor.getValue();

        // Verifica la causale trasformata
        assertTrue(pendenzaInviata.getCausale().contains("Tassa rifiuti 2024"),
                "La causale dovrebbe contenere l'anno di riferimento");
        assertTrue(pendenzaInviata.getCausale().contains("Mario Rossi"),
                "La causale dovrebbe contenere il nome del contribuente");

        // Verifica l'importo
        assertEquals(250.75, pendenzaInviata.getImporto());

        // Verifica il soggetto pagatore
        assertNotNull(pendenzaInviata.getSoggettoPagatore());
        assertEquals("RSSMRA80A01H501U", pendenzaInviata.getSoggettoPagatore().getIdentificativo());
        assertEquals("Mario Rossi", pendenzaInviata.getSoggettoPagatore().getAnagrafica());

        // Verifica le voci
        assertNotNull(pendenzaInviata.getVoci());
        assertEquals(1, pendenzaInviata.getVoci().size());
        assertEquals("TARI-2024", pendenzaInviata.getVoci().get(0).getIdVocePendenza());
    }

    @Test
    @DisplayName("POST pendenze - Con utente SPID autenticato")
    void testCreaPendenza_ConUtenteSpid() throws Exception {
        // Given
        String requestBody = """
            {
                "idA2A": "APP_PORTAL",
                "idPendenza": "PEND-SPID-001",
                "importo": 100.00,
                "causale": "Test con SPID",
                "voci": [{
                    "idVocePendenza": "VOCE001",
                    "importo": 100.00,
                    "descrizione": "Voce test SPID",
                    "codEntrata": "ENTRATA_TEST"
                }]
            }
            """;

        when(tipoVersamentoDominioRepository.findByDominioCodDominioAndTipoVersamentoCodTipoVersamento(
                ID_DOMINIO, ID_TIPO_PENDENZA))
                .thenReturn(Optional.of(tipoVersamentoDominio));

        PendenzaCreata pendenzaCreata = new PendenzaCreata();
        pendenzaCreata.setIdDominio(ID_DOMINIO);
        pendenzaCreata.setNumeroAvviso("301000000000000003");

        ArgumentCaptor<NuovaPendenza> pendenzaCaptor = ArgumentCaptor.forClass(NuovaPendenza.class);

        when(pendenzeApi.addPendenza(eq(COD_APPLICAZIONE), anyString(), eq(false), isNull(), pendenzaCaptor.capture()))
                .thenReturn(pendenzaCreata);

        // When - Simula utente SPID autenticato
        mockMvc.perform(post("/pendenze/{idDominio}/{idTipoPendenza}", ID_DOMINIO, ID_TIPO_PENDENZA)
                        .with(csrf())
                        .with(authentication(createSpidAuthentication("VRDGPP80A01H501X", "Giuseppe", "Verdi", "giuseppe.verdi@email.it")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        // Then - Verifica che il soggetto pagatore sia stato impostato da SPID
        NuovaPendenza pendenzaInviata = pendenzaCaptor.getValue();

        // Verifica che sia il body corretto
        assertEquals("Test con SPID", pendenzaInviata.getCausale(),
                "La causale deve essere quella del test SPID");
        assertEquals(100.00, pendenzaInviata.getImporto(), 0.01,
                "L'importo deve essere quello del test SPID");

        // Verifica soggetto pagatore da SPID
        assertNotNull(pendenzaInviata.getSoggettoPagatore(),
                "Il soggetto pagatore deve essere impostato da SPID");
        assertEquals("VRDGPP80A01H501X", pendenzaInviata.getSoggettoPagatore().getIdentificativo(),
                "L'identificativo deve provenire da SPID");
        assertEquals("Giuseppe Verdi", pendenzaInviata.getSoggettoPagatore().getAnagrafica(),
                "L'anagrafica deve provenire da SPID");
    }

    @Test
    @DisplayName("POST pendenze - Tipo pendenza non trovato restituisce 404")
    void testCreaPendenza_TipoPendenzaNonTrovato() throws Exception {
        // Given
        String requestBody = """
            {
                "importo": 100.00,
                "causale": "Test"
            }
            """;

        when(tipoVersamentoDominioRepository.findByDominioCodDominioAndTipoVersamentoCodTipoVersamento(
                ID_DOMINIO, "TIPO_INESISTENTE"))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/pendenze/{idDominio}/{idTipoPendenza}", ID_DOMINIO, "TIPO_INESISTENTE")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());

        // Verifica che NON sia stata fatta chiamata a GovPay
        verify(pendenzeApi, never()).addPendenza(anyString(), anyString(), anyBoolean(), any(), any());
    }

    @Test
    @DisplayName("POST pendenze - Pagamento non abilitato restituisce 400")
    void testCreaPendenza_PagamentoNonAbilitato() throws Exception {
        // Given
        tipoVersamentoDominio.setPagAbilitato(false);

        String requestBody = """
            {
                "importo": 100.00,
                "causale": "Test"
            }
            """;

        when(tipoVersamentoDominioRepository.findByDominioCodDominioAndTipoVersamentoCodTipoVersamento(
                ID_DOMINIO, ID_TIPO_PENDENZA))
                .thenReturn(Optional.of(tipoVersamentoDominio));

        // When & Then
        mockMvc.perform(post("/pendenze/{idDominio}/{idTipoPendenza}", ID_DOMINIO, ID_TIPO_PENDENZA)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        // Verifica che NON sia stata fatta chiamata a GovPay
        verify(pendenzeApi, never()).addPendenza(anyString(), anyString(), anyBoolean(), any(), any());
    }

    @Test
    @DisplayName("POST pendenze - Con idA2A e idPendenza custom")
    void testCreaPendenza_ConParametriCustom() throws Exception {
        // Given
        String idA2ACustom = "APP_CUSTOM";
        String idPendenzaCustom = "PEND-2024-001";

        // idA2A e idPendenza nel body verranno sovrascritti dai parametri query
        String requestBody = """
            {
                "idA2A": "APP_PORTAL",
                "idPendenza": "PEND-DEFAULT",
                "causale": "Test con parametri custom",
                "importo": 75.00,
                "soggettoPagatore": {
                    "tipo": "F",
                    "identificativo": "RSSMRA80A01H501U",
                    "anagrafica": "Mario Rossi"
                },
                "voci": [{
                    "idVocePendenza": "VOCE001",
                    "importo": 75.00,
                    "descrizione": "Voce test",
                    "codEntrata": "ENTRATA_TEST"
                }]
            }
            """;

        when(tipoVersamentoDominioRepository.findByDominioCodDominioAndTipoVersamentoCodTipoVersamento(
                ID_DOMINIO, ID_TIPO_PENDENZA))
                .thenReturn(Optional.of(tipoVersamentoDominio));

        PendenzaCreata pendenzaCreata = new PendenzaCreata();
        pendenzaCreata.setIdDominio(ID_DOMINIO);
        pendenzaCreata.setNumeroAvviso("301000000000000004");

        when(pendenzeApi.addPendenza(eq(idA2ACustom), eq(idPendenzaCustom), eq(false), isNull(), any(NuovaPendenza.class)))
                .thenReturn(pendenzaCreata);

        // When & Then
        mockMvc.perform(post("/pendenze/{idDominio}/{idTipoPendenza}", ID_DOMINIO, ID_TIPO_PENDENZA)
                        .with(csrf())
                        .param("idA2A", idA2ACustom)
                        .param("idPendenza", idPendenzaCustom)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroAvviso").value("301000000000000004"));

        // Verifica che siano stati usati i parametri custom
        verify(pendenzeApi).addPendenza(eq(idA2ACustom), eq(idPendenzaCustom), eq(false), isNull(), any(NuovaPendenza.class));
    }

    @Test
    @DisplayName("POST pendenze - Trasformazione con contesto completo")
    void testCreaPendenza_TrasformazioneConContestoCompleto() throws Exception {
        // Given - Carica template da file
        String templateBase64 = TemplateTestUtils.getContestoCompletoTemplateBase64();

        tipoVersamentoDominio.setPagTrasformazioneTipo("freemarker");
        tipoVersamentoDominio.setPagTrasformazioneDef(templateBase64);

        String requestBody = """
            {
                "cf": "BNCLRA85M01H501Z",
                "nominativo": "Laura Bianchi",
                "importo": 320.00
            }
            """;

        when(tipoVersamentoDominioRepository.findByDominioCodDominioAndTipoVersamentoCodTipoVersamento(
                ID_DOMINIO, ID_TIPO_PENDENZA))
                .thenReturn(Optional.of(tipoVersamentoDominio));

        PendenzaCreata pendenzaCreata = new PendenzaCreata();
        pendenzaCreata.setIdDominio(ID_DOMINIO);
        pendenzaCreata.setNumeroAvviso("301000000000000005");

        ArgumentCaptor<NuovaPendenza> pendenzaCaptor = ArgumentCaptor.forClass(NuovaPendenza.class);

        when(pendenzeApi.addPendenza(eq(COD_APPLICAZIONE), anyString(), eq(false), isNull(), pendenzaCaptor.capture()))
                .thenReturn(pendenzaCreata);

        // When
        mockMvc.perform(post("/pendenze/{idDominio}/{idTipoPendenza}", ID_DOMINIO, ID_TIPO_PENDENZA)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        // Then - Verifica che le variabili di contesto siano state risolte
        NuovaPendenza pendenzaInviata = pendenzaCaptor.getValue();

        // Verifica che idDominio e idTipoVersamento siano stati usati nella causale
        assertTrue(pendenzaInviata.getCausale().contains(ID_DOMINIO),
                "La causale dovrebbe contenere l'idDominio");
        assertTrue(pendenzaInviata.getCausale().contains(ID_TIPO_PENDENZA),
                "La causale dovrebbe contenere l'idTipoVersamento");

        // Verifica idVocePendenza composto
        assertEquals(ID_DOMINIO + "-" + ID_TIPO_PENDENZA + "-001",
                pendenzaInviata.getVoci().get(0).getIdVocePendenza());

        // Verifica soggetto pagatore
        assertEquals("BNCLRA85M01H501Z", pendenzaInviata.getSoggettoPagatore().getIdentificativo());
        assertEquals("Laura Bianchi", pendenzaInviata.getSoggettoPagatore().getAnagrafica());
    }
}
