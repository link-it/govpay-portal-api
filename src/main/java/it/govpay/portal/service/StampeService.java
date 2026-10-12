package it.govpay.portal.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

import it.govpay.common.metrics.ExternalCallMetricsRecorder;
import it.govpay.portal.exception.BadGatewayException;
import it.govpay.portal.mapper.StampeMapper;
import it.govpay.portal.model.LinguaSecondaria;
import it.govpay.portal.repository.RptRepository;
import it.govpay.portal.repository.VersamentoRepository;
import it.govpay.portal.ricevuta.RicevutaPdfPayloadMapper;
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
    private final RicevutaPdfPayloadMapper ricevutaPdfPayloadMapper;
    private final ExternalCallMetricsRecorder externalCallMetricsRecorder;

    public StampeService(VersamentoRepository versamentoRepository,
            RptRepository rptRepository,
            PaymentNoticeApi paymentNoticeApi,
            ReceiptApi receiptApi,
            StampeMapper stampeMapper,
            RicevutaPdfPayloadMapper ricevutaPdfPayloadMapper,
            ExternalCallMetricsRecorder externalCallMetricsRecorder) {
        this.versamentoRepository = versamentoRepository;
        this.rptRepository = rptRepository;
        this.paymentNoticeApi = paymentNoticeApi;
        this.receiptApi = receiptApi;
        this.stampeMapper = stampeMapper;
        this.ricevutaPdfPayloadMapper = ricevutaPdfPayloadMapper;
        this.externalCallMetricsRecorder = externalCallMetricsRecorder;
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
                        byte[][] holder = new byte[1][];
                        externalCallMetricsRecorder.record("stampe", "payment_notice",
                                () -> holder[0] = paymentNoticeApi.createPaymentNotice(paymentNotice));
                        byte[] pdf = holder[0];
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
     * <p>Senza una RPT associata al versamento non c'e' alcuna ricevuta storica da stampare
     * (il payload si costruisce a partire dall'XML della RT, non dai dati correnti della
     * pendenza): restituisce {@code empty} invece di propagare al mapper un {@code rpt} nullo.
     *
     * <p>Un {@code rpt} trovato invece non produce mai {@code empty}: un errore nella
     * conversione dell'XML storico ({@code RptRtConversionException}, non catturata qui) o
     * nella comunicazione con {@code govpay-stampe} ({@link BadGatewayException}) e' un
     * problema diverso dall'assenza della ricevuta e non va mascherato da un 404.
     *
     * @param idDominio    identificativo del dominio
     * @param numeroAvviso numero avviso
     * @return il PDF come array di byte, o empty se il versamento o la sua RPT non esistono
     */
    public Optional<byte[]> generateRicevutaPdf(String idDominio, String numeroAvviso) {
        log.debug("Generazione PDF ricevuta per dominio {} e numero avviso {}", idDominio, numeroAvviso);

        return versamentoRepository.findByDominioCodDominioAndNumeroAvviso(idDominio, numeroAvviso)
                .flatMap(versamento -> rptRepository
                        .findFirstByVersamentoIdAndXmlRtIsNotNullOrderByDataMsgRicevutaDesc(versamento.getId()))
                .map(rpt -> {
                    Receipt receipt = ricevutaPdfPayloadMapper.toReceipt(rpt);
                    byte[][] holder = new byte[1][];
                    try {
                        externalCallMetricsRecorder.record("stampe", "receipt",
                                () -> holder[0] = receiptApi.createReceipt(receipt));
                    } catch (HttpStatusCodeException e) {
                        log.error("Errore HTTP {} dal microservizio govpay-stampe per la ricevuta: {}",
                                e.getStatusCode(), e.getResponseBodyAsString(), e);
                        throw new BadGatewayException(
                                "Errore dal microservizio govpay-stampe: " + e.getResponseBodyAsString(), e);
                    } catch (RestClientException e) {
                        log.error("Errore di comunicazione con il microservizio govpay-stampe per la ricevuta: {}",
                                e.getMessage(), e);
                        throw new BadGatewayException(
                                "Errore di comunicazione con il microservizio govpay-stampe: " + e.getMessage(), e);
                    }
                    byte[] pdf = holder[0];
                    log.debug("PDF ricevuta generato con successo, dimensione: {} bytes", pdf.length);
                    return pdf;
                });
    }
}
