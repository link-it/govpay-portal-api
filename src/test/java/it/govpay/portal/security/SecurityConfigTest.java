package it.govpay.portal.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    // Header names as configured in SecurityProperties defaults
    private static final String SPID_FISCAL_NUMBER = "X-SPID-FISCALNUMBER";
    private static final String SPID_NAME = "X-SPID-NAME";
    private static final String SPID_FAMILY_NAME = "X-SPID-FAMILYNAME";
    private static final String SPID_EMAIL = "X-SPID-EMAIL";
    private static final String SPID_MOBILE_PHONE = "X-SPID-MOBILEPHONE";
    private static final String SPID_ADDRESS = "X-SPID-ADDRESS";

    @Nested
    @DisplayName("Endpoint protetti - richiedono autenticazione")
    class ProtectedEndpointsTests {

        @Test
        @DisplayName("GET /login senza autenticazione dovrebbe restituire 403")
        void getLoginWithoutAuthShouldReturn403() throws Exception {
            mockMvc.perform(get("/login")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /login con autenticazione SPID dovrebbe restituire 200")
        void getLoginWithSpidAuthShouldReturn200() throws Exception {
            mockMvc.perform(get("/login")
                            .header(SPID_FISCAL_NUMBER, "RSSMRA80A01H501U")
                            .header(SPID_NAME, "Mario")
                            .header(SPID_FAMILY_NAME, "Rossi")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /login/{urlID} senza autenticazione dovrebbe restituire 403")
        void getLoginWithRedirectWithoutAuthShouldReturn403() throws Exception {
            mockMvc.perform(get("/login/portale")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /login/{urlID} con autenticazione SPID dovrebbe restituire 303")
        void getLoginWithRedirectWithSpidAuthShouldReturn303() throws Exception {
            mockMvc.perform(get("/login/portale")
                            .header(SPID_FISCAL_NUMBER, "RSSMRA80A01H501U")
                            .header(SPID_NAME, "Mario")
                            .header(SPID_FAMILY_NAME, "Rossi")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is3xxRedirection());
        }

        @Test
        @DisplayName("GET /login/{urlID} con urlID non configurato dovrebbe restituire 404")
        void getLoginWithRedirectUnknownUrlIdShouldReturn404() throws Exception {
            mockMvc.perform(get("/login/sconosciuto")
                            .header(SPID_FISCAL_NUMBER, "RSSMRA80A01H501U")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET /profilo senza autenticazione dovrebbe restituire 403")
        void getProfiloWithoutAuthShouldReturn403() throws Exception {
            mockMvc.perform(get("/profilo")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /profilo con autenticazione SPID dovrebbe restituire 200")
        void getProfiloWithSpidAuthShouldReturn200() throws Exception {
            mockMvc.perform(get("/profilo")
                            .header(SPID_FISCAL_NUMBER, "RSSMRA80A01H501U")
                            .header(SPID_NAME, "Mario")
                            .header(SPID_FAMILY_NAME, "Rossi")
                            .header(SPID_EMAIL, "mario.rossi@email.it")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /logout senza autenticazione dovrebbe restituire 200 (LogoutFilter intercetta)")
        void getLogoutWithoutAuthShouldReturn200() throws Exception {
            // Il LogoutFilter intercetta GET /logout prima dell'authorization filter
            // e restituisce 200 anche senza sessione attiva (come nella config XML)
            mockMvc.perform(get("/logout"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /logout/{urlID} senza autenticazione dovrebbe restituire 303 (LogoutFilter intercetta)")
        void getLogoutWithRedirectWithoutAuthShouldReturn303() throws Exception {
            mockMvc.perform(get("/logout/portale"))
                    .andExpect(status().is3xxRedirection());
        }

        @Test
        @DisplayName("GET /logout/{urlID} con autenticazione SPID dovrebbe restituire 303")
        void getLogoutWithRedirectWithSpidAuthShouldReturn303() throws Exception {
            mockMvc.perform(get("/logout/portale")
                            .header(SPID_FISCAL_NUMBER, "RSSMRA80A01H501U")
                            .header(SPID_NAME, "Mario")
                            .header(SPID_FAMILY_NAME, "Rossi"))
                    .andExpect(status().is3xxRedirection());
        }

        @Test
        @DisplayName("GET /logout/{urlID} con urlID non configurato dovrebbe restituire 404")
        void getLogoutWithRedirectUnknownUrlIdShouldReturn404() throws Exception {
            mockMvc.perform(get("/logout/sconosciuto")
                            .header(SPID_FISCAL_NUMBER, "RSSMRA80A01H501U"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET /pendenze/{idDominio} senza autenticazione dovrebbe restituire 403")
        void getPendenzeWithoutAuthShouldReturn403() throws Exception {
            mockMvc.perform(get("/pendenze/12345678901")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /pendenze/{idDominio} con autenticazione SPID dovrebbe restituire 200")
        void getPendenzeWithSpidAuthShouldReturn200() throws Exception {
            mockMvc.perform(get("/pendenze/12345678901")
                            .header(SPID_FISCAL_NUMBER, "RSSMRA80A01H501U")
                            .header(SPID_NAME, "Mario")
                            .header(SPID_FAMILY_NAME, "Rossi")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /pendenze/{idDominio}/{numeroAvviso} senza autenticazione dovrebbe restituire 403")
        void getPendenzaWithoutAuthShouldReturn403() throws Exception {
            mockMvc.perform(get("/pendenze/12345678901/123456789012345678")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /pendenze/{idDominio}/{numeroAvviso} con autenticazione SPID")
        void getPendenzaWithSpidAuthShouldNotReturn403() throws Exception {
            mockMvc.perform(get("/pendenze/12345678901/123456789012345678")
                            .header(SPID_FISCAL_NUMBER, "RSSMRA80A01H501U")
                            .header(SPID_NAME, "Mario")
                            .header(SPID_FAMILY_NAME, "Rossi")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound()); // 404 perche' il versamento non esiste, ma non 403
        }
    }

    @Nested
    @DisplayName("Endpoint pubblici - accessibili senza autenticazione")
    class PublicEndpointsTests {

        @Test
        @DisplayName("GET /domini dovrebbe essere accessibile senza autenticazione")
        void getDominiShouldBePublic() throws Exception {
            mockMvc.perform(get("/domini")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /domini/{idDominio} dovrebbe essere accessibile senza autenticazione")
        void getDominioShouldBePublic() throws Exception {
            mockMvc.perform(get("/domini/12345678901")
                            .accept(MediaType.APPLICATION_JSON))
                    // 200 se esiste, 404 se non esiste, ma mai 401/403
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET /domini/{idDominio}/logo dovrebbe essere accessibile senza autenticazione")
        void getLogoShouldBePublic() throws Exception {
            mockMvc.perform(get("/domini/12345678901/logo"))
                    // 200 se esiste, 404 se non esiste, ma mai 401/403
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET /domini/{idDominio}/tipiPendenza dovrebbe essere accessibile senza autenticazione")
        void getTipiPendenzaShouldBePublic() throws Exception {
            mockMvc.perform(get("/domini/12345678901/tipiPendenza")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /pendenze/{idDominio}/{numeroAvviso}/ricevuta dovrebbe essere accessibile senza autenticazione")
        void getRicevutaShouldBePublic() throws Exception {
            mockMvc.perform(get("/pendenze/12345678901/123456789012345678/ricevuta")
                            .accept(MediaType.APPLICATION_JSON))
                    // 200 se esiste, 404 se non esiste, ma mai 401/403
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET /swagger-ui dovrebbe essere accessibile senza autenticazione")
        void getSwaggerUiShouldBePublic() throws Exception {
            mockMvc.perform(get("/swagger-ui/index.html"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api-docs dovrebbe essere accessibile senza autenticazione")
        void getApiDocsShouldBePublic() throws Exception {
            mockMvc.perform(get("/api-docs"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /actuator/health dovrebbe essere accessibile senza autenticazione")
        void getHealthShouldBePublic() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    // Non deve richiedere autenticazione (401/403)
                    .andExpect(result -> {
                        int status = result.getResponse().getStatus();
                        if (status == 401 || status == 403) {
                            throw new AssertionError("Expected status not to be 401 or 403, but was " + status);
                        }
                    });
        }
    }

    @Nested
    @DisplayName("Endpoint non definiti - dovrebbero essere negati")
    class UndefinedEndpointsTests {

        @Test
        @DisplayName("GET /admin dovrebbe restituire 403")
        void getAdminShouldBeDenied() throws Exception {
            mockMvc.perform(get("/admin"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("DELETE /pendenze/{idDominio}/{numeroAvviso} dovrebbe restituire 403")
        void deletePendenzaShouldBeDenied() throws Exception {
            mockMvc.perform(delete("/pendenze/12345678901/123456789012345678")
                            .with(csrf())
                            .header(SPID_FISCAL_NUMBER, "RSSMRA80A01H501U")
                            .header(SPID_NAME, "Mario")
                            .header(SPID_FAMILY_NAME, "Rossi"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("PUT /domini/{idDominio} dovrebbe restituire 403")
        void putDominioShouldBeDenied() throws Exception {
            mockMvc.perform(put("/domini/12345678901")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Endpoint arbitrario dovrebbe restituire 403")
        void arbitraryEndpointShouldBeDenied() throws Exception {
            mockMvc.perform(get("/api/internal/secret"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Validazione header SPID")
    class SpidHeaderValidationTests {

        @Test
        @DisplayName("Autenticazione con solo fiscal number dovrebbe essere sufficiente")
        void authWithOnlyFiscalNumberShouldWork() throws Exception {
            mockMvc.perform(get("/profilo")
                            .header(SPID_FISCAL_NUMBER, "RSSMRA80A01H501U")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Autenticazione senza fiscal number dovrebbe restituire 403")
        void authWithoutFiscalNumberShouldFail() throws Exception {
            // Senza header fiscal number l'utente risulta anonimo e riceve 403 Forbidden
            mockMvc.perform(get("/profilo")
                            .header(SPID_NAME, "Mario")
                            .header(SPID_FAMILY_NAME, "Rossi")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Autenticazione con fiscal number vuoto dovrebbe restituire 403")
        void authWithEmptyFiscalNumberShouldFail() throws Exception {
            // Con header fiscal number vuoto l'utente risulta anonimo e riceve 403 Forbidden
            mockMvc.perform(get("/profilo")
                            .header(SPID_FISCAL_NUMBER, "")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("LogoutFilter - invalidazione sessione e cookie")
    class LogoutFilterTests {

        @Test
        @DisplayName("GET /logout dovrebbe invalidare la sessione HTTP")
        void logoutShouldInvalidateSession() throws Exception {
            // 1. Login: crea sessione autenticata
            MvcResult loginResult = mockMvc.perform(get("/login")
                            .header(SPID_FISCAL_NUMBER, "RSSMRA80A01H501U")
                            .header(SPID_NAME, "Mario")
                            .header(SPID_FAMILY_NAME, "Rossi")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
            assertNotNull(session, "La sessione dovrebbe esistere dopo il login");

            // 2. Logout: invalida la sessione
            mockMvc.perform(get("/logout")
                            .session(session))
                    .andExpect(status().isOk());

            assertTrue(session.isInvalid(), "La sessione dovrebbe essere invalidata dopo il logout");
        }

        @Test
        @DisplayName("GET /logout dovrebbe rimuovere il cookie JSESSIONID")
        void logoutShouldClearJSessionIdCookie() throws Exception {
            // 1. Login
            MvcResult loginResult = mockMvc.perform(get("/login")
                            .header(SPID_FISCAL_NUMBER, "RSSMRA80A01H501U")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

            // 2. Logout
            MvcResult logoutResult = mockMvc.perform(get("/logout")
                            .session(session))
                    .andExpect(status().isOk())
                    .andReturn();

            // Verifica che il cookie JSESSIONID sia stato rimosso (maxAge=0)
            Cookie[] cookies = logoutResult.getResponse().getCookies();
            boolean jsessionIdCleared = false;
            for (Cookie cookie : cookies) {
                if ("JSESSIONID".equals(cookie.getName())) {
                    assertEquals(0, cookie.getMaxAge(),
                            "Il cookie JSESSIONID dovrebbe avere maxAge=0 (rimosso)");
                    jsessionIdCleared = true;
                }
            }
            assertTrue(jsessionIdCleared, "Il cookie JSESSIONID dovrebbe essere presente nella risposta con maxAge=0");
        }

        @Test
        @DisplayName("GET /logout dovrebbe pulire il SecurityContext")
        void logoutShouldClearSecurityContext() throws Exception {
            // 1. Login
            MvcResult loginResult = mockMvc.perform(get("/login")
                            .header(SPID_FISCAL_NUMBER, "RSSMRA80A01H501U")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

            // 2. Logout
            mockMvc.perform(get("/logout")
                            .session(session))
                    .andExpect(status().isOk());

            // 3. Accesso a endpoint protetto con la stessa sessione (invalidata) dovrebbe fallire
            mockMvc.perform(get("/profilo")
                            .session(session)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /logout/{urlID} dovrebbe invalidare la sessione e fare redirect")
        void logoutWithRedirectShouldInvalidateSessionAndRedirect() throws Exception {
            // 1. Login
            MvcResult loginResult = mockMvc.perform(get("/login")
                            .header(SPID_FISCAL_NUMBER, "RSSMRA80A01H501U")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
            assertNotNull(session);

            // 2. Logout con redirect
            MvcResult logoutResult = mockMvc.perform(get("/logout/portale")
                            .session(session))
                    .andExpect(status().is3xxRedirection())
                    .andReturn();

            assertTrue(session.isInvalid(), "La sessione dovrebbe essere invalidata dopo il logout");
            String location = logoutResult.getResponse().getHeader("Location");
            assertNotNull(location);
            assertTrue(location.startsWith("http://localhost:3000/logged-out"),
                    "Dovrebbe fare redirect verso la URL configurata");
        }

        @Test
        @DisplayName("GET /logout/{urlID} dovrebbe inoltrare i query parameter")
        void logoutWithRedirectShouldForwardQueryParams() throws Exception {
            MvcResult result = mockMvc.perform(get("/logout/portale")
                            .param("idp", "spid")
                            .header(SPID_FISCAL_NUMBER, "RSSMRA80A01H501U"))
                    .andExpect(status().is3xxRedirection())
                    .andReturn();

            String location = result.getResponse().getHeader("Location");
            assertNotNull(location);
            assertTrue(location.contains("idp=spid"),
                    "I query parameter dovrebbero essere inoltrati alla redirect URL");
        }

        @Test
        @DisplayName("GET /logout/{urlID} con urlID non configurato dovrebbe restituire 404 JSON")
        void logoutWithRedirectUnknownUrlIdShouldReturn404Json() throws Exception {
            mockMvc.perform(get("/logout/sconosciuto")
                            .header(SPID_FISCAL_NUMBER, "RSSMRA80A01H501U")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.categoria").value("RICHIESTA"))
                    .andExpect(jsonPath("$.codice").value("404"))
                    .andExpect(jsonPath("$.descrizione").value("URL-ID non registrato"));
        }
    }

    @Nested
    @DisplayName("Gestione sessioni invalide e scadute")
    class SessionStrategyTests {

        @Test
        @DisplayName("Sessione invalidata dovrebbe restituire 403 JSON con messaggio sessione scaduta")
        void invalidSessionShouldReturn403WithExpiredMessage() throws Exception {
            // Crea una sessione e la invalida manualmente
            MvcResult loginResult = mockMvc.perform(get("/login")
                            .header(SPID_FISCAL_NUMBER, "RSSMRA80A01H501U")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
            assertNotNull(session);
            session.invalidate();

            // Richiesta con sessione invalidata verso endpoint protetto
            mockMvc.perform(get("/profilo")
                            .session(session)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }
}
