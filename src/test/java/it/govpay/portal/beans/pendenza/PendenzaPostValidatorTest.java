package it.govpay.portal.beans.pendenza;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import it.govpay.portal.exception.ValidationException;

/**
 * Test per PendenzaPostValidator.
 */
class PendenzaPostValidatorTest {

    private PendenzaPost pendenza;

    @BeforeEach
    void setUp() {
        pendenza = createValidPendenza();
    }

    private PendenzaPost createValidPendenza() {
        PendenzaPost p = new PendenzaPost();
        p.setIdA2A("APP_TEST");
        p.setIdPendenza("PEND_001");
        p.setIdDominio("01234567890");
        p.setIdTipoPendenza("TIPO_TEST");
        p.setCausale("Causale di test");
        p.setImporto(new BigDecimal("100.50"));

        VocePendenza voce = new VocePendenza();
        voce.setIdVocePendenza("VOCE001");
        voce.setImporto(new BigDecimal("100.50"));
        voce.setDescrizione("Descrizione voce");
        voce.setCodEntrata("ENTRATA_TEST");
        p.setVoci(Arrays.asList(voce));

        return p;
    }

    @Test
    @DisplayName("Pendenza valida non lancia eccezione")
    void testValidPendenza() {
        assertDoesNotThrow(() -> new PendenzaPostValidator(pendenza).validate());
    }

    @Test
    @DisplayName("Pendenza null lancia ValidationException")
    void testNullPendenza() {
        ValidationException ex = assertThrows(ValidationException.class,
            () -> new PendenzaPostValidator(null).validate());
        assertTrue(ex.getMessage().contains("non valorizzata"));
    }

    @Nested
    @DisplayName("Validazione idA2A")
    class IdA2AValidation {
        @Test
        void testIdA2AMancante() {
            pendenza.setIdA2A(null);
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("idA2A"));
        }

