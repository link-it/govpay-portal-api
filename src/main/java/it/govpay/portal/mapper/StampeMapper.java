package it.govpay.portal.mapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import it.govpay.portal.config.GovPayStampeClientConfig;
import it.govpay.portal.repository.DominioLogoRepository;
import it.govpay.portal.entity.CausaleUtils;
import it.govpay.portal.entity.Dominio;
import it.govpay.portal.entity.Rpt;
import it.govpay.portal.entity.SingoloVersamento;
import it.govpay.portal.entity.StatoVersamento;
import it.govpay.portal.entity.Versamento;
import it.govpay.portal.model.LinguaSecondaria;
import it.govpay.portal.util.IuvUtils;
import it.govpay.stampe.client.model.Amount;
import it.govpay.stampe.client.model.Creditor;
import it.govpay.stampe.client.model.Debtor;
import it.govpay.stampe.client.model.Languages;
import it.govpay.stampe.client.model.NoticeMetadataSecondLanguage;
import it.govpay.stampe.client.model.Payer;
import it.govpay.stampe.client.model.PaymentNotice;
import it.govpay.stampe.client.model.Receipt;
import it.govpay.stampe.client.model.ReceiptItem;
import it.govpay.stampe.client.model.ReceiptItemStatus;
import it.govpay.stampe.client.model.ReceiptOrganization;
import it.govpay.stampe.client.model.ReceiptStatus;
import it.govpay.stampe.client.model.ReceiptVersion;

@Component
public class StampeMapper {

    private static final Logger log = LoggerFactory.getLogger(StampeMapper.class);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final GovPayStampeClientConfig stampeConfig;
    private final DominioLogoRepository dominioLogoRepository;

    public StampeMapper(GovPayStampeClientConfig stampeConfig, DominioLogoRepository dominioLogoRepository) {
        this.stampeConfig = stampeConfig;
        this.dominioLogoRepository = dominioLogoRepository;
    }

    /**
     * Converte un Versamento in un PaymentNotice per la generazione del PDF avviso.
     */
    public PaymentNotice toPaymentNotice(Versamento versamento, LinguaSecondaria linguaSecondaria) {
        PaymentNotice notice = new PaymentNotice();
        Dominio dominio = versamento.getDominio();

        // Lingua principale
        notice.setLanguage(Languages.IT);
        notice.setTitle("Avviso di pagamento");

        // Lingua secondaria (se richiesta)
        if (linguaSecondaria != null) {
            NoticeMetadataSecondLanguage secondLang = new NoticeMetadataSecondLanguage();
            secondLang.setBilinguism(true);
            secondLang.setLanguage(mapLinguaSecondaria(linguaSecondaria));
            secondLang.setTitle(getTitleForLanguage(linguaSecondaria));
            notice.setSecondLanguage(secondLang);
        }

        // Creditore
        Creditor creditor = new Creditor();
        creditor.setFiscalCode(dominio.getCodDominio());
        creditor.setBusinessName(dominio.getRagioneSociale());
        if (dominio.getCbill() != null && !dominio.getCbill().isBlank()) {
            creditor.setCbillCode(dominio.getCbill());
        }
        notice.setCreditor(creditor);

        // Debitore
        Debtor debtor = new Debtor();
        debtor.setFiscalCode(versamento.getDebitoreIdentificativo());
        debtor.setFullName(versamento.getDebitoreAnagrafica());
        if (versamento.getDebitoreIndirizzo() != null) {
            debtor.setAddressLine1(versamento.getDebitoreIndirizzo() +
                    (versamento.getDebitoreCivico() != null ? ", " + versamento.getDebitoreCivico() : ""));
        }
        if (versamento.getDebitoreLocalita() != null) {
            debtor.setAddressLine2((versamento.getDebitoreCap() != null ? versamento.getDebitoreCap() + " " : "") +
                    versamento.getDebitoreLocalita() +
                    (versamento.getDebitoreProvincia() != null ? " (" + versamento.getDebitoreProvincia() + ")" : ""));
        }
        notice.setDebtor(debtor);

        // Bollettino postale (per ora disabilitato)
        notice.setPostal(false);

        // Importo e dati pagamento
        Amount fullAmount = new Amount();
        fullAmount.setAmount(versamento.getImportoTotale().doubleValue());
        fullAmount.setNoticeNumber(versamento.getNumeroAvviso());
        fullAmount.setQrcode(IuvUtils.buildQrCode002(dominio.getCodDominio(), 0, 0, null,
                BigDecimal.valueOf(versamento.getImportoTotale()), versamento.getNumeroAvviso()));
        if (versamento.getDataScadenza() != null) {
            fullAmount.setDueDate(versamento.getDataScadenza().toLocalDate());
        }
        notice.setFull(fullAmount);

        // Logo del creditore
        File logoFile = createLogoFile(dominio.getCodDominio());
        notice.setFirstLogo(logoFile);

        return notice;
    }

