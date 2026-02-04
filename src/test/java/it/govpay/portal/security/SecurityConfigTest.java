package it.govpay.portal.security;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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
        @DisplayName("GET /logout senza CSRF dovrebbe restituire 403")
        void getLogoutWithoutCsrfShouldReturn403() throws Exception {
            // Con CSRF abilitato, GET /logout richiede autenticazione
            mockMvc.perform(get("/logout"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("POST /logout con CSRF dovrebbe restituire redirect")
        void postLogoutWithCsrfShouldReturnRedirect() throws Exception {
            mockMvc.perform(post("/logout")
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection());
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
}
