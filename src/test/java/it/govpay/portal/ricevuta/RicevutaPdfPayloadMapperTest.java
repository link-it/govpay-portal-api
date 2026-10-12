package it.govpay.portal.ricevuta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import it.gov.digitpa.schemas._2011.pagamenti.CtDatiSingoloPagamentoRT;
import it.gov.digitpa.schemas._2011.pagamenti.CtDatiVersamentoRT;
import it.gov.digitpa.schemas._2011.pagamenti.CtDominio;
import it.gov.digitpa.schemas._2011.pagamenti.CtIdentificativoUnivocoPersonaFG;
import it.gov.digitpa.schemas._2011.pagamenti.CtSoggettoPagatore;
import it.gov.digitpa.schemas._2011.pagamenti.RPT;
import it.gov.digitpa.schemas._2011.pagamenti.RT;
import it.gov.digitpa.schemas._2011.pagamenti.StTipoIdentificativoUnivocoPersFG;
import it.govpay.portal.config.GovPayStampeClientConfig;
import it.govpay.portal.entity.Dominio;
import it.govpay.portal.entity.Rpt;
import it.govpay.portal.entity.Uo;
import it.govpay.portal.entity.Versamento;
import it.govpay.portal.repository.DominioLogoRepository;
import it.govpay.portal.repository.UoRepository;
import it.govpay.stampe.client.model.Receipt;
import it.govpay.stampe.client.model.ReceiptItem;
import it.govpay.stampe.client.model.ReceiptItemStatus;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

/**
 * Verifica che {@link RicevutaPdfPayloadMapper} legga data operazione/applicativa, soggetto
 * pagatore e voci dall'XML storico (RT, e per SANP_230 anche RPT) — non dai dati correnti
 * della pendenza — per tutti e tre i formati RT gestiti. Mirror di
 * {@code govpay-console-api} {@code RicevutaPdfPayloadMapperTest}.
 */
class RicevutaPdfPayloadMapperTest {

    private final UoRepository uoRepository = mock(UoRepository.class);
    private final DominioLogoRepository dominioLogoRepository = mock(DominioLogoRepository.class);
    private final GovPayStampeClientConfig stampeConfig = new GovPayStampeClientConfig();
    private final RicevutaPdfPayloadMapper mapper =
            new RicevutaPdfPayloadMapper(uoRepository, dominioLogoRepository, stampeConfig);

    // ---------- SANP_230 (ctRicevutaTelematica) ----------

    @Test
    void sanp230LeggeDataOperazioneDallRptEDataApplicativaDallaRt() throws JAXBException {
        Rpt rpt = rptSanp230(true);

        Receipt receipt = mapper.toReceipt(rpt);

        // RPT.dataOraMessaggioRichiesta = 2026-06-09T08:15 -> non mezzanotte, ore/minuti visibili
        assertThat(receipt.getOperationDate()).isEqualTo("09/06/2026 08:15");
        // RT.dataOraMessaggioRicevuta = 2026-06-10T09:32 -> sempre solo data
        assertThat(receipt.getApplicationDate()).isEqualTo("10/06/2026");
    }

    @Test
    void sanp230SenzaXmlRptRipiegaSullaColonnaDataMsgRichiesta() throws JAXBException {
        Rpt rpt = rptSanp230(false);
        // mezzanotte esatta -> la data operazione deve mostrare solo la data
        rpt.setDataMsgRichiesta(LocalDateTime.of(2026, 6, 9, 0, 0, 0));

        Receipt receipt = mapper.toReceipt(rpt);

        assertThat(receipt.getOperationDate()).isEqualTo("09/06/2026");
    }

