package it.govpay.portal.mapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.govpay.portal.config.GovPayStampeClientConfig;
import it.govpay.portal.entity.Dominio;
import it.govpay.portal.entity.Rpt;
import it.govpay.portal.entity.SingoloVersamento;
import it.govpay.portal.entity.StatoVersamento;
import it.govpay.portal.entity.Stazione;
import it.govpay.portal.entity.Versamento;
import it.govpay.portal.model.LinguaSecondaria;
import it.govpay.portal.repository.DominioLogoRepository;
import it.govpay.stampe.client.model.Languages;
import it.govpay.stampe.client.model.PaymentNotice;
import it.govpay.stampe.client.model.Receipt;
import it.govpay.stampe.client.model.ReceiptStatus;
import it.govpay.stampe.client.model.ReceiptVersion;

@ExtendWith(MockitoExtension.class)
class StampeMapperTest {

    @Mock
    private GovPayStampeClientConfig stampeConfig;

    @Mock
    private DominioLogoRepository dominioLogoRepository;

    private StampeMapper stampeMapper;

    private Dominio dominio;
    private Stazione stazione;
    private Versamento versamento;

    @BeforeEach
    void setUp() {
        stampeMapper = new StampeMapper(stampeConfig, dominioLogoRepository);

        stazione = Stazione.builder()
                .id(1L)
                .codStazione("12345678901_01")
                .abilitato(true)
                .build();

        dominio = Dominio.builder()
                .id(1L)
                .codDominio("12345678901")
                .ragioneSociale("Comune di Test")
                .stazione(stazione)
                .cbill("ABCDE")
                .build();

        versamento = Versamento.builder()
                .id(1L)
                .dominio(dominio)
                .numeroAvviso("123456789012345678")
                .iuvVersamento("01234567890123456")
                .importoTotale(150.50)
                .debitoreIdentificativo("RSSMRA80A01H501U")
                .debitoreAnagrafica("Mario Rossi")
                .debitoreIndirizzo("Via Roma")
                .debitoreCivico("1")
                .debitoreCap("00100")
                .debitoreLocalita("Roma")
                .debitoreProvincia("RM")
                .causaleVersamento("Pagamento TARI 2024")
                .dataScadenza(LocalDateTime.of(2024, 12, 31, 23, 59))
                .statoVersamento(StatoVersamento.NON_ESEGUITO)
                .singoliVersamenti(new ArrayList<>())
                .build();
    }

    @Nested
    @DisplayName("toPaymentNotice Tests")
    class ToPaymentNoticeTests {

        @Test
        @DisplayName("Dovrebbe creare PaymentNotice con tutti i dati completi")
        void shouldCreatePaymentNoticeWithCompleteData() {
            PaymentNotice notice = stampeMapper.toPaymentNotice(versamento, null);

            assertNotNull(notice);
            assertEquals(Languages.IT, notice.getLanguage());
            assertEquals("Avviso di pagamento", notice.getTitle());

            // Creditor
            assertNotNull(notice.getCreditor());
            assertEquals("12345678901", notice.getCreditor().getFiscalCode());
            assertEquals("Comune di Test", notice.getCreditor().getBusinessName());
            assertEquals("ABCDE", notice.getCreditor().getCbillCode());

            // Debtor
            assertNotNull(notice.getDebtor());
            assertEquals("RSSMRA80A01H501U", notice.getDebtor().getFiscalCode());
            assertEquals("Mario Rossi", notice.getDebtor().getFullName());
            assertEquals("Via Roma, 1", notice.getDebtor().getAddressLine1());
            assertEquals("00100 Roma (RM)", notice.getDebtor().getAddressLine2());

            // Full amount
            assertNotNull(notice.getFull());
            assertEquals(150.50, notice.getFull().getAmount());
            assertEquals("123456789012345678", notice.getFull().getNoticeNumber());
            assertNotNull(notice.getFull().getQrcode());
            assertNotNull(notice.getFull().getDueDate());

            // Postal disabled
            assertFalse(notice.getPostal());
        }

