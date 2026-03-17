package it.govpay.portal.security.hardening;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import it.govpay.common.configurazione.model.GoogleCaptcha;
import it.govpay.common.configurazione.model.Hardening;
import it.govpay.portal.security.hardening.exception.ReCaptchaConfigurationException;
import it.govpay.portal.security.hardening.model.CaptchaResponse;

@ExtendWith(MockitoExtension.class)
class ReCaptchaValidatorTest {

    @Mock
    private RestTemplate restTemplate;

    private static final String RESPONSE_PARAMETER = "g-recaptcha-response";

    @Nested
    @DisplayName("Configuration Validation Tests")
    class ConfigurationValidationTests {

        @Test
        @DisplayName("Configurazione GoogleCaptcha null dovrebbe lanciare eccezione")
        void nullGoogleCaptchaShouldThrowException() {
            Hardening hardening = new Hardening();
            hardening.setAbilitato(true);
            hardening.setGoogleCatpcha(null);

            assertThrows(ReCaptchaConfigurationException.class,
                    () -> new ReCaptchaValidator(hardening));
        }

        @Test
        @DisplayName("ServerURL mancante dovrebbe lanciare eccezione")
        void missingServerUrlShouldThrowException() {
            GoogleCaptcha captcha = createValidCaptcha();
            captcha.setServerURL(null);
            Hardening hardening = createHardening(captcha);

            ReCaptchaConfigurationException ex = assertThrows(
                    ReCaptchaConfigurationException.class,
                    () -> new ReCaptchaValidator(hardening));
            assertTrue(ex.getMessage().contains("URL"));
        }

        @Test
        @DisplayName("ServerURL vuota dovrebbe lanciare eccezione")
        void emptyServerUrlShouldThrowException() {
            GoogleCaptcha captcha = createValidCaptcha();
            captcha.setServerURL("");
            Hardening hardening = createHardening(captcha);

            ReCaptchaConfigurationException ex = assertThrows(
                    ReCaptchaConfigurationException.class,
                    () -> new ReCaptchaValidator(hardening));
            assertTrue(ex.getMessage().contains("URL"));
        }

        @Test
        @DisplayName("SecretKey mancante dovrebbe lanciare eccezione")
        void missingSecretKeyShouldThrowException() {
            GoogleCaptcha captcha = createValidCaptcha();
            captcha.setSecretKey(null);
            Hardening hardening = createHardening(captcha);

            ReCaptchaConfigurationException ex = assertThrows(
                    ReCaptchaConfigurationException.class,
                    () -> new ReCaptchaValidator(hardening));
            assertTrue(ex.getMessage().contains("Secret Key"));
        }

        @Test
        @DisplayName("SecretKey vuota dovrebbe lanciare eccezione")
        void emptySecretKeyShouldThrowException() {
            GoogleCaptcha captcha = createValidCaptcha();
            captcha.setSecretKey("   ");
            Hardening hardening = createHardening(captcha);

            ReCaptchaConfigurationException ex = assertThrows(
                    ReCaptchaConfigurationException.class,
                    () -> new ReCaptchaValidator(hardening));
            assertTrue(ex.getMessage().contains("Secret Key"));
        }

        @Test
        @DisplayName("ResponseParameter mancante dovrebbe lanciare eccezione")
        void missingResponseParameterShouldThrowException() {
            GoogleCaptcha captcha = createValidCaptcha();
            captcha.setResponseParameter(null);
            Hardening hardening = createHardening(captcha);

            ReCaptchaConfigurationException ex = assertThrows(
                    ReCaptchaConfigurationException.class,
                    () -> new ReCaptchaValidator(hardening));
            assertTrue(ex.getMessage().contains("Response Parameter"));
        }

        @Test
        @DisplayName("ConnectionTimeout zero dovrebbe lanciare eccezione")
        void zeroConnectionTimeoutShouldThrowException() {
            GoogleCaptcha captcha = createValidCaptcha();
            captcha.setConnectionTimeout(0);
            Hardening hardening = createHardening(captcha);

            ReCaptchaConfigurationException ex = assertThrows(
                    ReCaptchaConfigurationException.class,
                    () -> new ReCaptchaValidator(hardening));
            assertTrue(ex.getMessage().contains("Connection Timeout"));
        }

        @Test
        @DisplayName("ConnectionTimeout negativo dovrebbe lanciare eccezione")
        void negativeConnectionTimeoutShouldThrowException() {
            GoogleCaptcha captcha = createValidCaptcha();
            captcha.setConnectionTimeout(-100);
            Hardening hardening = createHardening(captcha);

            ReCaptchaConfigurationException ex = assertThrows(
                    ReCaptchaConfigurationException.class,
                    () -> new ReCaptchaValidator(hardening));
            assertTrue(ex.getMessage().contains("Connection Timeout"));
        }

