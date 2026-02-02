package it.govpay.portal.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Risposta di errore per validazione fallita.
 * Contiene la lista dettagliata degli errori di validazione.
 */
public class ErroreValidazione implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("categoria")
    private String categoria;

    @JsonProperty("codice")
    private String codice;

    @JsonProperty("descrizione")
    private String descrizione;

    @JsonProperty("dettagli")
    private List<DettaglioErrore> dettagli = new ArrayList<>();

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getCodice() {
        return codice;
    }

    public void setCodice(String codice) {
        this.codice = codice;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public List<DettaglioErrore> getDettagli() {
        return dettagli;
    }

    public void setDettagli(List<DettaglioErrore> dettagli) {
        this.dettagli = dettagli;
    }

    public void addDettaglio(String campo, String messaggio) {
        this.dettagli.add(new DettaglioErrore(campo, messaggio));
    }

    /**
     * Singolo errore di validazione.
     */
    public static class DettaglioErrore implements Serializable {

        private static final long serialVersionUID = 1L;

        @JsonProperty("campo")
        private String campo;

        @JsonProperty("messaggio")
        private String messaggio;

        public DettaglioErrore() {
        }

        public DettaglioErrore(String campo, String messaggio) {
            this.campo = campo;
            this.messaggio = messaggio;
        }

        public String getCampo() {
            return campo;
        }

        public void setCampo(String campo) {
            this.campo = campo;
        }

        public String getMessaggio() {
            return messaggio;
        }

        public void setMessaggio(String messaggio) {
            this.messaggio = messaggio;
        }
    }
}
