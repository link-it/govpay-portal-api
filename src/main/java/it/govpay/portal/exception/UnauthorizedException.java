package it.govpay.portal.exception;

/**
 * Eccezione per autenticazione mancante o non valida (401 Unauthorized).
 */
public class UnauthorizedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
