package it.govpay.portal.util;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class IuvUtilsTest {

    @Nested
    @DisplayName("buildQrCode002 Tests")
    class BuildQrCode002Tests {

        @Test
        @DisplayName("QR code con numeroAvviso dovrebbe usare direttamente il numeroAvviso")
        void qrCodeWithNumeroAvvisoShouldUseNumeroAvviso() {
            String qrCode = IuvUtils.buildQrCode002(
                    "12345678901",
                    3,
                    0,
                    "01234567890123456",
                    new BigDecimal("150.50"),
                    "301234567890123456"
            );

            assertEquals("PAGOPA|002|301234567890123456|12345678901|15050", qrCode);
        }

        @Test
        @DisplayName("QR code senza numeroAvviso e auxDigit 0 dovrebbe costruire con applicationCode")
        void qrCodeWithoutNumeroAvvisoAndAuxDigit0ShouldBuildWithApplicationCode() {
            String qrCode = IuvUtils.buildQrCode002(
                    "12345678901",
                    0,
                    1,
                    "234567890123456",
                    new BigDecimal("100.00"),
                    null
            );

            assertTrue(qrCode.startsWith("PAGOPA|002|0"));
            assertTrue(qrCode.contains("01234567890123456"));
            assertTrue(qrCode.endsWith("|10000"));
        }

        @Test
        @DisplayName("QR code senza numeroAvviso e auxDigit > 0 dovrebbe usare auxDigit")
        void qrCodeWithoutNumeroAvvisoAndAuxDigitGreaterThan0ShouldUseAuxDigit() {
            String qrCode = IuvUtils.buildQrCode002(
                    "12345678901",
                    3,
                    0,
                    "01234567890123456",
                    new BigDecimal("200.00"),
                    null
            );

            assertTrue(qrCode.startsWith("PAGOPA|002|3"));
            assertTrue(qrCode.contains("01234567890123456"));
        }

        @Test
        @DisplayName("Importo con decimali dovrebbe essere formattato correttamente")
        void importoWithDecimalsShouldBeFormattedCorrectly() {
            String qrCode = IuvUtils.buildQrCode002(
                    "12345678901",
                    3,
                    0,
                    "01234567890123456",
                    new BigDecimal("99.99"),
                    "301234567890123456"
            );

            assertTrue(qrCode.endsWith("|9999"));
        }

        @Test
        @DisplayName("Importo intero dovrebbe avere due decimali")
        void importoWithoutDecimalsShouldHaveTwoDecimals() {
            String qrCode = IuvUtils.buildQrCode002(
                    "12345678901",
                    3,
                    0,
                    "01234567890123456",
                    new BigDecimal("100"),
                    "301234567890123456"
            );

            assertTrue(qrCode.endsWith("|10000"));
        }
    }

    @Nested
    @DisplayName("buildBarCode Tests")
    class BuildBarCodeTests {

        @Test
        @DisplayName("Barcode con numeroAvviso dovrebbe usare direttamente il numeroAvviso")
        void barcodeWithNumeroAvvisoShouldUseNumeroAvviso() {
            String barcode = IuvUtils.buildBarCode(
                    "1234567890123",
                    3,
                    0,
                    "01234567890123456",
                    new BigDecimal("150.50"),
                    "301234567890123456"
            );

            assertTrue(barcode.startsWith("4151234567890123"));
            assertTrue(barcode.contains("8020301234567890123456"));
            assertTrue(barcode.endsWith("390215050"));
        }

        @Test
        @DisplayName("Barcode senza numeroAvviso e auxDigit 3 dovrebbe usare 3 come prefisso")
        void barcodeWithoutNumeroAvvisoAndAuxDigit3ShouldUse3AsPrefix() {
            String barcode = IuvUtils.buildBarCode(
                    "1234567890123",
                    3,
                    0,
                    "01234567890123456",
                    new BigDecimal("100.00"),
                    null
            );

            assertTrue(barcode.contains("8020301234567890123456"));
        }

        @Test
        @DisplayName("Barcode senza numeroAvviso e auxDigit != 3 dovrebbe usare 0 e applicationCode")
        void barcodeWithoutNumeroAvvisoAndAuxDigitNot3ShouldUse0AndApplicationCode() {
            String barcode = IuvUtils.buildBarCode(
                    "1234567890123",
                    0,
                    1,
                    "234567890123456",
                    new BigDecimal("100.00"),
                    null
            );

            assertTrue(barcode.contains("8020001234567890123456"));
        }
    }

    @Nested
    @DisplayName("toNumeroAvviso Tests")
    class ToNumeroAvvisoTests {

        @Test
        @DisplayName("auxDigit 0 dovrebbe aggiungere 0 + applicationCode(2) + iuv")
        void auxDigit0ShouldAdd0AndApplicationCodeAndIuv() {
            String result = IuvUtils.toNumeroAvviso("234567890123456", 0, 1);
            assertEquals("001234567890123456", result);
        }

        @Test
        @DisplayName("auxDigit > 0 dovrebbe aggiungere auxDigit + iuv")
        void auxDigitGreaterThan0ShouldAddAuxDigitAndIuv() {
            String result = IuvUtils.toNumeroAvviso("01234567890123456", 3, 0);
            assertEquals("301234567890123456", result);
        }

        @Test
        @DisplayName("applicationCode dovrebbe essere formattato con 2 cifre")
        void applicationCodeShouldBeFormattedWith2Digits() {
            String result = IuvUtils.toNumeroAvviso("234567890123456", 0, 99);
            assertEquals("099234567890123456", result);
        }
    }

    @Nested
    @DisplayName("toIuv Tests")
    class ToIuvTests {

        @Test
        @DisplayName("numeroAvviso null dovrebbe restituire null")
        void nullNumeroAvvisoShouldReturnNull() {
            assertNull(IuvUtils.toIuv(null));
        }

        @Test
        @DisplayName("numeroAvviso con lunghezza diversa da 18 dovrebbe lanciare eccezione")
        void numeroAvvisoWithWrongLengthShouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> IuvUtils.toIuv("12345")
            );
            assertTrue(exception.getMessage().contains("18 cifre"));
        }

        @Test
        @DisplayName("numeroAvviso non numerico dovrebbe lanciare eccezione")
        void nonNumericNumeroAvvisoShouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> IuvUtils.toIuv("12345678901234567A")
            );
            assertTrue(exception.getMessage().contains("formato numerico"));
        }

        @Test
        @DisplayName("numeroAvviso che inizia con 0 dovrebbe estrarre IUV dalla posizione 3")
        void numeroAvvisoStartingWith0ShouldExtractIuvFromPosition3() {
            String iuv = IuvUtils.toIuv("001234567890123456");
            assertEquals("234567890123456", iuv);
        }

        @Test
        @DisplayName("numeroAvviso che inizia con 1 dovrebbe estrarre IUV dalla posizione 1")
        void numeroAvvisoStartingWith1ShouldExtractIuvFromPosition1() {
            String iuv = IuvUtils.toIuv("112345678901234567");
            assertEquals("12345678901234567", iuv);
        }

        @Test
        @DisplayName("numeroAvviso che inizia con 2 dovrebbe estrarre IUV dalla posizione 1")
        void numeroAvvisoStartingWith2ShouldExtractIuvFromPosition1() {
            String iuv = IuvUtils.toIuv("212345678901234567");
            assertEquals("12345678901234567", iuv);
        }

        @Test
        @DisplayName("numeroAvviso che inizia con 3 dovrebbe estrarre IUV dalla posizione 1")
        void numeroAvvisoStartingWith3ShouldExtractIuvFromPosition1() {
            String iuv = IuvUtils.toIuv("312345678901234567");
            assertEquals("12345678901234567", iuv);
        }

        @Test
        @DisplayName("numeroAvviso che inizia con cifra non valida dovrebbe lanciare eccezione")
        void numeroAvvisoStartingWithInvalidDigitShouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> IuvUtils.toIuv("412345678901234567")
            );
            assertTrue(exception.getMessage().contains("[0|1|2|3]"));
        }
    }

    @Nested
    @DisplayName("checkIuvNumerico Tests")
    class CheckIuvNumericoTests {

        @Test
        @DisplayName("IUV valido con auxDigit 0 e lunghezza 15 dovrebbe restituire true")
        void validIuvWithAuxDigit0And15LengthShouldReturnTrue() {
            // Calcolo un IUV valido
            // Reference = 0000000000001, auxDigit = 0, applicationCode = 1
            // numeroAvviso = 001 + 0000000000001 = 0010000000000001
            // resto93 = 0010000000000001 % 93 = qualcosa...
            // Per semplicita', testiamo che la logica funzioni con un valore noto

            // 001 + 0000000000001 = 0010000000000001
            long value = 10000000000001L;
            long resto = value % 93;
            String reference = "0000000000001";
            String iuv = reference + String.format("%02d", resto);

            assertTrue(IuvUtils.checkIuvNumerico(iuv, 0, 1));
        }

        @Test
        @DisplayName("IUV non valido con auxDigit 0 dovrebbe restituire false")
        void invalidIuvWithAuxDigit0ShouldReturnFalse() {
            assertFalse(IuvUtils.checkIuvNumerico("000000000000199", 0, 1));
        }

        @Test
        @DisplayName("IUV con lunghezza sbagliata per auxDigit 0 dovrebbe restituire false")
        void iuvWithWrongLengthForAuxDigit0ShouldReturnFalse() {
            assertFalse(IuvUtils.checkIuvNumerico("00000000000001", 0, 1)); // 14 cifre
        }

        @Test
        @DisplayName("IUV valido con auxDigit 3 e lunghezza 17 dovrebbe restituire true")
        void validIuvWithAuxDigit3And17LengthShouldReturnTrue() {
            // Reference = 000000000000001, auxDigit = 3
            // numeroAvviso = 3 + 000000000000001 = 3000000000000001
            // resto93 = 3000000000000001 % 93
            long value = 3000000000000001L;
            long resto = value % 93;
            String reference = "000000000000001";
            String iuv = reference + String.format("%02d", resto);

            assertTrue(IuvUtils.checkIuvNumerico(iuv, 3, 0));
        }

        @Test
        @DisplayName("IUV con lunghezza sbagliata per auxDigit 3 dovrebbe restituire false")
        void iuvWithWrongLengthForAuxDigit3ShouldReturnFalse() {
            assertFalse(IuvUtils.checkIuvNumerico("0000000000000001", 3, 0)); // 16 cifre
        }

        @Test
        @DisplayName("IUV con auxDigit diverso da 0 e 3 dovrebbe restituire false")
        void iuvWithAuxDigitNot0Or3ShouldReturnFalse() {
            assertFalse(IuvUtils.checkIuvNumerico("01234567890123456", 1, 0));
            assertFalse(IuvUtils.checkIuvNumerico("01234567890123456", 2, 0));
        }
    }
}
