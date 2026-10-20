package it.govpay.portal.ricevuta;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import it.gov.digitpa.schemas._2011.pagamenti.CtDatiSingoloPagamentoRT;
import it.gov.digitpa.schemas._2011.pagamenti.CtSoggettoPagatore;
import it.gov.digitpa.schemas._2011.pagamenti.RPT;
import it.gov.digitpa.schemas._2011.pagamenti.RT;
import it.gov.pagopa.pagopa_api.pa.pafornode.CtReceipt;
import it.gov.pagopa.pagopa_api.pa.pafornode.CtReceiptV2;
import it.gov.pagopa.pagopa_api.pa.pafornode.CtSubject;
import it.gov.pagopa.pagopa_api.pa.pafornode.CtTransferPA;
import it.gov.pagopa.pagopa_api.pa.pafornode.CtTransferPAReceiptV2;
import it.govpay.portal.config.GovPayStampeClientConfig;
import it.govpay.portal.dominio.DominioLogoCodec;
import it.govpay.portal.entity.CausaleUtils;
import it.govpay.portal.entity.Dominio;
import it.govpay.portal.entity.Rpt;
import it.govpay.portal.entity.Uo;
import it.govpay.portal.entity.Versamento;
import it.govpay.portal.repository.DominioLogoRepository;
import it.govpay.portal.repository.UoRepository;
import it.govpay.portal.ricevuta.pagopa.JaxbUtils;
import it.govpay.portal.ricevuta.pagopa.RptRtConversionException;
import it.govpay.stampe.client.model.Payer;
import it.govpay.stampe.client.model.Receipt;
import it.govpay.stampe.client.model.ReceiptItem;
import it.govpay.stampe.client.model.ReceiptItemStatus;
import it.govpay.stampe.client.model.ReceiptOrganization;
import it.govpay.stampe.client.model.ReceiptStatus;
import it.govpay.stampe.client.model.ReceiptVersion;
import jakarta.xml.bind.JAXBException;

/**
 * Mapper {@link Rpt} → {@link Receipt} per la generazione PDF della ricevuta tramite
 * microservizio {@code govpay-stampe}.
 *
 * <p>Allineato a V1 ({@code RicevutaTelematica.java}, {@code RicevutaTelematicaPdf}) e a
 * {@code govpay-console-api}: data applicativa, data operazione, soggetto pagatore e voci
 * sono letti dall'XML della RT (e, solo per {@code dataOperazione} in SANP_230, dall'XML
 * della RPT originale) — non dai dati correnti della pendenza: sono gli stessi
 * identificativi/importi/anagrafiche che pagoPA ha certificato al momento del pagamento,
 * coerenti con quanto stampato dal legacy, e non cambiano se la pendenza viene modificata
 * in seguito.
 *
 * <p>{@code organization.address} / {@code location}: come in V1, letti dall'unità operativa
 * con {@code cod_uo='EC'} del dominio. Se l'UO EC non è censita, stringhe vuote.
 *
 * <p>{@code psp}: {@code denominazioneAttestante} se presente, altrimenti {@code codPsp},
 * altrimenti {@code "N/D"} — colonne denormalizzate al momento dell'acquisizione della RT,
 * non dati correnti della pendenza, quindi già coerenti con la RT storica.
 */
@Component
public class RicevutaPdfPayloadMapper {

    private static final DateTimeFormatter DATE_DDMMYYYY =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_DDMMYYYY_HHMM =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final String COD_UO_ENTE_CREDITORE = "EC";
    private static final String DESCRIZIONE_VOCE_DEFAULT = "Voce di pagamento";

    /** {@code ReceiptOrganization.address}/{@code .location}: {@code maxLength: 70} nel contratto stampe. */
    private static final int MAX_NOME_INDIRIZZO = 70;
    /** {@code Receipt.payment_subject}: {@code maxLength: 140} nel contratto stampe. */
    private static final int MAX_OGGETTO = 140;

