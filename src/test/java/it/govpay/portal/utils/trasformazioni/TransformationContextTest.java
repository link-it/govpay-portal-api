package it.govpay.portal.utils.trasformazioni;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test per TransformationContext.
 */
class TransformationContextTest {

    @Test
    @DisplayName("Builder crea contesto con date di default")
    void testBuilderWithDefaultDate() {
        Map<String, Object> context = TransformationContext.builder().build();

        assertTrue(context.containsKey(Costanti.MAP_DATE_OBJECT));
        assertInstanceOf(Date.class, context.get(Costanti.MAP_DATE_OBJECT));
    }

    @Test
    @DisplayName("Builder con data specifica")
    void testBuilderWithSpecificDate() {
        Date specificDate = new Date(0);
        Map<String, Object> context = TransformationContext.builder()
                .withDate(specificDate)
                .build();

        assertEquals(specificDate, context.get(Costanti.MAP_DATE_OBJECT));
    }

    @Test
    @DisplayName("Builder con data corrente")
    void testBuilderWithCurrentDate() {
        Date before = new Date();
        Map<String, Object> context = TransformationContext.builder()
                .withDate()
                .build();
        Date after = new Date();

        Date contextDate = (Date) context.get(Costanti.MAP_DATE_OBJECT);
        assertTrue(contextDate.getTime() >= before.getTime());
        assertTrue(contextDate.getTime() <= after.getTime());
    }

    @Test
    @DisplayName("Builder con transactionId specifico")
    void testBuilderWithTransactionId() {
        Map<String, Object> context = TransformationContext.builder()
                .withTransactionId("TX123456")
                .build();

        assertEquals("TX123456", context.get(Costanti.MAP_TRANSACTION_ID_OBJECT));
    }

    @Test
    @DisplayName("Builder con transactionId random (senza trattini)")
    void testBuilderWithRandomTransactionId() {
        Map<String, Object> context = TransformationContext.builder()
                .withRandomTransactionId()
                .build();

        String transactionId = (String) context.get(Costanti.MAP_TRANSACTION_ID_OBJECT);
        assertNotNull(transactionId);
        assertEquals(32, transactionId.length()); // UUID senza trattini
        assertFalse(transactionId.contains("-")); // Verifica che non ci siano trattini
    }

    @Test
    @DisplayName("Builder con application context")
    void testBuilderWithApplicationContext() {
        Map<String, Object> appContext = new HashMap<>();
        appContext.put("key1", "value1");

        Map<String, Object> context = TransformationContext.builder()
                .withApplicationContext(appContext)
                .build();

        assertEquals(appContext, context.get(Costanti.MAP_CTX_OBJECT));
    }

    @Test
    @DisplayName("Builder con application context null")
    void testBuilderWithNullApplicationContext() {
        Map<String, Object> context = TransformationContext.builder()
                .withApplicationContext(null)
                .build();

        assertTrue(context.containsKey(Costanti.MAP_CTX_OBJECT));
        assertInstanceOf(Map.class, context.get(Costanti.MAP_CTX_OBJECT));
    }

    @Test
    @DisplayName("Builder con headers")
    void testBuilderWithHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        Map<String, Object> context = TransformationContext.builder()
                .withHeaders(headers)
                .build();

