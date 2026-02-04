package it.govpay.portal.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CausaleTest {

    @Nested
    @DisplayName("CausaleSemplice Tests")
    class CausaleSempliceTests {

        @Test
        @DisplayName("Costruttore vuoto dovrebbe creare istanza valida")
        void emptyConstructorShouldCreateValidInstance() {
            CausaleSemplice causale = new CausaleSemplice();
            assertNotNull(causale);
            assertNull(causale.getCausale());
        }

        @Test
        @DisplayName("Costruttore con parametro dovrebbe impostare causale")
        void paramConstructorShouldSetCausale() {
            CausaleSemplice causale = new CausaleSemplice("TARI 2024");
            assertEquals("TARI 2024", causale.getCausale());
        }

        @Test
        @DisplayName("encode dovrebbe restituire formato 01 base64")
        void encodeShouldReturnFormat01Base64() {
            CausaleSemplice causale = new CausaleSemplice("TARI 2024");
            String encoded = causale.encode();

            assertTrue(encoded.startsWith("01 "));
            String base64Part = encoded.substring(3);
            String decoded = new String(Base64.getDecoder().decode(base64Part), StandardCharsets.UTF_8);
            assertEquals("TARI 2024", decoded);
        }

        @Test
        @DisplayName("encode con causale null dovrebbe restituire null")
        void encodeWithNullCausaleShouldReturnNull() {
            CausaleSemplice causale = new CausaleSemplice();
            assertNull(causale.encode());
        }

        @Test
        @DisplayName("getSimple dovrebbe restituire la causale")
        void getSimpleShouldReturnCausale() {
            CausaleSemplice causale = new CausaleSemplice("TARI 2024");
            assertEquals("TARI 2024", causale.getSimple());
        }

        @Test
        @DisplayName("toString dovrebbe restituire la causale")
        void toStringShouldReturnCausale() {
            CausaleSemplice causale = new CausaleSemplice("TARI 2024");
            assertEquals("TARI 2024", causale.toString());
        }

        @Test
        @DisplayName("setter dovrebbe modificare la causale")
        void setterShouldModifyCausale() {
            CausaleSemplice causale = new CausaleSemplice();
            causale.setCausale("IMU 2024");
            assertEquals("IMU 2024", causale.getCausale());
        }

        @Test
        @DisplayName("encode con caratteri speciali dovrebbe funzionare")
        void encodeWithSpecialCharactersShouldWork() {
            CausaleSemplice causale = new CausaleSemplice("Pagamento TARI - Anno 2024 (rata 1/3)");
            String encoded = causale.encode();

            assertTrue(encoded.startsWith("01 "));
            String base64Part = encoded.substring(3);
            String decoded = new String(Base64.getDecoder().decode(base64Part), StandardCharsets.UTF_8);
            assertEquals("Pagamento TARI - Anno 2024 (rata 1/3)", decoded);
        }
    }

    @Nested
    @DisplayName("CausaleSpezzoni Tests")
    class CausaleSpezzoniTests {

        @Test
        @DisplayName("Costruttore vuoto dovrebbe creare lista vuota")
        void emptyConstructorShouldCreateEmptyList() {
            CausaleSpezzoni causale = new CausaleSpezzoni();
            assertNotNull(causale);
            assertNotNull(causale.getSpezzoni());
            assertTrue(causale.getSpezzoni().isEmpty());
        }

        @Test
        @DisplayName("encode dovrebbe restituire formato 02 con spezzoni base64")
        void encodeShouldReturnFormat02WithBase64Spezzoni() {
            CausaleSpezzoni causale = new CausaleSpezzoni();
            causale.setSpezzoni(Arrays.asList("Voce 1", "Voce 2"));

            String encoded = causale.encode();

            assertTrue(encoded.startsWith("02 "));
            String[] parts = encoded.split(" ");
            assertEquals(3, parts.length);
            assertEquals("Voce 1", new String(Base64.getDecoder().decode(parts[1]), StandardCharsets.UTF_8));
            assertEquals("Voce 2", new String(Base64.getDecoder().decode(parts[2]), StandardCharsets.UTF_8));
        }

        @Test
        @DisplayName("encode con spezzoni null dovrebbe restituire null")
        void encodeWithNullSpezzoniShouldReturnNull() {
            CausaleSpezzoni causale = new CausaleSpezzoni();
            causale.setSpezzoni(null);
            assertNull(causale.encode());
        }

        @Test
        @DisplayName("getSimple dovrebbe restituire primo spezzone")
        void getSimpleShouldReturnFirstSpezzone() {
            CausaleSpezzoni causale = new CausaleSpezzoni();
            causale.setSpezzoni(Arrays.asList("Voce 1", "Voce 2"));
            assertEquals("Voce 1", causale.getSimple());
        }

        @Test
        @DisplayName("getSimple con lista vuota dovrebbe restituire stringa vuota")
        void getSimpleWithEmptyListShouldReturnEmptyString() {
            CausaleSpezzoni causale = new CausaleSpezzoni();
            assertEquals("", causale.getSimple());
        }

        @Test
        @DisplayName("getSimple con lista null dovrebbe restituire stringa vuota")
        void getSimpleWithNullListShouldReturnEmptyString() {
            CausaleSpezzoni causale = new CausaleSpezzoni();
            causale.setSpezzoni(null);
            assertEquals("", causale.getSimple());
        }

        @Test
        @DisplayName("toString dovrebbe concatenare spezzoni con punto e virgola")
        void toStringShouldJoinSpezzoniWithSemicolon() {
            CausaleSpezzoni causale = new CausaleSpezzoni();
            causale.setSpezzoni(Arrays.asList("Voce 1", "Voce 2", "Voce 3"));
            assertEquals("Voce 1; Voce 2; Voce 3", causale.toString());
        }

        @Test
        @DisplayName("encode con singolo spezzone dovrebbe funzionare")
        void encodeWithSingleSpezzoneShouldWork() {
            CausaleSpezzoni causale = new CausaleSpezzoni();
            causale.setSpezzoni(List.of("Unica voce"));

            String encoded = causale.encode();
            String[] parts = encoded.split(" ");
            assertEquals(2, parts.length);
            assertEquals("02", parts[0]);
        }
    }

    @Nested
    @DisplayName("CausaleSpezzoniStrutturati Tests")
    class CausaleSpezzoniStrutturatiTests {

        @Test
        @DisplayName("Costruttore vuoto dovrebbe creare liste vuote")
        void emptyConstructorShouldCreateEmptyLists() {
            CausaleSpezzoniStrutturati causale = new CausaleSpezzoniStrutturati();
            assertNotNull(causale);
            assertNotNull(causale.getSpezzoni());
            assertNotNull(causale.getImporti());
            assertTrue(causale.getSpezzoni().isEmpty());
            assertTrue(causale.getImporti().isEmpty());
        }

        @Test
        @DisplayName("addSpezzoneStrutturato dovrebbe aggiungere spezzone e importo")
        void addSpezzoneStrutturatoShouldAddSpezzoneAndImporto() {
            CausaleSpezzoniStrutturati causale = new CausaleSpezzoniStrutturati();
            causale.addSpezzoneStrutturato("TARI", new BigDecimal("100.50"));
            causale.addSpezzoneStrutturato("IMU", new BigDecimal("200.00"));

            assertEquals(2, causale.getSpezzoni().size());
            assertEquals(2, causale.getImporti().size());
            assertEquals("TARI", causale.getSpezzoni().get(0));
            assertEquals(new BigDecimal("100.50"), causale.getImporti().get(0));
        }

        @Test
        @DisplayName("encode dovrebbe restituire formato 03 con spezzoni e importi base64")
        void encodeShouldReturnFormat03WithBase64SpezzoniAndImporti() {
            CausaleSpezzoniStrutturati causale = new CausaleSpezzoniStrutturati();
            causale.addSpezzoneStrutturato("TARI", new BigDecimal("100.50"));

            String encoded = causale.encode();

            assertTrue(encoded.startsWith("03 "));
            String[] parts = encoded.split(" ");
            assertEquals(3, parts.length);
            assertEquals("TARI", new String(Base64.getDecoder().decode(parts[1]), StandardCharsets.UTF_8));
            String importoDecoded = new String(Base64.getDecoder().decode(parts[2]), StandardCharsets.UTF_8);
            assertEquals("100.5", importoDecoded);
        }

        @Test
        @DisplayName("encode con spezzoni null dovrebbe restituire null")
        void encodeWithNullSpezzoniShouldReturnNull() {
            CausaleSpezzoniStrutturati causale = new CausaleSpezzoniStrutturati();
            causale.setSpezzoni(null);
            assertNull(causale.encode());
        }

        @Test
        @DisplayName("getSimple dovrebbe restituire primo importo e spezzone")
        void getSimpleShouldReturnFirstImportoAndSpezzone() {
            CausaleSpezzoniStrutturati causale = new CausaleSpezzoniStrutturati();
            causale.addSpezzoneStrutturato("TARI", new BigDecimal("100.50"));
            causale.addSpezzoneStrutturato("IMU", new BigDecimal("200.00"));

            assertEquals("100.5: TARI", causale.getSimple());
        }

        @Test
        @DisplayName("getSimple con lista vuota dovrebbe restituire stringa vuota")
        void getSimpleWithEmptyListShouldReturnEmptyString() {
            CausaleSpezzoniStrutturati causale = new CausaleSpezzoniStrutturati();
            assertEquals("", causale.getSimple());
        }

        @Test
        @DisplayName("getSimple con lista null dovrebbe restituire stringa vuota")
        void getSimpleWithNullListShouldReturnEmptyString() {
            CausaleSpezzoniStrutturati causale = new CausaleSpezzoniStrutturati();
            causale.setSpezzoni(null);
            assertEquals("", causale.getSimple());
        }

        @Test
        @DisplayName("toString dovrebbe concatenare importi e spezzoni")
        void toStringShouldJoinImportiAndSpezzoni() {
            CausaleSpezzoniStrutturati causale = new CausaleSpezzoniStrutturati();
            causale.addSpezzoneStrutturato("TARI", new BigDecimal("100.50"));
            causale.addSpezzoneStrutturato("IMU", new BigDecimal("200.00"));

            String result = causale.toString();
            assertTrue(result.contains("100.5: TARI"));
            assertTrue(result.contains("200.0: IMU"));
        }

        @Test
        @DisplayName("setters dovrebbero modificare i valori")
        void settersShouldModifyValues() {
            CausaleSpezzoniStrutturati causale = new CausaleSpezzoniStrutturati();
            causale.setSpezzoni(Arrays.asList("A", "B"));
            causale.setImporti(Arrays.asList(new BigDecimal("10"), new BigDecimal("20")));

            assertEquals(2, causale.getSpezzoni().size());
            assertEquals(2, causale.getImporti().size());
        }

        @Test
        @DisplayName("encode con multipli spezzoni strutturati dovrebbe funzionare")
        void encodeWithMultipleSpezzoniStrutturatiShouldWork() {
            CausaleSpezzoniStrutturati causale = new CausaleSpezzoniStrutturati();
            causale.addSpezzoneStrutturato("Voce 1", new BigDecimal("50.00"));
            causale.addSpezzoneStrutturato("Voce 2", new BigDecimal("75.25"));
            causale.addSpezzoneStrutturato("Voce 3", new BigDecimal("124.75"));

            String encoded = causale.encode();
            assertTrue(encoded.startsWith("03 "));
            // 03 + 3 spezzoni + 3 importi = 7 parti
            String[] parts = encoded.split(" ");
            assertEquals(7, parts.length);
        }
    }
}
