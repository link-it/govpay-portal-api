package it.govpay.portal.utils.trasformazioni;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import it.govpay.portal.utils.trasformazioni.exception.JsonPathNotFoundException;
import it.govpay.portal.utils.trasformazioni.exception.JsonPathNotValidException;
import it.govpay.portal.utils.trasformazioni.exception.TrasformazioneException;

class JsonPathExtractorTest {

    private static final String SAMPLE_JSON = """
            {
                "nome": "Mario",
                "cognome": "Rossi",
                "eta": 35,
                "attivo": true,
                "indirizzo": {
                    "via": "Via Roma",
                    "civico": 123,
                    "citta": "Milano"
                },
                "telefoni": ["123456789", "987654321"],
                "ordini": [
                    {"id": 1, "importo": 100.50},
                    {"id": 2, "importo": 200.75}
                ]
            }
            """;

    @Test
    void testReadSimpleString() throws Exception {
        JsonPathExtractor extractor = new JsonPathExtractor(SAMPLE_JSON);
        String result = extractor.read("$.nome");
        assertEquals("Mario", result);
    }

    @Test
    void testReadNestedString() throws Exception {
        JsonPathExtractor extractor = new JsonPathExtractor(SAMPLE_JSON);
        String result = extractor.read("$.indirizzo.citta");
        assertEquals("Milano", result);
    }

    @Test
    void testReadNumber() throws Exception {
        JsonPathExtractor extractor = new JsonPathExtractor(SAMPLE_JSON);
        String result = extractor.read("$.eta");
        assertEquals("35", result);
    }

    @Test
    void testReadBoolean() throws Exception {
        JsonPathExtractor extractor = new JsonPathExtractor(SAMPLE_JSON);
        String result = extractor.read("$.attivo");
        assertEquals("true", result);
    }

    @Test
    void testReadListSingleElement() throws Exception {
        JsonPathExtractor extractor = new JsonPathExtractor(SAMPLE_JSON);
        String result = extractor.read("$.telefoni[0]");
        assertEquals("123456789", result);
    }

    @Test
    void testReadListAllElements() throws Exception {
        JsonPathExtractor extractor = new JsonPathExtractor(SAMPLE_JSON);
        List<String> results = extractor.readList("$.telefoni[*]");
        assertEquals(2, results.size());
        assertTrue(results.contains("123456789"));
        assertTrue(results.contains("987654321"));
    }

    @Test
    void testReadNestedArrayField() throws Exception {
        JsonPathExtractor extractor = new JsonPathExtractor(SAMPLE_JSON);
        List<String> results = extractor.readList("$.ordini[*].importo");
        assertEquals(2, results.size());
        assertTrue(results.contains("100.5"));
        assertTrue(results.contains("200.75"));
    }

    @Test
    void testReadAsNode() throws Exception {
        JsonPathExtractor extractor = new JsonPathExtractor(SAMPLE_JSON);
        JsonNode node = extractor.readAsNode("$.indirizzo");
        assertNotNull(node);
        assertTrue(node.has("via"));
        assertEquals("Via Roma", node.get("via").asText());
    }

    @Test
    void testMatchFound() throws Exception {
        JsonPathExtractor extractor = new JsonPathExtractor(SAMPLE_JSON);
        assertTrue(extractor.match("$.nome"));
    }

    @Test
    void testMatchNotFound() throws Exception {
        JsonPathExtractor extractor = new JsonPathExtractor(SAMPLE_JSON);
        assertFalse(extractor.match("$.nonEsistente"));
    }

    @Test
    void testReadNotFoundThrowsException() {
        JsonPathExtractor extractor = new JsonPathExtractor(SAMPLE_JSON);
        assertThrows(JsonPathNotFoundException.class, () -> extractor.read("$.nonEsistente"));
    }

    @Test
    void testInvalidPatternThrowsException() {
        JsonPathExtractor extractor = new JsonPathExtractor(SAMPLE_JSON);
        assertThrows(JsonPathNotValidException.class, () -> extractor.validate("[invalid"));
    }

    @Test
    void testNullDocumentThrowsException() {
        JsonPathExtractor extractor = new JsonPathExtractor(null);
        assertThrows(TrasformazioneException.class, () -> extractor.read("$.nome"));
    }

    @Test
    void testNullPatternThrowsException() {
        JsonPathExtractor extractor = new JsonPathExtractor(SAMPLE_JSON);
        assertThrows(TrasformazioneException.class, () -> extractor.read(null));
    }

    @Test
    void testStaticExtractAsString() throws Exception {
        String result = JsonPathExtractor.extractAsString(SAMPLE_JSON, "$.cognome");
        assertEquals("Rossi", result);
    }

    @Test
    void testStaticExtractAsStringNotFound() throws Exception {
        String result = JsonPathExtractor.extractAsString(SAMPLE_JSON, "$.nonEsistente");
        assertNull(result);
    }

    @Test
    void testStaticExtractAsList() throws Exception {
        List<String> results = JsonPathExtractor.extractAsList(SAMPLE_JSON, "$.telefoni[*]");
        assertEquals(2, results.size());
    }

    @Test
    void testStaticExtractAsListNotFound() throws Exception {
        List<String> results = JsonPathExtractor.extractAsList(SAMPLE_JSON, "$.nonEsistente");
        assertTrue(results.isEmpty());
    }
}