    private final UoRepository uoRepository;
    private final DominioLogoRepository dominioLogoRepository;
    private final GovPayStampeClientConfig stampeConfig;

    public RicevutaPdfPayloadMapper(UoRepository uoRepository, DominioLogoRepository dominioLogoRepository,
            GovPayStampeClientConfig stampeConfig) {
        this.uoRepository = uoRepository;
        this.dominioLogoRepository = dominioLogoRepository;
        this.stampeConfig = stampeConfig;
    }

    public Receipt toReceipt(Rpt rpt) {
        Versamento v = rpt.getVersamento();
        Dominio d = v != null ? v.getDominio() : null;
        DatiStoriciRicevuta dati = datiStoriciDi(rpt);

        Receipt receipt = new Receipt();
        receipt.setCreditorLogo(creditorLogoOf(d));
        receipt.setPagopaLogo(stampeConfig.getLogo().getPagopa());
        receipt.setPaymentSubject(tronca(v != null ? causaleOf(v) : "", MAX_OGGETTO));
        receipt.setOrganization(mapOrganization(d));
        receipt.setPayer(dati.payer());
        receipt.setPsp(pspOf(rpt));
        receipt.setAmount(rpt.getImportoTotalePagato() != null ? rpt.getImportoTotalePagato() : 0.0);
        receipt.setOperationDate(dati.operationDate());
        receipt.setApplicationDate(dati.applicationDate());
        receipt.setStatus(mapStatus(rpt.getCodEsitoPagamento()));
        receipt.setCreditorReferenceId(rpt.getIuv());
        receipt.setReceiptId(rpt.getCcp());
        receipt.setObjectVersion(mapVersione(rpt.getVersione()));
        receipt.setItems(dati.items());
        return receipt;
    }

    private static String causaleOf(Versamento v) {
        String causale = CausaleUtils.getSimple(v.getCausaleVersamento());
        return causale != null ? causale : "";
    }

    private String creditorLogoOf(Dominio d) {
        if (d == null) {
            return "";
        }
        return dominioLogoRepository.findLogoByCodDominio(d.getCodDominio())
                .map(DominioLogoCodec::toDataUri)
                .orElse("");
    }

    private ReceiptOrganization mapOrganization(Dominio d) {
        ReceiptOrganization o = new ReceiptOrganization();
        o.setFiscalCode(d != null ? d.getCodDominio() : "");
        o.setBusinessName(d != null ? d.getRagioneSociale() : "");
        Uo enteCreditore = d != null
                ? uoRepository.findByDominioIdAndCodUo(d.getId(), COD_UO_ENTE_CREDITORE).orElse(null)
                : null;
        if (enteCreditore != null) {
            o.setAddress(tronca(buildAddressLine(enteCreditore.getUoIndirizzo(), enteCreditore.getUoCivico()),
                    MAX_NOME_INDIRIZZO));
            o.setLocation(tronca(buildLocationLine(enteCreditore.getUoCap(), enteCreditore.getUoLocalita(),
                    enteCreditore.getUoProvincia()), MAX_NOME_INDIRIZZO));
        } else {
            o.setAddress("");
            o.setLocation("");
        }
        return o;
    }

    private static String pspOf(Rpt rpt) {
        if (StringUtils.hasText(rpt.getDenominazioneAttestante())) {
            return rpt.getDenominazioneAttestante();
        }
        if (StringUtils.hasText(rpt.getCodPsp())) {
            return rpt.getCodPsp();
        }
        return "N/D";
    }

    /**
     * {@code dataOperazione}/{@code dataApplicativa}: {@code "dd/MM/yyyy HH:mm"}, salvo
     * mezzanotte esatta (solo data) per la prima, sempre e solo data per la seconda — stessa
     * convenzione di {@code RicevutaTelematica.setDataOperazione}/{@code setDataApplicativa}
     * nel legacy.
     */
    private static String formatDataOperazione(LocalDateTime d) {
        if (d == null) {
            return "N/D";
        }
        boolean mezzanotteEsatta = d.getHour() == 0 && d.getMinute() == 0 && d.getSecond() == 0;
        return mezzanotteEsatta ? d.format(DATE_DDMMYYYY) : d.format(DATETIME_DDMMYYYY_HHMM);
    }