        @Test
        @DisplayName("ReadTimeout zero dovrebbe lanciare eccezione")
        void zeroReadTimeoutShouldThrowException() {
            GoogleCaptcha captcha = createValidCaptcha();
            captcha.setReadTimeout(0);
            Hardening hardening = createHardening(captcha);

            ReCaptchaConfigurationException ex = assertThrows(
                    ReCaptchaConfigurationException.class,
                    () -> new ReCaptchaValidator(hardening));
            assertTrue(ex.getMessage().contains("Read Timeout"));
        }

        @Test
        @DisplayName("Soglia zero dovrebbe lanciare eccezione")
        void zeroSogliaShouldThrowException() {
            GoogleCaptcha captcha = createValidCaptcha();
            captcha.setSoglia(0);
            Hardening hardening = createHardening(captcha);

            ReCaptchaConfigurationException ex = assertThrows(
                    ReCaptchaConfigurationException.class,
                    () -> new ReCaptchaValidator(hardening));
            assertTrue(ex.getMessage().contains("Soglia"));
        }

        @Test
        @DisplayName("Soglia negativa dovrebbe lanciare eccezione")
        void negativeSogliaShouldThrowException() {
            GoogleCaptcha captcha = createValidCaptcha();
            captcha.setSoglia(-0.5);
            Hardening hardening = createHardening(captcha);

            ReCaptchaConfigurationException ex = assertThrows(
                    ReCaptchaConfigurationException.class,
                    () -> new ReCaptchaValidator(hardening));
            assertTrue(ex.getMessage().contains("Soglia"));
        }

        @Test
        @DisplayName("Soglia maggiore di 1 dovrebbe lanciare eccezione")
        void sogliaGreaterThanOneShouldThrowException() {
            GoogleCaptcha captcha = createValidCaptcha();
            captcha.setSoglia(1.5);
            Hardening hardening = createHardening(captcha);

            ReCaptchaConfigurationException ex = assertThrows(
                    ReCaptchaConfigurationException.class,
                    () -> new ReCaptchaValidator(hardening));
            assertTrue(ex.getMessage().contains("Soglia"));
        }

        @Test
        @DisplayName("Configurazione valida dovrebbe creare validator")
        void validConfigurationShouldCreateValidator() {
            GoogleCaptcha captcha = createValidCaptcha();
            Hardening hardening = createHardening(captcha);

            ReCaptchaValidator validator = new ReCaptchaValidator(hardening);
            assertNotNull(validator);
        }
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class TokenValidationTests {

        private ReCaptchaValidator validator;

        @BeforeEach
        void setUp() throws Exception {
            GoogleCaptcha captcha = createValidCaptcha();
            Hardening hardening = createHardening(captcha);
            validator = new ReCaptchaValidator(hardening);
            injectMockRestTemplate(validator, restTemplate);
        }

        @Test
        @DisplayName("Token ReCaptcha mancante dovrebbe restituire false")
        void missingReCaptchaTokenShouldReturnFalse() {
            MockHttpServletRequest request = new MockHttpServletRequest();

            boolean result = validator.validate(request);

            assertFalse(result);
            verifyNoInteractions(restTemplate);
        }

        @Test
        @DisplayName("Token ReCaptcha vuoto dovrebbe restituire false")
        void emptyReCaptchaTokenShouldReturnFalse() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setParameter(RESPONSE_PARAMETER, "");

            boolean result = validator.validate(request);

            assertFalse(result);
            verifyNoInteractions(restTemplate);
        }

        @Test
        @DisplayName("Token ReCaptcha solo spazi dovrebbe restituire false")
        void blankReCaptchaTokenShouldReturnFalse() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setParameter(RESPONSE_PARAMETER, "   ");

            boolean result = validator.validate(request);

            assertFalse(result);
            verifyNoInteractions(restTemplate);
        }

        @Test
        @DisplayName("Token nell'header dovrebbe essere riconosciuto")
        void tokenInHeaderShouldBeRecognized() {
            CaptchaResponse captchaResponse = new CaptchaResponse();
            captchaResponse.setSuccess(true);
            captchaResponse.setScore(new BigDecimal("0.9"));

            when(restTemplate.getForObject(any(URI.class), eq(CaptchaResponse.class)))
                    .thenReturn(captchaResponse);

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader(RESPONSE_PARAMETER, "valid-token");

            boolean result = validator.validate(request);

            assertTrue(result);
            verify(restTemplate).getForObject(any(URI.class), eq(CaptchaResponse.class));
        }