    @Test
    void sanp230UsaIlSoggettoPagatoreDellaRtNonIlDebitoreCorrenteDellaPendenza() throws JAXBException {
        Rpt rpt = rptSanp230(true);

        Receipt receipt = mapper.toReceipt(rpt);

        assertThat(receipt.getPayer().getFiscalCode()).isEqualTo("RSSMRA80A01H501U");
        assertThat(receipt.getPayer().getFullName()).isEqualTo("Mario Rossi (storico RT)");
        assertThat(receipt.getPayer().getAddress()).isEqualTo("Via Roma, 1");
        assertThat(receipt.getPayer().getLocation()).isEqualTo("00100 Roma (RM)");
    }

    @Test
    void sanp230TroncaIndirizzoELocalitaDelPagatoreA70Caratteri() throws JAXBException {
        Rpt rpt = rptSanp230ConIndirizzoPagatoreLungo();

        Receipt receipt = mapper.toReceipt(rpt);

        assertThat(receipt.getPayer().getAddress()).hasSizeLessThanOrEqualTo(70);
        assertThat(receipt.getPayer().getLocation()).hasSizeLessThanOrEqualTo(70);
    }

    @Test
    void sanp230TroncaLaCausaleA140Caratteri() throws JAXBException {
        Rpt rpt = rptSanp230(true);
        rpt.getVersamento().setCausaleVersamento("A".repeat(141));

        Receipt receipt = mapper.toReceipt(rpt);

        assertThat(receipt.getPaymentSubject()).hasSize(140);
    }

    @Test
    void sanp230UsaLeVociDellaRtConIurVeroEStatoPerImporto() throws JAXBException {
        Rpt rpt = rptSanp230(true);

        Receipt receipt = mapper.toReceipt(rpt);

        assertThat(receipt.getItems()).hasSize(2);
        ReceiptItem eseguita = receipt.getItems().get(0);
        assertThat(eseguita.getIur()).isEqualTo("IUR900000004");
        assertThat(eseguita.getDescription()).isEqualTo("Diritti di segreteria");
        assertThat(eseguita.getAmount()).isEqualTo(16.00);
        assertThat(eseguita.getStatus()).isEqualTo(ReceiptItemStatus.EXECUTED);

        ReceiptItem nonEseguita = receipt.getItems().get(1);
        assertThat(nonEseguita.getIur()).isEqualTo("IUR900000005");
        assertThat(nonEseguita.getAmount()).isEqualTo(0.0);
        assertThat(nonEseguita.getStatus()).isEqualTo(ReceiptItemStatus.NOT_EXECUTED);
    }

    @Test
    void organizationLeggeIndirizzoDallUnitaOperativaEc() throws JAXBException {
        Rpt rpt = rptSanp230(true);
        Uo ec = new Uo();
        ec.setUoIndirizzo("Piazza Duomo");
        ec.setUoCivico("1");
        ec.setUoCap("20100");
        ec.setUoLocalita("Milano");
        ec.setUoProvincia("MI");
        when(uoRepository.findByDominioIdAndCodUo(eq(42L), eq("EC"))).thenReturn(Optional.of(ec));

        Receipt receipt = mapper.toReceipt(rpt);

        assertThat(receipt.getOrganization().getAddress()).isEqualTo("Piazza Duomo, 1");
        assertThat(receipt.getOrganization().getLocation()).isEqualTo("20100 Milano (MI)");
    }

    @Test
    void organizationHaIndirizzoVuotoSeLUnitaOperativaEcNonEsiste() throws JAXBException {
        Rpt rpt = rptSanp230(true);
        // uoRepository senza stub -> Optional.empty() di default

        Receipt receipt = mapper.toReceipt(rpt);

        assertThat(receipt.getOrganization().getAddress()).isEmpty();
        assertThat(receipt.getOrganization().getLocation()).isEmpty();
    }

