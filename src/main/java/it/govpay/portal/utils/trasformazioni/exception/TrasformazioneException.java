package it.govpay.portal.utils.trasformazioni.exception;

/**
 * Eccezione base per errori durante le trasformazioni FreeMarker.
 */
public class TrasformazioneException extends Exception {

    private static final long serialVersionUID = 1L;

    public TrasformazioneException(String message) {
        super(message);
    }

    public TrasformazioneException(String message, Throwable cause) {
        super(message, cause);
    }

    public TrasformazioneException(Throwable cause) {
        super(cause);
    }
}