        assertEquals(headers, context.get(Costanti.MAP_HEADER));
    }

    @Test
    @DisplayName("Builder con headers vuoti non aggiunge al contesto")
    void testBuilderWithEmptyHeaders() {
        Map<String, Object> context = TransformationContext.builder()
                .withHeaders(new HashMap<>())
                .build();

        assertFalse(context.containsKey(Costanti.MAP_HEADER));
    }

    @Test
    @DisplayName("Builder con headers null non aggiunge al contesto")
    void testBuilderWithNullHeaders() {
        Map<String, Object> context = TransformationContext.builder()
                .withHeaders(null)
                .build();

        assertFalse(context.containsKey(Costanti.MAP_HEADER));
    }

    @Test
    @DisplayName("Builder con query params")
    void testBuilderWithQueryParams() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");

        Map<String, Object> context = TransformationContext.builder()
                .withQueryParams(queryParams)
                .build();

        assertEquals(queryParams, context.get(Costanti.MAP_QUERY_PARAMETER));
    }

    @Test
    @DisplayName("Builder con query params multi-value")
    void testBuilderWithQueryParamsMultiValue() {
        Map<String, List<String>> queryParams = new HashMap<>();
        queryParams.put("ids", Arrays.asList("1", "2", "3"));

        Map<String, Object> context = TransformationContext.builder()
                .withQueryParamsMultiValue(queryParams)
                .build();

        @SuppressWarnings("unchecked")
        Map<String, String> result = (Map<String, String>) context.get(Costanti.MAP_QUERY_PARAMETER);
        assertEquals("1,2,3", result.get("ids"));
    }

    @Test
    @DisplayName("Builder con query params multi-value null")
    void testBuilderWithNullQueryParamsMultiValue() {
        Map<String, Object> context = TransformationContext.builder()
                .withQueryParamsMultiValue(null)
                .build();

        assertFalse(context.containsKey(Costanti.MAP_QUERY_PARAMETER));
    }

    @Test
    @DisplayName("Builder con path params")
    void testBuilderWithPathParams() {
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("id", "123");

        Map<String, Object> context = TransformationContext.builder()
                .withPathParams(pathParams)
                .build();

        assertEquals(pathParams, context.get(Costanti.MAP_PATH_PARAMETER));
    }

    @Test
    @DisplayName("Builder con URL")
    void testBuilderWithUrl() {
        Map<String, Object> context = TransformationContext.builder()
                .withUrl("https://example.com/api/resource/123")
                .build();

        assertTrue(context.containsKey(Costanti.MAP_ELEMENT_URL_REGEXP));
        assertTrue(context.containsKey(Costanti.MAP_ELEMENT_URL_REGEXP.toLowerCase()));
    }

    @Test
    @DisplayName("Builder con URL vuoto non aggiunge al contesto")
    void testBuilderWithEmptyUrl() {
        Map<String, Object> context = TransformationContext.builder()
                .withUrl("")
                .build();

        assertFalse(context.containsKey(Costanti.MAP_ELEMENT_URL_REGEXP));
    }

    @Test
    @DisplayName("Builder con JSON")
    void testBuilderWithJson() {
        String json = "{\"key\": \"value\"}";
        Map<String, Object> context = TransformationContext.builder()
                .withJson(json)
                .build();

        assertTrue(context.containsKey(Costanti.MAP_ELEMENT_JSON_PATH));
        assertTrue(context.containsKey(Costanti.MAP_ELEMENT_JSON_PATH.toLowerCase()));
    }

    @Test
    @DisplayName("Builder con JSON vuoto non aggiunge al contesto")
    void testBuilderWithEmptyJson() {
        Map<String, Object> context = TransformationContext.builder()
                .withJson("")
                .build();

        assertFalse(context.containsKey(Costanti.MAP_ELEMENT_JSON_PATH));
    }

    @Test
    @DisplayName("Builder con request body")
    void testBuilderWithRequestBody() {
        Map<String, Object> context = TransformationContext.builder()
                .withRequestBody("{\"data\": 123}")
                .build();

        assertEquals("{\"data\": 123}", context.get(Costanti.MAP_REQUEST_BODY));
    }

    @Test
    @DisplayName("Builder con response body")
    void testBuilderWithResponseBody() {
        Map<String, Object> context = TransformationContext.builder()
                .withResponseBody("{\"result\": \"ok\"}")
                .build();

        assertEquals("{\"result\": \"ok\"}", context.get(Costanti.MAP_RESPONSE_BODY));
    }

    @Test
    @DisplayName("Builder con idDominio")
    void testBuilderWithIdDominio() {
        Map<String, Object> context = TransformationContext.builder()
                .withIdDominio("01234567890")
                .build();

        assertEquals("01234567890", context.get(Costanti.MAP_ID_DOMINIO));
    }

    @Test
    @DisplayName("Builder con idTipoVersamento")
    void testBuilderWithIdTipoVersamento() {
        Map<String, Object> context = TransformationContext.builder()
                .withIdTipoVersamento("TARI")
                .build();

        assertEquals("TARI", context.get(Costanti.MAP_ID_TIPO_VERSAMENTO));
    }

    @Test
    @DisplayName("Builder con idUnitaOperativa")
    void testBuilderWithIdUnitaOperativa() {
        Map<String, Object> context = TransformationContext.builder()
                .withIdUnitaOperativa("UO001")
                .build();

        assertEquals("UO001", context.get(Costanti.MAP_ID_UNITA_OPERATIVA));
    }

    @Test
    @DisplayName("Builder con versamento")
    void testBuilderWithVersamento() {
        Object versamento = new Object();
        Map<String, Object> context = TransformationContext.builder()
                .withVersamento(versamento)
                .build();

        assertEquals(versamento, context.get(Costanti.MAP_VERSAMENTO));
    }

    @Test
    @DisplayName("Builder con dominio")
    void testBuilderWithDominio() {
        Object dominio = new Object();
        Map<String, Object> context = TransformationContext.builder()
                .withDominio(dominio)
                .build();

        assertEquals(dominio, context.get(Costanti.MAP_DOMINIO));
    }

    @Test
    @DisplayName("Builder con applicazione")
    void testBuilderWithApplicazione() {
        Object applicazione = new Object();
        Map<String, Object> context = TransformationContext.builder()
                .withApplicazione(applicazione)
                .build();

        assertEquals(applicazione, context.get(Costanti.MAP_APPLICAZIONE));
    }

    @Test
    @DisplayName("Builder con RPT")
    void testBuilderWithRpt() {
        Object rpt = new Object();
        Map<String, Object> context = TransformationContext.builder()
                .withRpt(rpt)
                .build();

        assertEquals(rpt, context.get(Costanti.MAP_RPT));
    }

    @Test
    @DisplayName("Builder con utente")
    void testBuilderWithUtente() {
        Map<String, Object> utente = new HashMap<>();
        utente.put("nome", "Mario");

        Map<String, Object> context = TransformationContext.builder()
                .withUtente(utente)
                .build();

        assertEquals(utente, context.get(Costanti.MAP_UTENTE));
    }

    @Test
    @DisplayName("Builder con chiave custom")
    void testBuilderWithCustomKey() {
        Map<String, Object> context = TransformationContext.builder()
                .with("customKey", "customValue")
                .build();

        assertEquals("customValue", context.get("customKey"));
    }

    @Test
    @DisplayName("Builder con chiave null non aggiunge")
    void testBuilderWithNullKey() {
        Map<String, Object> context = TransformationContext.builder()
                .with(null, "value")
                .build();

        assertFalse(context.containsKey(null));
    }

    @Test
    @DisplayName("Builder con valore null non aggiunge")
    void testBuilderWithNullValue() {
        Map<String, Object> context = TransformationContext.builder()
                .with("key", null)
                .build();

        assertFalse(context.containsKey("key"));
    }

    @Test
    @DisplayName("Builder con mappa completa")
    void testBuilderWithAll() {
        Map<String, Object> values = new HashMap<>();
        values.put("key1", "value1");
        values.put("key2", "value2");

        Map<String, Object> context = TransformationContext.builder()
                .withAll(values)
                .build();

        assertEquals("value1", context.get("key1"));
        assertEquals("value2", context.get("key2"));
    }

    @Test
    @DisplayName("Builder con mappa null")
    void testBuilderWithAllNull() {
        Map<String, Object> context = TransformationContext.builder()
                .withAll(null)
                .build();

        // Deve contenere solo i valori di default
        assertTrue(context.containsKey(Costanti.MAP_DATE_OBJECT));
        assertTrue(context.containsKey(Costanti.MAP_CTX_OBJECT));
    }

    @Test
    @DisplayName("toString restituisce rappresentazione corretta")
    void testToString() {
        TransformationContext builder = TransformationContext.builder()
                .withIdDominio("01234567890");

        String result = builder.toString();
        assertTrue(result.contains("TransformationContext"));
        assertTrue(result.contains("keys="));
    }

    @Test
    @DisplayName("Build restituisce copia del contesto")
    void testBuildReturnsCopy() {
        TransformationContext builder = TransformationContext.builder()
                .withIdDominio("01234567890");

        Map<String, Object> context1 = builder.build();
        Map<String, Object> context2 = builder.build();

        assertNotSame(context1, context2);
        assertEquals(context1, context2);
    }
}
