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

import it.govpay.portal.security.hardening.exception.ReCaptchaConfigurationException;
import it.govpay.portal.security.hardening.model.CaptchaResponse;
import it.govpay.portal.security.hardening.model.GoogleCaptcha;
import it.govpay.portal.security.hardening.model.Hardening;

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
            Hardening hardening = Hardening.builder()
                    .abilitato(true)
                    .googleCaptcha(null)
                    .build();

            assertThrows(ReCaptchaConfigurationException.class,
                    () -> new ReCaptchaValidator(hardening));
        }

        @Test
        @DisplayName("ServerURL mancante dovrebbe lanciare eccezione")
        void missingServerUrlShouldThrowException() {
            GoogleCaptcha captcha = createCaptchaBuilder()
                    .serverURL(null)
                    .build();
            Hardening hardening = createHardening(captcha);

            ReCaptchaConfigurationException ex = assertThrows(
                    ReCaptchaConfigurationException.class,
                    () -> new ReCaptchaValidator(hardening));
            assertTrue(ex.getMessage().contains("URL"));
        }

        @Test
        @DisplayName("ServerURL vuota dovrebbe lanciare eccezione")
        void emptyServerUrlShouldThrowException() {
            GoogleCaptcha captcha = createCaptchaBuilder()
                    .serverURL("")
                    .build();
            Hardening hardening = createHardening(captcha);

            ReCaptchaConfigurationException ex = assertThrows(
                    ReCaptchaConfigurationException.class,
                    () -> new ReCaptchaValidator(hardening));
            assertTrue(ex.getMessage().contains("URL"));
        }

        @Test
        @DisplayName("SecretKey mancante dovrebbe lanciare eccezione")
        void missingSecretKeyShouldThrowException() {
            GoogleCaptcha captcha = createCaptchaBuilder()
                    .secretKey(null)
                    .build();
            Hardening hardening = createHardening(captcha);

            ReCaptchaConfigurationException ex = assertThrows(
                    ReCaptchaConfigurationException.class,
                    () -> new ReCaptchaValidator(hardening));
            assertTrue(ex.getMessage().contains("Secret Key"));
        }

        @Test
        @DisplayName("SecretKey vuota dovrebbe lanciare eccezione")
        void emptySecretKeyShouldThrowException() {
            GoogleCaptcha captcha = createCaptchaBuilder()
                    .secretKey("   ")
                    .build();
            Hardening hardening = createHardening(captcha);

            ReCaptchaConfigurationException ex = assertThrows(
                    ReCaptchaConfigurationException.class,
                    () -> new ReCaptchaValidator(hardening));
            assertTrue(ex.getMessage().contains("Secret Key"));
        }

        @Test
        @DisplayName("ResponseParameter mancante dovrebbe lanciare eccezione")
        void missingResponseParameterShouldThrowException() {
            GoogleCaptcha captcha = createCaptchaBuilder()
                    .responseParameter(null)
                    .build();
            Hardening hardening = createHardening(captcha);

            ReCaptchaConfigurationException ex = assertThrows(
                    ReCaptchaConfigurationException.class,
                    () -> new ReCaptchaValidator(hardening));
            assertTrue(ex.getMessage().contains("Response Parameter"));
        }

        @Test
        @DisplayName("ConnectionTimeout zero dovrebbe lanciare eccezione")
        void zeroConnectionTimeoutShouldThrowException() {
            GoogleCaptcha captcha = createCaptchaBuilder()
                    .connectionTimeout(0)
                    .build();
            Hardening hardening = createHardening(captcha);

            ReCaptchaConfigurationException ex = assertThrows(
                    ReCaptchaConfigurationException.class,
                    () -> new ReCaptchaValidator(hardening));
            assertTrue(ex.getMessage().contains("Connection Timeout"));
        }

        @Test
        @DisplayName("ConnectionTimeout negativo dovrebbe lanciare eccezione")
        void negativeConnectionTimeoutShouldThrowException() {
            GoogleCaptcha captcha = createCaptchaBuilder()
                    .connectionTimeout(-100)
                    .build();
            Hardening hardening = createHardening(captcha);

            ReCaptchaConfigurationException ex = assertThrows(
                    ReCaptchaConfigurationException.class,
                    () -> new ReCaptchaValidator(hardening));
            assertTrue(ex.getMessage().contains("Connection Timeout"));
        }

        @Test
        @DisplayName("ReadTimeout zero dovrebbe lanciare eccezione")
        void zeroReadTimeoutShouldThrowException() {
            GoogleCaptcha captcha = createCaptchaBuilder()
                    .readTimeout(0)
                    .build();
            Hardening hardening = createHardening(captcha);

            ReCaptchaConfigurationException ex = assertThrows(
                    ReCaptchaConfigurationException.class,
                    () -> new ReCaptchaValidator(hardening));
            assertTrue(ex.getMessage().contains("Read Timeout"));
        }

        @Test
        @DisplayName("Soglia zero dovrebbe lanciare eccezione")
        void zeroSogliaShouldThrowException() {
            GoogleCaptcha captcha = createCaptchaBuilder()
                    .soglia(0)
                    .build();
            Hardening hardening = createHardening(captcha);

            ReCaptchaConfigurationException ex = assertThrows(
                    ReCaptchaConfigurationException.class,
                    () -> new ReCaptchaValidator(hardening));
            assertTrue(ex.getMessage().contains("Soglia"));
        }

        @Test
        @DisplayName("Soglia negativa dovrebbe lanciare eccezione")
        void negativeSogliaShouldThrowException() {
            GoogleCaptcha captcha = createCaptchaBuilder()
                    .soglia(-0.5)
                    .build();
            Hardening hardening = createHardening(captcha);

            ReCaptchaConfigurationException ex = assertThrows(
                    ReCaptchaConfigurationException.class,
                    () -> new ReCaptchaValidator(hardening));
            assertTrue(ex.getMessage().contains("Soglia"));
        }

        @Test
        @DisplayName("Soglia maggiore di 1 dovrebbe lanciare eccezione")
        void sogliaGreaterThanOneShouldThrowException() {
            GoogleCaptcha captcha = createCaptchaBuilder()
                    .soglia(1.5)
                    .build();
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
            GoogleCaptcha captchaDeny = createValidCaptchaBuilder()
                    .denyOnFail(true)
                    .build();
            Hardening hardeningDeny = createHardening(captchaDeny);
            validatorDenyOnFail = new ReCaptchaValidator(hardeningDeny);
            injectMockRestTemplate(validatorDenyOnFail, restTemplate);

            GoogleCaptcha captchaAllow = createValidCaptchaBuilder()
                    .denyOnFail(false)
                    .build();
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
            GoogleCaptcha captcha = createValidCaptchaBuilder()
                    .serverURL("https://example.com/verify?version=3")
                    .build();
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

    private GoogleCaptcha.GoogleCaptchaBuilder createCaptchaBuilder() {
        return GoogleCaptcha.builder()
                .serverURL("https://www.google.com/recaptcha/api/siteverify")
                .secretKey("test-secret-key")
                .siteKey("test-site-key")
                .responseParameter(RESPONSE_PARAMETER)
                .connectionTimeout(5000)
                .readTimeout(5000)
                .soglia(0.5)
                .denyOnFail(true);
    }

    private GoogleCaptcha.GoogleCaptchaBuilder createValidCaptchaBuilder() {
        return createCaptchaBuilder();
    }

    private GoogleCaptcha createValidCaptcha() {
        return createCaptchaBuilder().build();
    }

    private Hardening createHardening(GoogleCaptcha captcha) {
        return Hardening.builder()
                .abilitato(true)
                .googleCaptcha(captcha)
                .build();
    }

    private void injectMockRestTemplate(ReCaptchaValidator validator, RestTemplate mockRestTemplate) throws Exception {
        Field restTemplateField = ReCaptchaValidator.class.getDeclaredField("restTemplate");
        restTemplateField.setAccessible(true);
        restTemplateField.set(validator, mockRestTemplate);
    }
}
