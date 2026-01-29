package it.govpay.portal.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.govpay.portal.entity.Dominio;
import it.govpay.portal.entity.Rpt;
import it.govpay.portal.entity.StatoVersamento;
import it.govpay.portal.entity.Versamento;
import it.govpay.portal.mapper.StampeMapper;
import it.govpay.portal.model.LinguaSecondaria;
import it.govpay.portal.repository.RptRepository;
import it.govpay.portal.repository.VersamentoRepository;
import it.govpay.stampe.client.api.PaymentNoticeApi;
import it.govpay.stampe.client.api.ReceiptApi;
import it.govpay.stampe.client.model.PaymentNotice;
import it.govpay.stampe.client.model.Receipt;

@ExtendWith(MockitoExtension.class)
class StampeServiceTest {

    @Mock
    private VersamentoRepository versamentoRepository;

    @Mock
    private RptRepository rptRepository;

    @Mock
    private PaymentNoticeApi paymentNoticeApi;

    @Mock
    private ReceiptApi receiptApi;

    @Mock
    private StampeMapper stampeMapper;

    @InjectMocks
    private StampeService stampeService;

    private Dominio dominio;
    private Versamento versamento;
    private Rpt rpt;
    private PaymentNotice paymentNotice;
    private Receipt receipt;

    @BeforeEach
    void setUp() {
        dominio = Dominio.builder()
                .id(1L)
                .codDominio("12345678901")
                .ragioneSociale("Comune di Test")
                .build();

        versamento = Versamento.builder()
                .id(1L)
                .dominio(dominio)
                .numeroAvviso("123456789012345678")
                .iuvVersamento("01234567890123456")
                .importoTotale(150.50)
                .statoVersamento(StatoVersamento.ESEGUITO)
                .build();

        rpt = Rpt.builder()
                .id(1L)
                .versamento(versamento)
                .iuv("01234567890123456")
                .ccp("CCP001")
                .codDominio("12345678901")
                .versione("SANP_240")
                .dataMsgRicevuta(LocalDateTime.now())
                .build();

        paymentNotice = new PaymentNotice();
        receipt = new Receipt();
    }

    @Nested
    @DisplayName("generateAvvisoPdf Tests")
    class GenerateAvvisoPdfTests {

        @Test
        @DisplayName("Dovrebbe generare PDF avviso con successo")
        void shouldGenerateAvvisoPdfSuccessfully() {
            byte[] pdfContent = "PDF content".getBytes();

            when(versamentoRepository.findByDominioCodDominioAndNumeroAvviso("12345678901", "123456789012345678"))
                    .thenReturn(Optional.of(versamento));
            when(stampeMapper.toPaymentNotice(eq(versamento), any()))
                    .thenReturn(paymentNotice);
            when(paymentNoticeApi.createPaymentNotice(paymentNotice))
                    .thenReturn(pdfContent);

            Optional<byte[]> result = stampeService.generateAvvisoPdf("12345678901", "123456789012345678", null);

            assertTrue(result.isPresent());
            assertArrayEquals(pdfContent, result.get());

            verify(versamentoRepository).findByDominioCodDominioAndNumeroAvviso("12345678901", "123456789012345678");
            verify(stampeMapper).toPaymentNotice(versamento, null);
            verify(paymentNoticeApi).createPaymentNotice(paymentNotice);
        }

        @Test
        @DisplayName("Dovrebbe generare PDF avviso con lingua secondaria")
        void shouldGenerateAvvisoPdfWithSecondLanguage() {
            byte[] pdfContent = "Bilingual PDF".getBytes();

            when(versamentoRepository.findByDominioCodDominioAndNumeroAvviso("12345678901", "123456789012345678"))
                    .thenReturn(Optional.of(versamento));
            when(stampeMapper.toPaymentNotice(versamento, LinguaSecondaria.EN))
                    .thenReturn(paymentNotice);
            when(paymentNoticeApi.createPaymentNotice(paymentNotice))
                    .thenReturn(pdfContent);

            Optional<byte[]> result = stampeService.generateAvvisoPdf("12345678901", "123456789012345678", LinguaSecondaria.EN);

            assertTrue(result.isPresent());
            verify(stampeMapper).toPaymentNotice(versamento, LinguaSecondaria.EN);
        }

        @Test
        @DisplayName("Dovrebbe restituire empty quando versamento non trovato")
        void shouldReturnEmptyWhenVersamentoNotFound() {
            when(versamentoRepository.findByDominioCodDominioAndNumeroAvviso("12345678901", "999999999999999999"))
                    .thenReturn(Optional.empty());

            Optional<byte[]> result = stampeService.generateAvvisoPdf("12345678901", "999999999999999999", null);

            assertTrue(result.isEmpty());
            verify(paymentNoticeApi, never()).createPaymentNotice(any());
        }

