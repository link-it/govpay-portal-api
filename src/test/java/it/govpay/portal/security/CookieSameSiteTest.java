package it.govpay.portal.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

/**
 * Verifica l'attributo SameSite nell'header Set-Cookie prodotto dal Tomcat embedded
 * (segnalazione OWASP ZAP 10054). Serve il server reale: MockMvc non serializza
 * l'attributo SameSite e non applica server.servlet.session.cookie.same-site.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CookieSameSiteTest {

    @LocalServerPort
    private int port;

    @Test
    @DisplayName("Il cookie JSESSIONID creato al login ha SameSite=Lax")
    void sessionCookieShouldHaveSameSiteLax() throws Exception {
        HttpResponse<Void> response = get("/login", true);

        String setCookie = findSetCookie(response, "JSESSIONID");
        assertTrue(setCookie.contains("SameSite=Lax"), "SameSite=Lax atteso: " + setCookie);
    }

    @Test
    @DisplayName("Il cookie XSRF-TOKEN ha SameSite=Lax e non e' HttpOnly")
    void csrfCookieShouldHaveSameSiteLax() throws Exception {
        HttpResponse<Void> response = get("/domini", false);

        String setCookie = findSetCookie(response, "XSRF-TOKEN");
        assertTrue(setCookie.contains("SameSite=Lax"), "SameSite=Lax atteso: " + setCookie);
        // HttpOnly assente per scelta: Angular legge il cookie per l'header X-XSRF-TOKEN
        assertFalse(setCookie.contains("HttpOnly"), "HttpOnly non atteso: " + setCookie);
    }

    private HttpResponse<Void> get(String path, boolean spid) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Accept", "application/json")
                .GET();
        if (spid) {
            builder.header("X-SPID-FISCALNUMBER", "RSSMRA80A01H501U");
        }
        try (HttpClient client = HttpClient.newHttpClient()) {
            return client.send(builder.build(), HttpResponse.BodyHandlers.discarding());
        }
    }

    private static String findSetCookie(HttpResponse<Void> response, String name) {
        return response.headers().allValues("Set-Cookie").stream()
                .filter(h -> h.startsWith(name + "="))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Set-Cookie " + name + " assente: " + response.headers()));
    }
}
