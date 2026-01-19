package it.govpay.portal.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import it.govpay.portal.entity.Applicazione;
import it.govpay.portal.entity.Dominio;
import it.govpay.portal.entity.SingoloVersamento;
import it.govpay.portal.entity.StatoVersamento;
import it.govpay.portal.entity.Stazione;
import it.govpay.portal.entity.TipoVersamento;
import it.govpay.portal.entity.Versamento;
import it.govpay.portal.model.Avviso;
import it.govpay.portal.model.Pendenza;
import it.govpay.portal.model.Ricevuta;
import it.govpay.portal.model.StatoAvviso;
import it.govpay.portal.model.StatoPendenza;
import it.govpay.portal.model.TipoSoggetto;

class PendenzeMapperTest {

    private PendenzeMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PendenzeMapper();
    }

    @Nested
    @DisplayName("toPendenza Tests")
    class ToPendenzaTests {

        @Test
        @DisplayName("Null versamento dovrebbe restituire null")
        void nullVersamentoShouldReturnNull() {
            assertNull(mapper.toPendenza(null));
        }

        @Test
        @DisplayName("Versamento completo dovrebbe essere mappato correttamente")
        void completeVersamentoShouldBeMappedCorrectly() {
            Versamento versamento = createCompleteVersamento();

            Pendenza pendenza = mapper.toPendenza(versamento);

            assertNotNull(pendenza);
            assertEquals("APP001", pendenza.getIdA2A());
            assertEquals("COD_VERS_001", pendenza.getIdPendenza());
            assertEquals("TARI", pendenza.getIdTipoPendenza());
            assertEquals("12345678901", pendenza.getDominio().getIdDominio());
            assertEquals("Comune di Test", pendenza.getDominio().getRagioneSociale());
            assertEquals(StatoPendenza.NON_ESEGUITA, pendenza.getStato());
            assertEquals("RF23000000000000001", pendenza.getIuv());
            assertEquals("TARI 2024", pendenza.getCausale());
            assertEquals(150.50, pendenza.getImporto());
            assertEquals("301000000000000001", pendenza.getNumeroAvviso());
            assertEquals(LocalDate.of(2024, 1, 15), pendenza.getDataCaricamento());
            assertEquals(LocalDate.of(2024, 12, 31), pendenza.getDataValidita());
            assertEquals(LocalDate.of(2024, 6, 30), pendenza.getDataScadenza());
            assertEquals(2024, pendenza.getAnnoRiferimento());
        }

        @Test
        @DisplayName("Versamento senza applicazione dovrebbe avere idA2A null")
        void versamentoWithoutApplicazioneShouldHaveNullIdA2A() {
            Versamento versamento = createMinimalVersamento();
            versamento.setApplicazione(null);

            Pendenza pendenza = mapper.toPendenza(versamento);

            assertNull(pendenza.getIdA2A());
        }

        @Test
        @DisplayName("Versamento senza tipo versamento dovrebbe avere idTipoPendenza null")
        void versamentoWithoutTipoVersamentoShouldHaveNullIdTipoPendenza() {
            Versamento versamento = createMinimalVersamento();
            versamento.setTipoVersamento(null);

            Pendenza pendenza = mapper.toPendenza(versamento);

            assertNull(pendenza.getIdTipoPendenza());
        }

        @Test
        @DisplayName("Versamento senza dominio dovrebbe avere dominio null")
        void versamentoWithoutDominioShouldHaveNullDominio() {
            Versamento versamento = createMinimalVersamento();
            versamento.setDominio(null);

            Pendenza pendenza = mapper.toPendenza(versamento);

            assertNull(pendenza.getDominio());
        }

        @Test
        @DisplayName("Anno tributario non numerico dovrebbe essere ignorato")
        void nonNumericAnnoTributarioShouldBeIgnored() {
            Versamento versamento = createMinimalVersamento();
            versamento.setCodAnnoTributario("INVALID");

            Pendenza pendenza = mapper.toPendenza(versamento);

            assertNull(pendenza.getAnnoRiferimento());
        }

        @Test
        @DisplayName("Soggetto pagatore F dovrebbe essere mappato come persona fisica")
        void soggettoPagatoreFShouldBeMappedAsPersonaFisica() {
            Versamento versamento = createVersamentoWithDebitore("F");

            Pendenza pendenza = mapper.toPendenza(versamento);

            assertEquals(TipoSoggetto.F, pendenza.getSoggettoPagatore().getTipo());
            assertEquals("RSSMRA80A01H501U", pendenza.getSoggettoPagatore().getIdentificativo());
            assertEquals("Mario Rossi", pendenza.getSoggettoPagatore().getAnagrafica());
        }

        @Test
        @DisplayName("Soggetto pagatore G dovrebbe essere mappato come persona giuridica")
        void soggettoPagatoreGShouldBeMappedAsPersonaGiuridica() {
            Versamento versamento = createVersamentoWithDebitore("G");

            Pendenza pendenza = mapper.toPendenza(versamento);

            assertEquals(TipoSoggetto.G, pendenza.getSoggettoPagatore().getTipo());
        }

        @Test
        @DisplayName("Voci pendenza dovrebbero essere mappate")
        void vociPendenzaShouldBeMapped() {
            Versamento versamento = createVersamentoWithSingoliVersamenti();

            Pendenza pendenza = mapper.toPendenza(versamento);

            assertNotNull(pendenza.getVoci());
            assertEquals(2, pendenza.getVoci().size());
            assertEquals("SV001", pendenza.getVoci().get(0).getIdVocePendenza());
            assertEquals(100.0, pendenza.getVoci().get(0).getImporto());
            assertEquals("SV002", pendenza.getVoci().get(1).getIdVocePendenza());
            assertEquals(50.50, pendenza.getVoci().get(1).getImporto());
        }
    }

    @Nested
    @DisplayName("Stato Versamento Mapping Tests")
    class StatoVersamentoMappingTests {

        @Test
        @DisplayName("ESEGUITO dovrebbe essere mappato a ESEGUITA")
        void eseguitoShouldBeMappedToEseguita() {
            Versamento versamento = createMinimalVersamento();
            versamento.setStatoVersamento(StatoVersamento.ESEGUITO);

            Pendenza pendenza = mapper.toPendenza(versamento);

            assertEquals(StatoPendenza.ESEGUITA, pendenza.getStato());
        }

        @Test
        @DisplayName("NON_ESEGUITO dovrebbe essere mappato a NON_ESEGUITA")
        void nonEseguitoShouldBeMappedToNonEseguita() {
            Versamento versamento = createMinimalVersamento();
            versamento.setStatoVersamento(StatoVersamento.NON_ESEGUITO);

            Pendenza pendenza = mapper.toPendenza(versamento);

            assertEquals(StatoPendenza.NON_ESEGUITA, pendenza.getStato());
        }

        @Test
        @DisplayName("PARZIALMENTE_ESEGUITO dovrebbe essere mappato a ESEGUITA_PARZIALE")
        void parzialmenteEseguitoShouldBeMappedToEseguitaParziale() {
            Versamento versamento = createMinimalVersamento();
            versamento.setStatoVersamento(StatoVersamento.PARZIALMENTE_ESEGUITO);

            Pendenza pendenza = mapper.toPendenza(versamento);

            assertEquals(StatoPendenza.ESEGUITA_PARZIALE, pendenza.getStato());
        }

        @Test
        @DisplayName("ANNULLATO dovrebbe essere mappato a ANNULLATA")
        void annullatoShouldBeMappedToAnnullata() {
            Versamento versamento = createMinimalVersamento();
            versamento.setStatoVersamento(StatoVersamento.ANNULLATO);

            Pendenza pendenza = mapper.toPendenza(versamento);

            assertEquals(StatoPendenza.ANNULLATA, pendenza.getStato());
        }

        @Test
        @DisplayName("INCASSATO dovrebbe essere mappato a ESEGUITA")
        void incassatoShouldBeMappedToEseguita() {
            Versamento versamento = createMinimalVersamento();
            versamento.setStatoVersamento(StatoVersamento.INCASSATO);

            Pendenza pendenza = mapper.toPendenza(versamento);

            assertEquals(StatoPendenza.ESEGUITA, pendenza.getStato());
        }

        @Test
        @DisplayName("ANOMALO dovrebbe essere mappato a ANOMALA")
        void anomaloShouldBeMappedToAnomala() {
            Versamento versamento = createMinimalVersamento();
            versamento.setStatoVersamento(StatoVersamento.ANOMALO);

            Pendenza pendenza = mapper.toPendenza(versamento);

            assertEquals(StatoPendenza.ANOMALA, pendenza.getStato());
        }

        @Test
        @DisplayName("Stato null dovrebbe restituire null")
        void nullStatoShouldReturnNull() {
            Versamento versamento = createMinimalVersamento();
            versamento.setStatoVersamento(null);

            Pendenza pendenza = mapper.toPendenza(versamento);

            assertNull(pendenza.getStato());
        }

        @Test
        @DisplayName("ESEGUITO_ALTRO_CANALE dovrebbe essere mappato a NON_ESEGUITA (default)")
        void eseguitoAltroAnaleShouldBeMappedToDefault() {
            Versamento versamento = createMinimalVersamento();
            versamento.setStatoVersamento(StatoVersamento.ESEGUITO_ALTRO_CANALE);

            Pendenza pendenza = mapper.toPendenza(versamento);

            assertEquals(StatoPendenza.NON_ESEGUITA, pendenza.getStato());
        }
    }

    @Nested
    @DisplayName("toAvviso Tests")
    class ToAvvisoTests {

        @Test
        @DisplayName("Null versamento dovrebbe restituire null")
        void nullVersamentoShouldReturnNull() {
            assertNull(mapper.toAvviso(null, null));
        }

        @Test
        @DisplayName("Avviso completo dovrebbe essere mappato correttamente")
        void completeAvvisoShouldBeMappedCorrectly() {
            Versamento versamento = createCompleteVersamento();
            Dominio dominio = createDominioWithStazione();

            Avviso avviso = mapper.toAvviso(versamento, dominio);

            assertNotNull(avviso);
            assertEquals("TARI 2024", avviso.getDescrizione());
            assertEquals(LocalDate.of(2024, 6, 30), avviso.getDataScadenza());
            assertEquals("12345678901", avviso.getIdDominio());
            assertEquals(150.50, avviso.getImporto());
            assertEquals("301000000000000001", avviso.getNumeroAvviso());
            assertNotNull(avviso.getBarcode());
            assertNotNull(avviso.getQrcode());
        }

        @Test
        @DisplayName("Versamento senza dominio dovrebbe avere idDominio null")
        void versamentoWithoutDominioShouldHaveNullIdDominio() {
            Versamento versamento = createMinimalVersamento();
            versamento.setDominio(null);
            Dominio dominio = createDominioWithStazione();

            Avviso avviso = mapper.toAvviso(versamento, dominio);

            assertNull(avviso.getIdDominio());
        }

        @Test
        @DisplayName("Tassonomia avviso valida dovrebbe essere mappata")
        void validTassonomiaAvvisoShouldBeMapped() {
            Versamento versamento = createMinimalVersamento();
            versamento.setTassonomiaAvviso("Ticket e prestazioni sanitarie");
            Dominio dominio = createDominioWithStazione();

            Avviso avviso = mapper.toAvviso(versamento, dominio);

            assertNotNull(avviso.getTassonomiaAvviso());
        }

        @Test
        @DisplayName("Tassonomia avviso non valida dovrebbe essere ignorata")
        void invalidTassonomiaAvvisoShouldBeIgnored() {
            Versamento versamento = createMinimalVersamento();
            versamento.setTassonomiaAvviso("INVALID_VALUE");
            Dominio dominio = createDominioWithStazione();

            Avviso avviso = mapper.toAvviso(versamento, dominio);

            assertNull(avviso.getTassonomiaAvviso());
        }
    }

    @Nested
    @DisplayName("Stato Avviso Mapping Tests")
    class StatoAvvisoMappingTests {

        @Test
        @DisplayName("ANNULLATO dovrebbe essere mappato a ANNULLATA")
        void annullatoShouldBeMappedToAnnullata() {
            Versamento versamento = createMinimalVersamento();
            versamento.setStatoVersamento(StatoVersamento.ANNULLATO);
            Dominio dominio = createDominioWithStazione();

            Avviso avviso = mapper.toAvviso(versamento, dominio);

            assertEquals(StatoAvviso.ANNULLATA, avviso.getStato());
        }

        @Test
        @DisplayName("ESEGUITO dovrebbe essere mappato a DUPLICATA")
        void eseguitoShouldBeMappedToDuplicata() {
            Versamento versamento = createMinimalVersamento();
            versamento.setStatoVersamento(StatoVersamento.ESEGUITO);
            Dominio dominio = createDominioWithStazione();

            Avviso avviso = mapper.toAvviso(versamento, dominio);

            assertEquals(StatoAvviso.DUPLICATA, avviso.getStato());
        }

        @Test
        @DisplayName("ESEGUITO_ALTRO_CANALE dovrebbe essere mappato a DUPLICATA")
        void eseguitoAltroAnaleShouldBeMappedToDuplicata() {
            Versamento versamento = createMinimalVersamento();
            versamento.setStatoVersamento(StatoVersamento.ESEGUITO_ALTRO_CANALE);
            Dominio dominio = createDominioWithStazione();

            Avviso avviso = mapper.toAvviso(versamento, dominio);

            assertEquals(StatoAvviso.DUPLICATA, avviso.getStato());
        }

        @Test
        @DisplayName("PARZIALMENTE_ESEGUITO dovrebbe essere mappato a DUPLICATA")
        void parzialmenteEseguitoShouldBeMappedToDuplicata() {
            Versamento versamento = createMinimalVersamento();
            versamento.setStatoVersamento(StatoVersamento.PARZIALMENTE_ESEGUITO);
            Dominio dominio = createDominioWithStazione();

            Avviso avviso = mapper.toAvviso(versamento, dominio);

            assertEquals(StatoAvviso.DUPLICATA, avviso.getStato());
        }

        @Test
        @DisplayName("NON_ESEGUITO con data scadenza passata dovrebbe essere SCADUTA")
        void nonEseguitoWithPastScadenzaShouldBeScaduta() {
            Versamento versamento = createMinimalVersamento();
            versamento.setStatoVersamento(StatoVersamento.NON_ESEGUITO);
            versamento.setDataScadenza(LocalDateTime.now().minusDays(1));
            Dominio dominio = createDominioWithStazione();

            Avviso avviso = mapper.toAvviso(versamento, dominio);

            assertEquals(StatoAvviso.SCADUTA, avviso.getStato());
        }

        @Test
        @DisplayName("NON_ESEGUITO con data scadenza futura dovrebbe essere NON_ESEGUITA")
        void nonEseguitoWithFutureScadenzaShouldBeNonEseguita() {
            Versamento versamento = createMinimalVersamento();
            versamento.setStatoVersamento(StatoVersamento.NON_ESEGUITO);
            versamento.setDataScadenza(LocalDateTime.now().plusDays(30));
            Dominio dominio = createDominioWithStazione();

            Avviso avviso = mapper.toAvviso(versamento, dominio);

            assertEquals(StatoAvviso.NON_ESEGUITA, avviso.getStato());
        }

        @Test
        @DisplayName("NON_ESEGUITO senza data scadenza dovrebbe essere NON_ESEGUITA")
        void nonEseguitoWithoutScadenzaShouldBeNonEseguita() {
            Versamento versamento = createMinimalVersamento();
            versamento.setStatoVersamento(StatoVersamento.NON_ESEGUITO);
            versamento.setDataScadenza(null);
            Dominio dominio = createDominioWithStazione();

            Avviso avviso = mapper.toAvviso(versamento, dominio);

            assertEquals(StatoAvviso.NON_ESEGUITA, avviso.getStato());
        }

        @Test
        @DisplayName("Stato null dovrebbe essere SCONOSCIUTA")
        void nullStatoShouldBeSconosciuta() {
            Versamento versamento = createMinimalVersamento();
            versamento.setStatoVersamento(null);
            Dominio dominio = createDominioWithStazione();

            Avviso avviso = mapper.toAvviso(versamento, dominio);

            assertEquals(StatoAvviso.SCONOSCIUTA, avviso.getStato());
        }

        @Test
        @DisplayName("INCASSATO dovrebbe essere mappato a SCONOSCIUTA (default)")
        void incassatoShouldBeMappedToDefault() {
            Versamento versamento = createMinimalVersamento();
            versamento.setStatoVersamento(StatoVersamento.INCASSATO);
            Dominio dominio = createDominioWithStazione();

            Avviso avviso = mapper.toAvviso(versamento, dominio);

            assertEquals(StatoAvviso.SCONOSCIUTA, avviso.getStato());
        }
    }

    @Nested
    @DisplayName("toRicevuta Tests")
    class ToRicevutaTests {

        @Test
        @DisplayName("Null versamento dovrebbe restituire null")
        void nullVersamentoShouldReturnNull() {
            assertNull(mapper.toRicevuta(null));
        }

        @Test
        @DisplayName("Ricevuta completa dovrebbe essere mappata correttamente")
        void completeRicevutaShouldBeMappedCorrectly() {
            Versamento versamento = createCompleteVersamento();
            versamento.setStatoVersamento(StatoVersamento.ESEGUITO);
            versamento.setDataPagamento(LocalDateTime.of(2024, 3, 15, 10, 30));

            Ricevuta ricevuta = mapper.toRicevuta(versamento);

            assertNotNull(ricevuta);
            assertEquals("TARI 2024", ricevuta.getOggettoDelPagamento());
            assertEquals("12345678901", ricevuta.getDominio().getIdDominio());
            assertEquals("Comune di Test", ricevuta.getDominio().getRagioneSociale());
            assertEquals(150.50, ricevuta.getImportoTotale());
            assertEquals(LocalDate.of(2024, 3, 15), ricevuta.getDataApplicativa());
            assertEquals("ESEGUITO", ricevuta.getStato());
            assertEquals("RF23000000000000001", ricevuta.getIuv());
        }

        @Test
        @DisplayName("Versamento senza dominio dovrebbe avere dominio null")
        void versamentoWithoutDominioShouldHaveNullDominio() {
            Versamento versamento = createMinimalVersamento();
            versamento.setDominio(null);

            Ricevuta ricevuta = mapper.toRicevuta(versamento);

            assertNull(ricevuta.getDominio());
        }

        @Test
        @DisplayName("Versamento con singoli versamenti dovrebbe avere voci")
        void versamentoWithSingoliVersamentiShouldHaveVoci() {
            Versamento versamento = createVersamentoWithSingoliVersamenti();

            Ricevuta ricevuta = mapper.toRicevuta(versamento);

            assertNotNull(ricevuta.getElencoVoci());
            assertEquals(2, ricevuta.getElencoVoci().size());
            assertEquals("Descrizione 1", ricevuta.getElencoVoci().get(0).getDescrizione());
            assertEquals(100.0, ricevuta.getElencoVoci().get(0).getImporto());
        }

        @Test
        @DisplayName("Versamento senza singoli versamenti dovrebbe avere voce di default")
        void versamentoWithoutSingoliVersamentiShouldHaveDefaultVoce() {
            Versamento versamento = createMinimalVersamento();
            versamento.setSingoliVersamenti(Collections.emptyList());

            Ricevuta ricevuta = mapper.toRicevuta(versamento);

            assertNotNull(ricevuta.getElencoVoci());
            assertEquals(1, ricevuta.getElencoVoci().size());
            assertEquals("1", ricevuta.getElencoVoci().get(0).getIdRiscossione());
        }

        @Test
        @DisplayName("Singolo versamento senza descrizione dovrebbe usare default")
        void singoloVersamentoWithoutDescrizioneShouldUseDefault() {
            Versamento versamento = createMinimalVersamento();
            SingoloVersamento sv = SingoloVersamento.builder()
                    .descrizione(null)
                    .indiceDati(1)
                    .importoSingoloVersamento(100.0)
                    .build();
            versamento.setSingoliVersamenti(List.of(sv));

            Ricevuta ricevuta = mapper.toRicevuta(versamento);

            assertEquals("Voce di pagamento", ricevuta.getElencoVoci().get(0).getDescrizione());
        }
    }

    @Nested
    @DisplayName("Stato Ricevuta Mapping Tests")
    class StatoRicevutaMappingTests {

        @Test
        @DisplayName("ESEGUITO dovrebbe essere mappato a ESEGUITO")
        void eseguitoShouldBeMappedToEseguito() {
            Versamento versamento = createMinimalVersamento();
            versamento.setStatoVersamento(StatoVersamento.ESEGUITO);

            Ricevuta ricevuta = mapper.toRicevuta(versamento);

            assertEquals("ESEGUITO", ricevuta.getStato());
        }

        @Test
        @DisplayName("INCASSATO dovrebbe essere mappato a ESEGUITO")
        void incassatoShouldBeMappedToEseguito() {
            Versamento versamento = createMinimalVersamento();
            versamento.setStatoVersamento(StatoVersamento.INCASSATO);

            Ricevuta ricevuta = mapper.toRicevuta(versamento);

            assertEquals("ESEGUITO", ricevuta.getStato());
        }

        @Test
        @DisplayName("ESEGUITO_ALTRO_CANALE dovrebbe essere mappato a ESEGUITO")
        void eseguitoAltroAnaleShouldBeMappedToEseguito() {
            Versamento versamento = createMinimalVersamento();
            versamento.setStatoVersamento(StatoVersamento.ESEGUITO_ALTRO_CANALE);

            Ricevuta ricevuta = mapper.toRicevuta(versamento);

            assertEquals("ESEGUITO", ricevuta.getStato());
        }

        @Test
        @DisplayName("ESEGUITO_SENZA_RPT dovrebbe essere mappato a ESEGUITO")
        void eseguitoSenzaRptShouldBeMappedToEseguito() {
            Versamento versamento = createMinimalVersamento();
            versamento.setStatoVersamento(StatoVersamento.ESEGUITO_SENZA_RPT);

            Ricevuta ricevuta = mapper.toRicevuta(versamento);

            assertEquals("ESEGUITO", ricevuta.getStato());
        }

        @Test
        @DisplayName("PARZIALMENTE_ESEGUITO dovrebbe essere mappato a PARZIALMENTE_ESEGUITO")
        void parzialmenteEseguitoShouldBeMappedToParzialmenteEseguito() {
            Versamento versamento = createMinimalVersamento();
            versamento.setStatoVersamento(StatoVersamento.PARZIALMENTE_ESEGUITO);

            Ricevuta ricevuta = mapper.toRicevuta(versamento);

            assertEquals("PARZIALMENTE_ESEGUITO", ricevuta.getStato());
        }

        @Test
        @DisplayName("NON_ESEGUITO dovrebbe essere mappato a NON_ESEGUITO")
        void nonEseguitoShouldBeMappedToNonEseguito() {
            Versamento versamento = createMinimalVersamento();
            versamento.setStatoVersamento(StatoVersamento.NON_ESEGUITO);

            Ricevuta ricevuta = mapper.toRicevuta(versamento);

            assertEquals("NON_ESEGUITO", ricevuta.getStato());
        }

        @Test
        @DisplayName("ANNULLATO dovrebbe essere mappato a ANNULLATO")
        void annullatoShouldBeMappedToAnnullato() {
            Versamento versamento = createMinimalVersamento();
            versamento.setStatoVersamento(StatoVersamento.ANNULLATO);

            Ricevuta ricevuta = mapper.toRicevuta(versamento);

            assertEquals("ANNULLATO", ricevuta.getStato());
        }

        @Test
        @DisplayName("ANOMALO dovrebbe essere mappato a ANOMALO")
        void anomaloShouldBeMappedToAnomalo() {
            Versamento versamento = createMinimalVersamento();
            versamento.setStatoVersamento(StatoVersamento.ANOMALO);

            Ricevuta ricevuta = mapper.toRicevuta(versamento);

            assertEquals("ANOMALO", ricevuta.getStato());
        }

        @Test
        @DisplayName("Stato null dovrebbe essere SCONOSCIUTO")
        void nullStatoShouldBeSconosciuto() {
            Versamento versamento = createMinimalVersamento();
            versamento.setStatoVersamento(null);

            Ricevuta ricevuta = mapper.toRicevuta(versamento);

            assertEquals("SCONOSCIUTO", ricevuta.getStato());
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

        Applicazione applicazione = Applicazione.builder()
                .codApplicazione("APP001")
                .build();

        TipoVersamento tipoVersamento = TipoVersamento.builder()
                .codTipoVersamento("TARI")
                .build();

        return Versamento.builder()
                .dominio(dominio)
                .applicazione(applicazione)
                .tipoVersamento(tipoVersamento)
                .codVersamentoEnte("COD_VERS_001")
                .statoVersamento(StatoVersamento.NON_ESEGUITO)
                .iuvVersamento("RF23000000000000001")
                .causaleVersamento("TARI 2024")
                .importoTotale(150.50)
                .numeroAvviso("301000000000000001")
                .dataCreazione(LocalDateTime.of(2024, 1, 15, 10, 0))
                .dataValidita(LocalDateTime.of(2024, 12, 31, 23, 59))
                .dataScadenza(LocalDateTime.of(2024, 6, 30, 23, 59))
                .codAnnoTributario("2024")
                .debitoreTipo("F")
                .debitoreIdentificativo("RSSMRA80A01H501U")
                .debitoreAnagrafica("Mario Rossi")
                .singoliVersamenti(new ArrayList<>())
                .build();
    }

    private Versamento createVersamentoWithDebitore(String tipo) {
        Versamento versamento = createMinimalVersamento();
        versamento.setDebitoreTipo(tipo);
        versamento.setDebitoreIdentificativo("RSSMRA80A01H501U");
        versamento.setDebitoreAnagrafica("Mario Rossi");
        versamento.setDebitoreIndirizzo("Via Roma 1");
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
        Dominio dominio = Dominio.builder()
                .codDominio("12345678901")
                .ragioneSociale("Comune di Test")
                .build();

        SingoloVersamento sv1 = SingoloVersamento.builder()
                .codSingoloVersamentoEnte("SV001")
                .importoSingoloVersamento(100.0)
                .descrizione("Descrizione 1")
                .indiceDati(1)
                .dominio(dominio)
                .build();

        SingoloVersamento sv2 = SingoloVersamento.builder()
                .codSingoloVersamentoEnte("SV002")
                .importoSingoloVersamento(50.50)
                .descrizione("Descrizione 2")
                .indiceDati(2)
                .dominio(dominio)
                .build();

        Versamento versamento = createMinimalVersamento();
        versamento.setSingoliVersamenti(List.of(sv1, sv2));
        return versamento;
    }

    private Dominio createDominioWithStazione() {
        Stazione stazione = Stazione.builder()
                .applicationCode(Integer.valueOf(1))
                .build();

        return Dominio.builder()
                .codDominio("12345678901")
                .ragioneSociale("Comune di Test")
                .gln("1234567890123")
                .auxDigit(Integer.valueOf(3))
                .stazione(stazione)
                .build();
    }
}