    private static String formatDataApplicativa(LocalDate d) {
        return d != null ? d.format(DATE_DDMMYYYY) : "N/D";
    }

    private static String formatDataApplicativa(LocalDateTime d) {
        return formatDataApplicativa(d != null ? d.toLocalDate() : null);
    }

    /**
     * Mapping {@code cod_esito_pagamento} (codifica AgID) → {@link ReceiptStatus}:
     * 0 → EXECUTED, 1 → NOT_EXECUTED, 2 → PARTIALLY_EXECUTED,
     * 3 → EXPIRED, 4 → PARTIALLY_EXPIRED. Default: NOT_EXECUTED.
     */
    static ReceiptStatus mapStatus(Integer codEsitoPagamento) {
        if (codEsitoPagamento == null) {
            return ReceiptStatus.NOT_EXECUTED;
        }
        return switch (codEsitoPagamento) {
            case 0 -> ReceiptStatus.EXECUTED;
            case 2 -> ReceiptStatus.PARTIALLY_EXECUTED;
            case 3 -> ReceiptStatus.EXPIRED;
            case 4 -> ReceiptStatus.PARTIALLY_EXPIRED;
            default -> ReceiptStatus.NOT_EXECUTED;
        };
    }

    /** Stringa SANP V1 → enum {@link ReceiptVersion} del client microservizio. */
    static ReceiptVersion mapVersione(String versione) {
        if (versione == null) {
            return ReceiptVersion.SANP_240;
        }
        return switch (versione) {
            case "SANP_321_V2", "RPTV1_RTV2", "RPTSANP230_RTV2" -> ReceiptVersion.SANP_240_V2;
            case "SANP_240", "RPTV2_RTV1" -> ReceiptVersion.SANP_240;
            default -> ReceiptVersion.SANP_230;
        };
    }

    /** Dati letti dall'XML storico (RT, e per SANP_230 anche RPT), non dalla pendenza corrente. */
    private record DatiStoriciRicevuta(String operationDate, String applicationDate, Payer payer,
            List<ReceiptItem> items) {
    }

    private DatiStoriciRicevuta datiStoriciDi(Rpt rpt) {
        String versione = rpt.getVersione();
        try {
            return switch (versione) {
                case "SANP_240", "RPTV2_RTV1" -> datiStoriciDaCtReceipt(rpt);
                case "SANP_321_V2", "RPTV1_RTV2", "RPTSANP230_RTV2" -> datiStoriciDaCtReceiptV2(rpt);
                default -> datiStoriciDaRT(rpt);
            };
        } catch (JAXBException e) {
            throw new RptRtConversionException(
                    "Errore nella conversione della RT (versione " + versione + ") per la stampa PDF", e);
        }
    }

    private DatiStoriciRicevuta datiStoriciDaRT(Rpt rpt) throws JAXBException {
        RT rt = JaxbUtils.toRT(rpt.getXmlRt());
        String operationDate = formatDataOperazione(dataOperazioneSanp230(rpt));
        String applicationDate = formatDataApplicativa(rt.getDataOraMessaggioRicevuta());
        return new DatiStoriciRicevuta(operationDate, applicationDate,
                payerDaSoggettoPagatore(rt.getSoggettoPagatore()), itemsDaRT(rt));
    }

    /**
     * Per SANP_230 il legacy legge {@code dataOperazione} dall'RPT originale
     * ({@code dataOraMessaggioRichiesta}), non dalla RT. Se {@code xml_rpt} non è disponibile
     * (transazioni acquisite in standin), ripiego sulla colonna {@code data_msg_richiesta}:
     * è la stessa fonte da cui il legacy popola quel campo XML quando costruisce l'RPT in
     * uscita, quindi equivalente anche se l'XML non è conservato.
     */
    private LocalDateTime dataOperazioneSanp230(Rpt rpt) throws JAXBException {
        if (rpt.getXmlRpt() != null) {
            RPT rpTelematico = JaxbUtils.toRPT(rpt.getXmlRpt());
            return rpTelematico.getDataOraMessaggioRichiesta();
        }
        return rpt.getDataMsgRichiesta();
    }