    /**
     * Crea un file temporaneo con il logo del dominio.
     * Se il logo non è presente nel database, usa il logo di default.
     */
    private File createLogoFile(String codDominio) {
        try {
            byte[] logoBytes = dominioLogoRepository.findLogoByCodDominio(codDominio)
                    .orElseGet(this::getDefaultLogoBytes);

            File tempFile = File.createTempFile("logo_", ".png");
            tempFile.deleteOnExit();

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(logoBytes);
            }

            return tempFile;
        } catch (IOException e) {
            log.error("Errore nella creazione del file logo: {}", e.getMessage());
            throw new RuntimeException("Impossibile creare il file del logo", e);
        }
    }

    /**
     * Restituisce i bytes del logo di default (immagine placeholder).
     */
    private byte[] getDefaultLogoBytes() {
        // Logo placeholder minimo PNG 1x1 pixel trasparente
        return new byte[] {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
            0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
            0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4,
            (byte) 0x89, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41,
            0x54, 0x78, (byte) 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00,
            0x05, 0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte) 0xB4, 0x00,
            0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte) 0xAE,
            0x42, 0x60, (byte) 0x82
        };
    }

    /**
     * Converte un Versamento e la sua Rpt associata in una Receipt per la generazione del PDF ricevuta.
     */
    public Receipt toReceipt(Versamento versamento, Rpt rpt) {
        Receipt receipt = new Receipt();
        Dominio dominio = versamento.getDominio();

        // Causale
        String causale = getCausale(versamento);
        receipt.setPaymentSubject(causale != null ? causale : "Pagamento");

        // Organizzazione creditore
        ReceiptOrganization organization = new ReceiptOrganization();
        organization.setFiscalCode(dominio.getCodDominio());
        organization.setBusinessName(dominio.getRagioneSociale());
        organization.setAddress("");
        organization.setLocation("");
        receipt.setOrganization(organization);

        // Pagatore
        Payer payer = new Payer();
        payer.setFiscalCode(versamento.getDebitoreIdentificativo());
        payer.setFullName(versamento.getDebitoreAnagrafica());
        payer.setAddress(versamento.getDebitoreIndirizzo() != null ? versamento.getDebitoreIndirizzo() : "");
        payer.setLocation(buildPayerLocation(versamento));
        receipt.setPayer(payer);

        // PSP - placeholder, normalmente estratto dall'XML RT
        receipt.setPsp("N/D");

        // Importo
        receipt.setAmount(versamento.getImportoTotale().doubleValue());

        // Date
        if (versamento.getDataPagamento() != null) {
            receipt.setOperationDate(versamento.getDataPagamento().format(DATE_TIME_FORMATTER));
            receipt.setApplicationDate(versamento.getDataPagamento().format(DATE_FORMATTER));
        } else {
            receipt.setOperationDate("N/D");
            receipt.setApplicationDate("N/D");
        }

        // Stato
        receipt.setStatus(mapReceiptStatus(versamento.getStatoVersamento()));

        // IUV e ID ricevuta
        receipt.setCreditorReferenceId(versamento.getIuvVersamento() != null ? versamento.getIuvVersamento() : "");
        receipt.setReceiptId(rpt != null && rpt.getCcp() != null ? rpt.getCcp() :
                (versamento.getIdSessione() != null ? versamento.getIdSessione() : ""));

        // Versione oggetto - da RPT o default
        receipt.setObjectVersion(mapVersione(rpt));

        // Voci di pagamento
        List<ReceiptItem> items = new ArrayList<>();
        if (versamento.getSingoliVersamenti() != null) {
            for (SingoloVersamento sv : versamento.getSingoliVersamenti()) {
                ReceiptItem item = new ReceiptItem();
                item.setDescription(sv.getDescrizione() != null ? sv.getDescrizione() : "Voce di pagamento");
                item.setIur(String.valueOf(sv.getIndiceDati() != null ? sv.getIndiceDati() : 1));
                item.setAmount(sv.getImportoSingoloVersamento() != null ?
                        sv.getImportoSingoloVersamento().doubleValue() : 0.0);
                item.setStatus(ReceiptItemStatus.EXECUTED);
                items.add(item);
            }
        }
        if (items.isEmpty()) {
            // Aggiungi almeno una voce
            ReceiptItem item = new ReceiptItem();
            item.setDescription(causale != null ? causale : "Pagamento");
            item.setIur("1");
            item.setAmount(versamento.getImportoTotale().doubleValue());
            item.setStatus(ReceiptItemStatus.EXECUTED);
            items.add(item);
        }
        receipt.setItems(items);

        // Logo
        String creditorLogo = dominioLogoRepository.findLogoByCodDominio(dominio.getCodDominio())
                .map(String::new)
                .orElse("");
        receipt.setCreditorLogo(creditorLogo);
        receipt.setPagopaLogo(stampeConfig.getLogo().getPagopa());

        return receipt;
    }

    private Languages mapLinguaSecondaria(LinguaSecondaria lingua) {
        if (lingua == null || lingua == LinguaSecondaria.FALSE) return null;
        return switch (lingua) {
            case EN -> Languages.EN;
            case DE -> Languages.DE;
            case FR -> Languages.FR;
            case SL -> Languages.SL;
            case FALSE -> null;
        };
    }

    private String getTitleForLanguage(LinguaSecondaria lingua) {
        return switch (lingua) {
            case EN -> "Payment notice";
            case DE -> "Zahlungsaufforderung";
            case FR -> "Avis de paiement";
            case SL -> "Obvestilo o plačilu";
            case FALSE -> null;
        };
    }

    private String getCausale(Versamento versamento) {
        if (versamento.getCausaleVersamento() == null) {
            return null;
        }
        return CausaleUtils.getSimple(versamento.getCausaleVersamento());
    }

    private String buildPayerLocation(Versamento versamento) {
        StringBuilder sb = new StringBuilder();
        if (versamento.getDebitoreCap() != null) {
            sb.append(versamento.getDebitoreCap()).append(" ");
        }
        if (versamento.getDebitoreLocalita() != null) {
            sb.append(versamento.getDebitoreLocalita());
        }
        if (versamento.getDebitoreProvincia() != null) {
            sb.append(" (").append(versamento.getDebitoreProvincia()).append(")");
        }
        return sb.toString().trim();
    }

    private ReceiptStatus mapReceiptStatus(StatoVersamento stato) {
        if (stato == null) return ReceiptStatus.NOT_EXECUTED;
        return switch (stato) {
            case ESEGUITO, INCASSATO, ESEGUITO_ALTRO_CANALE, ESEGUITO_SENZA_RPT -> ReceiptStatus.EXECUTED;
            case PARZIALMENTE_ESEGUITO -> ReceiptStatus.PARTIALLY_EXECUTED;
            case NON_ESEGUITO, ANNULLATO, ANOMALO -> ReceiptStatus.NOT_EXECUTED;
        };
    }

    private ReceiptVersion mapVersione(Rpt rpt) {
        if (rpt == null || rpt.getVersione() == null) {
            return ReceiptVersion._240;
        }
        return switch (rpt.getVersione()) {
            case "SANP_321_V2", "RPTV1_RTV2", "RPTSANP230_RTV2" -> ReceiptVersion._240_V2;
            case "SANP_240", "RPTV2_RTV1" -> ReceiptVersion._240;
            default -> ReceiptVersion._230;
        };
    }
}
