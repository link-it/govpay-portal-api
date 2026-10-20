package it.govpay.portal.mapper;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import it.govpay.portal.repository.DominioLogoRepository;
import it.govpay.portal.entity.CausaleUtils;
import it.govpay.portal.entity.Dominio;
import it.govpay.portal.entity.Versamento;
import it.govpay.portal.model.LinguaSecondaria;
import it.govpay.portal.util.IuvUtils;
import it.govpay.stampe.client.model.Amount;
import it.govpay.stampe.client.model.Creditor;
import it.govpay.stampe.client.model.Debtor;
import it.govpay.stampe.client.model.Languages;
import it.govpay.stampe.client.model.NoticeMetadataSecondLanguage;
import it.govpay.stampe.client.model.PaymentNotice;

@Component
public class StampeMapper {

    private final DominioLogoRepository dominioLogoRepository;

    public StampeMapper(DominioLogoRepository dominioLogoRepository) {
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
        notice.setTitle(getCausale(versamento));

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
        fullAmount.setAmount(versamento.getImportoTotale());
        fullAmount.setNoticeNumber(versamento.getNumeroAvviso());
        fullAmount.setQrcode(IuvUtils.buildQrCode002(dominio.getCodDominio(), 0, 0, null,
                BigDecimal.valueOf(versamento.getImportoTotale()), versamento.getNumeroAvviso()));
        if (versamento.getDataScadenza() != null) {
            fullAmount.setDueDate(versamento.getDataScadenza().toLocalDate());
        }
        notice.setFull(fullAmount);

        // Logo del creditore
        byte[] logoBytes = dominioLogoRepository.findLogoByCodDominio(dominio.getCodDominio())
                .orElseGet(this::getDefaultLogoBytes);
        notice.setFirstLogo(logoBytes);

        return notice;
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
            return "";
        }
        return CausaleUtils.getSimple(versamento.getCausaleVersamento());
    }

}