    private DatiStoriciRicevuta datiStoriciDaCtReceipt(Rpt rpt) throws JAXBException {
        CtReceipt receipt = JaxbUtils.toPaSendRTReqRT(rpt.getXmlRt()).getReceipt();
        String operationDate = formatDataOperazione(receipt.getPaymentDateTime());
        String applicationDate = formatDataApplicativa(receipt.getApplicationDate());
        return new DatiStoriciRicevuta(operationDate, applicationDate,
                payerDaSubject(receipt.getDebtor()), itemsDaCtReceipt(receipt));
    }

    private DatiStoriciRicevuta datiStoriciDaCtReceiptV2(Rpt rpt) throws JAXBException {
        CtReceiptV2 receipt = JaxbUtils.toPaSendRTV2RequestRT(rpt.getXmlRt()).getReceipt();
        String operationDate = formatDataOperazione(receipt.getPaymentDateTime());
        String applicationDate = formatDataApplicativa(receipt.getApplicationDate());
        return new DatiStoriciRicevuta(operationDate, applicationDate,
                payerDaSubject(receipt.getDebtor()), itemsDaCtReceiptV2(receipt));
    }

    private static Payer payerDaSoggettoPagatore(CtSoggettoPagatore soggetto) {
        Payer p = new Payer();
        if (soggetto == null) {
            p.setFiscalCode("");
            p.setFullName("");
            p.setAddress("");
            p.setLocation("");
            return p;
        }
        String cf = soggetto.getIdentificativoUnivocoPagatore() != null
                ? soggetto.getIdentificativoUnivocoPagatore().getCodiceIdentificativoUnivoco() : null;
        p.setFiscalCode(cf != null ? cf : "");
        p.setFullName(soggetto.getAnagraficaPagatore() != null ? soggetto.getAnagraficaPagatore() : "");
        p.setAddress(tronca(buildAddressLine(soggetto.getIndirizzoPagatore(), soggetto.getCivicoPagatore()),
                MAX_NOME_INDIRIZZO));
        p.setLocation(tronca(buildLocationLine(soggetto.getCapPagatore(), soggetto.getLocalitaPagatore(),
                soggetto.getProvinciaPagatore()), MAX_NOME_INDIRIZZO));
        return p;
    }

    /** {@code debtor}, non {@code payer}: il legacy stampa sempre il debitore come "soggetto pagatore". */
    private static Payer payerDaSubject(CtSubject soggetto) {
        Payer p = new Payer();
        if (soggetto == null) {
            p.setFiscalCode("");
            p.setFullName("");
            p.setAddress("");
            p.setLocation("");
            return p;
        }
        String cf = soggetto.getUniqueIdentifier() != null
                ? soggetto.getUniqueIdentifier().getEntityUniqueIdentifierValue() : null;
        p.setFiscalCode(cf != null ? cf : "");
        p.setFullName(soggetto.getFullName() != null ? soggetto.getFullName() : "");
        p.setAddress(tronca(buildAddressLine(soggetto.getStreetName(), soggetto.getCivicNumber()),
                MAX_NOME_INDIRIZZO));
        p.setLocation(tronca(buildLocationLine(soggetto.getPostalCode(), soggetto.getCity(),
                soggetto.getStateProvinceRegion()), MAX_NOME_INDIRIZZO));
        return p;
    }

    private static String buildAddressLine(String indirizzo, String civico) {
        if (!StringUtils.hasText(indirizzo)) {
            return "";
        }
        StringBuilder sb = new StringBuilder(indirizzo);
        if (StringUtils.hasText(civico)) {
            sb.append(", ").append(civico);
        }
        return sb.toString();
    }

