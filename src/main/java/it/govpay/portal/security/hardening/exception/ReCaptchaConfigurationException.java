package it.govpay.portal.security.hardening.exception;

public class ReCaptchaConfigurationException extends RuntimeException {

    public ReCaptchaConfigurationException(String message) {
        super(message);
    }

    public ReCaptchaConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
