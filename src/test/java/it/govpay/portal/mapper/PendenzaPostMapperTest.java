package it.govpay.portal.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import it.govpay.pendenze.client.model.NuovaPendenza;
import it.govpay.pendenze.client.model.NuovaVocePendenza;
import it.govpay.portal.beans.pendenza.Contabilita;
import it.govpay.portal.beans.pendenza.Documento;
import it.govpay.portal.beans.pendenza.NuovoAllegatoPendenza;
import it.govpay.portal.beans.pendenza.PendenzaPost;
import it.govpay.portal.beans.pendenza.QuotaContabilita;
import it.govpay.portal.beans.pendenza.TipoContabilita;
import it.govpay.portal.beans.pendenza.VocePendenza;

/**
 * Test per PendenzaPostMapper.
 */
class PendenzaPostMapperTest {

    private PendenzaPostMapper mapper;
    private PendenzaPost pendenza;

    @BeforeEach
    void setUp() {
        mapper = new PendenzaPostMapper();
        pendenza = createBasicPendenza();
    }

    private PendenzaPost createBasicPendenza() {
        PendenzaPost p = new PendenzaPost();
        p.setIdDominio("01234567890");
        p.setIdTipoPendenza("TARI");
        p.setCausale("Pagamento test");
        p.setImporto(new BigDecimal("100.50"));

        VocePendenza voce = new VocePendenza();
        voce.setIdVocePendenza("VOCE001");
        voce.setImporto(new BigDecimal("100.50"));
        voce.setDescrizione("Voce test");
        voce.setCodEntrata("ENTRATA_TEST");
        p.setVoci(Arrays.asList(voce));

        return p;
    }

    @Test
    @DisplayName("Mappa pendenza base correttamente")
    void testMapBasicPendenza() {
        NuovaPendenza result = mapper.toNuovaPendenza(pendenza);

        assertNotNull(result);
        assertEquals("01234567890", result.getIdDominio());
        assertEquals("TARI", result.getIdTipoPendenza());
        assertEquals("Pagamento test", result.getCausale());
        assertEquals(100.50, result.getImporto());
    }

    @Test
    @DisplayName("Mappa pendenza null restituisce null")
    void testMapNullPendenza() {
        NuovaPendenza result = mapper.toNuovaPendenza(null);
        assertNull(result);
    }

    @Nested
    @DisplayName("Mapping soggetto pagatore")
    class SoggettoPagatoreMapping {

        @Test
        @DisplayName("Mappa soggetto pagatore completo")
        void testMapFullSoggetto() {
            it.govpay.portal.beans.pendenza.Soggetto soggetto = new it.govpay.portal.beans.pendenza.Soggetto();
            soggetto.setTipo(it.govpay.portal.beans.pendenza.Soggetto.TipoEnum.F);
            soggetto.setIdentificativo("RSSMRA80A01H501U");
            soggetto.setAnagrafica("Mario Rossi");
            soggetto.setIndirizzo("Via Roma 1");
            soggetto.setCivico("10");
            soggetto.setCap("00100");
            soggetto.setLocalita("Roma");
            soggetto.setProvincia("RM");
            soggetto.setNazione("IT");
            soggetto.setEmail("mario@test.it");
            soggetto.setCellulare("+39123456789");
            pendenza.setSoggettoPagatore(soggetto);

            NuovaPendenza result = mapper.toNuovaPendenza(pendenza);

            assertNotNull(result.getSoggettoPagatore());
            assertEquals("RSSMRA80A01H501U", result.getSoggettoPagatore().getIdentificativo());
            assertEquals("Mario Rossi", result.getSoggettoPagatore().getAnagrafica());
            assertEquals("Via Roma 1", result.getSoggettoPagatore().getIndirizzo());
            assertEquals("10", result.getSoggettoPagatore().getCivico());
            assertEquals("00100", result.getSoggettoPagatore().getCap());
            assertEquals("Roma", result.getSoggettoPagatore().getLocalita());
            assertEquals("RM", result.getSoggettoPagatore().getProvincia());
            assertEquals("IT", result.getSoggettoPagatore().getNazione());
            assertEquals("mario@test.it", result.getSoggettoPagatore().getEmail());
            assertEquals("+39123456789", result.getSoggettoPagatore().getCellulare());
        }
    }

    @Nested
    @DisplayName("Mapping voci pendenza")
    class VociMapping {

        @Test
        @DisplayName("Mappa voce con codEntrata")
        void testMapVoceConCodEntrata() {
            NuovaPendenza result = mapper.toNuovaPendenza(pendenza);

            assertNotNull(result.getVoci());
            assertEquals(1, result.getVoci().size());

            NuovaVocePendenza voce = result.getVoci().get(0);
            assertEquals("VOCE001", voce.getIdVocePendenza());
            assertEquals(100.50, voce.getImporto());
            assertEquals("Voce test", voce.getDescrizione());
            assertEquals("ENTRATA_TEST", voce.getCodEntrata());
        }

