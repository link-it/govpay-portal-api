package it.govpay.portal.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.govpay.common.configurazione.model.GoogleCaptcha;
import it.govpay.common.configurazione.model.Hardening;

@ExtendWith(MockitoExtension.class)
class ConfigurazioneServiceTest {

    @Mock
    private it.govpay.common.configurazione.service.ConfigurazioneService commonConfigurazioneService;

    private ConfigurazioneService service;

    @BeforeEach
    void setUp() {
        service = new ConfigurazioneService(commonConfigurazioneService);
    }

    @Nested
    @DisplayName("getHardening Tests")
    class GetHardeningTests {

        @Test
        @DisplayName("Configurazione non presente dovrebbe restituire hardening disabilitato")
        void missingConfigurationShouldReturnDisabledHardening() {
            when(commonConfigurazioneService.getHardening()).thenReturn(Optional.empty());

            Hardening result = service.getHardening();

            assertNotNull(result);
            assertFalse(result.isAbilitato());
        }

        @Test
        @DisplayName("Configurazione valida dovrebbe restituire hardening configurato")
        void validConfigurationShouldReturnConfiguredHardening() {
            Hardening hardening = new Hardening();
            hardening.setAbilitato(true);
            GoogleCaptcha captcha = new GoogleCaptcha();
            captcha.setServerURL("https://www.google.com/recaptcha/api/siteverify");
            captcha.setSecretKey("test-secret");
            captcha.setSoglia(0.5);
            hardening.setGoogleCatpcha(captcha);

            when(commonConfigurazioneService.getHardening()).thenReturn(Optional.of(hardening));

            Hardening result = service.getHardening();

            assertNotNull(result);
            assertTrue(result.isAbilitato());
            assertNotNull(result.getGoogleCatpcha());
            assertEquals("https://www.google.com/recaptcha/api/siteverify", result.getGoogleCatpcha().getServerURL());
            assertEquals("test-secret", result.getGoogleCatpcha().getSecretKey());
            assertEquals(0.5, result.getGoogleCatpcha().getSoglia());
        }

        @Test
        @DisplayName("Configurazione abilitato=false dovrebbe restituire hardening disabilitato")
        void disabledConfigurationShouldReturnDisabledHardening() {
            Hardening hardening = new Hardening();
            hardening.setAbilitato(false);

            when(commonConfigurazioneService.getHardening()).thenReturn(Optional.of(hardening));

            Hardening result = service.getHardening();

            assertNotNull(result);
            assertFalse(result.isAbilitato());
        }

        @Test
        @DisplayName("Eccezione dovrebbe restituire hardening disabilitato")
        void exceptionShouldReturnDisabledHardening() {
            when(commonConfigurazioneService.getHardening())
                    .thenThrow(new IllegalArgumentException("JSON non valido"));

            Hardening result = service.getHardening();

            assertNotNull(result);
            assertFalse(result.isAbilitato());
        }

        @Test
        @DisplayName("Configurazione con tutti i campi GoogleCaptcha dovrebbe essere restituita")
        void fullGoogleCaptchaConfigShouldBeReturned() {
            Hardening hardening = new Hardening();
            hardening.setAbilitato(true);
            GoogleCaptcha captcha = new GoogleCaptcha();
            captcha.setServerURL("https://custom.server/verify");
            captcha.setSecretKey("my-secret-key");
            captcha.setSiteKey("my-site-key");
            captcha.setResponseParameter("custom-param");
            captcha.setConnectionTimeout(10000);
            captcha.setReadTimeout(15000);
            captcha.setSoglia(0.7);
            captcha.setDenyOnFail(false);
            hardening.setGoogleCatpcha(captcha);

            when(commonConfigurazioneService.getHardening()).thenReturn(Optional.of(hardening));

            Hardening result = service.getHardening();

            assertNotNull(result);
            assertTrue(result.isAbilitato());
            assertNotNull(result.getGoogleCatpcha());
            assertEquals("https://custom.server/verify", result.getGoogleCatpcha().getServerURL());
            assertEquals("my-secret-key", result.getGoogleCatpcha().getSecretKey());
            assertEquals("my-site-key", result.getGoogleCatpcha().getSiteKey());
            assertEquals("custom-param", result.getGoogleCatpcha().getResponseParameter());
            assertEquals(10000, result.getGoogleCatpcha().getConnectionTimeout());
            assertEquals(15000, result.getGoogleCatpcha().getReadTimeout());
            assertEquals(0.7, result.getGoogleCatpcha().getSoglia());
            assertFalse(result.getGoogleCatpcha().isDenyOnFail());
        }
    }
}
