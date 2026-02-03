package it.govpay.portal.beans.pendenza;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test per i bean di pendenza (POJO).
 */
class PendenzaBeansTest {

    @Nested
    @DisplayName("Test Soggetto")
    class SoggettoTest {
        @Test
        void testGettersAndSetters() {
            Soggetto soggetto = new Soggetto();

            soggetto.setTipo(Soggetto.TipoEnum.F);
            assertEquals(Soggetto.TipoEnum.F, soggetto.getTipo());

            soggetto.setIdentificativo("RSSMRA80A01H501U");
            assertEquals("RSSMRA80A01H501U", soggetto.getIdentificativo());

            soggetto.setAnagrafica("Mario Rossi");
            assertEquals("Mario Rossi", soggetto.getAnagrafica());

            soggetto.setIndirizzo("Via Roma 1");
            assertEquals("Via Roma 1", soggetto.getIndirizzo());

            soggetto.setCivico("10");
            assertEquals("10", soggetto.getCivico());

            soggetto.setCap("00100");
            assertEquals("00100", soggetto.getCap());

            soggetto.setLocalita("Roma");
            assertEquals("Roma", soggetto.getLocalita());

            soggetto.setProvincia("RM");
            assertEquals("RM", soggetto.getProvincia());

            soggetto.setNazione("IT");
            assertEquals("IT", soggetto.getNazione());

            soggetto.setEmail("mario.rossi@email.it");
            assertEquals("mario.rossi@email.it", soggetto.getEmail());

            soggetto.setCellulare("+39123456789");
            assertEquals("+39123456789", soggetto.getCellulare());
        }

        @Test
        void testTipoEnumFromValue() {
            assertEquals(Soggetto.TipoEnum.F, Soggetto.TipoEnum.fromValue("F"));
            assertEquals(Soggetto.TipoEnum.G, Soggetto.TipoEnum.fromValue("G"));
        }

        @Test
        void testTipoEnumInvalid() {
            // fromValue restituisce null per valori non validi
            assertNull(Soggetto.TipoEnum.fromValue("X"));
        }
    }

    @Nested
    @DisplayName("Test VocePendenza")
    class VocePendenzaTest {
        @Test
        void testGettersAndSetters() {
            VocePendenza voce = new VocePendenza();

            voce.setIdVocePendenza("VOCE001");
            assertEquals("VOCE001", voce.getIdVocePendenza());

            voce.setImporto(new BigDecimal("100.50"));
            assertEquals(new BigDecimal("100.50"), voce.getImporto());

            voce.setDescrizione("Descrizione voce");
            assertEquals("Descrizione voce", voce.getDescrizione());

            voce.setCodEntrata("ENTRATA001");
            assertEquals("ENTRATA001", voce.getCodEntrata());

            voce.setTipoBollo("01");
            assertEquals("01", voce.getTipoBollo());

            voce.setHashDocumento("hash123");
            assertEquals("hash123", voce.getHashDocumento());

            voce.setProvinciaResidenza("RM");
            assertEquals("RM", voce.getProvinciaResidenza());

            voce.setIbanAccredito("IT60X0542811101000000123456");
            assertEquals("IT60X0542811101000000123456", voce.getIbanAccredito());

            voce.setIbanAppoggio("IT60X0542811101000000123457");
            assertEquals("IT60X0542811101000000123457", voce.getIbanAppoggio());

            voce.setTipoContabilita(TipoContabilita.CAPITOLO);
            assertEquals(TipoContabilita.CAPITOLO, voce.getTipoContabilita());

            voce.setCodiceContabilita("COD001");
            assertEquals("COD001", voce.getCodiceContabilita());

            voce.setDescrizioneCausaleRPT("Causale RPT");
            assertEquals("Causale RPT", voce.getDescrizioneCausaleRPT());

            voce.setIdDominio("01234567890");
            assertEquals("01234567890", voce.getIdDominio());
        }
    }

