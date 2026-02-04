package it.govpay.portal.utils.trasformazioni.exception;

/**
 * Eccezione lanciata quando un'espressione JsonPath non è valida.
 */
public class JsonPathNotValidException extends TrasformazioneException {

    private static final long serialVersionUID = 1L;

    public JsonPathNotValidException(String message) {
        super(message);
    }

    public JsonPathNotValidException(String message, Throwable cause) {
        super(message, cause);
    }
}