        @Test
        @DisplayName("Dovrebbe creare PaymentNotice bilingue italiano-inglese")
        void shouldCreateBilingualPaymentNoticeItalianEnglish() {
            PaymentNotice notice = stampeMapper.toPaymentNotice(versamento, LinguaSecondaria.EN);

            assertNotNull(notice);
            assertEquals(Languages.IT, notice.getLanguage());
            assertNotNull(notice.getSecondLanguage());
            assertTrue(notice.getSecondLanguage().getBilinguism());
            assertEquals(Languages.EN, notice.getSecondLanguage().getLanguage());
            assertEquals("Payment notice", notice.getSecondLanguage().getTitle());
        }

        @Test
        @DisplayName("Dovrebbe creare PaymentNotice bilingue italiano-tedesco")
        void shouldCreateBilingualPaymentNoticeItalianGerman() {
            PaymentNotice notice = stampeMapper.toPaymentNotice(versamento, LinguaSecondaria.DE);

            assertNotNull(notice.getSecondLanguage());
            assertEquals(Languages.DE, notice.getSecondLanguage().getLanguage());
            assertEquals("Zahlungsaufforderung", notice.getSecondLanguage().getTitle());
        }

        @Test
        @DisplayName("Dovrebbe creare PaymentNotice bilingue italiano-francese")
        void shouldCreateBilingualPaymentNoticeItalianFrench() {
            PaymentNotice notice = stampeMapper.toPaymentNotice(versamento, LinguaSecondaria.FR);

            assertNotNull(notice.getSecondLanguage());
            assertEquals(Languages.FR, notice.getSecondLanguage().getLanguage());
            assertEquals("Avis de paiement", notice.getSecondLanguage().getTitle());
        }

        @Test
        @DisplayName("Dovrebbe creare PaymentNotice bilingue italiano-sloveno")
        void shouldCreateBilingualPaymentNoticeItalianSlovenian() {
            PaymentNotice notice = stampeMapper.toPaymentNotice(versamento, LinguaSecondaria.SL);

            assertNotNull(notice.getSecondLanguage());
            assertEquals(Languages.SL, notice.getSecondLanguage().getLanguage());
            assertEquals("Obvestilo o plačilu", notice.getSecondLanguage().getTitle());
        }

        @Test
        @DisplayName("Dovrebbe gestire LinguaSecondaria.FALSE senza lingua secondaria valida")
        void shouldHandleFalseLanguageAsNull() {
            PaymentNotice notice = stampeMapper.toPaymentNotice(versamento, LinguaSecondaria.FALSE);

            // Il mapper crea comunque l'oggetto ma con language null
            if (notice.getSecondLanguage() != null) {
                assertNull(notice.getSecondLanguage().getLanguage());
            }
        }

        @Test
        @DisplayName("Dovrebbe gestire versamento senza indirizzo debitore")
        void shouldHandleVersamentoWithoutDebtorAddress() {
            versamento.setDebitoreIndirizzo(null);
            versamento.setDebitoreCivico(null);
            versamento.setDebitoreLocalita(null);
            versamento.setDebitoreCap(null);
            versamento.setDebitoreProvincia(null);

            PaymentNotice notice = stampeMapper.toPaymentNotice(versamento, null);

            assertNotNull(notice.getDebtor());
            assertNull(notice.getDebtor().getAddressLine1());
            assertNull(notice.getDebtor().getAddressLine2());
        }

        @Test
        @DisplayName("Dovrebbe gestire versamento senza data scadenza")
        void shouldHandleVersamentoWithoutDueDate() {
            versamento.setDataScadenza(null);

            PaymentNotice notice = stampeMapper.toPaymentNotice(versamento, null);

            assertNull(notice.getFull().getDueDate());
        }