        @Test
        @DisplayName("Validazione riuscita dovrebbe restituire true")
        void successfulValidationShouldReturnTrue() {
            CaptchaResponse captchaResponse = new CaptchaResponse();
            captchaResponse.setSuccess(true);
            captchaResponse.setScore(new BigDecimal("0.9"));

            when(restTemplate.getForObject(any(URI.class), eq(CaptchaResponse.class)))
                    .thenReturn(captchaResponse);

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setParameter(RESPONSE_PARAMETER, "valid-token");

            boolean result = validator.validate(request);

            assertTrue(result);
        }

        @Test
        @DisplayName("Validazione fallita (success=false) dovrebbe restituire false")
        void failedValidationShouldReturnFalse() {
            CaptchaResponse captchaResponse = new CaptchaResponse();
            captchaResponse.setSuccess(false);
            captchaResponse.setErrorCodes(new String[]{"invalid-input-response"});

            when(restTemplate.getForObject(any(URI.class), eq(CaptchaResponse.class)))
                    .thenReturn(captchaResponse);

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setParameter(RESPONSE_PARAMETER, "invalid-token");

            boolean result = validator.validate(request);

            assertFalse(result);
        }

        @Test
        @DisplayName("Risposta null dovrebbe restituire false")
        void nullResponseShouldReturnFalse() {
            when(restTemplate.getForObject(any(URI.class), eq(CaptchaResponse.class)))
                    .thenReturn(null);

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setParameter(RESPONSE_PARAMETER, "some-token");

            boolean result = validator.validate(request);

            assertFalse(result);
        }

        @Test
        @DisplayName("Score sotto soglia dovrebbe restituire false")
        void scoreBelowThresholdShouldReturnFalse() {
            CaptchaResponse captchaResponse = new CaptchaResponse();
            captchaResponse.setSuccess(true);
            captchaResponse.setScore(new BigDecimal("0.3")); // Sotto soglia 0.5

            when(restTemplate.getForObject(any(URI.class), eq(CaptchaResponse.class)))
                    .thenReturn(captchaResponse);

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setParameter(RESPONSE_PARAMETER, "low-score-token");

            boolean result = validator.validate(request);

            assertFalse(result);
        }

        @Test
        @DisplayName("Score uguale a soglia dovrebbe restituire true (non e' sotto soglia)")
        void scoreEqualToThresholdShouldReturnTrue() {
            CaptchaResponse captchaResponse = new CaptchaResponse();
            captchaResponse.setSuccess(true);
            captchaResponse.setScore(new BigDecimal("0.5")); // Uguale a soglia - non e' < soglia

            when(restTemplate.getForObject(any(URI.class), eq(CaptchaResponse.class)))
                    .thenReturn(captchaResponse);

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setParameter(RESPONSE_PARAMETER, "threshold-token");

            boolean result = validator.validate(request);

            // La condizione nel codice e': if (score < soglia) return false
            // 0.5 NON E' < 0.5, quindi passa
            assertTrue(result);
        }