    @Nested
    @DisplayName("Test PendenzaPost")
    class PendenzaPostTest {
        @Test
        void testGettersAndSetters() {
            PendenzaPost pendenza = new PendenzaPost();

            pendenza.setIdA2A("APP001");
            assertEquals("APP001", pendenza.getIdA2A());

            pendenza.setIdPendenza("PEND001");
            assertEquals("PEND001", pendenza.getIdPendenza());

            pendenza.setIdDominio("01234567890");
            assertEquals("01234567890", pendenza.getIdDominio());

            pendenza.setIdUnitaOperativa("UO001");
            assertEquals("UO001", pendenza.getIdUnitaOperativa());

            pendenza.setIdTipoPendenza("TIPO001");
            assertEquals("TIPO001", pendenza.getIdTipoPendenza());

            pendenza.setNome("Nome pendenza");
            assertEquals("Nome pendenza", pendenza.getNome());

            pendenza.setCausale("Causale pendenza");
            assertEquals("Causale pendenza", pendenza.getCausale());

            pendenza.setImporto(new BigDecimal("150.50"));
            assertEquals(new BigDecimal("150.50"), pendenza.getImporto());

            pendenza.setNumeroAvviso("301000000000000001");
            assertEquals("301000000000000001", pendenza.getNumeroAvviso());

            pendenza.setAnnoRiferimento(new BigDecimal("2024"));
            assertEquals(new BigDecimal("2024"), pendenza.getAnnoRiferimento());

            pendenza.setCartellaPagamento("CART001");
            assertEquals("CART001", pendenza.getCartellaPagamento());

            pendenza.setDirezione("DIR001");
            assertEquals("DIR001", pendenza.getDirezione());

            pendenza.setDivisione("DIV001");
            assertEquals("DIV001", pendenza.getDivisione());

            pendenza.setTassonomia("TASS001");
            assertEquals("TASS001", pendenza.getTassonomia());

            pendenza.setSoggettoPagatore(new Soggetto());
            assertNotNull(pendenza.getSoggettoPagatore());

            pendenza.setDocumento(new Documento());
            assertNotNull(pendenza.getDocumento());

            pendenza.setVoci(Arrays.asList(new VocePendenza()));
            assertNotNull(pendenza.getVoci());
            assertEquals(1, pendenza.getVoci().size());

            pendenza.setAllegati(Arrays.asList(new NuovoAllegatoPendenza()));
            assertNotNull(pendenza.getAllegati());
        }
    }

    @Nested
    @DisplayName("Test Documento")
    class DocumentoTest {
        @Test
        void testGettersAndSetters() {
            Documento documento = new Documento();

            documento.setIdentificativo("DOC001");
            assertEquals("DOC001", documento.getIdentificativo());

            documento.setDescrizione("Descrizione documento");
            assertEquals("Descrizione documento", documento.getDescrizione());

            documento.setRata(BigDecimal.valueOf(3));
            assertEquals(BigDecimal.valueOf(3), documento.getRata());
        }
    }

    @Nested
    @DisplayName("Test NuovoAllegatoPendenza")
    class NuovoAllegatoPendenzaTest {
        @Test
        void testGettersAndSetters() {
            NuovoAllegatoPendenza allegato = new NuovoAllegatoPendenza();

            allegato.setNome("documento.pdf");
            assertEquals("documento.pdf", allegato.getNome());

            allegato.setTipo("application/pdf");
            assertEquals("application/pdf", allegato.getTipo());

            allegato.setDescrizione("Allegato di prova");
            assertEquals("Allegato di prova", allegato.getDescrizione());

            byte[] contenuto = new byte[]{1, 2, 3};
            allegato.setContenuto(contenuto);
            assertArrayEquals(contenuto, allegato.getContenuto());
        }
    }

    @Nested
    @DisplayName("Test TipoContabilita")
    class TipoContabilitaTest {
        @Test
        void testEnumValues() {
            assertNotNull(TipoContabilita.values());
            assertTrue(TipoContabilita.values().length > 0);
        }

        @Test
        void testFromValue() {
            assertEquals(TipoContabilita.CAPITOLO, TipoContabilita.fromValue("CAPITOLO"));
            assertEquals(TipoContabilita.SIOPE, TipoContabilita.fromValue("SIOPE"));
        }

        @Test
        void testFromValueInvalid() {
            // fromValue restituisce null per valori non validi
            assertNull(TipoContabilita.fromValue("INVALID"));
        }
    }

    @Nested
    @DisplayName("Test Contabilita")
    class ContabilitaTest {
        @Test
        void testGettersAndSetters() {
            Contabilita contabilita = new Contabilita();

            contabilita.setQuote(Arrays.asList(new QuotaContabilita()));
            assertNotNull(contabilita.getQuote());
            assertEquals(1, contabilita.getQuote().size());

            contabilita.setProprietaCustom("custom");
            assertEquals("custom", contabilita.getProprietaCustom());
        }
    }

    @Nested
    @DisplayName("Test QuotaContabilita")
    class QuotaContabilitaTest {
        @Test
        void testGettersAndSetters() {
            QuotaContabilita quota = new QuotaContabilita();

            quota.setCapitolo("CAP001");
            assertEquals("CAP001", quota.getCapitolo());

            quota.setAnnoEsercizio(new BigDecimal("2024"));
            assertEquals(new BigDecimal("2024"), quota.getAnnoEsercizio());

            quota.setImporto(new BigDecimal("150.50"));
            assertEquals(new BigDecimal("150.50"), quota.getImporto());

            quota.setAccertamento("ACC001");
            assertEquals("ACC001", quota.getAccertamento());

            quota.setProprietaCustom("custom");
            assertEquals("custom", quota.getProprietaCustom());
        }
    }
}
