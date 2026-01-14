package it.govpay.portal.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 * Interfaccia per la gestione della causale versamento.
 * La causale può essere di tre tipi:
 * - Semplice: una singola stringa di testo
 * - Spezzoni: più stringhe di testo
 * - SpezzoniStrutturati: coppie di stringa + importo
 */
public interface Causale extends Serializable {

    /**
     * Codifica la causale nel formato stringa per la persistenza.
     * Formato: "01 base64" per semplice, "02 base64 base64..." per spezzoni,
     * "03 base64 base64..." per spezzoni strutturati.
     */
    String encode() throws UnsupportedEncodingException;

    /**
     * Restituisce una rappresentazione semplificata della causale.
     * Per la causale semplice restituisce il testo,
     * per le altre restituisce il primo elemento.
     */
    String getSimple() throws UnsupportedEncodingException;
}