    private static Rpt rptSanp230(boolean conXmlRpt) throws JAXBException {
        RT rt = new RT();
        rt.setVersioneOggetto("6.2");
        CtDominio dominioRt = new CtDominio();
        dominioRt.setIdentificativoDominio("12345678901");
        rt.setDominio(dominioRt);
        rt.setIdentificativoMessaggioRicevuta("RT900000004");
        rt.setDataOraMessaggioRicevuta(LocalDateTime.of(2026, 6, 10, 9, 32, 0));

        CtIdentificativoUnivocoPersonaFG idPagatore = new CtIdentificativoUnivocoPersonaFG();
        idPagatore.setTipoIdentificativoUnivoco(StTipoIdentificativoUnivocoPersFG.F);
        idPagatore.setCodiceIdentificativoUnivoco("RSSMRA80A01H501U");
        CtSoggettoPagatore pagatore = new CtSoggettoPagatore();
        pagatore.setIdentificativoUnivocoPagatore(idPagatore);
        pagatore.setAnagraficaPagatore("Mario Rossi (storico RT)");
        pagatore.setIndirizzoPagatore("Via Roma");
        pagatore.setCivicoPagatore("1");
        pagatore.setCapPagatore("00100");
        pagatore.setLocalitaPagatore("Roma");
        pagatore.setProvinciaPagatore("RM");
        rt.setSoggettoPagatore(pagatore);

        CtDatiVersamentoRT dati = new CtDatiVersamentoRT();
        dati.setCodiceEsitoPagamento("2");
        dati.setImportoTotalePagato(new BigDecimal("16.00"));
        dati.setIdentificativoUnivocoVersamento("IUV900000004");
        dati.setCodiceContestoPagamento("CCP900000004");

        CtDatiSingoloPagamentoRT eseguita = new CtDatiSingoloPagamentoRT();
        eseguita.setSingoloImportoPagato(new BigDecimal("16.00"));
        eseguita.setIdentificativoUnivocoRiscossione("IUR900000004");
        eseguita.setCausaleVersamento("Diritti di segreteria");
        dati.getDatiSingoloPagamentos().add(eseguita);

        CtDatiSingoloPagamentoRT nonEseguita = new CtDatiSingoloPagamentoRT();
        nonEseguita.setSingoloImportoPagato(BigDecimal.ZERO);
        nonEseguita.setIdentificativoUnivocoRiscossione("IUR900000005");
        nonEseguita.setCausaleVersamento("Sanzione non applicata");
        dati.getDatiSingoloPagamentos().add(nonEseguita);

        rt.setDatiPagamento(dati);

        Dominio d = Dominio.builder()
                .id(42L)
                .codDominio("12345678901")
                .ragioneSociale("Comune di Test")
                .build();

        Versamento v = Versamento.builder()
                .dominio(d)
                .causaleVersamento("Causale versamento corrente (non e' la voce della ricevuta)")
                // Debitore corrente deliberatamente diverso dal soggetto pagatore storico della
                // RT: se il mapper tornasse a leggerlo da qui, i test sul payer lo scoprirebbero.
                .debitoreAnagrafica("Debitore attuale, diverso da quello storico")
                .debitoreIdentificativo("DBTATT00A00A000A")
                .build();

        Rpt.RptBuilder builder = Rpt.builder()
                .versamento(v)
                .iuv("IUV900000004")
                .ccp("CCP900000004")
                .codDominio("12345678901")
                .versione("SANP_230")
                .codEsitoPagamento(2)
                .importoTotalePagato(16.00)
                // Diversa dalla data vera nella RPT: se il mapper ripiegasse comunque su questa
                // colonna anche con xml_rpt presente, il test del caso "con xmlRpt" lo scoprirebbe.
                .dataMsgRichiesta(LocalDateTime.of(2000, 1, 1, 0, 0, 0))
                .xmlRt(marshal(RT.class, rt));

        if (conXmlRpt) {
            RPT rpTelematico = new RPT();
            rpTelematico.setDataOraMessaggioRichiesta(LocalDateTime.of(2026, 6, 9, 8, 15, 0));
            builder.xmlRpt(marshal(RPT.class, rpTelematico));
        }

        return builder.build();
    }

