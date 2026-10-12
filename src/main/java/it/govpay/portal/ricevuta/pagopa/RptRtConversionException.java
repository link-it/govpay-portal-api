package it.govpay.portal.ricevuta.pagopa;

/**
 * Errore non recuperabile nella conversione XML della RT (XML malformato rispetto
 * alla versione attesa, o versione SANP non gestita).
 */
public class RptRtConversionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public RptRtConversionException(String message) {
		super(message);
	}

	public RptRtConversionException(String message, Throwable cause) {
		super(message, cause);
	}
}