    /**
     * Le colonne sorgente (indirizzo UO, CAP, localita') sono spesso piu' larghe del limite
     * del contratto stampe: troncare un campo di sola resa grafica stampa una ricevuta con
     * l'intestazione accorciata, non troncarlo farebbe rifiutare l'intero payload con un 400.
     */
    private static String tronca(String value, int max) {
        if (value == null || value.length() <= max) {
            return value;
        }
        return value.substring(0, max);
    }

    private static String buildLocationLine(String cap, String localita, String provincia) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(cap)) {
            sb.append(cap).append(' ');
        }
        if (StringUtils.hasText(localita)) {
            sb.append(localita);
        }
        if (StringUtils.hasText(provincia)) {
            sb.append(" (").append(provincia).append(')');
        }
        return sb.toString().trim();
    }

    /**
     * Stato per singola voce: importo zero → {@code NOT_EXECUTED}, altrimenti
     * {@code EXECUTED} — stesso criterio del legacy (indipendente dall'esito complessivo),
     * non un unico stato ripetuto su tutte le voci.
     */
    private static ReceiptItemStatus statusDaImporto(double importo) {
        return importo == 0.0 ? ReceiptItemStatus.NOT_EXECUTED : ReceiptItemStatus.EXECUTED;
    }

    private static List<ReceiptItem> itemsDaRT(RT rt) {
        List<ReceiptItem> items = new ArrayList<>();
        List<CtDatiSingoloPagamentoRT> voci = rt.getDatiPagamento() != null
                ? rt.getDatiPagamento().getDatiSingoloPagamentos() : null;
        if (voci == null) {
            return items;
        }
        for (CtDatiSingoloPagamentoRT voce : voci) {
            double importo = voce.getSingoloImportoPagato() != null
                    ? voce.getSingoloImportoPagato().doubleValue() : 0.0;
            ReceiptItem item = new ReceiptItem();
            item.setDescription(voce.getCausaleVersamento() != null
                    ? voce.getCausaleVersamento() : DESCRIZIONE_VOCE_DEFAULT);
            item.setIur(voce.getIdentificativoUnivocoRiscossione());
            item.setAmount(importo);
            item.setStatus(statusDaImporto(importo));
            items.add(item);
        }
        return items;
    }

    /** {@code receiptId} come IUR per tutte le voci: stesso appiattimento del legacy per questo formato. */
    private static List<ReceiptItem> itemsDaCtReceipt(CtReceipt receipt) {
        List<ReceiptItem> items = new ArrayList<>();
        if (receipt.getTransferList() == null) {
            return items;
        }
        for (CtTransferPA transfer : receipt.getTransferList().getTransfers()) {
            double importo = transfer.getTransferAmount() != null
                    ? transfer.getTransferAmount().doubleValue() : 0.0;
            ReceiptItem item = new ReceiptItem();
            item.setDescription(transfer.getRemittanceInformation() != null
                    ? transfer.getRemittanceInformation() : DESCRIZIONE_VOCE_DEFAULT);
            item.setIur(receipt.getReceiptId());
            item.setAmount(importo);
            item.setStatus(statusDaImporto(importo));
            items.add(item);
        }
        return items;
    }

    private static List<ReceiptItem> itemsDaCtReceiptV2(CtReceiptV2 receipt) {
        List<ReceiptItem> items = new ArrayList<>();
        if (receipt.getTransferList() == null) {
            return items;
        }
        for (CtTransferPAReceiptV2 transfer : receipt.getTransferList().getTransfers()) {
            double importo = transfer.getTransferAmount() != null
                    ? transfer.getTransferAmount().doubleValue() : 0.0;
            ReceiptItem item = new ReceiptItem();
            item.setDescription(transfer.getRemittanceInformation() != null
                    ? transfer.getRemittanceInformation() : DESCRIZIONE_VOCE_DEFAULT);
            item.setIur(receipt.getReceiptId());
            item.setAmount(importo);
            item.setStatus(statusDaImporto(importo));
            items.add(item);
        }
        return items;
    }
}