    private static Rpt rptSanp230ConIndirizzoPagatoreLungo() throws JAXBException {
        RT rt = new RT();
        rt.setVersioneOggetto("6.2");
        CtDominio dominioRt = new CtDominio();
        dominioRt.setIdentificativoDominio("12345678901");
        rt.setDominio(dominioRt);
        rt.setIdentificativoMessaggioRicevuta("RT900000099");
        rt.setDataOraMessaggioRicevuta(LocalDateTime.of(2026, 6, 10, 9, 32, 0));

        CtSoggettoPagatore pagatore = new CtSoggettoPagatore();
        pagatore.setAnagraficaPagatore("Mario Rossi");
        pagatore.setIndirizzoPagatore("Via Interminabile Dei Giardini Pubblici Comunali Del Quartiere Nord");
        pagatore.setCivicoPagatore("123/A-bis");
        pagatore.setCapPagatore("00100");
        pagatore.setLocalitaPagatore("Localita' Con Un Nome Lunghissimo Da Superare Il Limite Di Settanta");
        pagatore.setProvinciaPagatore("RM");
        rt.setSoggettoPagatore(pagatore);

        CtDatiVersamentoRT dati = new CtDatiVersamentoRT();
        dati.setCodiceEsitoPagamento("0");
        dati.setImportoTotalePagato(new BigDecimal("10.00"));
        dati.setIdentificativoUnivocoVersamento("IUV900000099");
        dati.setCodiceContestoPagamento("CCP900000099");
        rt.setDatiPagamento(dati);

        Dominio d = Dominio.builder()
                .id(1L)
                .codDominio("12345678901")
                .ragioneSociale("Comune di Test")
                .build();
        Versamento v = Versamento.builder().dominio(d).build();

        return Rpt.builder()
                .versamento(v)
                .iuv("IUV900000099")
                .ccp("CCP900000099")
                .codDominio("12345678901")
                .versione("SANP_230")
                .codEsitoPagamento(0)
                .importoTotalePagato(10.00)
                .xmlRt(marshal(RT.class, rt))
                .build();
    }

    // ---------- ctReceipt (SANP_240) ----------

    /** La fixture non porta {@code paymentDateTime}/{@code applicationDate}: "N/D", non un valore dal DB. */
    @Test
    void ctReceiptSenzaDateNellaRtNonRipiegaSulleColonneDbMaMostraNd() throws IOException {
        Rpt rpt = rptCtReceipt();

        Receipt receipt = mapper.toReceipt(rpt);

        assertThat(receipt.getOperationDate()).isEqualTo("N/D");
        assertThat(receipt.getApplicationDate()).isEqualTo("N/D");
    }

    @Test
    void ctReceiptUsaIlDebtorDellaRtNonIlPayerNeIlDebitoreCorrente() throws IOException {
        Rpt rpt = rptCtReceipt();

        Receipt receipt = mapper.toReceipt(rpt);

        assertThat(receipt.getPayer().getFullName()).isEqualTo("Mario Rossi");
        assertThat(receipt.getPayer().getFiscalCode()).isEqualTo("RSSMRA80A01H501U");
    }

    @Test
    void ctReceiptUsaLeVociDellaRtConReceiptIdComeIur() throws IOException {
        Rpt rpt = rptCtReceipt();

        Receipt receipt = mapper.toReceipt(rpt);

        assertThat(receipt.getItems()).hasSize(1);
        ReceiptItem item = receipt.getItems().get(0);
        assertThat(item.getIur()).isEqualTo("RT900000003");
        assertThat(item.getDescription()).isEqualTo("TARI 2026 saldo");
        assertThat(item.getAmount()).isEqualTo(10.00);
        assertThat(item.getStatus()).isEqualTo(ReceiptItemStatus.EXECUTED);
    }

