package it.govpay.portal.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class IuvUtils {

	private IuvUtils() {}

	private static final DecimalFormat nFormatter = new DecimalFormat("00.00", new DecimalFormatSymbols(Locale.ENGLISH));

	public static String buildQrCode002(String codDominio, int auxDigit, int applicationCode, String iuv, BigDecimal importoTotale, String numeroAvviso) {
		String qrCode = null; 
		if(numeroAvviso == null) {
			if(auxDigit == 0)
				qrCode = "PAGOPA|002|0" + String.format("%02d", applicationCode) + iuv + "|" + codDominio + "|" + (nFormatter.format(importoTotale).replace(".", ""));
			else 
				qrCode = "PAGOPA|002|" + auxDigit + iuv + "|" + codDominio + "|" + (nFormatter.format(importoTotale).replace(".", ""));
		} else {
			qrCode = "PAGOPA|002|" + numeroAvviso + "|" + codDominio + "|" + (nFormatter.format(importoTotale).replace(".", ""));
		}

		return qrCode;
	}

	public static String buildBarCode(String gln, int auxDigit, int applicationCode, String iuv, BigDecimal importoTotale, String numeroAvviso) {
		// Da Guida Tecnica di Adesione PA 3.8 pag 25 
		String payToLoc = "415";
		String refNo = "8020";
		String amount = "3902";
		String importo = nFormatter.format(importoTotale).replace(".", "");

		if(numeroAvviso == null) {
			if(auxDigit == 3)
				return payToLoc + gln + refNo + "3" + iuv + amount + importo;
			else 
				return payToLoc + gln + refNo + "0" + String.format("%02d", applicationCode) + iuv + amount + importo;
		} else {
			return payToLoc + gln + refNo + numeroAvviso + amount + importo;
		}

	}

	public static String toNumeroAvviso(String iuv, int auxDigit, int applicationCode) {
		if (auxDigit == 0) {
			return auxDigit + String.format("%02d", applicationCode) + iuv;
		}
		return auxDigit + iuv;
	}

	public static String toIuv(String numeroAvviso) {
		if (numeroAvviso == null) {
			return null;
		}

		if (numeroAvviso.length() != 18) {
			throw new IllegalArgumentException("Numero Avviso [" + numeroAvviso + "] fornito non valido: Consentite 18 cifre trovate [" + numeroAvviso.length() + "].");
		}

		try {
			Long.parseLong(numeroAvviso);
		} catch (Exception e) {
			throw new IllegalArgumentException("Numero Avviso [" + numeroAvviso + "] fornito non valido: non e' in formato numerico.");
		}

		if (numeroAvviso.startsWith("0")) { // '0' + applicationCode(2) + ref(13) + check(2)
			return numeroAvviso.substring(3);
		} else if (numeroAvviso.startsWith("1")) { // '1' + reference(17)
			return numeroAvviso.substring(1);
		} else if (numeroAvviso.startsWith("2")) { // '2' + ref(15) + check(2)
			return numeroAvviso.substring(1);
		} else if (numeroAvviso.startsWith("3")) { // '3' + segregationCode(2) + ref(13) + check(2)
			return numeroAvviso.substring(1);
		} else {
			throw new IllegalArgumentException("Numero Avviso [" + numeroAvviso + "] fornito non valido: prima cifra non e' [0|1|2|3]");
		}
	}

	public static boolean checkIuvNumerico(String iuv, int auxDigit, int applicationCode) {
		if (iuv.length() == 15 && auxDigit == 0) {
			String reference = iuv.substring(0, 13);
			long resto93 = (Long.parseLong(String.valueOf(auxDigit) + String.format("%02d", applicationCode) + reference)) % 93;
			return iuv.equals(reference + String.format("%02d", resto93));
		} else if (iuv.length() == 17 && auxDigit == 3) {
			String reference = iuv.substring(0, 15);
			long resto93 = (Long.parseLong(String.valueOf(auxDigit) + reference)) % 93;
			return iuv.equals(reference + String.format("%02d", resto93));
		} else {
			return false;
		}
	}

}
