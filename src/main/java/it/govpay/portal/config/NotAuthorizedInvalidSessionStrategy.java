package it.govpay.portal.config;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.session.InvalidSessionStrategy;

import tools.jackson.databind.json.JsonMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class NotAuthorizedInvalidSessionStrategy implements InvalidSessionStrategy {

    private static final JsonMapper OBJECT_MAPPER = JsonMapper.builder().build();

    private boolean createNewSession = false;

    @Override
    public void onInvalidSessionDetected(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (createNewSession) {
            request.getSession(true);
        }
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        OBJECT_MAPPER.writeValue(response.getOutputStream(), Map.of(
                "categoria", "AUTORIZZAZIONE",
                "codice", "AUTENTICAZIONE",
                "descrizione", "Sessione scaduta",
                "dettaglio", "La sessione risulta scaduta o non valida"
        ));
    }

    public void setCreateNewSession(boolean createNewSession) {
        this.createNewSession = createNewSession;
    }
}
