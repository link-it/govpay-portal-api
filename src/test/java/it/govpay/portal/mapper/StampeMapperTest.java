package it.govpay.portal.mapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
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
import it.govpay.portal.entity.StatoVersamento;
import it.govpay.portal.entity.Stazione;
import it.govpay.portal.entity.Versamento;
import it.govpay.portal.model.LinguaSecondaria;
import it.govpay.portal.repository.DominioLogoRepository;
import it.govpay.stampe.client.model.Languages;
import it.govpay.stampe.client.model.PaymentNotice;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class StampeMapperTest {

    @Mock
    private DominioLogoRepository dominioLogoRepository;

    private final GovPayStampeClientConfig stampeConfig = new GovPayStampeClientConfig();

    private StampeMapper stampeMapper;

    private Dominio dominio;
    private Stazione stazione;
    private Versamento versamento;

    @BeforeEach
    void setUp() {
        stampeMapper = new StampeMapper(dominioLogoRepository, stampeConfig);

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
            assertEquals("Pagamento TARI 2024", notice.getTitle());

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
    @DisplayName("creditor.pay_on_website (issue #32)")
    class PayOnWebsiteTests {

        @Test
        @DisplayName("Senza configurazione pay_on_website e' false")
        void payOnWebsiteFalseDiDefault() {
            PaymentNotice notice = stampeMapper.toPaymentNotice(versamento, null);

            assertEquals(Boolean.FALSE, notice.getCreditor().getPayOnWebsite());
            assertFalse(json(notice).get("creditor").get("pay_on_website").asBoolean());
        }

        @Test
        @DisplayName("Con govpay.stampe.pay-on-website=true pay_on_website e' true")
        void payOnWebsiteTrueDaConfigurazione() {
            stampeConfig.setPayOnWebsite(true);

            PaymentNotice notice = stampeMapper.toPaymentNotice(versamento, null);

            assertEquals(Boolean.TRUE, notice.getCreditor().getPayOnWebsite());
            assertTrue(json(notice).get("creditor").get("pay_on_website").asBoolean());
        }
    }

    @Nested
    @DisplayName("first_logo come byte dell'immagine (issue #33)")
    class FirstLogoTests {

        private final byte[] immagine = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 1, 2, 3};

        @Test
        @DisplayName("Il logo salvato come testo base64 arriva come byte dell'immagine")
        void logoDecodificatoDallaColonna() {
            byte[] inColonna = Base64.getEncoder().encodeToString(immagine).getBytes(StandardCharsets.UTF_8);
            when(dominioLogoRepository.findLogoByCodDominio("12345678901")).thenReturn(Optional.of(inColonna));

            PaymentNotice notice = stampeMapper.toPaymentNotice(versamento, null);

            assertArrayEquals(immagine, notice.getFirstLogo());
            // In JSON: base64 dell'immagine, senza prefisso data:
            assertEquals(Base64.getEncoder().encodeToString(immagine), json(notice).get("first_logo").asString());
        }

        @Test
        @DisplayName("Il logo salvato come URI data arriva come byte dell'immagine")
        void logoDecodificatoDaUriData() {
            String uriData = "data:image/png;base64," + Base64.getEncoder().encodeToString(immagine);
            when(dominioLogoRepository.findLogoByCodDominio("12345678901"))
                    .thenReturn(Optional.of(uriData.getBytes(StandardCharsets.UTF_8)));

            PaymentNotice notice = stampeMapper.toPaymentNotice(versamento, null);

            assertArrayEquals(immagine, notice.getFirstLogo());
        }

        @Test
        @DisplayName("Senza logo si usa il PNG segnaposto")
        void senzaLogoUsaSegnaposto() {
            when(dominioLogoRepository.findLogoByCodDominio("12345678901")).thenReturn(Optional.empty());

            PaymentNotice notice = stampeMapper.toPaymentNotice(versamento, null);

            assertEquals((byte) 0x89, notice.getFirstLogo()[0]);
            assertEquals('P', notice.getFirstLogo()[1]);
        }
    }

    private static JsonNode json(PaymentNotice notice) {
        JsonMapper mapper = JsonMapper.builder().build();
        return mapper.readTree(mapper.writeValueAsString(notice));
    }
}
