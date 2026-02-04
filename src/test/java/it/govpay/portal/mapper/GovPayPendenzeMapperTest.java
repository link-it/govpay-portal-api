package it.govpay.portal.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import it.govpay.pendenze.client.model.NuovaPendenza;
import it.govpay.pendenze.client.model.NuovaVocePendenza;
import it.govpay.pendenze.client.model.TipoSoggetto;
import it.govpay.portal.entity.Dominio;
import it.govpay.portal.entity.IbanAccredito;
import it.govpay.portal.entity.SingoloVersamento;
import it.govpay.portal.entity.TipoTributo;
import it.govpay.portal.entity.TipoVersamento;
import it.govpay.portal.entity.Tributo;
import it.govpay.portal.entity.Uo;
import it.govpay.portal.entity.Versamento;

class GovPayPendenzeMapperTest {

    private GovPayPendenzeMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new GovPayPendenzeMapper();
    }

    @Nested
    @DisplayName("toNuovaPendenza Tests")
    class ToNuovaPendenzaTests {

        @Test
        @DisplayName("Null versamento dovrebbe restituire null")
        void nullVersamentoShouldReturnNull() {
            assertNull(mapper.toNuovaPendenza(null));
        }

        @Test
        @DisplayName("Versamento completo dovrebbe essere mappato correttamente")
        void completeVersamentoShouldBeMappedCorrectly() {
            Versamento versamento = createCompleteVersamento();

            NuovaPendenza pendenza = mapper.toNuovaPendenza(versamento);

            assertNotNull(pendenza);
            assertEquals("12345678901", pendenza.getIdDominio());
            assertEquals("TARI", pendenza.getIdTipoPendenza());
            assertEquals("UO001", pendenza.getIdUnitaOperativa());
            assertEquals("TARI 2024", pendenza.getCausale());
            assertEquals(150.50, pendenza.getImporto());
            assertEquals("301000000000000001", pendenza.getNumeroAvviso());
            assertEquals("9/SERVIZI", pendenza.getTassonomia());
            assertEquals("DIR001", pendenza.getDirezione());
            assertEquals("DIV001", pendenza.getDivisione());
        }

        @Test
        @DisplayName("Versamento senza dominio dovrebbe avere idDominio null")
        void versamentoWithoutDominioShouldHaveNullIdDominio() {
            Versamento versamento = createMinimalVersamento();
            versamento.setDominio(null);

            NuovaPendenza pendenza = mapper.toNuovaPendenza(versamento);

            assertNull(pendenza.getIdDominio());
        }

        @Test
        @DisplayName("Versamento senza tipo versamento dovrebbe avere idTipoPendenza null")
        void versamentoWithoutTipoVersamentoShouldHaveNullIdTipoPendenza() {
            Versamento versamento = createMinimalVersamento();
            versamento.setTipoVersamento(null);

            NuovaPendenza pendenza = mapper.toNuovaPendenza(versamento);

            assertNull(pendenza.getIdTipoPendenza());
        }

        @Test
        @DisplayName("Versamento senza UO dovrebbe non avere idUnitaOperativa")
        void versamentoWithoutUoShouldNotHaveIdUnitaOperativa() {
            Versamento versamento = createMinimalVersamento();
            versamento.setUo(null);

            NuovaPendenza pendenza = mapper.toNuovaPendenza(versamento);

            assertNull(pendenza.getIdUnitaOperativa());
        }

        @Test
        @DisplayName("Anno tributario numerico dovrebbe essere mappato")
        void numericAnnoTributarioShouldBeMapped() {
            Versamento versamento = createMinimalVersamento();
            versamento.setCodAnnoTributario("2024");

            NuovaPendenza pendenza = mapper.toNuovaPendenza(versamento);

            assertEquals(2024, pendenza.getAnnoRiferimento());
        }

        @Test
        @DisplayName("Anno tributario non numerico dovrebbe essere ignorato")
        void nonNumericAnnoTributarioShouldBeIgnored() {
            Versamento versamento = createMinimalVersamento();
            versamento.setCodAnnoTributario("INVALID");

            NuovaPendenza pendenza = mapper.toNuovaPendenza(versamento);

            assertNull(pendenza.getAnnoRiferimento());
        }

        @Test
        @DisplayName("Tassonomia avviso valida dovrebbe essere mappata")
        void validTassonomiaAvvisoShouldBeMapped() {
            Versamento versamento = createMinimalVersamento();
            versamento.setTassonomiaAvviso("Ticket e prestazioni sanitarie");

            NuovaPendenza pendenza = mapper.toNuovaPendenza(versamento);

            assertNotNull(pendenza.getTassonomiaAvviso());
        }

        @Test
        @DisplayName("Tassonomia avviso non valida dovrebbe essere ignorata")
        void invalidTassonomiaAvvisoShouldBeIgnored() {
            Versamento versamento = createMinimalVersamento();
            versamento.setTassonomiaAvviso("INVALID_VALUE");

            NuovaPendenza pendenza = mapper.toNuovaPendenza(versamento);

            assertNull(pendenza.getTassonomiaAvviso());
        }

        @Test
        @DisplayName("Date dovrebbero essere mappate correttamente")
        void datesShouldBeMappedCorrectly() {
            Versamento versamento = createMinimalVersamento();
            versamento.setDataValidita(LocalDateTime.of(2024, 12, 31, 23, 59));
            versamento.setDataScadenza(LocalDateTime.of(2024, 6, 30, 23, 59));
            versamento.setDataNotificaAvviso(LocalDateTime.of(2024, 1, 15, 10, 0));

            NuovaPendenza pendenza = mapper.toNuovaPendenza(versamento);

            assertNotNull(pendenza.getDataValidita());
            assertNotNull(pendenza.getDataScadenza());
            assertNotNull(pendenza.getDataNotificaAvviso());
        }
    }

    @Nested
    @DisplayName("Soggetto Pagatore Mapping Tests")
    class SoggettoPagatoreMappingTests {

        @Test
        @DisplayName("Tipo debitore F dovrebbe essere mappato a persona fisica")
        void tipoDebitoreFShouldBeMappedToF() {
            Versamento versamento = createVersamentoWithDebitore("F");

            NuovaPendenza pendenza = mapper.toNuovaPendenza(versamento);

            assertEquals(TipoSoggetto.F, pendenza.getSoggettoPagatore().getTipo());
        }

        @Test
        @DisplayName("Tipo debitore G dovrebbe essere mappato a persona giuridica")
        void tipoDebitoreGShouldBeMappedToG() {
            Versamento versamento = createVersamentoWithDebitore("G");

            NuovaPendenza pendenza = mapper.toNuovaPendenza(versamento);

            assertEquals(TipoSoggetto.G, pendenza.getSoggettoPagatore().getTipo());
        }

        @Test
        @DisplayName("Tipo debitore null dovrebbe essere mappato a F (default)")
        void tipoDebitoreNullShouldBeMappedToFDefault() {
            Versamento versamento = createVersamentoWithDebitore(null);

            NuovaPendenza pendenza = mapper.toNuovaPendenza(versamento);

            assertEquals(TipoSoggetto.F, pendenza.getSoggettoPagatore().getTipo());
        }

        @Test
        @DisplayName("Tipo debitore non valido dovrebbe essere mappato a F (default)")
        void invalidTipoDebitoreShouldBeMappedToFDefault() {
            Versamento versamento = createVersamentoWithDebitore("X");

            NuovaPendenza pendenza = mapper.toNuovaPendenza(versamento);

            assertEquals(TipoSoggetto.F, pendenza.getSoggettoPagatore().getTipo());
        }

        @Test
        @DisplayName("Dati anagrafici debitore dovrebbero essere mappati")
        void debitoreDataShouldBeMapped() {
            Versamento versamento = createVersamentoWithFullDebitore();

            NuovaPendenza pendenza = mapper.toNuovaPendenza(versamento);

            assertEquals("RSSMRA80A01H501U", pendenza.getSoggettoPagatore().getIdentificativo());
            assertEquals("Mario Rossi", pendenza.getSoggettoPagatore().getAnagrafica());
            assertEquals("Via Roma", pendenza.getSoggettoPagatore().getIndirizzo());
            assertEquals("1", pendenza.getSoggettoPagatore().getCivico());
            assertEquals("00100", pendenza.getSoggettoPagatore().getCap());
            assertEquals("Roma", pendenza.getSoggettoPagatore().getLocalita());
            assertEquals("RM", pendenza.getSoggettoPagatore().getProvincia());
            assertEquals("IT", pendenza.getSoggettoPagatore().getNazione());
            assertEquals("mario.rossi@email.it", pendenza.getSoggettoPagatore().getEmail());
            assertEquals("3331234567", pendenza.getSoggettoPagatore().getCellulare());
        }
    }

    @Nested
    @DisplayName("Voci Pendenza Mapping Tests")
    class VociPendenzaMappingTests {

        @Test
        @DisplayName("Singoli versamenti dovrebbero essere mappati come voci")
        void singoliVersamentiShouldBeMappedAsVoci() {
            Versamento versamento = createVersamentoWithSingoliVersamenti();

            NuovaPendenza pendenza = mapper.toNuovaPendenza(versamento);

            assertNotNull(pendenza.getVoci());
            assertEquals(2, pendenza.getVoci().size());
            assertEquals("SV001", pendenza.getVoci().get(0).getIdVocePendenza());
            assertEquals(100.0, pendenza.getVoci().get(0).getImporto());
        }

        @Test
        @DisplayName("Voce pendenza senza dominio dovrebbe avere idDominio null")
        void voceSenzaDominioShouldHaveNullIdDominio() {
            Versamento versamento = createMinimalVersamento();
            SingoloVersamento sv = SingoloVersamento.builder()
                    .codSingoloVersamentoEnte("SV001")
                    .importoSingoloVersamento(100.0)
                    .dominio(null)
                    .build();
            versamento.setSingoliVersamenti(List.of(sv));

            NuovaPendenza pendenza = mapper.toNuovaPendenza(versamento);

            assertNull(pendenza.getVoci().get(0).getIdDominio());
        }

        @Test
        @DisplayName("Voce con tributo dovrebbe avere codEntrata")
        void voceWithTributoShouldHaveCodEntrata() {
            Versamento versamento = createVersamentoWithTributo();

            NuovaPendenza pendenza = mapper.toNuovaPendenza(versamento);

            assertEquals("TARI", pendenza.getVoci().get(0).getCodEntrata());
        }

        @Test
        @DisplayName("Voce con tributo senza tipoTributo dovrebbe avere codEntrata null")
        void voceWithTributoWithoutTipoTributoShouldHaveNullCodEntrata() {
            Versamento versamento = createMinimalVersamento();
            Tributo tributo = Tributo.builder()
                    .tipoTributo(null)
                    .build();
            SingoloVersamento sv = SingoloVersamento.builder()
                    .codSingoloVersamentoEnte("SV001")
                    .importoSingoloVersamento(100.0)
                    .tributo(tributo)
                    .build();
            versamento.setSingoliVersamenti(List.of(sv));

            NuovaPendenza pendenza = mapper.toNuovaPendenza(versamento);

            assertNull(pendenza.getVoci().get(0).getCodEntrata());
        }

        @Test
        @DisplayName("Voce con iban accredito dovrebbe essere mappato")
        void voceWithIbanAccreditoShouldBeMapped() {
            Versamento versamento = createVersamentoWithIban();

            NuovaPendenza pendenza = mapper.toNuovaPendenza(versamento);

            assertEquals("IT60X0542811101000000123456", pendenza.getVoci().get(0).getIbanAccredito());
        }

        @Test
        @DisplayName("Voce con iban appoggio dovrebbe essere mappato")
        void voceWithIbanAppoggioShouldBeMapped() {
            Versamento versamento = createVersamentoWithIbanAppoggio();

            NuovaPendenza pendenza = mapper.toNuovaPendenza(versamento);

            assertEquals("IT60X0542811101000000654321", pendenza.getVoci().get(0).getIbanAppoggio());
        }

        @Test
        @DisplayName("Voce con tipo contabilita valido dovrebbe essere mappato")
        void voceWithValidTipoContabilitaShouldBeMapped() {
            Versamento versamento = createMinimalVersamento();
            SingoloVersamento sv = SingoloVersamento.builder()
                    .codSingoloVersamentoEnte("SV001")
                    .importoSingoloVersamento(100.0)
                    .tipoContabilita("SIOPE")
                    .codiceContabilita("1234")
                    .build();
            versamento.setSingoliVersamenti(List.of(sv));

            NuovaPendenza pendenza = mapper.toNuovaPendenza(versamento);

            assertNotNull(pendenza.getVoci().get(0).getTipoContabilita());
            assertEquals("1234", pendenza.getVoci().get(0).getCodiceContabilita());
        }

        @Test
        @DisplayName("Voce con tipo contabilita non valido dovrebbe essere ignorato")
        void voceWithInvalidTipoContabilitaShouldBeIgnored() {
            Versamento versamento = createMinimalVersamento();
            SingoloVersamento sv = SingoloVersamento.builder()
                    .codSingoloVersamentoEnte("SV001")
                    .importoSingoloVersamento(100.0)
                    .tipoContabilita("INVALID")
                    .build();
            versamento.setSingoliVersamenti(List.of(sv));

            NuovaPendenza pendenza = mapper.toNuovaPendenza(versamento);

            assertNull(pendenza.getVoci().get(0).getTipoContabilita());
        }

        @Test
        @DisplayName("Voce con bollo dovrebbe essere mappata")
        void voceWithBolloShouldBeMapped() {
            Versamento versamento = createMinimalVersamento();
            SingoloVersamento sv = SingoloVersamento.builder()
                    .codSingoloVersamentoEnte("SV001")
                    .importoSingoloVersamento(16.0)
                    .tipoBollo("01")
                    .hashDocumento("abc123")
                    .provinciaResidenza("RM")
                    .build();
            versamento.setSingoliVersamenti(List.of(sv));

            NuovaPendenza pendenza = mapper.toNuovaPendenza(versamento);

            NuovaVocePendenza voce = pendenza.getVoci().get(0);
            assertNotNull(voce.getTipoBollo());
            assertEquals("abc123", voce.getHashDocumento());
            assertEquals("RM", voce.getProvinciaResidenza());
        }

        @Test
        @DisplayName("Versamento senza singoli versamenti dovrebbe avere lista voci vuota")
        void versamentoWithoutSingoliVersamentiShouldHaveEmptyVociList() {
            Versamento versamento = createMinimalVersamento();
            versamento.setSingoliVersamenti(null);

            NuovaPendenza pendenza = mapper.toNuovaPendenza(versamento);

            assertNotNull(pendenza.getVoci());
            assertTrue(pendenza.getVoci().isEmpty());
        }
    }

    // Helper methods

    private Versamento createMinimalVersamento() {
        Dominio dominio = Dominio.builder()
                .codDominio("12345678901")
                .ragioneSociale("Comune di Test")
                .build();

        return Versamento.builder()
                .dominio(dominio)
                .codVersamentoEnte("COD_VERS_001")
                .singoliVersamenti(new ArrayList<>())
                .build();
    }

    private Versamento createCompleteVersamento() {
        Dominio dominio = Dominio.builder()
                .codDominio("12345678901")
                .ragioneSociale("Comune di Test")
                .build();

        TipoVersamento tipoVersamento = TipoVersamento.builder()
                .codTipoVersamento("TARI")
                .build();

        Uo uo = Uo.builder()
                .codUo("UO001")
                .build();

        return Versamento.builder()
                .dominio(dominio)
                .tipoVersamento(tipoVersamento)
                .uo(uo)
                .codVersamentoEnte("COD_VERS_001")
                .causaleVersamento("TARI 2024")
                .importoTotale(150.50)
                .numeroAvviso("301000000000000001")
                .tassonomia("9/SERVIZI")
                .direzione("DIR001")
                .divisione("DIV001")
                .dataValidita(LocalDateTime.of(2024, 12, 31, 23, 59))
                .dataScadenza(LocalDateTime.of(2024, 6, 30, 23, 59))
                .codAnnoTributario("2024")
                .singoliVersamenti(new ArrayList<>())
                .build();
    }

    private Versamento createVersamentoWithDebitore(String tipo) {
        Versamento versamento = createMinimalVersamento();
        versamento.setDebitoreTipo(tipo);
        versamento.setDebitoreIdentificativo("RSSMRA80A01H501U");
        return versamento;
    }

    private Versamento createVersamentoWithFullDebitore() {
        Versamento versamento = createMinimalVersamento();
        versamento.setDebitoreTipo("F");
        versamento.setDebitoreIdentificativo("RSSMRA80A01H501U");
        versamento.setDebitoreAnagrafica("Mario Rossi");
        versamento.setDebitoreIndirizzo("Via Roma");
        versamento.setDebitoreCivico("1");
        versamento.setDebitoreCap("00100");
        versamento.setDebitoreLocalita("Roma");
        versamento.setDebitoreProvincia("RM");
        versamento.setDebitoreNazione("IT");
        versamento.setDebitoreEmail("mario.rossi@email.it");
        versamento.setDebitoreCellulare("3331234567");
        return versamento;
    }

    private Versamento createVersamentoWithSingoliVersamenti() {
        Versamento versamento = createMinimalVersamento();

        SingoloVersamento sv1 = SingoloVersamento.builder()
                .codSingoloVersamentoEnte("SV001")
                .importoSingoloVersamento(100.0)
                .descrizione("Voce 1")
                .build();

        SingoloVersamento sv2 = SingoloVersamento.builder()
                .codSingoloVersamentoEnte("SV002")
                .importoSingoloVersamento(50.50)
                .descrizione("Voce 2")
                .build();

        versamento.setSingoliVersamenti(List.of(sv1, sv2));
        return versamento;
    }

    private Versamento createVersamentoWithTributo() {
        Versamento versamento = createMinimalVersamento();

        TipoTributo tipoTributo = TipoTributo.builder()
                .codTributo("TARI")
                .build();

        Tributo tributo = Tributo.builder()
                .tipoTributo(tipoTributo)
                .build();

        SingoloVersamento sv = SingoloVersamento.builder()
                .codSingoloVersamentoEnte("SV001")
                .importoSingoloVersamento(100.0)
                .tributo(tributo)
                .build();

        versamento.setSingoliVersamenti(List.of(sv));
        return versamento;
    }

    private Versamento createVersamentoWithIban() {
        Versamento versamento = createMinimalVersamento();

        IbanAccredito iban = IbanAccredito.builder()
                .codIban("IT60X0542811101000000123456")
                .build();

        SingoloVersamento sv = SingoloVersamento.builder()
                .codSingoloVersamentoEnte("SV001")
                .importoSingoloVersamento(100.0)
                .ibanAccredito(iban)
                .build();

        versamento.setSingoliVersamenti(List.of(sv));
        return versamento;
    }

    private Versamento createVersamentoWithIbanAppoggio() {
        Versamento versamento = createMinimalVersamento();

        IbanAccredito ibanAppoggio = IbanAccredito.builder()
                .codIban("IT60X0542811101000000654321")
                .build();

        SingoloVersamento sv = SingoloVersamento.builder()
                .codSingoloVersamentoEnte("SV001")
                .importoSingoloVersamento(100.0)
                .ibanAppoggio(ibanAppoggio)
                .build();

        versamento.setSingoliVersamenti(List.of(sv));
        return versamento;
    }
}
