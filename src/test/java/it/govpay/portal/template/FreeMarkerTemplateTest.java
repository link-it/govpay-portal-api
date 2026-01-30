package it.govpay.portal.template;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openspcoop2.utils.json.JSONUtils;
import org.openspcoop2.utils.json.JsonPathExpressionEngine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.govpay.portal.utils.trasformazioni.TransformationContext;
import it.govpay.portal.utils.trasformazioni.TrasformazioniUtils;

class FreeMarkerTemplateTest {

    private ObjectMapper objectMapper;

    // Template fornito dall'utente (mensa scolastica)
    private static final String MENSA_TEMPLATE = """
            <#assign jsonUtilities = class["org.openspcoop2.utils.json.JSONUtils"].getInstance()>
            <#assign request = jsonUtilities.getAsNode(jsonPath.read("$"))>
            <#assign calendar = class["java.util.Calendar"]>
            <#assign now = new("java.util.Date")>
            <#assign calendarInstance = calendar.getInstance()>
            <#assign xxx = calendarInstance.setTime(now)!>
            <#assign yyy = calendarInstance.add(calendar.MONTH, 1)!>
            <#assign zzz = calendarInstance.set(calendar.DATE, calendarInstance.getActualMaximum(calendar.DAY_OF_MONTH))!>
            <#assign dataValidita = calendarInstance.getTime()?string("yyyy-MM-dd")>
            <#assign dataOraGiuliana = calendarInstance.getTime()?string("yyyyDDDHHmmss")>
            <#assign biglietti = request.get("biglietti").asText()>
            <#assign importo = request.get("importo").asText()>
            <#setting locale="en_US">
            {
                "idA2A": "A2A-DEMO",
                "idPendenza": "${request.get("identificativo").asText()}-${dataOraGiuliana}",
                "idDominio": "${pathParams["idDominio"]}",
                "idTipoPendenza": "${pathParams["idTipoPendenza"]}",
                "causale": "Mensa scolastica - Acquisto ${biglietti} buoni pasto",
                "soggettoPagatore": {
                    "tipo": <#if request.get("identificativo").asText()?length == 11>"G"<#else>"F"</#if>,
                    "identificativo": "${request.get("identificativo").asText()}",
                    "anagrafica": "${request.get("anagrafica").asText()}",
                    "email": "${request.get("email").asText()}"
                },
                "importo": "${importo}",
                "dataValidita": "${dataValidita}",
                "dataScadenza": "${dataValidita}",
                "tassonomiaAvviso": "Servizi erogati dal comune",
                "voci": [
                    {
                        "idVocePendenza": "1",
                        "importo": "${importo}",
                        "descrizione": "${biglietti} buoni pasto",
                        "ibanAccredito": "IT02L1234500000111110000001",
                        "tipoContabilita": "ALTRO",
                        "codiceContabilita": "${pathParams["idTipoPendenza"]}"
                    }
                ]
            }
            """;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Template mensa scolastica - persona fisica")
    void shouldProcessMensaTemplateForPersonaFisica() throws Exception {
        // Dati di input (request body)
        String requestBody = """
                {
                    "identificativo": "RSSMRA80A01H501U",
                    "anagrafica": "Mario Rossi",
                    "email": "mario.rossi@example.com",
                    "biglietti": "10",
                    "importo": "45.00"
                }
                """;

        // Path parameters
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("idDominio", "12345678901");
        pathParams.put("idTipoPendenza", "MENSA");

        // Esegui il template usando TrasformazioniUtils
        TransformationContext context = TransformationContext.builder()
                .withJson(requestBody)
                .withPathParams(pathParams);

        String result = TrasformazioniUtils.transform("mensa-template", MENSA_TEMPLATE, context);

        // Verifica che il risultato sia un JSON valido
        JsonNode resultNode = objectMapper.readTree(result.trim());

        // Verifiche
        assertEquals("A2A-DEMO", resultNode.get("idA2A").asText());
        assertEquals("12345678901", resultNode.get("idDominio").asText());
        assertEquals("MENSA", resultNode.get("idTipoPendenza").asText());
        assertTrue(resultNode.get("causale").asText().contains("10 buoni pasto"));
        assertEquals("45.00", resultNode.get("importo").asText());

        // Verifica soggetto pagatore
        JsonNode soggetto = resultNode.get("soggettoPagatore");
        assertEquals("F", soggetto.get("tipo").asText()); // Persona fisica (CF 16 caratteri)
        assertEquals("RSSMRA80A01H501U", soggetto.get("identificativo").asText());
        assertEquals("Mario Rossi", soggetto.get("anagrafica").asText());
        assertEquals("mario.rossi@example.com", soggetto.get("email").asText());

        // Verifica voci
        JsonNode voci = resultNode.get("voci");
        assertEquals(1, voci.size());
        assertEquals("10 buoni pasto", voci.get(0).get("descrizione").asText());
        assertEquals("45.00", voci.get(0).get("importo").asText());
        assertEquals("MENSA", voci.get(0).get("codiceContabilita").asText());

        // Verifica date (devono essere valorizzate)
        assertNotNull(resultNode.get("dataValidita").asText());
        assertNotNull(resultNode.get("dataScadenza").asText());
        assertTrue(resultNode.get("dataValidita").asText().matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    @DisplayName("Template mensa scolastica - persona giuridica")
    void shouldProcessMensaTemplateForPersonaGiuridica() throws Exception {
        // Dati di input con partita IVA (11 caratteri -> persona giuridica)
        String requestBody = """
                {
                    "identificativo": "12345678901",
                    "anagrafica": "Azienda SRL",
                    "email": "info@azienda.it",
                    "biglietti": "50",
                    "importo": "225.00"
                }
                """;

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("idDominio", "98765432109");
        pathParams.put("idTipoPendenza", "MENSA");

        TransformationContext context = TransformationContext.builder()
                .withJson(requestBody)
                .withPathParams(pathParams);

        String result = TrasformazioniUtils.transform("mensa-template", MENSA_TEMPLATE, context);
        JsonNode resultNode = objectMapper.readTree(result.trim());

        // Verifica che sia persona giuridica
        assertEquals("G", resultNode.get("soggettoPagatore").get("tipo").asText());
        assertEquals("50 buoni pasto", resultNode.get("voci").get(0).get("descrizione").asText());
    }

    @Test
    @DisplayName("Verifica accesso a classi statiche via class[]")
    void shouldAccessStaticClassesViaClassBuiltin() throws Exception {
        // Template semplice che usa solo java.lang.Math
        String template = """
                <#assign math = class["java.lang.Math"]>
                Il valore di PI e': ${math.PI}
                """;

        TransformationContext context = TransformationContext.builder();
        String result = TrasformazioniUtils.transform("math-test", template, context);

        assertTrue(result.contains("3.14"), "Dovrebbe contenere il valore di PI");
    }

    @Test
    @DisplayName("JSONUtils - getAsNode funziona correttamente")
    void jsonUtilsGetAsNodeShouldWork() throws Exception {
        JSONUtils jsonUtils = JSONUtils.getInstance();

        String json = """
                {"nome": "Mario", "cognome": "Rossi", "eta": 42}
                """;

        JsonNode node = jsonUtils.getAsNode(json);

        assertNotNull(node);
        assertEquals("Mario", node.get("nome").asText());
        assertEquals("Rossi", node.get("cognome").asText());
        assertEquals(42, node.get("eta").asInt());
    }

    @Test
    @DisplayName("JsonPathExpressionEngine - read funziona correttamente")
    void jsonPathReadShouldWork() {
        String json = """
                {
                    "persona": {
                        "nome": "Mario",
                        "cognome": "Rossi"
                    },
                    "importo": 100.50
                }
                """;

        JsonPathExpressionEngine jsonPath = new JsonPathExpressionEngine(json);

        // Leggi l'intero documento
        String root = jsonPath.read("$");
        assertNotNull(root);
        assertTrue(root.contains("Mario"));

        // Leggi un campo specifico
        String nome = jsonPath.read("$.persona.nome");
        assertEquals("Mario", nome);

        // Leggi un numero
        String importo = jsonPath.read("$.importo");
        assertEquals("100.5", importo);
    }

    @Test
    @DisplayName("Template semplice senza dipendenze esterne")
    void shouldProcessSimpleTemplate() throws Exception {
        String simpleTemplate = """
                <#assign data = jsonPath.read("$")>
                <#assign jsonUtils = class["org.openspcoop2.utils.json.JSONUtils"].getInstance()>
                <#assign node = jsonUtils.getAsNode(data)>
                {
                    "messaggio": "Ciao ${node.get("nome").asText()}!",
                    "dominio": "${pathParams["idDominio"]}"
                }
                """;

        String requestBody = """
                {"nome": "Mondo"}
                """;

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("idDominio", "TEST123");

        TransformationContext context = TransformationContext.builder()
                .withJson(requestBody)
                .withPathParams(pathParams);

        String result = TrasformazioniUtils.transform("simple-template", simpleTemplate, context);
        JsonNode resultNode = objectMapper.readTree(result.trim());

        assertEquals("Ciao Mondo!", resultNode.get("messaggio").asText());
        assertEquals("TEST123", resultNode.get("dominio").asText());
    }

}
