package it.govpay.portal.utils.trasformazioni;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import it.govpay.portal.utils.trasformazioni.exception.JsonPathNotFoundException;
import it.govpay.portal.utils.trasformazioni.exception.JsonPathNotValidException;

/**
 * Test per gli estrattori (RegExpExtractor e JsonPathExtractor).
 */
class ExtractorsTest {

    @Nested
    @DisplayName("Test RegExpExtractor")
    class RegExpExtractorTest {

        @Test
        @DisplayName("Trova match con find")
        void testFindMatch() throws Exception {
            RegExpExtractor extractor = new RegExpExtractor("https://api.example.com/users/12345/orders");

            String result = extractor.find("/users/(\\d+)/");
            assertEquals("12345", result);
        }

        @Test
        @DisplayName("Estrae gruppo con pattern complesso")
        void testExtractGroupWithComplexPattern() throws Exception {
            RegExpExtractor extractor = new RegExpExtractor("order-2024-001-confirmed");

            // read richiede un match completo
            String result = extractor.read("order-(\\d+)-(\\d+)-(\\w+)");
            assertEquals("2024", result); // primo gruppo catturato
        }

        @Test
        @DisplayName("Restituisce null se pattern non matcha")
        void testNoMatchReturnsNull() throws Exception {
            RegExpExtractor extractor = new RegExpExtractor("test string");

            String result = extractor.read("^\\d+$");
            assertNull(result);
        }

        @Test
        @DisplayName("Match verifica corrispondenza")
        void testMatch() throws Exception {
            RegExpExtractor extractor = new RegExpExtractor("12345");

            assertTrue(extractor.match("\\d+"));
            assertFalse(extractor.match("[a-z]+"));
        }

        @Test
        @DisplayName("Found verifica presenza parziale")
        void testFound() throws Exception {
            RegExpExtractor extractor = new RegExpExtractor("abc-123-def");

            assertTrue(extractor.found("\\d+"));
            assertFalse(extractor.found("xyz"));
        }

        @Test
        @DisplayName("FindAll trova tutti i match")
        void testFindAll() throws Exception {
            RegExpExtractor extractor = new RegExpExtractor("abc-123-def-456-ghi");

            java.util.List<String> results = extractor.findAll("\\d+");
            assertEquals(2, results.size());
            assertEquals("123", results.get(0));
            assertEquals("456", results.get(1));
        }

        @Test
        @DisplayName("ReadList restituisce tutti i gruppi")
        void testReadList() throws Exception {
            RegExpExtractor extractor = new RegExpExtractor("order-2024-001-confirmed");

            java.util.List<String> results = extractor.readList("order-(\\d+)-(\\d+)-(\\w+)");
            assertEquals(3, results.size());
            assertEquals("2024", results.get(0));
            assertEquals("001", results.get(1));
            assertEquals("confirmed", results.get(2));
        }

        @Test
        @DisplayName("ReplaceAll sostituisce tutte le occorrenze")
        void testReplaceAll() throws Exception {
            RegExpExtractor extractor = new RegExpExtractor("abc-123-def-456");

            String result = extractor.replaceAll("\\d+", "XXX");
            assertEquals("abc-XXX-def-XXX", result);
        }

        @Test
        @DisplayName("Gestisce contenuto null")
        void testNullContent() throws Exception {
            RegExpExtractor extractor = new RegExpExtractor(null);

            assertNull(extractor.read("\\d+"));
            assertNull(extractor.find("\\d+"));
            assertTrue(extractor.readList("\\d+").isEmpty());
            assertTrue(extractor.findAll("\\d+").isEmpty());
            assertNull(extractor.replaceAll("\\d+", "X"));
        }

        @Test
        @DisplayName("IsValidPattern verifica pattern valido")
        void testIsValidPattern() {
            assertTrue(RegExpExtractor.isValidPattern("\\d+"));
            assertTrue(RegExpExtractor.isValidPattern("[a-z]+"));
            assertFalse(RegExpExtractor.isValidPattern("[invalid"));
        }

        @Test
        @DisplayName("Lancia eccezione per pattern non valido")
        void testInvalidPatternThrowsException() {
            RegExpExtractor extractor = new RegExpExtractor("test");

            assertThrows(it.govpay.portal.utils.trasformazioni.exception.TrasformazioneException.class,
                () -> extractor.read("[invalid"));
        }
    }

    @Nested
    @DisplayName("Test JsonPathExtractor")
    class JsonPathExtractorTest {

        @Test
        @DisplayName("Estrae valore stringa")
        void testExtractString() throws Exception {
            String json = "{\"name\": \"Mario\", \"age\": 30}";
            JsonPathExtractor extractor = new JsonPathExtractor(json);

            Object result = extractor.read("$.name");
            assertEquals("Mario", result);
        }

        @Test
        @DisplayName("Estrae valore numerico")
        void testExtractNumber() throws Exception {
            String json = "{\"name\": \"Mario\", \"age\": 30}";
            JsonPathExtractor extractor = new JsonPathExtractor(json);

            Object result = extractor.read("$.age");
            // JsonPath può restituire String o Integer a seconda della configurazione
            assertNotNull(result);
            assertTrue(result.toString().equals("30"));
        }