        @Test
        @DisplayName("Mappa voce con tipoBollo")
        void testMapVoceConTipoBollo() {
            VocePendenza voce = pendenza.getVoci().get(0);
            voce.setCodEntrata(null);
            voce.setTipoBollo("01");
            voce.setHashDocumento("hash123");
            voce.setProvinciaResidenza("RM");

            NuovaPendenza result = mapper.toNuovaPendenza(pendenza);
            NuovaVocePendenza risultato = result.getVoci().get(0);

            assertEquals(NuovaVocePendenza.TipoBolloEnum._01, risultato.getTipoBollo());
            assertEquals("hash123", risultato.getHashDocumento());
            assertEquals("RM", risultato.getProvinciaResidenza());
        }

        @Test
        @DisplayName("Mappa voce con ibanAccredito")
        void testMapVoceConIbanAccredito() {
            VocePendenza voce = pendenza.getVoci().get(0);
            voce.setCodEntrata(null);
            voce.setIbanAccredito("IT60X0542811101000000123456");
            voce.setIbanAppoggio("IT60X0542811101000000123457");
            voce.setTipoContabilita(TipoContabilita.CAPITOLO);
            voce.setCodiceContabilita("CAP001");

            NuovaPendenza result = mapper.toNuovaPendenza(pendenza);
            NuovaVocePendenza risultato = result.getVoci().get(0);

            assertEquals("IT60X0542811101000000123456", risultato.getIbanAccredito());
            assertEquals("IT60X0542811101000000123457", risultato.getIbanAppoggio());
            assertEquals("CAP001", risultato.getCodiceContabilita());
        }

        @Test
        @DisplayName("Mappa voce con contabilita")
        void testMapVoceConContabilita() {
            VocePendenza voce = pendenza.getVoci().get(0);

            Contabilita contabilita = new Contabilita();
            QuotaContabilita quota = new QuotaContabilita();
            quota.setCapitolo("CAP001");
            quota.setAnnoEsercizio(new BigDecimal("2024"));
            quota.setImporto(new BigDecimal("100.50"));
            contabilita.setQuote(Arrays.asList(quota));
            contabilita.setProprietaCustom("custom");
            voce.setContabilita(contabilita);

            NuovaPendenza result = mapper.toNuovaPendenza(pendenza);
            NuovaVocePendenza risultato = result.getVoci().get(0);

            assertNotNull(risultato.getContabilita());
        }
    }

    @Nested
    @DisplayName("Mapping documento")
    class DocumentoMapping {

        @Test
        @DisplayName("Mappa documento completo")
        void testMapDocumento() {
            Documento doc = new Documento();
            doc.setIdentificativo("DOC001");
            doc.setDescrizione("Documento test");
            doc.setRata(new BigDecimal("3"));
            pendenza.setDocumento(doc);

            NuovaPendenza result = mapper.toNuovaPendenza(pendenza);

            assertNotNull(result.getDocumento());
            assertEquals("DOC001", result.getDocumento().getIdentificativo());
            assertEquals("Documento test", result.getDocumento().getDescrizione());
        }
    }

    @Nested
    @DisplayName("Mapping allegati")
    class AllegatiMapping {

        @Test
        @DisplayName("Mappa allegato completo")
        void testMapAllegato() {
            NuovoAllegatoPendenza allegato = new NuovoAllegatoPendenza();
            allegato.setNome("file.pdf");
            allegato.setTipo("application/pdf");
            allegato.setDescrizione("Allegato test");
            allegato.setContenuto(new byte[]{1, 2, 3});
            pendenza.setAllegati(Arrays.asList(allegato));

            NuovaPendenza result = mapper.toNuovaPendenza(pendenza);

            assertNotNull(result.getAllegati());
            assertEquals(1, result.getAllegati().size());
            assertEquals("file.pdf", result.getAllegati().get(0).getNome());
        }
    }

    @Nested
    @DisplayName("Mapping altri campi")
    class AltriCampiMapping {

        @Test
        @DisplayName("Mappa campi opzionali")
        void testMapCampiOpzionali() {
            pendenza.setIdUnitaOperativa("UO001");
            pendenza.setNome("Nome pendenza");
            pendenza.setNumeroAvviso("301000000000000001");
            pendenza.setTassonomia("TASS001");
            pendenza.setAnnoRiferimento(new BigDecimal("2024"));
            pendenza.setCartellaPagamento("CART001");
            pendenza.setDirezione("DIR001");
            pendenza.setDivisione("DIV001");

            NuovaPendenza result = mapper.toNuovaPendenza(pendenza);

            assertEquals("UO001", result.getIdUnitaOperativa());
            assertEquals("301000000000000001", result.getNumeroAvviso());
            assertEquals("TASS001", result.getTassonomia());
            assertEquals("CART001", result.getCartellaPagamento());
            assertEquals("DIR001", result.getDirezione());
            assertEquals("DIV001", result.getDivisione());
        }
    }
}
