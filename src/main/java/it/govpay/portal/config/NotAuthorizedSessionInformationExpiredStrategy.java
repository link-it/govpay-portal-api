package it.govpay.portal.config;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

import tools.jackson.databind.json.JsonMapper;

import jakarta.servlet.http.HttpServletResponse;

public class NotAuthorizedSessionInformationExpiredStrategy implements SessionInformationExpiredStrategy {

    private static final JsonMapper OBJECT_MAPPER = JsonMapper.builder().build();

    @Override
    public void onExpiredSessionDetected(SessionInformationExpiredEvent event) throws IOException {
        HttpServletResponse response = event.getResponse();
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        OBJECT_MAPPER.writeValue(response.getOutputStream(), Map.of(
                "categoria", "AUTORIZZAZIONE",
                "codice", "AUTENTICAZIONE",
                "descrizione", "Sessione scaduta",
                "dettaglio", "La sessione risulta scaduta per superamento del numero massimo di sessioni concorrenti"
        ));
    }
}