        @Test
        @DisplayName("Estrae oggetto nested")
        void testExtractNestedObject() throws Exception {
            String json = "{\"user\": {\"name\": \"Mario\", \"address\": {\"city\": \"Roma\"}}}";
            JsonPathExtractor extractor = new JsonPathExtractor(json);

            Object result = extractor.read("$.user.address.city");
            assertEquals("Roma", result);
        }

        @Test
        @DisplayName("Estrae elemento da array")
        void testExtractArrayElement() throws Exception {
            String json = "{\"items\": [\"a\", \"b\", \"c\"]}";
            JsonPathExtractor extractor = new JsonPathExtractor(json);

            Object result = extractor.read("$.items[1]");
            assertEquals("b", result);
        }

        @Test
        @DisplayName("Lancia eccezione per path non trovato")
        void testPathNotFound() {
            String json = "{\"name\": \"Mario\"}";
            JsonPathExtractor extractor = new JsonPathExtractor(json);

            assertThrows(JsonPathNotFoundException.class, () -> extractor.read("$.nonexistent"));
        }

        @Test
        @DisplayName("Lancia eccezione per path non valido")
        void testInvalidPath() {
            String json = "{\"name\": \"Mario\"}";
            JsonPathExtractor extractor = new JsonPathExtractor(json);

            // Può lanciare vari tipi di eccezione
            assertThrows(Exception.class, () -> extractor.read("invalid[path"));
        }

        @Test
        @DisplayName("Gestisce JSON null")
        void testNullJson() {
            JsonPathExtractor extractor = new JsonPathExtractor(null);

            // JsonPathExtractor lancia TrasformazioneException per JSON null
            assertThrows(Exception.class, () -> extractor.read("$.name"));
        }

        @Test
        @DisplayName("Gestisce JSON vuoto")
        void testEmptyJson() {
            JsonPathExtractor extractor = new JsonPathExtractor("");

            // JsonPathExtractor lancia TrasformazioneException per JSON vuoto
            assertThrows(Exception.class, () -> extractor.read("$.name"));
        }

        @Test
        @DisplayName("Gestisce JSON malformato")
        void testMalformedJson() {
            JsonPathExtractor extractor = new JsonPathExtractor("{invalid json}");

            // JsonPathExtractor lancia TrasformazioneException per JSON malformato
            assertThrows(Exception.class, () -> extractor.read("$.name"));
        }

        @Test
        @DisplayName("Estrae valore booleano")
        void testExtractBoolean() throws Exception {
            String json = "{\"active\": true, \"deleted\": false}";
            JsonPathExtractor extractor = new JsonPathExtractor(json);

            // JsonPath può restituire String a seconda della configurazione
            Object active = extractor.read("$.active");
            Object deleted = extractor.read("$.deleted");
            assertNotNull(active);
            assertNotNull(deleted);
            assertEquals("true", active.toString());
            assertEquals("false", deleted.toString());
        }

        @Test
        @DisplayName("Estrae valore da oggetto complesso")
        void testExtractFromComplexObject() throws Exception {
            String json = "{\"user\": {\"name\": \"Mario\"}}";
            JsonPathExtractor extractor = new JsonPathExtractor(json);

            Object result = extractor.read("$.user.name");
            assertEquals("Mario", result);
        }

        @Test
        @DisplayName("Estrae con wildcard")
        void testExtractWithWildcard() throws Exception {
            String json = "{\"items\": [{\"id\": 1}, {\"id\": 2}, {\"id\": 3}]}";
            JsonPathExtractor extractor = new JsonPathExtractor(json);

            Object result = extractor.read("$.items[*].id");
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("Test eccezioni")
    class ExceptionsTest {

        @Test
        @DisplayName("JsonPathNotFoundException con messaggio")
        void testJsonPathNotFoundException() {
            JsonPathNotFoundException ex = new JsonPathNotFoundException("Path not found");
            assertEquals("Path not found", ex.getMessage());
        }

        @Test
        @DisplayName("JsonPathNotFoundException con causa")
        void testJsonPathNotFoundExceptionWithCause() {
            Exception cause = new RuntimeException("original");
            JsonPathNotFoundException ex = new JsonPathNotFoundException("Path not found", cause);
            assertEquals("Path not found", ex.getMessage());
            assertEquals(cause, ex.getCause());
        }

        @Test
        @DisplayName("JsonPathNotValidException con messaggio")
        void testJsonPathNotValidException() {
            JsonPathNotValidException ex = new JsonPathNotValidException("Invalid path");
            assertEquals("Invalid path", ex.getMessage());
        }

        @Test
        @DisplayName("JsonPathNotValidException con causa")
        void testJsonPathNotValidExceptionWithCause() {
            Exception cause = new RuntimeException("original");
            JsonPathNotValidException ex = new JsonPathNotValidException("Invalid path", cause);
            assertEquals("Invalid path", ex.getMessage());
            assertEquals(cause, ex.getCause());
        }
    }
}