        @Test
        @DisplayName("Dovrebbe gestire dominio senza cbill")
        void shouldHandleDominioWithoutCbill() {
            dominio.setCbill(null);

            PaymentNotice notice = stampeMapper.toPaymentNotice(versamento, null);

            assertNull(notice.getCreditor().getCbillCode());
        }
    }

    @Nested
    @DisplayName("toReceipt Tests")
    class ToReceiptTests {

        private Rpt rpt;

        @BeforeEach
        void setUpReceipt() {
            GovPayStampeClientConfig.Logo logo = new GovPayStampeClientConfig.Logo();
            logo.setPagopa("base64logostring");
            when(stampeConfig.getLogo()).thenReturn(logo);
            when(dominioLogoRepository.findLogoByCodDominio("12345678901"))
                    .thenReturn(Optional.of("creditorLogoBase64".getBytes()));

            rpt = Rpt.builder()
                    .id(1L)
                    .versamento(versamento)
                    .iuv("01234567890123456")
                    .ccp("CCP001")
                    .codDominio("12345678901")
                    .versione("SANP_240")
                    .dataMsgRicevuta(LocalDateTime.of(2024, 6, 15, 10, 30))
                    .build();

            versamento.setStatoVersamento(StatoVersamento.ESEGUITO);
            versamento.setDataPagamento(LocalDateTime.of(2024, 6, 15, 10, 30));
        }

        @Test
        @DisplayName("Dovrebbe creare Receipt con tutti i dati completi")
        void shouldCreateReceiptWithCompleteData() {
            Receipt receipt = stampeMapper.toReceipt(versamento, rpt);

            assertNotNull(receipt);
            assertEquals("Pagamento TARI 2024", receipt.getPaymentSubject());

            // Organization
            assertNotNull(receipt.getOrganization());
            assertEquals("12345678901", receipt.getOrganization().getFiscalCode());
            assertEquals("Comune di Test", receipt.getOrganization().getBusinessName());

            // Payer
            assertNotNull(receipt.getPayer());
            assertEquals("RSSMRA80A01H501U", receipt.getPayer().getFiscalCode());
            assertEquals("Mario Rossi", receipt.getPayer().getFullName());

            // Amount and dates
            assertEquals(150.50, receipt.getAmount());
            assertNotNull(receipt.getOperationDate());
            assertNotNull(receipt.getApplicationDate());

            // Status
            assertEquals(ReceiptStatus.EXECUTED, receipt.getStatus());

            // IDs
            assertEquals("01234567890123456", receipt.getCreditorReferenceId());
            assertEquals("CCP001", receipt.getReceiptId());

            // Version
            assertEquals(ReceiptVersion._240, receipt.getObjectVersion());

            // Logos
            assertEquals("creditorLogoBase64", receipt.getCreditorLogo());
            assertEquals("base64logostring", receipt.getPagopaLogo());
        }

        @Test
        @DisplayName("Dovrebbe mappare stato ESEGUITO correttamente")
        void shouldMapEseguitoStatus() {
            versamento.setStatoVersamento(StatoVersamento.ESEGUITO);
            Receipt receipt = stampeMapper.toReceipt(versamento, rpt);
            assertEquals(ReceiptStatus.EXECUTED, receipt.getStatus());
        }

        @Test
        @DisplayName("Dovrebbe mappare stato INCASSATO correttamente")
        void shouldMapIncassatoStatus() {
            versamento.setStatoVersamento(StatoVersamento.INCASSATO);
            Receipt receipt = stampeMapper.toReceipt(versamento, rpt);
            assertEquals(ReceiptStatus.EXECUTED, receipt.getStatus());
        }

        @Test
        @DisplayName("Dovrebbe mappare stato PARZIALMENTE_ESEGUITO correttamente")
        void shouldMapParzialmenteEseguitoStatus() {
            versamento.setStatoVersamento(StatoVersamento.PARZIALMENTE_ESEGUITO);
            Receipt receipt = stampeMapper.toReceipt(versamento, rpt);
            assertEquals(ReceiptStatus.PARTIALLY_EXECUTED, receipt.getStatus());
        }

