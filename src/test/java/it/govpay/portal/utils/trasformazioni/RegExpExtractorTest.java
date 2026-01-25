package it.govpay.portal.utils.trasformazioni;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import it.govpay.portal.utils.trasformazioni.exception.TrasformazioneException;

class RegExpExtractorTest {

    @Test
    void testMatchFullString() throws Exception {
        RegExpExtractor extractor = new RegExpExtractor("hello world");
        assertTrue(extractor.match("hello world"));
    }

    @Test
    void testMatchWithPattern() throws Exception {
        RegExpExtractor extractor = new RegExpExtractor("12345");
        assertTrue(extractor.match("\\d+"));
    }

    @Test
    void testMatchNoMatch() throws Exception {
        RegExpExtractor extractor = new RegExpExtractor("hello");
        assertFalse(extractor.match("\\d+"));
    }

    @Test
    void testReadWithCapturingGroup() throws Exception {
        RegExpExtractor extractor = new RegExpExtractor("user@example.com");
        String result = extractor.read("(\\w+)@(\\w+)\\.(\\w+)");
        assertEquals("user", result); // First capturing group
    }

    @Test
    void testReadListWithCapturingGroups() throws Exception {
        RegExpExtractor extractor = new RegExpExtractor("user@example.com");
        List<String> results = extractor.readList("(\\w+)@(\\w+)\\.(\\w+)");
        assertEquals(3, results.size());
        assertEquals("user", results.get(0));
        assertEquals("example", results.get(1));
        assertEquals("com", results.get(2));
    }

    @Test
    void testFindPartialMatch() throws Exception {
        RegExpExtractor extractor = new RegExpExtractor("Il codice è ABC123 ok");
        String result = extractor.find("([A-Z]+\\d+)");
        assertEquals("ABC123", result);
    }

    @Test
    void testFoundPartialMatch() throws Exception {
        RegExpExtractor extractor = new RegExpExtractor("Il codice è ABC123 ok");
        assertTrue(extractor.found("[A-Z]+\\d+"));
    }

    @Test
    void testFindAllMatches() throws Exception {
        RegExpExtractor extractor = new RegExpExtractor("a1 b2 c3 d4");
        List<String> results = extractor.findAll("([a-z]\\d)");
        assertEquals(4, results.size());
        assertTrue(results.contains("a1"));
        assertTrue(results.contains("b2"));
        assertTrue(results.contains("c3"));
        assertTrue(results.contains("d4"));
    }

    @Test
    void testReplaceAll() throws Exception {
        RegExpExtractor extractor = new RegExpExtractor("hello world");
        String result = extractor.replaceAll("world", "universe");
        assertEquals("hello universe", result);
    }

    @Test
    void testReplaceAllWithPattern() throws Exception {
        RegExpExtractor extractor = new RegExpExtractor("abc123def456");
        String result = extractor.replaceAll("\\d+", "X");
        assertEquals("abcXdefX", result);
    }

    @Test
    void testNullContentRead() throws Exception {
        RegExpExtractor extractor = new RegExpExtractor(null);
        assertNull(extractor.read(".*"));
    }

    @Test
    void testNullContentFind() throws Exception {
        RegExpExtractor extractor = new RegExpExtractor(null);
        assertNull(extractor.find(".*"));
    }

    @Test
    void testInvalidPatternThrowsException() {
        RegExpExtractor extractor = new RegExpExtractor("test");
        assertThrows(TrasformazioneException.class, () -> extractor.read("[invalid"));
    }

    @Test
    void testIsValidPatternTrue() {
        assertTrue(RegExpExtractor.isValidPattern("\\d+"));
        assertTrue(RegExpExtractor.isValidPattern("[a-z]+"));
        assertTrue(RegExpExtractor.isValidPattern("^hello$"));
    }

    @Test
    void testIsValidPatternFalse() {
        assertFalse(RegExpExtractor.isValidPattern("[invalid"));
        assertFalse(RegExpExtractor.isValidPattern("(unclosed"));
    }

    @Test
    void testUrlPatternExtraction() throws Exception {
        String url = "/api/v1/pendenze/01234567890/ABC123";
        RegExpExtractor extractor = new RegExpExtractor(url);

        // Match path parameters
        String result = extractor.find("/pendenze/(\\d+)/");
        assertEquals("01234567890", result);
    }

    @Test
    void testExtractAllPathParams() throws Exception {
        String url = "/api/v1/domini/12345/versamenti/67890";
        RegExpExtractor extractor = new RegExpExtractor(url);

        List<String> params = extractor.findAll("/(\\d+)");
        assertEquals(2, params.size());
        assertEquals("12345", params.get(0));
        assertEquals("67890", params.get(1));
    }
}
