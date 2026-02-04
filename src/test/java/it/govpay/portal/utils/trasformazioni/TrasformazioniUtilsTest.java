package it.govpay.portal.utils.trasformazioni;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import it.govpay.portal.utils.trasformazioni.exception.TrasformazioneException;

class TrasformazioniUtilsTest {

    @Test
    void testSimpleTransformation() throws Exception {
        String template = "Ciao ${nome}!";
        Map<String, Object> context = new HashMap<>();
        context.put("nome", "Mario");

        String result = TrasformazioniUtils.transform("test", template, context);
        assertEquals("Ciao Mario!", result);
    }

    @Test
    void testTransformationWithMultipleVariables() throws Exception {
        String template = "${nome} ${cognome} ha ${eta} anni.";
        Map<String, Object> context = Map.of(
                "nome", "Mario",
                "cognome", "Rossi",
                "eta", 35
        );

        String result = TrasformazioniUtils.transform("test", template, context);
        assertEquals("Mario Rossi ha 35 anni.", result);
    }

    @Test
    void testTransformationWithConditional() throws Exception {
        String template = "<#if attivo>Utente attivo<#else>Utente inattivo</#if>";

        Map<String, Object> contextActive = Map.of("attivo", true);
        assertEquals("Utente attivo", TrasformazioniUtils.transform("test", template, contextActive));

        Map<String, Object> contextInactive = Map.of("attivo", false);
        assertEquals("Utente inattivo", TrasformazioniUtils.transform("test", template, contextInactive));
    }

    @Test
    void testTransformationWithLoop() throws Exception {
        String template = "<#list items as item>${item}<#sep>, </#list>";
        Map<String, Object> context = Map.of("items", java.util.List.of("a", "b", "c"));

        String result = TrasformazioniUtils.transform("test", template, context);
        assertEquals("a, b, c", result);
    }

    @Test
    void testTransformationWithNestedObjects() throws Exception {
        String template = "${utente.nome} abita a ${utente.indirizzo.citta}";

        Map<String, Object> indirizzo = Map.of("citta", "Milano", "via", "Via Roma");
        Map<String, Object> utente = Map.of("nome", "Mario", "indirizzo", indirizzo);
        Map<String, Object> context = Map.of("utente", utente);

        String result = TrasformazioniUtils.transform("test", template, context);
        assertEquals("Mario abita a Milano", result);
    }

    @Test
    void testTransformationWithContextBuilder() throws Exception {
        String template = "Data: ${date?string('dd/MM/yyyy')}";

        TransformationContext ctx = TransformationContext.builder()
                .withDate(new Date(1704067200000L)); // 2024-01-01 00:00:00 UTC

        String result = TrasformazioniUtils.transform("test", template, ctx);
        assertTrue(result.startsWith("Data: "));
    }

    @Test
    void testTransformationWithHeaders() throws Exception {
        String template = "Authorization: ${header.Authorization}";

        Map<String, Object> context = TransformationContext.builder()
                .withHeaders(Map.of("Authorization", "Bearer token123"))
                .build();

        String result = TrasformazioniUtils.transform("test", template, context);
        assertEquals("Authorization: Bearer token123", result);
    }

    @Test
    void testTransformationWithPathParams() throws Exception {
        String template = "ID Dominio: ${pathParams.idDominio}";

        Map<String, Object> context = TransformationContext.builder()
                .withPathParams(Map.of("idDominio", "01234567890"))
                .build();

        String result = TrasformazioniUtils.transform("test", template, context);
        assertEquals("ID Dominio: 01234567890", result);
    }

    @Test
    void testTransformationWithJsonPath() throws Exception {
        String json = "{\"nome\":\"Mario\",\"cognome\":\"Rossi\"}";
        String template = "Nome: ${jsonPath.read(\"$.nome\")}";

        Map<String, Object> context = TransformationContext.builder()
                .withJson(json)
                .build();

        String result = TrasformazioniUtils.transform("test", template, context);
        assertEquals("Nome: Mario", result);
    }