        @Test
        @DisplayName("Dovrebbe restituire empty quando si verifica un errore nella generazione")
        void shouldReturnEmptyWhenGenerationFails() {
            when(versamentoRepository.findByDominioCodDominioAndNumeroAvviso("12345678901", "123456789012345678"))
                    .thenReturn(Optional.of(versamento));
            when(stampeMapper.toPaymentNotice(eq(versamento), any()))
                    .thenReturn(paymentNotice);
            when(paymentNoticeApi.createPaymentNotice(paymentNotice))
                    .thenThrow(new RuntimeException("API Error"));

            Optional<byte[]> result = stampeService.generateAvvisoPdf("12345678901", "123456789012345678", null);

            // Optional.map() con funzione che ritorna null produce Optional.empty()
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("generateRicevutaPdf Tests")
    class GenerateRicevutaPdfTests {

        @Test
        @DisplayName("Dovrebbe generare PDF ricevuta con successo con RPT")
        void shouldGenerateRicevutaPdfSuccessfullyWithRpt() {
            byte[] pdfContent = "Receipt PDF".getBytes();

            when(versamentoRepository.findByDominioCodDominioAndNumeroAvviso("12345678901", "123456789012345678"))
                    .thenReturn(Optional.of(versamento));
            when(rptRepository.findFirstByVersamentoIdOrderByDataMsgRicevutaDesc(1L))
                    .thenReturn(Optional.of(rpt));
            when(stampeMapper.toReceipt(versamento, rpt))
                    .thenReturn(receipt);
            when(receiptApi.createReceipt(receipt))
                    .thenReturn(pdfContent);

            Optional<byte[]> result = stampeService.generateRicevutaPdf("12345678901", "123456789012345678");

            assertTrue(result.isPresent());
            assertArrayEquals(pdfContent, result.get());

            verify(rptRepository).findFirstByVersamentoIdOrderByDataMsgRicevutaDesc(1L);
            verify(stampeMapper).toReceipt(versamento, rpt);
            verify(receiptApi).createReceipt(receipt);
        }

        @Test
        @DisplayName("Dovrebbe generare PDF ricevuta senza RPT")
        void shouldGenerateRicevutaPdfWithoutRpt() {
            byte[] pdfContent = "Receipt PDF without RPT".getBytes();

            when(versamentoRepository.findByDominioCodDominioAndNumeroAvviso("12345678901", "123456789012345678"))
                    .thenReturn(Optional.of(versamento));
            when(rptRepository.findFirstByVersamentoIdOrderByDataMsgRicevutaDesc(1L))
                    .thenReturn(Optional.empty());
            when(stampeMapper.toReceipt(versamento, null))
                    .thenReturn(receipt);
            when(receiptApi.createReceipt(receipt))
                    .thenReturn(pdfContent);

            Optional<byte[]> result = stampeService.generateRicevutaPdf("12345678901", "123456789012345678");

            assertTrue(result.isPresent());
            verify(stampeMapper).toReceipt(versamento, null);
        }

        @Test
        @DisplayName("Dovrebbe restituire RPT più recente quando ce ne sono multiple")
        void shouldReturnMostRecentRptWhenMultipleExist() {
            byte[] pdfContent = "Receipt PDF".getBytes();

            Rpt recentRpt = Rpt.builder()
                    .id(2L)
                    .versamento(versamento)
                    .iuv("01234567890123456")
                    .ccp("CCP002")
                    .codDominio("12345678901")
                    .versione("SANP_321_V2")
                    .dataMsgRicevuta(LocalDateTime.now())
                    .build();

            when(versamentoRepository.findByDominioCodDominioAndNumeroAvviso("12345678901", "123456789012345678"))
                    .thenReturn(Optional.of(versamento));
            when(rptRepository.findFirstByVersamentoIdOrderByDataMsgRicevutaDesc(1L))
                    .thenReturn(Optional.of(recentRpt));
            when(stampeMapper.toReceipt(versamento, recentRpt))
                    .thenReturn(receipt);
            when(receiptApi.createReceipt(receipt))
                    .thenReturn(pdfContent);

            Optional<byte[]> result = stampeService.generateRicevutaPdf("12345678901", "123456789012345678");

            assertTrue(result.isPresent());
            verify(stampeMapper).toReceipt(versamento, recentRpt);
        }

        @Test
        @DisplayName("Dovrebbe restituire empty quando versamento non trovato")
        void shouldReturnEmptyWhenVersamentoNotFound() {
            when(versamentoRepository.findByDominioCodDominioAndNumeroAvviso("12345678901", "999999999999999999"))
                    .thenReturn(Optional.empty());

            Optional<byte[]> result = stampeService.generateRicevutaPdf("12345678901", "999999999999999999");

            assertTrue(result.isEmpty());
            verify(receiptApi, never()).createReceipt(any());
        }

        @Test
        @DisplayName("Dovrebbe restituire empty quando si verifica un errore nella generazione")
        void shouldReturnEmptyWhenGenerationFails() {
            when(versamentoRepository.findByDominioCodDominioAndNumeroAvviso("12345678901", "123456789012345678"))
                    .thenReturn(Optional.of(versamento));
            when(rptRepository.findFirstByVersamentoIdOrderByDataMsgRicevutaDesc(1L))
                    .thenReturn(Optional.of(rpt));
            when(stampeMapper.toReceipt(versamento, rpt))
                    .thenReturn(receipt);
            when(receiptApi.createReceipt(receipt))
                    .thenThrow(new RuntimeException("API Error"));

            Optional<byte[]> result = stampeService.generateRicevutaPdf("12345678901", "123456789012345678");

            // Optional.map() con funzione che ritorna null produce Optional.empty()
            assertTrue(result.isEmpty());
        }
    }
}
