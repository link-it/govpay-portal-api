package it.govpay.portal.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.govpay.portal.entity.Rpt;
import it.govpay.portal.entity.Versamento;
import it.govpay.portal.mapper.StampeMapper;
import it.govpay.portal.model.LinguaSecondaria;
import it.govpay.portal.repository.RptRepository;
import it.govpay.portal.repository.VersamentoRepository;
import it.govpay.stampe.client.api.PaymentNoticeApi;
import it.govpay.stampe.client.api.ReceiptApi;
import it.govpay.stampe.client.model.PaymentNotice;
import it.govpay.stampe.client.model.Receipt;

@Service
@Transactional(readOnly = true)
public class StampeService {

    private static final Logger log = LoggerFactory.getLogger(StampeService.class);

    private final VersamentoRepository versamentoRepository;
    private final RptRepository rptRepository;
    private final PaymentNoticeApi paymentNoticeApi;
    private final ReceiptApi receiptApi;
    private final StampeMapper stampeMapper;

    public StampeService(VersamentoRepository versamentoRepository,
            RptRepository rptRepository,
            PaymentNoticeApi paymentNoticeApi,
            ReceiptApi receiptApi,
            StampeMapper stampeMapper) {
        this.versamentoRepository = versamentoRepository;
        this.rptRepository = rptRepository;
        this.paymentNoticeApi = paymentNoticeApi;
        this.receiptApi = receiptApi;
        this.stampeMapper = stampeMapper;
    }

    /**
     * Genera il PDF dell'avviso di pagamento.
     *
     * @param idDominio      identificativo del dominio
     * @param numeroAvviso   numero avviso
     * @param linguaSecondaria lingua secondaria per avviso bilingue (opzionale)
     * @return il PDF come array di byte, o empty se non trovato
     */
    public Optional<byte[]> generateAvvisoPdf(String idDominio, String numeroAvviso,
            LinguaSecondaria linguaSecondaria) {
        log.debug("Generazione PDF avviso per dominio {} e numero avviso {}", idDominio, numeroAvviso);

        return versamentoRepository.findByDominioCodDominioAndNumeroAvviso(idDominio, numeroAvviso)
                .map(versamento -> {
                    try {
                        PaymentNotice paymentNotice = stampeMapper.toPaymentNotice(versamento, linguaSecondaria);
                        byte[] pdf = paymentNoticeApi.createPaymentNotice(paymentNotice);
                        log.debug("PDF avviso generato con successo, dimensione: {} bytes", pdf.length);
                        return pdf;
                    } catch (Exception e) {
                        log.error("Errore durante la generazione del PDF avviso: {}", e.getMessage(), e);
                        return null;
                    }
                });
    }

    /**
     * Genera il PDF della ricevuta di pagamento.
     *
     * @param idDominio    identificativo del dominio
     * @param numeroAvviso numero avviso
     * @return il PDF come array di byte, o empty se non trovato
     */
    public Optional<byte[]> generateRicevutaPdf(String idDominio, String numeroAvviso) {
        log.debug("Generazione PDF ricevuta per dominio {} e numero avviso {}", idDominio, numeroAvviso);

        return versamentoRepository.findByDominioCodDominioAndNumeroAvviso(idDominio, numeroAvviso)
                .map(versamento -> {
                    try {
                        // Recupera l'RPT più recente associata al versamento (può essere null)
                        Rpt rpt = rptRepository.findFirstByVersamentoIdOrderByDataMsgRicevutaDesc(versamento.getId())
                                .orElse(null);
                        Receipt receipt = stampeMapper.toReceipt(versamento, rpt);
                        byte[] pdf = receiptApi.createReceipt(receipt);
                        log.debug("PDF ricevuta generato con successo, dimensione: {} bytes", pdf.length);
                        return pdf;
                    } catch (Exception e) {
                        log.error("Errore durante la generazione del PDF ricevuta: {}", e.getMessage(), e);
                        return null;
                    }
                });
    }
}
