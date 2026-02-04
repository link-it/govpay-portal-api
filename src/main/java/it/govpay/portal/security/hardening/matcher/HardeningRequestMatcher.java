package it.govpay.portal.security.hardening.matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import it.govpay.portal.security.hardening.ReCaptchaValidator;
import it.govpay.portal.security.hardening.exception.ReCaptchaConfigurationException;
import it.govpay.portal.security.hardening.model.Hardening;
import it.govpay.portal.service.ConfigurazioneService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * RequestMatcher che applica controlli di hardening (ReCaptcha) per le richieste anonime.
 * Se l'utente è autenticato, il matcher restituisce sempre true.
 * Se l'utente è anonimo e l'hardening è abilitato, verifica il ReCaptcha.
 */
public class HardeningRequestMatcher implements RequestMatcher {

    private static final Logger log = LoggerFactory.getLogger(HardeningRequestMatcher.class);

    private final AntPathRequestMatcher pathMatcher;
    private final ConfigurazioneService configurazioneService;

    public HardeningRequestMatcher(String pattern, ConfigurazioneService configurazioneService) {
        this.pathMatcher = new AntPathRequestMatcher(pattern);
        this.configurazioneService = configurazioneService;
    }

    public HardeningRequestMatcher(String pattern, HttpMethod method, ConfigurazioneService configurazioneService) {
        this.pathMatcher = new AntPathRequestMatcher(pattern, method != null ? method.name() : null);
        this.configurazioneService = configurazioneService;
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        // Prima verifica se il path corrisponde
        if (!pathMatcher.matches(request)) {
            return false;
        }

        // Se l'utente è autenticato, consenti l'accesso
        if (isAuthenticated()) {
            log.debug("Utente autenticato, accesso consentito a {}", request.getRequestURI());
            return true;
        }

        // Utente anonimo: applica controlli di hardening
        return applyHardening(request);
    }

    protected boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }

    protected boolean applyHardening(HttpServletRequest request) {
        try {
            Hardening hardening = configurazioneService.getHardening();

            if (!hardening.isAbilitato()) {
                log.debug("Hardening disabilitato, accesso consentito a {}", request.getRequestURI());
                return true;
            }

            log.debug("Applico controlli hardening per {}", request.getRequestURI());
            return validateReCaptcha(request, hardening);

        } catch (Exception e) {
            log.error("Errore durante i controlli di hardening: {}", e.getMessage(), e);
            return false;
        }
    }

    protected boolean validateReCaptcha(HttpServletRequest request, Hardening hardening) {
        try {
            ReCaptchaValidator validator = new ReCaptchaValidator(hardening);
            boolean valid = validator.validate(request);
            log.debug("Validazione ReCaptcha completata con esito: {}", valid);
            return valid;
        } catch (ReCaptchaConfigurationException e) {
            log.error("Configurazione ReCaptcha non valida: {}", e.getMessage());
            return false;
        }
    }

    public AntPathRequestMatcher getPathMatcher() {
        return pathMatcher;
    }

    protected ConfigurazioneService getConfigurazioneService() {
        return configurazioneService;
    }
}
