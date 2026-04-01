package it.govpay.portal.config;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.govpay.portal.gde.Costanti;
import it.govpay.portal.gde.service.GdeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PortalLogoutSuccessHandler implements LogoutSuccessHandler {

    private final SecurityProperties securityProperties;
    private final GdeService gdeService;

    public PortalLogoutSuccessHandler(SecurityProperties securityProperties, GdeService gdeService) {
        this.securityProperties = securityProperties;
        this.gdeService = gdeService;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {

        OffsetDateTime now = OffsetDateTime.now();
        String principal = extractPrincipal(authentication);
        String urlID = extractUrlID(request.getRequestURI());

        if (urlID == null) {
            // GET /logout -> 200 OK
            log.info("Logout effettuato per l'utente [{}]", principal);
            gdeService.saveEventOk(Costanti.OP_LOGOUT, now, OffsetDateTime.now(),
                    request, HttpStatus.OK.value(), null, null, null, principal);
            response.setStatus(HttpStatus.OK.value());
            return;
        }

        // GET /logout/{urlID} -> 303 redirect
        String redirectUrl = securityProperties.getLogoutRedirectUrls().get(urlID);

        if (redirectUrl == null) {
            log.warn("Logout con urlID [{}] non configurato per l'utente [{}]", urlID, principal);
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            new ObjectMapper().writeValue(response.getOutputStream(), Map.of(
                    "categoria", "RICHIESTA",
                    "codice", "404",
                    "descrizione", "URL-ID non registrato",
                    "dettaglio", "URL-ID non registrato: " + urlID
            ));
            return;
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(redirectUrl);
        request.getParameterMap().forEach((key, values) -> {
            for (String value : values) {
                builder.queryParam(key, value);
            }
        });
        String location = builder.build().toUri().toString();

        log.info("Logout con redirect effettuato per l'utente [{}], redirect verso [{}]", principal, location);
        gdeService.saveEventOk(Costanti.OP_LOGOUT, now, OffsetDateTime.now(),
                request, HttpStatus.SEE_OTHER.value(), null, null, null, principal);
        response.setStatus(HttpStatus.SEE_OTHER.value());
        response.setHeader("Location", location);
    }

    private String extractUrlID(String requestUri) {
        // /logout -> null, /logout/portale -> "portale"
        String prefix = "/logout/";
        if (requestUri.startsWith(prefix) && requestUri.length() > prefix.length()) {
            return requestUri.substring(prefix.length());
        }
        return null;
    }

    private String extractPrincipal(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof SpidUserDetails spidUser) {
            return spidUser.getFiscalNumber();
        }
        return null;
    }
}