        @Test
        void testIdA2AVuoto() {
            pendenza.setIdA2A("");
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("idA2A"));
        }

        @Test
        void testIdA2APatternNonValido() {
            pendenza.setIdA2A("app@invalid!");
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("idA2A") && ex.getMessage().contains("pattern"));
        }
    }

    @Nested
    @DisplayName("Validazione idPendenza")
    class IdPendenzaValidation {
        @Test
        void testIdPendenzaMancante() {
            pendenza.setIdPendenza(null);
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("idPendenza"));
        }

        @Test
        void testIdPendenzaPatternNonValido() {
            pendenza.setIdPendenza("pend@invalid!");
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("idPendenza") && ex.getMessage().contains("pattern"));
        }
    }

    @Nested
    @DisplayName("Validazione idDominio")
    class IdDominioValidation {
        @Test
        void testIdDominioMancante() {
            pendenza.setIdDominio(null);
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("idDominio"));
        }

        @Test
        void testIdDominioPatternNonValido() {
            pendenza.setIdDominio("123"); // deve essere 11 cifre
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("idDominio") && ex.getMessage().contains("pattern"));
        }
    }

    @Nested
    @DisplayName("Validazione idUnitaOperativa")
    class IdUnitaOperativaValidation {
        @Test
        void testIdUnitaOperativaPatternNonValido() {
            pendenza.setIdUnitaOperativa("uo@invalid!");
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("idUnitaOperativa") && ex.getMessage().contains("pattern"));
        }

        @Test
        void testIdUnitaOperativaValido() {
            pendenza.setIdUnitaOperativa("UO_001");
            assertDoesNotThrow(() -> new PendenzaPostValidator(pendenza).validate());
        }
    }

    @Nested
    @DisplayName("Validazione idTipoPendenza")
    class IdTipoPendenzaValidation {
        @Test
        void testIdTipoPendenzaPatternNonValido() {
            pendenza.setIdTipoPendenza("tipo@invalid!");
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("idTipoPendenza") && ex.getMessage().contains("pattern"));
        }
    }

    @Nested
    @DisplayName("Validazione nome")
    class NomeValidation {
        @Test
        void testNomeTroppoLungo() {
            pendenza.setNome("A".repeat(36)); // max 35
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("nome") && ex.getMessage().contains("35"));
        }

        @Test
        void testNomeValido() {
            pendenza.setNome("Nome valido");
            assertDoesNotThrow(() -> new PendenzaPostValidator(pendenza).validate());
        }
    }

    @Nested
    @DisplayName("Validazione causale")
    class CausaleValidation {
        @Test
        void testCausaleMancante() {
            pendenza.setCausale(null);
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("causale"));
        }

        @Test
        void testCausaleTroppoLunga() {
            pendenza.setCausale("A".repeat(141)); // max 140
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("causale") && ex.getMessage().contains("140"));
        }
    }

    @Nested
    @DisplayName("Validazione soggettoPagatore")
    class SoggettoPagatoreValidation {
        @Test
        void testSoggettoPagatoreNull() {
            pendenza.setSoggettoPagatore(null);
            assertDoesNotThrow(() -> new PendenzaPostValidator(pendenza).validate());
        }

        @Test
        void testIdentificativoTroppoLungo() {
            Soggetto soggetto = new Soggetto();
            soggetto.setIdentificativo("A".repeat(17)); // max 16
            pendenza.setSoggettoPagatore(soggetto);
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("identificativo") && ex.getMessage().contains("16"));
        }

        @Test
        void testAnagraficaTroppoLunga() {
            Soggetto soggetto = new Soggetto();
            soggetto.setAnagrafica("A".repeat(71)); // max 70
            pendenza.setSoggettoPagatore(soggetto);
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("anagrafica") && ex.getMessage().contains("70"));
        }

        @Test
        void testIndirizzoTroppoLungo() {
            Soggetto soggetto = new Soggetto();
            soggetto.setIndirizzo("A".repeat(71)); // max 70
            pendenza.setSoggettoPagatore(soggetto);
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("indirizzo"));
        }

        @Test
        void testCivicoTroppoLungo() {
            Soggetto soggetto = new Soggetto();
            soggetto.setCivico("A".repeat(17)); // max 16
            pendenza.setSoggettoPagatore(soggetto);
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("civico"));
        }

        @Test
        void testCapTroppoLungo() {
            Soggetto soggetto = new Soggetto();
            soggetto.setCap("A".repeat(17)); // max 16
            pendenza.setSoggettoPagatore(soggetto);
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("cap"));
        }

        @Test
        void testLocalitaTroppoLunga() {
            Soggetto soggetto = new Soggetto();
            soggetto.setLocalita("A".repeat(36)); // max 35
            pendenza.setSoggettoPagatore(soggetto);
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("localita"));
        }

        @Test
        void testProvinciaTroppoLunga() {
            Soggetto soggetto = new Soggetto();
            soggetto.setProvincia("A".repeat(36)); // max 35
            pendenza.setSoggettoPagatore(soggetto);
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("provincia"));
        }

        @Test
        void testNazionePatternNonValido() {
            Soggetto soggetto = new Soggetto();
            soggetto.setNazione("ITA"); // deve essere 2 lettere maiuscole
            pendenza.setSoggettoPagatore(soggetto);
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("nazione") && ex.getMessage().contains("pattern"));
        }

        @Test
        void testEmailTroppoLunga() {
            Soggetto soggetto = new Soggetto();
            soggetto.setEmail("a".repeat(250) + "@test.it"); // max 256
            pendenza.setSoggettoPagatore(soggetto);
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("email"));
        }

        @Test
        void testEmailPatternNonValido() {
            Soggetto soggetto = new Soggetto();
            soggetto.setEmail("invalid-email");
            pendenza.setSoggettoPagatore(soggetto);
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("email") && ex.getMessage().contains("pattern"));
        }

        @Test
        void testCellulareTroppoLungo() {
            Soggetto soggetto = new Soggetto();
            soggetto.setCellulare("1".repeat(36)); // max 35
            pendenza.setSoggettoPagatore(soggetto);
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("cellulare"));
        }
    }

    @Nested
    @DisplayName("Validazione importo")
    class ImportoValidation {
        @Test
        void testImportoNegativo() {
            pendenza.setImporto(new BigDecimal("-1.00"));
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("importo") && ex.getMessage().contains("0"));
        }

        @Test
        void testImportoTroppoGrande() {
            pendenza.setImporto(new BigDecimal("9999999999.99"));
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("importo"));
        }

        @Test
        void testImportoTroppeCifreDecimali() {
            pendenza.setImporto(new BigDecimal("100.123"));
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("importo") && ex.getMessage().contains("decimali"));
        }
    }

    @Nested
    @DisplayName("Validazione numeroAvviso")
    class NumeroAvvisoValidation {
        @Test
        void testNumeroAvvisoTroppoLungo() {
            pendenza.setNumeroAvviso("3010000000000000019"); // 19 cifre, max 18
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("numeroAvviso"));
        }

        @Test
        void testNumeroAvvisoPatternNonValido() {
            pendenza.setNumeroAvviso("30100000000000000A"); // deve essere solo cifre
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("numeroAvviso") && ex.getMessage().contains("pattern"));
        }

        @Test
        void testNumeroAvvisoValido() {
            pendenza.setNumeroAvviso("301000000000000001");
            assertDoesNotThrow(() -> new PendenzaPostValidator(pendenza).validate());
        }
    }

    @Nested
    @DisplayName("Validazione annoRiferimento")
    class AnnoRiferimentoValidation {
        @Test
        void testAnnoRiferimentoPatternNonValido() {
            pendenza.setAnnoRiferimento(new BigDecimal("24")); // deve essere 4 cifre
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("annoRiferimento") && ex.getMessage().contains("pattern"));
        }

        @Test
        void testAnnoRiferimentoValido() {
            pendenza.setAnnoRiferimento(new BigDecimal("2024"));
            assertDoesNotThrow(() -> new PendenzaPostValidator(pendenza).validate());
        }
    }

    @Nested
    @DisplayName("Validazione cartellaPagamento")
    class CartellaPagamentoValidation {
        @Test
        void testCartellaPagamentoTroppoLunga() {
            pendenza.setCartellaPagamento("A".repeat(36)); // max 35
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("cartellaPagamento"));
        }
    }

    @Nested
    @DisplayName("Validazione direzione e divisione")
    class DirezioneDivisioneValidation {
        @Test
        void testDirezioneTroppoLunga() {
            pendenza.setDirezione("A".repeat(36)); // max 35
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("direzione"));
        }

        @Test
        void testDivisioneTroppoLunga() {
            pendenza.setDivisione("A".repeat(36)); // max 35
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("divisione"));
        }
    }

    @Nested
    @DisplayName("Validazione documento")
    class DocumentoValidation {
        @Test
        void testDocumentoIdentificativoTroppoLungo() {
            Documento doc = new Documento();
            doc.setIdentificativo("A".repeat(36)); // max 35
            pendenza.setDocumento(doc);
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("documento.identificativo"));
        }

        @Test
        void testDocumentoDescrizioneTroppoLunga() {
            Documento doc = new Documento();
            doc.setDescrizione("A".repeat(256)); // max 255
            pendenza.setDocumento(doc);
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("documento.descrizione"));
        }

        @Test
        void testDocumentoRataNonValida() {
            Documento doc = new Documento();
            doc.setRata(BigDecimal.ZERO); // deve essere >= 1
            pendenza.setDocumento(doc);
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("documento.rata"));
        }
    }

    @Nested
    @DisplayName("Validazione voci")
    class VociValidation {
        @Test
        void testVociNull() {
            pendenza.setVoci(null);
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("voci") && ex.getMessage().contains("vuoto"));
        }

        @Test
        void testVociVuote() {
            pendenza.setVoci(new ArrayList<>());
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("voci") && ex.getMessage().contains("vuoto"));
        }

        @Test
        void testTroppeVoci() {
            List<VocePendenza> voci = new ArrayList<>();
            for (int i = 0; i < 6; i++) { // max 5
                VocePendenza v = new VocePendenza();
                v.setIdVocePendenza("VOCE" + i);
                v.setImporto(new BigDecimal("10.00"));
                v.setDescrizione("Voce " + i);
                v.setCodEntrata("ENTRATA");
                voci.add(v);
            }
            pendenza.setVoci(voci);
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("voci") && ex.getMessage().contains("5"));
        }

        @Test
        void testVoceNull() {
            List<VocePendenza> voci = new ArrayList<>();
            voci.add(null);
            pendenza.setVoci(voci);
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("voci") || ex.getMessage().contains("posizione"));
        }

        @Test
        void testVoceIdVocePendenzaMancante() {
            VocePendenza voce = pendenza.getVoci().get(0);
            voce.setIdVocePendenza(null);
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("idVocePendenza"));
        }

        @Test
        void testVoceIdVocePendenzaPatternNonValido() {
            VocePendenza voce = pendenza.getVoci().get(0);
            voce.setIdVocePendenza("voce@invalid!");
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("idVocePendenza") && ex.getMessage().contains("pattern"));
        }

        @Test
        void testVoceImportoMancante() {
            VocePendenza voce = pendenza.getVoci().get(0);
            voce.setImporto(null);
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("importo") && ex.getMessage().contains("vuoto"));
        }

        @Test
        void testVoceImportoNegativo() {
            VocePendenza voce = pendenza.getVoci().get(0);
            voce.setImporto(new BigDecimal("-1.00"));
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("importo") && ex.getMessage().contains("0"));
        }

        @Test
        void testVoceDescrizioneMancante() {
            VocePendenza voce = pendenza.getVoci().get(0);
            voce.setDescrizione(null);
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("descrizione") && ex.getMessage().contains("vuoto"));
        }

        @Test
        void testVoceDescrizioneTroppoLunga() {
            VocePendenza voce = pendenza.getVoci().get(0);
            voce.setDescrizione("A".repeat(256)); // max 255
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("descrizione") && ex.getMessage().contains("255"));
        }

        @Test
        void testVoceDescrizioneCausaleRPTTroppoLunga() {
            VocePendenza voce = pendenza.getVoci().get(0);
            voce.setDescrizioneCausaleRPT("A".repeat(141)); // max 140
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("descrizioneCausaleRPT") && ex.getMessage().contains("140"));
        }
    }

    @Nested
    @DisplayName("Validazione mutua esclusività voci")
    class VociMutuaEsclusivitaValidation {
        @Test
        void testCodEntrataConTipoBollo() {
            VocePendenza voce = pendenza.getVoci().get(0);
            voce.setCodEntrata("ENTRATA");
            voce.setTipoBollo("01");
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("codEntrata") && ex.getMessage().contains("tipoBollo"));
        }

        @Test
        void testCodEntrataConIbanAccredito() {
            VocePendenza voce = pendenza.getVoci().get(0);
            voce.setCodEntrata("ENTRATA");
            voce.setIbanAccredito("IT60X0542811101000000123456");
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("codEntrata") && ex.getMessage().contains("ibanAccredito"));
        }

        @Test
        void testTipoBolloConIbanAccredito() {
            VocePendenza voce = pendenza.getVoci().get(0);
            voce.setCodEntrata(null);
            voce.setTipoBollo("01");
            voce.setHashDocumento("hash");
            voce.setProvinciaResidenza("RM");
            voce.setIbanAccredito("IT60X0542811101000000123456");
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("tipoBollo") && ex.getMessage().contains("ibanAccredito"));
        }

        @Test
        void testTipoBolloSenzaHashDocumento() {
            VocePendenza voce = pendenza.getVoci().get(0);
            voce.setCodEntrata(null);
            voce.setTipoBollo("01");
            voce.setProvinciaResidenza("RM");
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("hashDocumento"));
        }

        @Test
        void testTipoBolloSenzaProvinciaResidenza() {
            VocePendenza voce = pendenza.getVoci().get(0);
            voce.setCodEntrata(null);
            voce.setTipoBollo("01");
            voce.setHashDocumento("hash");
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("provinciaResidenza"));
        }

        @Test
        void testTipoBolloProvinciaResidenzaPatternNonValido() {
            VocePendenza voce = pendenza.getVoci().get(0);
            voce.setCodEntrata(null);
            voce.setTipoBollo("01");
            voce.setHashDocumento("hash");
            voce.setProvinciaResidenza("Roma"); // deve essere 2 lettere maiuscole
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("provinciaResidenza") && ex.getMessage().contains("pattern"));
        }

        @Test
        void testIbanAccreditoSenzaTipoContabilita() {
            VocePendenza voce = pendenza.getVoci().get(0);
            voce.setCodEntrata(null);
            voce.setIbanAccredito("IT60X0542811101000000123456");
            voce.setCodiceContabilita("COD001");
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("tipoContabilita"));
        }

        @Test
        void testIbanAccreditoSenzaCodiceContabilita() {
            VocePendenza voce = pendenza.getVoci().get(0);
            voce.setCodEntrata(null);
            voce.setIbanAccredito("IT60X0542811101000000123456");
            voce.setTipoContabilita(TipoContabilita.CAPITOLO);
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("codiceContabilita"));
        }

        @Test
        void testNessunTipoPagamento() {
            VocePendenza voce = pendenza.getVoci().get(0);
            voce.setCodEntrata(null);
            voce.setTipoBollo(null);
            voce.setIbanAccredito(null);
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("ibanAccredito") ||
                       ex.getMessage().contains("tipoBollo") ||
                       ex.getMessage().contains("codEntrata"));
        }

        @Test
        void testIbanAccreditoValido() {
            VocePendenza voce = pendenza.getVoci().get(0);
            voce.setCodEntrata(null);
            voce.setIbanAccredito("IT60X0542811101000000123456");
            voce.setTipoContabilita(TipoContabilita.CAPITOLO);
            voce.setCodiceContabilita("COD001");
            assertDoesNotThrow(() -> new PendenzaPostValidator(pendenza).validate());
        }

        @Test
        void testTipoBolloValido() {
            VocePendenza voce = pendenza.getVoci().get(0);
            voce.setCodEntrata(null);
            voce.setTipoBollo("01");
            voce.setHashDocumento("hash123");
            voce.setProvinciaResidenza("RM");
            assertDoesNotThrow(() -> new PendenzaPostValidator(pendenza).validate());
        }
    }

    @Nested
    @DisplayName("Validazione allegati")
    class AllegatiValidation {
        @Test
        void testAllegatoNomeMancante() {
            NuovoAllegatoPendenza allegato = new NuovoAllegatoPendenza();
            allegato.setContenuto(new byte[]{1, 2, 3});
            pendenza.setAllegati(Arrays.asList(allegato));
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("nome") && ex.getMessage().contains("vuoto"));
        }

        @Test
        void testAllegatoNomeTroppoLungo() {
            NuovoAllegatoPendenza allegato = new NuovoAllegatoPendenza();
            allegato.setNome("A".repeat(256)); // max 255
            allegato.setContenuto(new byte[]{1, 2, 3});
            pendenza.setAllegati(Arrays.asList(allegato));
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("nome") && ex.getMessage().contains("255"));
        }

        @Test
        void testAllegatoTipoTroppoLungo() {
            NuovoAllegatoPendenza allegato = new NuovoAllegatoPendenza();
            allegato.setNome("file.pdf");
            allegato.setTipo("A".repeat(256)); // max 255
            allegato.setContenuto(new byte[]{1, 2, 3});
            pendenza.setAllegati(Arrays.asList(allegato));
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("tipo") && ex.getMessage().contains("255"));
        }

        @Test
        void testAllegatoDescrizioneTroppoLunga() {
            NuovoAllegatoPendenza allegato = new NuovoAllegatoPendenza();
            allegato.setNome("file.pdf");
            allegato.setDescrizione("A".repeat(256)); // max 255
            allegato.setContenuto(new byte[]{1, 2, 3});
            pendenza.setAllegati(Arrays.asList(allegato));
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("descrizione") && ex.getMessage().contains("255"));
        }

        @Test
        void testAllegatoContenutoMancante() {
            NuovoAllegatoPendenza allegato = new NuovoAllegatoPendenza();
            allegato.setNome("file.pdf");
            pendenza.setAllegati(Arrays.asList(allegato));
            ValidationException ex = assertThrows(ValidationException.class,
                () -> new PendenzaPostValidator(pendenza).validate());
            assertTrue(ex.getMessage().contains("contenuto") && ex.getMessage().contains("vuoto"));
        }

        @Test
        void testAllegatoValido() {
            NuovoAllegatoPendenza allegato = new NuovoAllegatoPendenza();
            allegato.setNome("file.pdf");
            allegato.setTipo("application/pdf");
            allegato.setDescrizione("Allegato di test");
            allegato.setContenuto(new byte[]{1, 2, 3});
            pendenza.setAllegati(Arrays.asList(allegato));
            assertDoesNotThrow(() -> new PendenzaPostValidator(pendenza).validate());
        }
    }
}
