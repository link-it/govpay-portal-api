package it.govpay.portal.utils.trasformazioni.exception;

/**
 * Eccezione lanciata quando un'espressione JsonPath non trova risultati.
 */
public class JsonPathNotFoundException extends TrasformazioneException {

    private static final long serialVersionUID = 1L;

    public JsonPathNotFoundException(String message) {
        super(message);
    }

    public JsonPathNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
