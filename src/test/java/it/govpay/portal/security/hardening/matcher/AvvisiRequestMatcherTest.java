package it.govpay.portal.security.hardening.matcher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Optional;

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

import it.govpay.portal.config.SpidUserDetails;
import it.govpay.portal.entity.Versamento;
import it.govpay.portal.repository.VersamentoRepository;
import it.govpay.portal.security.hardening.model.GoogleCaptcha;
import it.govpay.portal.security.hardening.model.Hardening;
import it.govpay.portal.service.ConfigurazioneService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AvvisiRequestMatcherTest {

    @Mock
    private ConfigurazioneService configurazioneService;

    @Mock
    private VersamentoRepository versamentoRepository;

    private AvvisiRequestMatcher matcher;

    private static final String ID_DOMINIO = "12345678901";
    private static final String NUMERO_AVVISO = "123456789012345678"; // 18 caratteri
    private static final String IUV = "01234567890123456"; // 17 caratteri
    private static final String VALID_UUID = "550e8400-e29b-41d4-a716-446655440000";

    @BeforeEach
    void setUp() {
        matcher = new AvvisiRequestMatcher("/pendenze/*/*/avviso", HttpMethod.GET,
                configurazioneService, versamentoRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("Path Matching Tests")
    class PathMatchingTests {

        @Test
        @DisplayName("Dovrebbe non matchare path non corrispondente")
        void shouldNotMatchNonMatchingPath() {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/pendenze/12345678901");

            boolean result = matcher.matches(request);

            assertFalse(result);
        }

        @Test
        @DisplayName("Dovrebbe non matchare metodo HTTP diverso")
        void shouldNotMatchDifferentHttpMethod() {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/pendenze/12345678901/123456789012345678/avviso");

            boolean result = matcher.matches(request);

            assertFalse(result);
        }

        @Test
        @DisplayName("Dovrebbe matchare path avviso corretto con utente autenticato")
        void shouldMatchCorrectAvvisoPath() {
            setAuthenticatedUser();
            MockHttpServletRequest request = createAvvisoRequest(ID_DOMINIO, NUMERO_AVVISO);

            boolean result = matcher.matches(request);

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("Authenticated User Tests")
    class AuthenticatedUserTests {

        @Test
        @DisplayName("Utente autenticato dovrebbe bypassare hardening")
        void authenticatedUserShouldBypassHardening() {
            setAuthenticatedUser();

            MockHttpServletRequest request = createAvvisoRequest(ID_DOMINIO, NUMERO_AVVISO);

            boolean result = matcher.matches(request);

            assertTrue(result);
            verifyNoInteractions(configurazioneService);
            verifyNoInteractions(versamentoRepository);
        }
    }

    @Nested
    @DisplayName("Hardening Disabled Tests")
    class HardeningDisabledTests {

        @Test
        @DisplayName("Utente anonimo con hardening disabilitato dovrebbe essere consentito")
        void anonymousUserWithHardeningDisabledShouldBeAllowed() {
            setAnonymousAuthentication();
            when(configurazioneService.getHardening())
                    .thenReturn(Hardening.builder().abilitato(false).build());

            MockHttpServletRequest request = createAvvisoRequest(ID_DOMINIO, NUMERO_AVVISO);

            boolean result = matcher.matches(request);

            assertTrue(result);
            verifyNoInteractions(versamentoRepository);
        }
    }

    @Nested
    @DisplayName("UUID Validation Tests")
    class UuidValidationTests {

        @Test
        @DisplayName("UUID valido con numero avviso dovrebbe consentire accesso")
        void validUuidWithNumeroAvvisoShouldAllowAccess() {
            setAnonymousAuthentication();
            Hardening hardening = createEnabledHardening();
            when(configurazioneService.getHardening()).thenReturn(hardening);
            when(versamentoRepository.findByDominioCodDominioAndNumeroAvvisoAndIdSessione(
                    ID_DOMINIO, NUMERO_AVVISO, VALID_UUID))
                    .thenReturn(Optional.of(new Versamento()));

            MockHttpServletRequest request = createAvvisoRequest(ID_DOMINIO, NUMERO_AVVISO);
            request.setParameter("UUID", VALID_UUID);

            boolean result = matcher.matches(request);

            assertTrue(result);
            verify(versamentoRepository).findByDominioCodDominioAndNumeroAvvisoAndIdSessione(
                    ID_DOMINIO, NUMERO_AVVISO, VALID_UUID);
        }

        @Test
        @DisplayName("UUID valido con IUV dovrebbe consentire accesso")
        void validUuidWithIuvShouldAllowAccess() {
            setAnonymousAuthentication();
            Hardening hardening = createEnabledHardening();
            when(configurazioneService.getHardening()).thenReturn(hardening);
            when(versamentoRepository.findByDominioCodDominioAndIuvVersamentoAndIdSessione(
                    ID_DOMINIO, IUV, VALID_UUID))
                    .thenReturn(Optional.of(new Versamento()));

            MockHttpServletRequest request = createAvvisoRequest(ID_DOMINIO, IUV);
            request.setParameter("UUID", VALID_UUID);

            boolean result = matcher.matches(request);

            assertTrue(result);
            verify(versamentoRepository).findByDominioCodDominioAndIuvVersamentoAndIdSessione(
                    ID_DOMINIO, IUV, VALID_UUID);
        }

        @Test
        @DisplayName("UUID non valido dovrebbe richiedere ReCaptcha")
        void invalidUuidShouldRequireReCaptcha() {
            setAnonymousAuthentication();
            Hardening hardening = createEnabledHardening();
            when(configurazioneService.getHardening()).thenReturn(hardening);
            when(versamentoRepository.findByDominioCodDominioAndNumeroAvvisoAndIdSessione(
                    ID_DOMINIO, NUMERO_AVVISO, "invalid-uuid"))
                    .thenReturn(Optional.empty());

            MockHttpServletRequest request = createAvvisoRequest(ID_DOMINIO, NUMERO_AVVISO);
            request.setParameter("UUID", "invalid-uuid");

            boolean result = matcher.matches(request);

            // Senza ReCaptcha valido, dovrebbe negare
            assertFalse(result);
        }

        @Test
        @DisplayName("UUID mancante dovrebbe richiedere ReCaptcha")
        void missingUuidShouldRequireReCaptcha() {
            setAnonymousAuthentication();
            Hardening hardening = createEnabledHardening();
            when(configurazioneService.getHardening()).thenReturn(hardening);

            MockHttpServletRequest request = createAvvisoRequest(ID_DOMINIO, NUMERO_AVVISO);

            boolean result = matcher.matches(request);

            // Senza ReCaptcha valido, dovrebbe negare
            assertFalse(result);
            verifyNoInteractions(versamentoRepository);
        }

        @Test
        @DisplayName("UUID vuoto dovrebbe richiedere ReCaptcha")
        void emptyUuidShouldRequireReCaptcha() {
            setAnonymousAuthentication();
            Hardening hardening = createEnabledHardening();
            when(configurazioneService.getHardening()).thenReturn(hardening);

            MockHttpServletRequest request = createAvvisoRequest(ID_DOMINIO, NUMERO_AVVISO);
            request.setParameter("UUID", "");

            boolean result = matcher.matches(request);

            // Senza ReCaptcha valido, dovrebbe negare
            assertFalse(result);
            verifyNoInteractions(versamentoRepository);
        }
    }

    @Nested
    @DisplayName("Path Extraction Tests")
    class PathExtractionTests {

        @Test
        @DisplayName("Path non valido dovrebbe negare accesso")
        void invalidPathShouldDenyAccess() {
            // Non impostiamo autenticazione perche' il path non matcha comunque
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/pendenze/avviso");
            request.setPathInfo("/pendenze/avviso");

            boolean result = matcher.matches(request);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Errore database durante verifica UUID dovrebbe proseguire con ReCaptcha")
        void databaseErrorDuringUuidCheckShouldProceedToReCaptcha() {
            setAnonymousAuthentication();
            Hardening hardening = createEnabledHardening();
            when(configurazioneService.getHardening()).thenReturn(hardening);
            when(versamentoRepository.findByDominioCodDominioAndNumeroAvvisoAndIdSessione(
                    any(), any(), any()))
                    .thenThrow(new RuntimeException("DB error"));

            MockHttpServletRequest request = createAvvisoRequest(ID_DOMINIO, NUMERO_AVVISO);
            request.setParameter("UUID", VALID_UUID);

            boolean result = matcher.matches(request);

            // UUID fallito, nessun ReCaptcha → deny
            assertFalse(result);
        }

        @Test
        @DisplayName("Errore getHardening dovrebbe negare accesso")
        void getHardeningErrorShouldDenyAccess() {
            setAnonymousAuthentication();
            when(configurazioneService.getHardening()).thenThrow(new RuntimeException("Config error"));

            MockHttpServletRequest request = createAvvisoRequest(ID_DOMINIO, NUMERO_AVVISO);

            boolean result = matcher.matches(request);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Costruttore senza metodo dovrebbe funzionare")
        void constructorWithoutMethodShouldWork() {
            AvvisiRequestMatcher m = new AvvisiRequestMatcher("/pendenze/*/*/avviso",
                    configurazioneService, versamentoRepository);
            assertNotNull(m);
        }
    }

    private MockHttpServletRequest createAvvisoRequest(String idDominio, String numeroAvvisoOrIuv) {
        String path = "/pendenze/" + idDominio + "/" + numeroAvvisoOrIuv + "/avviso";
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        request.setPathInfo(path);
        return request;
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

    private Hardening createEnabledHardening() {
        GoogleCaptcha googleCaptcha = GoogleCaptcha.builder()
                .serverURL("https://www.google.com/recaptcha/api/siteverify")
                .secretKey("test-secret-key")
                .siteKey("test-site-key")
                .responseParameter("g-recaptcha-response")
                .connectionTimeout(5000)
                .readTimeout(5000)
                .soglia(0.5)
                .denyOnFail(true)
                .build();
        return Hardening.builder()
                .abilitato(true)
                .googleCaptcha(googleCaptcha)
                .build();
    }
}
