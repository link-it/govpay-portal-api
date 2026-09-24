package it.govpay.portal.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifica della tracciatura di govpay-common (BP-LOG-3) su portal-api.
 * <p>
 * Il filtro gira prima della catena di sicurezza: gli header sono presenti sia
 * sulle risposte dei path pubblici sia su quelle negate con 403, che sono
 * proprio quelle su cui serve poter risalire ai log.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Test Tracciatura Transaction ID / Correlation ID")
class TracciaturaIntegrationTest {

    private static final String HEADER_TRANSACTION_ID = "X-Transaction-ID";
    private static final String HEADER_TRANSACTION_ID_LEGACY = "X-Govpay-IdTransazione";
    private static final String HEADER_CORRELATION_ID = "X-Correlation-ID";
    private static final String HEADER_REQUEST_ID = "X-Request-ID";

    private static final String PATH_PUBBLICO = "/domini";
    private static final String PATH_PROTETTO = "/profilo";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("La risposta espone transaction id e correlation id generati")
    void rispostaEsponeIdentificativiGenerati() throws Exception {
        MockHttpServletResponse response = esegui(PATH_PUBBLICO, null, null);

        String transactionId = header(response, HEADER_TRANSACTION_ID);
        String correlationId = header(response, HEADER_CORRELATION_ID);

        assertNotNull(UUID.fromString(transactionId));
        assertNotNull(UUID.fromString(correlationId));
        assertNotEquals(transactionId, correlationId);

        assertEquals(transactionId, header(response, HEADER_TRANSACTION_ID_LEGACY));
        assertEquals(correlationId, header(response, HEADER_REQUEST_ID));
    }

    @Test
    @DisplayName("Il correlation id del chiamante viene riusato")
    void correlationIdDelChiamanteRiusato() throws Exception {
        MockHttpServletResponse response = esegui(PATH_PUBBLICO, HEADER_CORRELATION_ID, "flusso-esterno-1");

        assertEquals("flusso-esterno-1", header(response, HEADER_CORRELATION_ID));
        assertEquals("flusso-esterno-1", header(response, HEADER_REQUEST_ID));
    }

    @Test
    @DisplayName("Anche X-Request-ID e' accettato come correlation id")
    void requestIdAccettatoComeCorrelationId() throws Exception {
        MockHttpServletResponse response = esegui(PATH_PUBBLICO, HEADER_REQUEST_ID, "da-gateway");

        assertEquals("da-gateway", header(response, HEADER_CORRELATION_ID));
    }

    @Test
    @DisplayName("Il transaction id non e' imponibile dal client")
    void transactionIdNonImponibileDalClient() throws Exception {
        MockHttpServletResponse response =
                esegui(PATH_PUBBLICO, HEADER_TRANSACTION_ID_LEGACY, "imposto-dal-client");

        String transactionId = header(response, HEADER_TRANSACTION_ID);
        assertNotEquals("imposto-dal-client", transactionId);
        assertNotNull(UUID.fromString(transactionId));
    }

    @Test
    @DisplayName("Un correlation id con caratteri non ammessi viene scartato")
    void correlationIdNonConformeScartato() throws Exception {
        MockHttpServletResponse response = esegui(PATH_PUBBLICO, HEADER_CORRELATION_ID, "valore con spazi");

        assertNotEquals("valore con spazi", header(response, HEADER_CORRELATION_ID));
    }

    @Test
    @DisplayName("Gli identificativi ci sono anche sulle risposte negate")
    void identificativiPresentiSulleRisposteNegate() throws Exception {
        MockHttpServletResponse response = esegui(PATH_PROTETTO, HEADER_CORRELATION_ID, "flusso-negato");

        assertEquals(403, response.getStatus());
        assertEquals("flusso-negato", header(response, HEADER_CORRELATION_ID));
        assertNotNull(UUID.fromString(header(response, HEADER_TRANSACTION_ID)));
    }

    private static String header(MockHttpServletResponse response, String nome) {
        String valore = response.getHeader(nome);
        assertTrue(valore != null && !valore.isBlank(),
                "Header " + nome + " assente o vuoto nella risposta");
        return valore;
    }

    private MockHttpServletResponse esegui(String path, String headerNome, String headerValore)
            throws Exception {
        var richiesta = get(path).accept(MediaType.APPLICATION_JSON);
        if (headerNome != null) {
            richiesta = richiesta.header(headerNome, headerValore);
        }
        return mockMvc.perform(richiesta).andReturn().getResponse();
    }
}
