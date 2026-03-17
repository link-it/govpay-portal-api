package it.govpay.portal.security.hardening.matcher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import it.govpay.common.configurazione.model.GoogleCaptcha;
import it.govpay.common.configurazione.model.Hardening;
import it.govpay.portal.config.SpidUserDetails;
import it.govpay.portal.service.ConfigurazioneService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HardeningRequestMatcherTest {

    @Mock
    private ConfigurazioneService configurazioneService;

    private HardeningRequestMatcher matcher;

    @BeforeEach
    void setUp() {
        matcher = new HardeningRequestMatcher("/pendenze/*/*", HttpMethod.POST, configurazioneService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private MockHttpServletRequest createRequest(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setServletPath(path);
        return request;
    }

    @Nested
    @DisplayName("Path Matching Tests")
    class PathMatchingTests {

        @Test
        @DisplayName("Dovrebbe non matchare path non corrispondente")
        void shouldNotMatchNonMatchingPath() {
            MockHttpServletRequest request = createRequest("GET", "/domini");

            boolean result = matcher.matches(request);

            assertFalse(result);
            verifyNoInteractions(configurazioneService);
        }

        @Test
        @DisplayName("Dovrebbe non matchare metodo HTTP diverso")
        void shouldNotMatchDifferentHttpMethod() {
            MockHttpServletRequest request = createRequest("GET", "/pendenze/12345678901/TARI");

            boolean result = matcher.matches(request);

            assertFalse(result);
            verifyNoInteractions(configurazioneService);
        }

        @Test
        @DisplayName("Dovrebbe matchare path e metodo corretti con utente anonimo e hardening disabilitato")
        void shouldMatchCorrectPathAndMethod() {
            setAnonymousAuthentication();
            Hardening hardening = new Hardening();
            hardening.setAbilitato(false);
            when(configurazioneService.getHardening()).thenReturn(hardening);

            MockHttpServletRequest request = createRequest("POST", "/pendenze/12345678901/TARI");

            boolean result = matcher.matches(request);

            assertTrue(result);
            verify(configurazioneService).getHardening();
        }
    }

    @Nested
    @DisplayName("Authenticated User Tests")
    class AuthenticatedUserTests {

        @Test
        @DisplayName("Utente autenticato dovrebbe bypassare hardening")
        void authenticatedUserShouldBypassHardening() {
            setAuthenticatedUser();

            MockHttpServletRequest request = createRequest("POST", "/pendenze/12345678901/TARI");

            boolean result = matcher.matches(request);

            assertTrue(result);
            verifyNoInteractions(configurazioneService);
        }
    }

    @Nested
    @DisplayName("Hardening Disabled Tests")
    class HardeningDisabledTests {

        @Test
        @DisplayName("Utente anonimo con hardening disabilitato dovrebbe essere consentito")
        void anonymousUserWithHardeningDisabledShouldBeAllowed() {
            setAnonymousAuthentication();
            Hardening hardening = new Hardening();
            hardening.setAbilitato(false);
            when(configurazioneService.getHardening()).thenReturn(hardening);

            MockHttpServletRequest request = createRequest("POST", "/pendenze/12345678901/TARI");

            boolean result = matcher.matches(request);

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("Hardening Enabled Tests")
    class HardeningEnabledTests {

        @Test
        @DisplayName("Utente anonimo con hardening abilitato ma senza ReCaptcha dovrebbe essere negato")
        void anonymousUserWithHardeningEnabledWithoutReCaptchaShouldBeDenied() {
            setAnonymousAuthentication();
            Hardening hardening = new Hardening();
            hardening.setAbilitato(true);
            hardening.setGoogleCatpcha(createValidGoogleCaptcha());
            when(configurazioneService.getHardening()).thenReturn(hardening);

            MockHttpServletRequest request = createRequest("POST", "/pendenze/12345678901/TARI");

            boolean result = matcher.matches(request);

            assertFalse(result);
        }

        @Test
        @DisplayName("Utente anonimo con hardening abilitato e ReCaptcha presente ma non validabile dovrebbe essere negato")
        void anonymousUserWithInvalidReCaptchaShouldBeDenied() {
            setAnonymousAuthentication();
            Hardening hardening = new Hardening();
            hardening.setAbilitato(true);
            hardening.setGoogleCatpcha(createValidGoogleCaptcha());
            when(configurazioneService.getHardening()).thenReturn(hardening);

            MockHttpServletRequest request = createRequest("POST", "/pendenze/12345678901/TARI");
            request.setParameter("g-recaptcha-response", "invalid-token");

            boolean result = matcher.matches(request);

            // La validazione fallira' perche' non c'e' un server ReCaptcha mock
            assertFalse(result);
        }

        @Test
        @DisplayName("Errore durante getHardening dovrebbe negare accesso")
        void errorDuringGetHardeningShouldDenyAccess() {
            setAnonymousAuthentication();
            when(configurazioneService.getHardening()).thenThrow(new RuntimeException("DB error"));

            MockHttpServletRequest request = createRequest("POST", "/pendenze/12345678901/TARI");

            boolean result = matcher.matches(request);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("Hardening con GoogleCaptcha null dovrebbe negare accesso")
        void hardeningWithNullGoogleCaptchaShouldDenyAccess() {
            setAnonymousAuthentication();
            Hardening hardening = new Hardening();
            hardening.setAbilitato(true);
            hardening.setGoogleCatpcha(null);
            when(configurazioneService.getHardening()).thenReturn(hardening);

            MockHttpServletRequest request = createRequest("POST", "/pendenze/12345678901/TARI");

            boolean result = matcher.matches(request);

            assertFalse(result);
        }

        @Test
        @DisplayName("Hardening con configurazione GoogleCaptcha incompleta dovrebbe negare accesso")
        void hardeningWithIncompleteGoogleCaptchaShouldDenyAccess() {
            setAnonymousAuthentication();
            GoogleCaptcha incompleteConfig = new GoogleCaptcha();
            incompleteConfig.setServerURL("https://www.google.com/recaptcha/api/siteverify");
            // Missing secretKey
            incompleteConfig.setResponseParameter("g-recaptcha-response");
            incompleteConfig.setConnectionTimeout(5000);
            incompleteConfig.setReadTimeout(5000);
            incompleteConfig.setSoglia(0.5);

            Hardening hardening = new Hardening();
            hardening.setAbilitato(true);
            hardening.setGoogleCatpcha(incompleteConfig);
            when(configurazioneService.getHardening()).thenReturn(hardening);

            MockHttpServletRequest request = createRequest("POST", "/pendenze/12345678901/TARI");

            boolean result = matcher.matches(request);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Costruttore con pattern e service dovrebbe funzionare")
        void constructorWithPatternAndServiceShouldWork() {
            HardeningRequestMatcher m = new HardeningRequestMatcher("/test/**", configurazioneService);
            assertNotNull(m);
            assertNotNull(m.getPathMatcher());
        }

        @Test
        @DisplayName("Costruttore con null method dovrebbe funzionare")
        void constructorWithNullMethodShouldWork() {
            HardeningRequestMatcher m = new HardeningRequestMatcher("/test/**", null, configurazioneService);
            assertNotNull(m);
        }
    }

    private void setAuthenticatedUser() {
        SpidUserDetails spidUser = new SpidUserDetails(
                "RSSMRA80A01H501U", "Mario", "Rossi",
                "mario.rossi@email.it", null, null);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                spidUser, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void setAnonymousAuthentication() {
        AnonymousAuthenticationToken authentication = new AnonymousAuthenticationToken(
                "key", "anonymousUser", Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private GoogleCaptcha createValidGoogleCaptcha() {
        GoogleCaptcha captcha = new GoogleCaptcha();
        captcha.setServerURL("https://www.google.com/recaptcha/api/siteverify");
        captcha.setSecretKey("test-secret-key");
        captcha.setSiteKey("test-site-key");
        captcha.setResponseParameter("g-recaptcha-response");
        captcha.setConnectionTimeout(5000);
        captcha.setReadTimeout(5000);
        captcha.setSoglia(0.5);
        captcha.setDenyOnFail(true);
        return captcha;
    }
}
