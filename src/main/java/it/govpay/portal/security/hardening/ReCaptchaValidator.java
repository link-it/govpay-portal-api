package it.govpay.portal.security.hardening;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import it.govpay.common.configurazione.model.GoogleCaptcha;
import it.govpay.common.configurazione.model.Hardening;
import it.govpay.portal.security.hardening.exception.ReCaptchaConfigurationException;
import it.govpay.portal.security.hardening.model.CaptchaResponse;
import jakarta.servlet.http.HttpServletRequest;

public class ReCaptchaValidator {

    private static final Logger log = LoggerFactory.getLogger(ReCaptchaValidator.class);
    private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";

    private final Hardening hardeningSettings;
    private final RestTemplate restTemplate;

    public ReCaptchaValidator(Hardening settings) {
        this.hardeningSettings = settings;

        GoogleCaptcha captcha = settings.getGoogleCatpcha();
        if (captcha == null) {
            throw new ReCaptchaConfigurationException("Configurazione Google ReCaptcha non presente");
        }

        validateConfiguration(captcha);

        this.restTemplate = new RestTemplateBuilder()
                .connectTimeout(Duration.ofMillis(captcha.getConnectionTimeout()))
                .readTimeout(Duration.ofMillis(captcha.getReadTimeout()))
                .build();
    }

    private void validateConfiguration(GoogleCaptcha captcha) {
        if (!StringUtils.hasText(captcha.getServerURL())) {
            throw new ReCaptchaConfigurationException("URL servizio Google ReCaptcha non presente");
        }
        if (!StringUtils.hasText(captcha.getSecretKey())) {
            throw new ReCaptchaConfigurationException("Secret Key Google ReCaptcha non presente");
        }
        if (!StringUtils.hasText(captcha.getResponseParameter())) {
            throw new ReCaptchaConfigurationException("Response Parameter Google ReCaptcha non presente");
        }
        if (captcha.getConnectionTimeout() <= 0) {
            throw new ReCaptchaConfigurationException("Connection Timeout deve essere > 0");
        }
        if (captcha.getReadTimeout() <= 0) {
            throw new ReCaptchaConfigurationException("Read Timeout deve essere > 0");
        }
        if (captcha.getSoglia() <= 0 || captcha.getSoglia() > 1) {
            throw new ReCaptchaConfigurationException("Soglia deve essere compresa tra 0 e 1");
        }
    }

    public boolean validate(HttpServletRequest request) {
        GoogleCaptcha captcha = hardeningSettings.getGoogleCatpcha();
        String responseParameter = captcha.getResponseParameter();

        // Leggi il token ReCaptcha dalla request (parametro o header)
        String reCaptchaResponse = request.getParameter(responseParameter);
        if (!StringUtils.hasText(reCaptchaResponse)) {
            reCaptchaResponse = request.getHeader(responseParameter);
        }

        if (!StringUtils.hasText(reCaptchaResponse)) {
            log.warn("Parametro ReCaptcha '{}' non trovato nella request", responseParameter);
            return false;
        }

        // Costruisci URL di verifica
        String payload = String.format("secret=%s&response=%s&remoteip=%s",
                URLEncoder.encode(captcha.getSecretKey(), StandardCharsets.UTF_8),
                URLEncoder.encode(reCaptchaResponse, StandardCharsets.UTF_8),
                URLEncoder.encode(getClientIP(request), StandardCharsets.UTF_8));

        StringBuilder urlBuilder = new StringBuilder(captcha.getServerURL());
        urlBuilder.append(captcha.getServerURL().contains("?") ? "&" : "?");
        urlBuilder.append(payload);

        URI verifyUri = URI.create(urlBuilder.toString());

        try {
            log.debug("Richiesta validazione ReCaptcha alla URL: {}", verifyUri);
            CaptchaResponse googleResponse = restTemplate.getForObject(verifyUri, CaptchaResponse.class);

            if (googleResponse == null) {
                log.warn("Risposta ReCaptcha null");
                return false;
            }

            log.debug("Risposta ReCaptcha: success={}, score={}", googleResponse.isSuccess(), googleResponse.getScore());

            if (!googleResponse.isSuccess()) {
                log.warn("Verifica ReCaptcha fallita: {}", Arrays.toString(googleResponse.getErrorCodes()));
                return false;
            }

            // Controllo soglia (solo per ReCaptcha v3)
            BigDecimal score = googleResponse.getScore();
            if (score != null && score.doubleValue() < captcha.getSoglia()) {
                log.warn("Score ReCaptcha insufficiente: {} < {}", score, captcha.getSoglia());
                return false;
            }

            return true;

        } catch (RestClientException e) {
            log.error("Errore durante la verifica ReCaptcha: {}", e.getMessage());
            if (captcha.isDenyOnFail()) {
                return false;
            } else {
                log.warn("Servizio ReCaptcha non disponibile, accesso consentito per configurazione denyOnFail=false");
                return true;
            }
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader(HEADER_X_FORWARDED_FOR);
        if (StringUtils.hasText(xfHeader)) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