    @Test
    void testTransformationWithRegExp() throws Exception {
        String url = "/api/v1/pendenze/01234567890/ABC123";
        String template = "<#assign codFiscale = urlRegExp.find(\"/pendenze/(\\\\d+)/\")!\"not found\">CF: ${codFiscale}";

        Map<String, Object> context = TransformationContext.builder()
                .withUrl(url)
                .build();

        String result = TrasformazioniUtils.transform("test", template, context);
        assertEquals("CF: 01234567890", result);
    }

    @Test
    void testTransformToBytes() throws Exception {
        String template = "Test output";
        Map<String, Object> context = new HashMap<>();

        byte[] result = TrasformazioniUtils.transformToBytes("test", template, context);
        assertEquals("Test output", new String(result));
    }

    @Test
    void testTransformToStream() throws Exception {
        String template = "Stream output";
        Map<String, Object> context = new HashMap<>();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        TrasformazioniUtils.transformToStream("test", template, context, baos);
        assertEquals("Stream output", baos.toString());
    }

    @Test
    void testValidateTemplateValid() {
        assertDoesNotThrow(() -> TrasformazioniUtils.validateTemplate("test", "Hello ${name}!"));
    }

    @Test
    void testValidateTemplateInvalid() {
        assertThrows(TrasformazioneException.class,
                () -> TrasformazioniUtils.validateTemplate("test", "Hello ${name!"));
    }

    @Test
    void testTransformationWithStaticMethods() throws Exception {
        String template = "${class[\"java.lang.Math\"].max(5, 10)}";
        Map<String, Object> context = new HashMap<>();

        String result = TrasformazioniUtils.transform("test", template, context);
        assertEquals("10", result);
    }

    @Test
    void testTransformationWithNewInstance() throws Exception {
        String template = "<#assign sb = new(\"java.lang.StringBuilder\")>${sb.append(\"Hello\").append(\" World\").toString()}";
        Map<String, Object> context = new HashMap<>();

        String result = TrasformazioniUtils.transform("test", template, context);
        assertEquals("Hello World", result);
    }

    @Test
    void testResponseMapAvailable() throws Exception {
        // Verifica che responseMap sia disponibile nel contesto (anche se non modificabile direttamente)
        String template = "<#if responseMap??>responseMap disponibile<#else>responseMap non disponibile</#if>";
        Map<String, Object> context = new HashMap<>();

        String result = TrasformazioniUtils.transform("test", template, context);
        assertEquals("responseMap disponibile", result);
    }

    @Test
    void testContextBuilderWithAllParameters() {
        Map<String, Object> context = TransformationContext.builder()
                .withDate()
                .withRandomTransactionId()
                .withHeaders(Map.of("X-Header", "value"))
                .withQueryParams(Map.of("param", "value"))
                .withPathParams(Map.of("id", "123"))
                .withUrl("http://example.com")
                .withJson("{\"key\":\"value\"}")
                .withIdDominio("01234567890")
                .withIdTipoVersamento("TIPO1")
                .with("custom", "customValue")
                .build();

        assertNotNull(context.get(Costanti.MAP_DATE_OBJECT));
        assertNotNull(context.get(Costanti.MAP_TRANSACTION_ID_OBJECT));
        assertNotNull(context.get(Costanti.MAP_HEADER));
        assertNotNull(context.get(Costanti.MAP_QUERY_PARAMETER));
        assertNotNull(context.get(Costanti.MAP_PATH_PARAMETER));
        assertNotNull(context.get(Costanti.MAP_ELEMENT_URL_REGEXP));
        assertNotNull(context.get(Costanti.MAP_ELEMENT_JSON_PATH));
        assertEquals("01234567890", context.get(Costanti.MAP_ID_DOMINIO));
        assertEquals("TIPO1", context.get(Costanti.MAP_ID_TIPO_VERSAMENTO));
        assertEquals("customValue", context.get("custom"));
    }
}
