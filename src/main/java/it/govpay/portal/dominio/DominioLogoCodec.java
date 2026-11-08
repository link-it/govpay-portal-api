package it.govpay.portal.dominio;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Incapsula il formato con cui il logo del dominio e' persistito nella colonna
 * {@code domini.logo}: non i byte grezzi dell'immagine, ma i byte UTF-8 del suo
 * testo Base64. E' l'unico punto che conosce questa codifica, cosi' che i punti
 * di lettura lavorino sempre con i byte grezzi dell'immagine (anche le stampe:
 * govpay-stampe-api riceve i loghi come byte dell'immagine, non come data-URI).
 */
public final class DominioLogoCodec {

    private DominioLogoCodec() {
    }

    /**
     * Decodifica il contenuto memorizzato in colonna (testo Base64 dell'immagine,
     * in byte UTF-8) nei byte grezzi dell'immagine. Tollera un eventuale prefisso
     * data-URI ({@code data:...;base64,}).
     */
    public static byte[] decode(byte[] stored) {
        if (stored == null || stored.length == 0) {
            return stored;
        }
        return Base64.getDecoder().decode(base64Of(stored));
    }

    /**
     * Decodifica un logo scritto come testo Base64, con o senza prefisso data-URI
     * ({@code data:...;base64,}), nei byte grezzi dell'immagine. Usato per il logo
     * pagoPA letto dalla configurazione.
     */
    public static byte[] decode(String base64) {
        if (base64 == null || base64.isBlank()) {
            return new byte[0];
        }
        return decode(base64.getBytes(StandardCharsets.UTF_8));
    }

    private static String base64Of(byte[] stored) {
        String value = new String(stored, StandardCharsets.UTF_8).trim();
        int comma = value.indexOf(',');
        if (value.startsWith("data:") && comma >= 0) {
            return value.substring(comma + 1);
        }
        return value;
    }
}