        @Test
        @DisplayName("Dovrebbe mappare stato NON_ESEGUITO correttamente")
        void shouldMapNonEseguitoStatus() {
            versamento.setStatoVersamento(StatoVersamento.NON_ESEGUITO);
            Receipt receipt = stampeMapper.toReceipt(versamento, rpt);
            assertEquals(ReceiptStatus.NOT_EXECUTED, receipt.getStatus());
        }

        @Test
        @DisplayName("Dovrebbe mappare stato ANNULLATO correttamente")
        void shouldMapAnnullatoStatus() {
            versamento.setStatoVersamento(StatoVersamento.ANNULLATO);
            Receipt receipt = stampeMapper.toReceipt(versamento, rpt);
            assertEquals(ReceiptStatus.NOT_EXECUTED, receipt.getStatus());
        }

        @Test
        @DisplayName("Dovrebbe mappare versione SANP_321_V2 correttamente")
        void shouldMapVersionSanp321V2() {
            rpt.setVersione("SANP_321_V2");
            Receipt receipt = stampeMapper.toReceipt(versamento, rpt);
            assertEquals(ReceiptVersion._240_V2, receipt.getObjectVersion());
        }

        @Test
        @DisplayName("Dovrebbe mappare versione sconosciuta come _230")
        void shouldMapUnknownVersionAs230() {
            rpt.setVersione("UNKNOWN_VERSION");
            Receipt receipt = stampeMapper.toReceipt(versamento, rpt);
            assertEquals(ReceiptVersion._230, receipt.getObjectVersion());
        }

        @Test
        @DisplayName("Dovrebbe creare Receipt con singoli versamenti")
        void shouldCreateReceiptWithSingoliVersamenti() {
            SingoloVersamento sv1 = SingoloVersamento.builder()
                    .id(1L)
                    .descrizione("Quota fissa TARI")
                    .importoSingoloVersamento(80.50)
                    .indiceDati(1)
                    .build();
            SingoloVersamento sv2 = SingoloVersamento.builder()
                    .id(2L)
                    .descrizione("Quota variabile TARI")
                    .importoSingoloVersamento(70.00)
                    .indiceDati(2)
                    .build();
            versamento.setSingoliVersamenti(List.of(sv1, sv2));

            Receipt receipt = stampeMapper.toReceipt(versamento, rpt);

            assertNotNull(receipt.getItems());
            assertEquals(2, receipt.getItems().size());
            assertEquals("Quota fissa TARI", receipt.getItems().get(0).getDescription());
            assertEquals(80.50, receipt.getItems().get(0).getAmount());
            assertEquals("Quota variabile TARI", receipt.getItems().get(1).getDescription());
            assertEquals(70.00, receipt.getItems().get(1).getAmount());
        }

        @Test
        @DisplayName("Dovrebbe gestire logo creditore non trovato")
        void shouldHandleMissingCreditorLogo() {
            when(dominioLogoRepository.findLogoByCodDominio("12345678901"))
                    .thenReturn(Optional.empty());

            Receipt receipt = stampeMapper.toReceipt(versamento, rpt);

            assertEquals("", receipt.getCreditorLogo());
        }

        @Test
        @DisplayName("Dovrebbe gestire versamento senza data pagamento")
        void shouldHandleVersamentoWithoutPaymentDate() {
            versamento.setDataPagamento(null);

            Receipt receipt = stampeMapper.toReceipt(versamento, rpt);

            assertEquals("N/D", receipt.getOperationDate());
            assertEquals("N/D", receipt.getApplicationDate());
        }

        @Test
        @DisplayName("Dovrebbe gestire versamento senza causale")
        void shouldHandleVersamentoWithoutCausale() {
            versamento.setCausaleVersamento(null);

            Receipt receipt = stampeMapper.toReceipt(versamento, rpt);

            assertEquals("Pagamento", receipt.getPaymentSubject());
        }
    }
}