        @Test
        @DisplayName("Score null (ReCaptcha v2) dovrebbe restituire true se success=true")
        void nullScoreShouldReturnTrueIfSuccessful() {
            CaptchaResponse captchaResponse = new CaptchaResponse();
            captchaResponse.setSuccess(true);
            captchaResponse.setScore(null); // ReCaptcha v2 non ha score

            when(restTemplate.getForObject(any(URI.class), eq(CaptchaResponse.class)))
                    .thenReturn(captchaResponse);

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setParameter(RESPONSE_PARAMETER, "v2-token");

            boolean result = validator.validate(request);

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        private ReCaptchaValidator validatorDenyOnFail;
        private ReCaptchaValidator validatorAllowOnFail;

        @BeforeEach
        void setUp() throws Exception {
            GoogleCaptcha captchaDeny = createValidCaptcha();
            captchaDeny.setDenyOnFail(true);
            Hardening hardeningDeny = createHardening(captchaDeny);
            validatorDenyOnFail = new ReCaptchaValidator(hardeningDeny);
            injectMockRestTemplate(validatorDenyOnFail, restTemplate);

            GoogleCaptcha captchaAllow = createValidCaptcha();
            captchaAllow.setDenyOnFail(false);
            Hardening hardeningAllow = createHardening(captchaAllow);
            validatorAllowOnFail = new ReCaptchaValidator(hardeningAllow);
            injectMockRestTemplate(validatorAllowOnFail, restTemplate);
        }

        @Test
        @DisplayName("Errore connessione con denyOnFail=true dovrebbe restituire false")
        void connectionErrorWithDenyOnFailShouldReturnFalse() {
            when(restTemplate.getForObject(any(URI.class), eq(CaptchaResponse.class)))
                    .thenThrow(new RestClientException("Connection refused"));

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setParameter(RESPONSE_PARAMETER, "valid-token");

            boolean result = validatorDenyOnFail.validate(request);

            assertFalse(result);
        }

        @Test
        @DisplayName("Errore connessione con denyOnFail=false dovrebbe restituire true")
        void connectionErrorWithAllowOnFailShouldReturnTrue() {
            when(restTemplate.getForObject(any(URI.class), eq(CaptchaResponse.class)))
                    .thenThrow(new RestClientException("Connection refused"));

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setParameter(RESPONSE_PARAMETER, "valid-token");

            boolean result = validatorAllowOnFail.validate(request);

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("X-Forwarded-For Tests")
    class XForwardedForTests {

        private ReCaptchaValidator validator;

        @BeforeEach
        void setUp() throws Exception {
            GoogleCaptcha captcha = createValidCaptcha();
            Hardening hardening = createHardening(captcha);
            validator = new ReCaptchaValidator(hardening);
            injectMockRestTemplate(validator, restTemplate);
        }

        @Test
        @DisplayName("Dovrebbe usare X-Forwarded-For se presente")
        void shouldUseXForwardedForIfPresent() {
            CaptchaResponse captchaResponse = new CaptchaResponse();
            captchaResponse.setSuccess(true);
            captchaResponse.setScore(new BigDecimal("0.9"));

            when(restTemplate.getForObject(any(URI.class), eq(CaptchaResponse.class)))
                    .thenReturn(captchaResponse);

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setParameter(RESPONSE_PARAMETER, "test-token");
            request.addHeader("X-Forwarded-For", "192.168.1.100, 10.0.0.1");
            request.setRemoteAddr("127.0.0.1");

            boolean result = validator.validate(request);

            assertTrue(result);
            // L'IP nella request dovrebbe essere 192.168.1.100 (primo dell'header)
        }

        @Test
        @DisplayName("Dovrebbe usare remoteAddr se X-Forwarded-For non presente")
        void shouldUseRemoteAddrIfNoXForwardedFor() {
            CaptchaResponse captchaResponse = new CaptchaResponse();
            captchaResponse.setSuccess(true);
            captchaResponse.setScore(new BigDecimal("0.9"));

            when(restTemplate.getForObject(any(URI.class), eq(CaptchaResponse.class)))
                    .thenReturn(captchaResponse);

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setParameter(RESPONSE_PARAMETER, "test-token");
            request.setRemoteAddr("10.0.0.1");

            boolean result = validator.validate(request);

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("URL Construction Tests")
    class UrlConstructionTests {

        @Test
        @DisplayName("URL con query string esistente dovrebbe aggiungere parametri con &")
        void urlWithExistingQueryStringShouldAppendWithAmpersand() throws Exception {
            GoogleCaptcha captcha = createValidCaptcha();
            captcha.setServerURL("https://example.com/verify?version=3");
            Hardening hardening = createHardening(captcha);
            ReCaptchaValidator validator = new ReCaptchaValidator(hardening);
            injectMockRestTemplate(validator, restTemplate);

            CaptchaResponse captchaResponse = new CaptchaResponse();
            captchaResponse.setSuccess(true);

            when(restTemplate.getForObject(any(URI.class), eq(CaptchaResponse.class)))
                    .thenReturn(captchaResponse);

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setParameter(RESPONSE_PARAMETER, "test-token");

            validator.validate(request);

            verify(restTemplate).getForObject(any(URI.class), eq(CaptchaResponse.class));
        }
    }

    private GoogleCaptcha createValidCaptcha() {
        GoogleCaptcha captcha = new GoogleCaptcha();
        captcha.setServerURL("https://www.google.com/recaptcha/api/siteverify");
        captcha.setSecretKey("test-secret-key");
        captcha.setSiteKey("test-site-key");
        captcha.setResponseParameter(RESPONSE_PARAMETER);
        captcha.setConnectionTimeout(5000);
        captcha.setReadTimeout(5000);
        captcha.setSoglia(0.5);
        captcha.setDenyOnFail(true);
        return captcha;
    }

    private Hardening createHardening(GoogleCaptcha captcha) {
        Hardening hardening = new Hardening();
        hardening.setAbilitato(true);
        hardening.setGoogleCatpcha(captcha);
        return hardening;
    }

    private void injectMockRestTemplate(ReCaptchaValidator validator, RestTemplate mockRestTemplate) throws Exception {
        Field restTemplateField = ReCaptchaValidator.class.getDeclaredField("restTemplate");
        restTemplateField.setAccessible(true);
        restTemplateField.set(validator, mockRestTemplate);
    }
}
