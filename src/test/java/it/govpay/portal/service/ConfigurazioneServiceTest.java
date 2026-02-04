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

import com.fasterxml.jackson.databind.ObjectMapper;

import it.govpay.portal.entity.Configurazione;
import it.govpay.portal.repository.ConfigurazioneRepository;
import it.govpay.portal.security.hardening.model.Hardening;

@ExtendWith(MockitoExtension.class)
class ConfigurazioneServiceTest {

    @Mock
    private ConfigurazioneRepository configurazioneRepository;

    private ConfigurazioneService service;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new ConfigurazioneService(configurazioneRepository, objectMapper);
    }

    @Nested
    @DisplayName("getHardening Tests")
    class GetHardeningTests {

        @Test
        @DisplayName("Configurazione non presente dovrebbe restituire hardening disabilitato")
        void missingConfigurationShouldReturnDisabledHardening() {
            when(configurazioneRepository.findByNome(Hardening.CONFIGURAZIONE_HARDENING))
                    .thenReturn(Optional.empty());

            Hardening result = service.getHardening();

            assertNotNull(result);
            assertFalse(result.isAbilitato());
        }

        @Test
        @DisplayName("Configurazione con valore null dovrebbe restituire hardening disabilitato")
        void configurationWithNullValueShouldReturnDisabledHardening() {
            Configurazione config = new Configurazione();
            config.setNome(Hardening.CONFIGURAZIONE_HARDENING);
            config.setValore(null);

            when(configurazioneRepository.findByNome(Hardening.CONFIGURAZIONE_HARDENING))
                    .thenReturn(Optional.of(config));

            Hardening result = service.getHardening();

            assertNotNull(result);
            assertFalse(result.isAbilitato());
        }

        @Test
        @DisplayName("Configurazione valida dovrebbe restituire hardening configurato")
        void validConfigurationShouldReturnConfiguredHardening() {
            String jsonConfig = """
                    {
                        "abilitato": true,
                        "googleCaptcha": {
                            "serverURL": "https://www.google.com/recaptcha/api/siteverify",
                            "secretKey": "test-secret",
                            "siteKey": "test-site",
                            "responseParameter": "g-recaptcha-response",
                            "connectionTimeout": 5000,
                            "readTimeout": 5000,
                            "soglia": 0.5,
                            "denyOnFail": true
                        }
                    }
                    """;

            Configurazione config = new Configurazione();
            config.setNome(Hardening.CONFIGURAZIONE_HARDENING);
            config.setValore(jsonConfig);

            when(configurazioneRepository.findByNome(Hardening.CONFIGURAZIONE_HARDENING))
                    .thenReturn(Optional.of(config));

            Hardening result = service.getHardening();

            assertNotNull(result);
            assertTrue(result.isAbilitato());
            assertNotNull(result.getGoogleCaptcha());
            assertEquals("https://www.google.com/recaptcha/api/siteverify", result.getGoogleCaptcha().getServerURL());
            assertEquals("test-secret", result.getGoogleCaptcha().getSecretKey());
            assertEquals(0.5, result.getGoogleCaptcha().getSoglia());
        }

        @Test
        @DisplayName("Configurazione abilitato=false dovrebbe restituire hardening disabilitato")
        void disabledConfigurationShouldReturnDisabledHardening() {
            String jsonConfig = "{\"abilitato\": false}";

            Configurazione config = new Configurazione();
            config.setNome(Hardening.CONFIGURAZIONE_HARDENING);
            config.setValore(jsonConfig);

            when(configurazioneRepository.findByNome(Hardening.CONFIGURAZIONE_HARDENING))
                    .thenReturn(Optional.of(config));

            Hardening result = service.getHardening();

            assertNotNull(result);
            assertFalse(result.isAbilitato());
        }

        @Test
        @DisplayName("JSON non valido dovrebbe restituire hardening disabilitato")
        void invalidJsonShouldReturnDisabledHardening() {
            Configurazione config = new Configurazione();
            config.setNome(Hardening.CONFIGURAZIONE_HARDENING);
            config.setValore("questo non e' json valido{");

            when(configurazioneRepository.findByNome(Hardening.CONFIGURAZIONE_HARDENING))
                    .thenReturn(Optional.of(config));

            Hardening result = service.getHardening();

            assertNotNull(result);
            assertFalse(result.isAbilitato());
        }

        @Test
        @DisplayName("JSON parziale dovrebbe essere parsato correttamente")
        void partialJsonShouldBeParsedCorrectly() {
            String jsonConfig = "{\"abilitato\": true}";

            Configurazione config = new Configurazione();
            config.setNome(Hardening.CONFIGURAZIONE_HARDENING);
            config.setValore(jsonConfig);

            when(configurazioneRepository.findByNome(Hardening.CONFIGURAZIONE_HARDENING))
                    .thenReturn(Optional.of(config));

            Hardening result = service.getHardening();

            assertNotNull(result);
            assertTrue(result.isAbilitato());
            assertNull(result.getGoogleCaptcha());
        }

        @Test
        @DisplayName("Configurazione con tutti i campi GoogleCaptcha dovrebbe essere parsata")
        void fullGoogleCaptchaConfigShouldBeParsed() {
            String jsonConfig = """
                    {
                        "abilitato": true,
                        "googleCaptcha": {
                            "serverURL": "https://custom.server/verify",
                            "secretKey": "my-secret-key",
                            "siteKey": "my-site-key",
                            "responseParameter": "custom-param",
                            "connectionTimeout": 10000,
                            "readTimeout": 15000,
                            "soglia": 0.7,
                            "denyOnFail": false
                        }
                    }
                    """;

            Configurazione config = new Configurazione();
            config.setNome(Hardening.CONFIGURAZIONE_HARDENING);
            config.setValore(jsonConfig);

            when(configurazioneRepository.findByNome(Hardening.CONFIGURAZIONE_HARDENING))
                    .thenReturn(Optional.of(config));

            Hardening result = service.getHardening();

            assertNotNull(result);
            assertTrue(result.isAbilitato());
            assertNotNull(result.getGoogleCaptcha());
            assertEquals("https://custom.server/verify", result.getGoogleCaptcha().getServerURL());
            assertEquals("my-secret-key", result.getGoogleCaptcha().getSecretKey());
            assertEquals("my-site-key", result.getGoogleCaptcha().getSiteKey());
            assertEquals("custom-param", result.getGoogleCaptcha().getResponseParameter());
            assertEquals(10000, result.getGoogleCaptcha().getConnectionTimeout());
            assertEquals(15000, result.getGoogleCaptcha().getReadTimeout());
            assertEquals(0.7, result.getGoogleCaptcha().getSoglia());
            assertFalse(result.getGoogleCaptcha().isDenyOnFail());
        }
    }
}
