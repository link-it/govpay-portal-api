package it.govpay.portal.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CausaleUtilsTest {

    @Nested
    @DisplayName("decode Tests")
    class DecodeTests {

        @Test
        @DisplayName("Null input dovrebbe restituire null")
        void nullInputShouldReturnNull() throws UnsupportedEncodingException {
            assertNull(CausaleUtils.decode(null));
        }

        @Test
        @DisplayName("Stringa vuota dovrebbe restituire null")
        void emptyStringShouldReturnNull() throws UnsupportedEncodingException {
            assertNull(CausaleUtils.decode(""));
        }

        @Test
        @DisplayName("Stringa con solo spazi dovrebbe restituire null")
        void whitespaceStringShouldReturnNull() throws UnsupportedEncodingException {
            assertNull(CausaleUtils.decode("   "));
        }

        @Test
        @DisplayName("Formato 01 dovrebbe restituire CausaleSemplice")
        void format01ShouldReturnCausaleSemplice() throws UnsupportedEncodingException {
            String base64 = Base64.getEncoder().encodeToString("TARI 2024".getBytes(StandardCharsets.UTF_8));
            String encoded = "01 " + base64;

            Causale result = CausaleUtils.decode(encoded);

            assertTrue(result instanceof CausaleSemplice);
            assertEquals("TARI 2024", ((CausaleSemplice) result).getCausale());
        }

        @Test
        @DisplayName("Formato 01 senza contenuto dovrebbe restituire null")
        void format01WithoutContentShouldReturnNull() throws UnsupportedEncodingException {
            assertNull(CausaleUtils.decode("01"));
        }

        @Test
        @DisplayName("Formato 02 dovrebbe restituire CausaleSpezzoni")
        void format02ShouldReturnCausaleSpezzoni() throws UnsupportedEncodingException {
            String base64_1 = Base64.getEncoder().encodeToString("Voce 1".getBytes(StandardCharsets.UTF_8));
            String base64_2 = Base64.getEncoder().encodeToString("Voce 2".getBytes(StandardCharsets.UTF_8));
            String encoded = "02 " + base64_1 + " " + base64_2;

            Causale result = CausaleUtils.decode(encoded);

            assertTrue(result instanceof CausaleSpezzoni);
            CausaleSpezzoni spezzoni = (CausaleSpezzoni) result;
            assertEquals(2, spezzoni.getSpezzoni().size());
            assertEquals("Voce 1", spezzoni.getSpezzoni().get(0));
            assertEquals("Voce 2", spezzoni.getSpezzoni().get(1));
        }

        @Test
        @DisplayName("Formato 03 dovrebbe restituire CausaleSpezzoniStrutturati")
        void format03ShouldReturnCausaleSpezzoniStrutturati() throws UnsupportedEncodingException {
            String base64Voce = Base64.getEncoder().encodeToString("TARI".getBytes(StandardCharsets.UTF_8));
            String base64Importo = Base64.getEncoder().encodeToString("100.50".getBytes(StandardCharsets.UTF_8));
            String encoded = "03 " + base64Voce + " " + base64Importo;

            Causale result = CausaleUtils.decode(encoded);

            assertTrue(result instanceof CausaleSpezzoniStrutturati);
            CausaleSpezzoniStrutturati strutturati = (CausaleSpezzoniStrutturati) result;
            assertEquals(1, strutturati.getSpezzoni().size());
            assertEquals(1, strutturati.getImporti().size());
            assertEquals("TARI", strutturati.getSpezzoni().get(0));
            assertEquals(new BigDecimal("100.5"), strutturati.getImporti().get(0));
        }

        @Test
        @DisplayName("Formato 03 con multipli spezzoni dovrebbe funzionare")
        void format03WithMultipleSpezzoniShouldWork() throws UnsupportedEncodingException {
            String base64Voce1 = Base64.getEncoder().encodeToString("TARI".getBytes(StandardCharsets.UTF_8));
            String base64Importo1 = Base64.getEncoder().encodeToString("100.50".getBytes(StandardCharsets.UTF_8));
            String base64Voce2 = Base64.getEncoder().encodeToString("IMU".getBytes(StandardCharsets.UTF_8));
            String base64Importo2 = Base64.getEncoder().encodeToString("200.00".getBytes(StandardCharsets.UTF_8));
            String encoded = "03 " + base64Voce1 + " " + base64Importo1 + " " + base64Voce2 + " " + base64Importo2;

            Causale result = CausaleUtils.decode(encoded);

            assertTrue(result instanceof CausaleSpezzoniStrutturati);
            CausaleSpezzoniStrutturati strutturati = (CausaleSpezzoniStrutturati) result;
            assertEquals(2, strutturati.getSpezzoni().size());
            assertEquals(2, strutturati.getImporti().size());
        }

        @Test
        @DisplayName("Formato non riconosciuto dovrebbe lanciare UnsupportedEncodingException")
        void unrecognizedFormatShouldThrowException() {
            assertThrows(UnsupportedEncodingException.class, () -> CausaleUtils.decode("04 test"));
        }

        @Test
        @DisplayName("Formato 99 dovrebbe lanciare UnsupportedEncodingException")
        void format99ShouldThrowException() {
            assertThrows(UnsupportedEncodingException.class, () -> CausaleUtils.decode("99 test"));
        }
    }

    @Nested
    @DisplayName("getSimple Tests")
    class GetSimpleTests {

        @Test
        @DisplayName("Null input dovrebbe restituire null")
        void nullInputShouldReturnNull() {
            assertNull(CausaleUtils.getSimple(null));
        }

        @Test
        @DisplayName("Stringa vuota dovrebbe restituire null")
        void emptyStringShouldReturnNull() {
            assertNull(CausaleUtils.getSimple(""));
        }

        @Test
        @DisplayName("Formato 01 dovrebbe restituire testo semplice")
        void format01ShouldReturnSimpleText() {
            String base64 = Base64.getEncoder().encodeToString("TARI 2024".getBytes(StandardCharsets.UTF_8));
            String encoded = "01 " + base64;

            String result = CausaleUtils.getSimple(encoded);

            assertEquals("TARI 2024", result);
        }

        @Test
        @DisplayName("Formato 02 dovrebbe restituire primo spezzone")
        void format02ShouldReturnFirstSpezzone() {
            String base64_1 = Base64.getEncoder().encodeToString("Prima voce".getBytes(StandardCharsets.UTF_8));
            String base64_2 = Base64.getEncoder().encodeToString("Seconda voce".getBytes(StandardCharsets.UTF_8));
            String encoded = "02 " + base64_1 + " " + base64_2;

            String result = CausaleUtils.getSimple(encoded);

            assertEquals("Prima voce", result);
        }

        @Test
        @DisplayName("Formato 03 dovrebbe restituire importo e primo spezzone")
        void format03ShouldReturnImportoAndFirstSpezzone() {
            String base64Voce = Base64.getEncoder().encodeToString("TARI".getBytes(StandardCharsets.UTF_8));
            String base64Importo = Base64.getEncoder().encodeToString("100.5".getBytes(StandardCharsets.UTF_8));
            String encoded = "03 " + base64Voce + " " + base64Importo;

            String result = CausaleUtils.getSimple(encoded);

            assertEquals("100.5: TARI", result);
        }

        @Test
        @DisplayName("Formato non valido dovrebbe restituire stringa originale")
        void invalidFormatShouldReturnOriginalString() {
            String result = CausaleUtils.getSimple("99 invalid");
            assertEquals("99 invalid", result);
        }

        @Test
        @DisplayName("Formato 01 senza contenuto dovrebbe restituire null")
        void format01WithoutContentShouldReturnNull() {
            assertNull(CausaleUtils.getSimple("01"));
        }
    }

    @Nested
    @DisplayName("Round-trip Tests")
    class RoundTripTests {

        @Test
        @DisplayName("CausaleSemplice encode/decode dovrebbe essere consistente")
        void causaleSempliceEncodeShouldBeConsistent() throws UnsupportedEncodingException {
            CausaleSemplice original = new CausaleSemplice("Pagamento TARI 2024");
            String encoded = original.encode();

            Causale decoded = CausaleUtils.decode(encoded);

            assertTrue(decoded instanceof CausaleSemplice);
            assertEquals("Pagamento TARI 2024", ((CausaleSemplice) decoded).getCausale());
        }

        @Test
        @DisplayName("CausaleSpezzoni encode/decode dovrebbe essere consistente")
        void causaleSpezzoniEncodeShouldBeConsistent() throws UnsupportedEncodingException {
            CausaleSpezzoni original = new CausaleSpezzoni();
            original.getSpezzoni().add("Voce 1");
            original.getSpezzoni().add("Voce 2");
            String encoded = original.encode();

            Causale decoded = CausaleUtils.decode(encoded);

            assertTrue(decoded instanceof CausaleSpezzoni);
            CausaleSpezzoni spezzoni = (CausaleSpezzoni) decoded;
            assertEquals(2, spezzoni.getSpezzoni().size());
            assertEquals("Voce 1", spezzoni.getSpezzoni().get(0));
            assertEquals("Voce 2", spezzoni.getSpezzoni().get(1));
        }

        @Test
        @DisplayName("CausaleSpezzoniStrutturati encode/decode dovrebbe essere consistente")
        void causaleSpezzoniStrutturatiEncodeShouldBeConsistent() throws UnsupportedEncodingException {
            CausaleSpezzoniStrutturati original = new CausaleSpezzoniStrutturati();
            original.addSpezzoneStrutturato("TARI", new BigDecimal("100.50"));
            original.addSpezzoneStrutturato("IMU", new BigDecimal("200.00"));
            String encoded = original.encode();

            Causale decoded = CausaleUtils.decode(encoded);

            assertTrue(decoded instanceof CausaleSpezzoniStrutturati);
            CausaleSpezzoniStrutturati strutturati = (CausaleSpezzoniStrutturati) decoded;
            assertEquals(2, strutturati.getSpezzoni().size());
            assertEquals("TARI", strutturati.getSpezzoni().get(0));
            assertEquals("IMU", strutturati.getSpezzoni().get(1));
        }
    }
}