    private static Rpt rptCtReceipt() throws IOException {
        Dominio d = Dominio.builder()
                .id(1L)
                .codDominio("12345678901")
                .ragioneSociale("Comune di Prova")
                .build();

        Versamento v = Versamento.builder()
                .dominio(d)
                .causaleVersamento("Causale versamento corrente")
                .debitoreAnagrafica("Debitore attuale, diverso dal debtor della RT")
                .build();

        return Rpt.builder()
                .versamento(v)
                .iuv("IUV900000003")
                .ccp("RT900000003")
                .codDominio("12345678901")
                .versione("SANP_240")
                .codEsitoPagamento(0)
                .importoTotalePagato(10.00)
                .dataMsgRichiesta(LocalDateTime.of(2026, 9, 1, 0, 0, 0))
                .xmlRt(risorsa("rt-v2-ok.xml"))
                .build();
    }

    // ---------- ctReceiptV2 (SANP 3.2.1 V2) ----------

    @Test
    void ctReceiptV2LeggeDataOperazioneEApplicativaDalPaymentDateTimeEApplicationDateDellaRt()
            throws IOException {
        Rpt rpt = rptCtReceiptV2();

        Receipt receipt = mapper.toReceipt(rpt);

        // paymentDateTime = 2026-09-01T10:00:00+02:00 -> non mezzanotte, ore/minuti visibili
        assertThat(receipt.getOperationDate()).isEqualTo("01/09/2026 10:00");
        // applicationDate = 2026-09-01 -> sempre solo data
        assertThat(receipt.getApplicationDate()).isEqualTo("01/09/2026");
    }

    @Test
    void ctReceiptV2UsaIlDebtorDellaRtConIndirizzo() throws IOException {
        Rpt rpt = rptCtReceiptV2();

        Receipt receipt = mapper.toReceipt(rpt);

        assertThat(receipt.getPayer().getFullName()).isEqualTo("Mario Rossi");
        assertThat(receipt.getPayer().getAddress()).isEqualTo("Via Roma 1");
        assertThat(receipt.getPayer().getLocation()).isEqualTo("00100 Roma (RM)");
    }

    @Test
    void ctReceiptV2UsaLeVociDellaRtConReceiptIdComeIur() throws IOException {
        Rpt rpt = rptCtReceiptV2();

        Receipt receipt = mapper.toReceipt(rpt);

        assertThat(receipt.getItems()).hasSize(1);
        ReceiptItem item = receipt.getItems().get(0);
        assertThat(item.getIur()).isEqualTo("RT900000001");
        assertThat(item.getDescription()).isEqualTo("TARI 2026 saldo");
        assertThat(item.getAmount()).isEqualTo(10.00);
        assertThat(item.getStatus()).isEqualTo(ReceiptItemStatus.EXECUTED);
    }

    private static Rpt rptCtReceiptV2() throws IOException {
        Dominio d = Dominio.builder()
                .id(1L)
                .codDominio("12345678901")
                .ragioneSociale("Comune di Prova")
                .build();

        Versamento v = Versamento.builder()
                .dominio(d)
                .causaleVersamento("Causale versamento corrente")
                .debitoreAnagrafica("Debitore attuale, diverso dal debtor della RT")
                .build();

        return Rpt.builder()
                .versamento(v)
                .iuv("IUV900000001")
                .ccp("RT900000001")
                .codDominio("12345678901")
                .versione("SANP_321_V2")
                .codEsitoPagamento(0)
                .importoTotalePagato(10.00)
                .dataMsgRichiesta(LocalDateTime.of(2026, 9, 1, 0, 0, 0))
                .xmlRt(risorsa("rt-v2_2-ok.xml"))
                .build();
    }

    // ---------- helpers ----------

    private static byte[] marshal(Class<?> type, Object value) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(type);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        context.createMarshaller().marshal(value, baos);
        return baos.toByteArray();
    }

    private static byte[] risorsa(String nome) throws IOException {
        try (InputStream in = RicevutaPdfPayloadMapperTest.class.getClassLoader().getResourceAsStream(nome)) {
            return in.readAllBytes();
        }
    }
}
