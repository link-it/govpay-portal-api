package it.govpay.portal.entity;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Utility per la decodifica della causale versamento dal formato encoded nel DB.
 */
public final class CausaleUtils {

    private CausaleUtils() {
        // utility class
    }

    /**
     * Decodifica una causale dal formato encoded presente nel database.
     *
     * @param encodedCausale la stringa encoded (es. "01 base64...", "02 base64...", "03 base64...")
     * @return l'oggetto Causale corrispondente, o null se la stringa è vuota
     * @throws UnsupportedEncodingException se il formato non è riconosciuto
     */
    public static Causale decode(String encodedCausale) throws UnsupportedEncodingException {
        if (encodedCausale == null || encodedCausale.trim().isEmpty()) {
            return null;
        }

        String[] causaleSplit = encodedCausale.split(" ");

        if ("01".equals(causaleSplit[0])) {
            CausaleSemplice causale = new CausaleSemplice();
            if (causaleSplit.length > 1 && causaleSplit[1] != null) {
                causale.setCausale(new String(Base64.getDecoder().decode(causaleSplit[1]), StandardCharsets.UTF_8));
                return causale;
            } else {
                return null;
            }
        }

        if ("02".equals(causaleSplit[0])) {
            List<String> spezzoni = new ArrayList<>();
            for (int i = 1; i < causaleSplit.length; i++) {
                spezzoni.add(new String(Base64.getDecoder().decode(causaleSplit[i]), StandardCharsets.UTF_8));
            }
            CausaleSpezzoni causale = new CausaleSpezzoni();
            causale.setSpezzoni(spezzoni);
            return causale;
        }

        if ("03".equals(causaleSplit[0])) {
            List<String> spezzoni = new ArrayList<>();
            List<BigDecimal> importi = new ArrayList<>();

            for (int i = 1; i < causaleSplit.length; i = i + 2) {
                spezzoni.add(new String(Base64.getDecoder().decode(causaleSplit[i]), StandardCharsets.UTF_8));
                importi.add(BigDecimal.valueOf(Double.parseDouble(
                    new String(Base64.getDecoder().decode(causaleSplit[i + 1]), StandardCharsets.UTF_8))));
            }
            CausaleSpezzoniStrutturati causale = new CausaleSpezzoniStrutturati();
            causale.setSpezzoni(spezzoni);
            causale.setImporti(importi);
            return causale;
        }

        throw new UnsupportedEncodingException("Formato causale non riconosciuto: " + causaleSplit[0]);
    }

    /**
     * Restituisce una rappresentazione semplificata della causale encoded.
     * Utile per ottenere il testo della causale senza dover gestire l'oggetto Causale.
     *
     * @param encodedCausale la stringa encoded
     * @return il testo semplificato della causale, o null se la stringa è vuota
     */
    public static String getSimple(String encodedCausale) {
        try {
            Causale causale = decode(encodedCausale);
            if (causale != null) {
                return causale.getSimple();
            }
        } catch (UnsupportedEncodingException e) {
            // Se non riesco a decodificare, restituisco la stringa originale
            return encodedCausale;
        }
        return null;
    }
}
