package it.govpay.portal.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Eccezione per errori dal servizio upstream (502 Bad Gateway).
 * Usata quando GovPay o altri servizi backend restituiscono un errore.
 */
@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class BadGatewayException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BadGatewayException(String message) {
        super(message);
    }

    public BadGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}
