/*
 * GovPay - Porta di Accesso al Nodo dei Pagamenti SPC
 * http://www.gov4j.it/govpay
 *
 * Copyright (c) 2014-2026 Link.it srl (http://www.link.it).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3, as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.govpay.portal.beans.pendenza;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

import it.govpay.portal.exception.ValidationException;

/**
 * Validatore per il bean PendenzaPost.
 * Basato sul codice di validazione di GovPay.
 */
public class PendenzaPostValidator {

    // Pattern da GovPay CostantiValidazione
    private static final String PATTERN_ID_A2A = "^[a-zA-Z0-9\\-_]{1,35}$";
    private static final String PATTERN_ID_PENDENZA = "^[a-zA-Z0-9\\-_]{1,35}$";
    private static final String PATTERN_ID_DOMINIO = "^(\\d){11}$";
    private static final String PATTERN_ID_TIPO_VERSAMENTO = "^[a-zA-Z0-9\\-_\\.]{1,35}$";
    private static final String PATTERN_ID_UO = "^[a-zA-Z0-9\\-_]{1,35}$";
    private static final String PATTERN_ID_VOCE_PENDENZA = "^[a-zA-Z0-9\\-_]{1,35}$";
    private static final String PATTERN_NUMERO_AVVISO = "^\\d{18}$";
    private static final String PATTERN_ANNO_RIFERIMENTO = "^\\d{4}$";
    private static final String PATTERN_NAZIONE = "^[A-Z]{2}$";
    private static final String PATTERN_EMAIL = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$";
    private static final String PATTERN_PROVINCIA = "^[A-Z]{2}$";

    private static final BigDecimal MAX_IMPORTO = new BigDecimal("999999999.99");
    private static final int MAX_VOCI = 5;

    private final PendenzaPost pendenzaPost;

    public PendenzaPostValidator(PendenzaPost pendenzaPost) {
        this.pendenzaPost = pendenzaPost;
    }

    /**
     * Esegue la validazione completa della pendenza.
     *
     * @throws ValidationException se la validazione fallisce
     */
    public void validate() throws ValidationException {
        if (pendenzaPost == null) {
            throw new ValidationException("Pendenza non valorizzata");
        }

        // Validazione campi identificativi obbligatori
        validaIdA2A(pendenzaPost.getIdA2A());
        validaIdPendenza(pendenzaPost.getIdPendenza());
        validaIdDominio(pendenzaPost.getIdDominio());
        validaIdUnitaOperativa(pendenzaPost.getIdUnitaOperativa());
        validaIdTipoPendenza(pendenzaPost.getIdTipoPendenza());

        // Validazione nome e causale
        validaNomePendenza(pendenzaPost.getNome());
        validaCausale(pendenzaPost.getCausale());

        // Validazione soggetto pagatore (opzionale ma se presente deve essere valido)
        validaSoggettoPagatore(pendenzaPost.getSoggettoPagatore());

        // Validazione importo
        validaImporto(pendenzaPost.getImporto());

        // Validazione numero avviso
        validaNumeroAvviso(pendenzaPost.getNumeroAvviso());

        // Validazione anno riferimento
        validaAnnoRiferimento(pendenzaPost.getAnnoRiferimento());

        // Validazione cartella pagamento
        validaCartellaPagamento(pendenzaPost.getCartellaPagamento());

        // Validazione direzione e divisione
        validaDirezione(pendenzaPost.getDirezione());
        validaDivisione(pendenzaPost.getDivisione());

        // Validazione documento
        validaDocumento(pendenzaPost.getDocumento());

        // Validazione voci
        validaVoci(pendenzaPost.getVoci());

        // Validazione allegati
        validaAllegati(pendenzaPost.getAllegati());
    }

    private void validaIdA2A(String idA2A) throws ValidationException {
        if (!StringUtils.hasText(idA2A)) {
            throw new ValidationException("Il campo idA2A non deve essere vuoto.");
        }
        if (!Pattern.matches(PATTERN_ID_A2A, idA2A)) {
            throw new ValidationException("Il valore [" + idA2A + "] del campo idA2A non rispetta il pattern richiesto: " + PATTERN_ID_A2A);
        }
    }

    private void validaIdPendenza(String idPendenza) throws ValidationException {
        if (!StringUtils.hasText(idPendenza)) {
            throw new ValidationException("Il campo idPendenza non deve essere vuoto.");
        }
        if (!Pattern.matches(PATTERN_ID_PENDENZA, idPendenza)) {
            throw new ValidationException("Il valore [" + idPendenza + "] del campo idPendenza non rispetta il pattern richiesto: " + PATTERN_ID_PENDENZA);
        }
    }

    private void validaIdDominio(String idDominio) throws ValidationException {
        if (!StringUtils.hasText(idDominio)) {
            throw new ValidationException("Il campo idDominio non deve essere vuoto.");
        }
        if (!Pattern.matches(PATTERN_ID_DOMINIO, idDominio)) {
            throw new ValidationException("Il valore [" + idDominio + "] del campo idDominio non rispetta il pattern richiesto: " + PATTERN_ID_DOMINIO);
        }
    }

    private void validaIdUnitaOperativa(String idUnitaOperativa) throws ValidationException {
        if (StringUtils.hasText(idUnitaOperativa) && !Pattern.matches(PATTERN_ID_UO, idUnitaOperativa)) {
            throw new ValidationException("Il valore [" + idUnitaOperativa + "] del campo idUnitaOperativa non rispetta il pattern richiesto: " + PATTERN_ID_UO);
        }
    }

    private void validaIdTipoPendenza(String idTipoPendenza) throws ValidationException {
        if (StringUtils.hasText(idTipoPendenza) && !Pattern.matches(PATTERN_ID_TIPO_VERSAMENTO, idTipoPendenza)) {
            throw new ValidationException("Il valore [" + idTipoPendenza + "] del campo idTipoPendenza non rispetta il pattern richiesto: " + PATTERN_ID_TIPO_VERSAMENTO);
        }
    }

    private void validaNomePendenza(String nome) throws ValidationException {
        if (StringUtils.hasText(nome) && nome.length() > 35) {
            throw new ValidationException("Il valore [" + nome + "] del campo nome non rispetta la lunghezza massima di 35 caratteri.");
        }
    }

    private void validaCausale(String causale) throws ValidationException {
        if (!StringUtils.hasText(causale)) {
            throw new ValidationException("Il campo causale non deve essere vuoto.");
        }
        if (causale.length() > 140) {
            throw new ValidationException("Il valore del campo causale non rispetta la lunghezza massima di 140 caratteri.");
        }
    }

    private void validaSoggettoPagatore(Soggetto soggetto) throws ValidationException {
        // Il vincolo di obbligatorieta' del soggetto pagatore e' stato eliminato
        // per consentire di acquisire pendenze senza indicare il debitore.
        if (soggetto == null) {
            return;
        }

        // Tipo
        if (soggetto.getTipo() != null && soggetto.getTipo().toString().length() > 1) {
            throw new ValidationException("Il valore del campo soggettoPagatore.tipo non rispetta la lunghezza massima di 1 carattere.");
        }

        validaLunghezzaMassima(soggetto.getIdentificativo(), 16, "soggettoPagatore.identificativo");
        validaLunghezzaMassima(soggetto.getAnagrafica(), 70, "soggettoPagatore.anagrafica");
        validaLunghezzaMassima(soggetto.getIndirizzo(), 70, "soggettoPagatore.indirizzo");
        validaLunghezzaMassima(soggetto.getCivico(), 16, "soggettoPagatore.civico");
        validaLunghezzaMassima(soggetto.getCap(), 16, "soggettoPagatore.cap");
        validaLunghezzaMassima(soggetto.getLocalita(), 35, "soggettoPagatore.localita");
        validaLunghezzaMassima(soggetto.getProvincia(), 35, "soggettoPagatore.provincia");

        // Nazione
        if (StringUtils.hasText(soggetto.getNazione()) && !Pattern.matches(PATTERN_NAZIONE, soggetto.getNazione())) {
            throw new ValidationException("Il valore [" + soggetto.getNazione() + "] del campo soggettoPagatore.nazione non rispetta il pattern richiesto: " + PATTERN_NAZIONE);
        }

        validaEmailSoggetto(soggetto.getEmail());

        validaLunghezzaMassima(soggetto.getCellulare(), 35, "soggettoPagatore.cellulare");
    }

    private void validaEmailSoggetto(String email) throws ValidationException {
        if (!StringUtils.hasText(email)) {
            return;
        }
        validaLunghezzaMassima(email, 256, "soggettoPagatore.email");
        if (!Pattern.matches(PATTERN_EMAIL, email)) {
            throw new ValidationException("Il valore [" + email + "] del campo soggettoPagatore.email non rispetta il pattern richiesto: " + PATTERN_EMAIL);
        }
    }

    /**
     * Verifica che il valore, se valorizzato, non superi la lunghezza massima indicata.
     */
    private void validaLunghezzaMassima(String valore, int lunghezzaMassima, String campo) throws ValidationException {
        if (StringUtils.hasText(valore) && valore.length() > lunghezzaMassima) {
            throw new ValidationException("Il valore del campo " + campo + " non rispetta la lunghezza massima di " + lunghezzaMassima + " caratteri.");
        }
    }

    private void validaImporto(BigDecimal importo) throws ValidationException {
        if (importo == null) {
            return; // Importo opzionale, può essere calcolato dalla somma delle voci
        }

        if (importo.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Il campo importo deve essere superiore o uguale a 0.");
        }

        if (importo.compareTo(MAX_IMPORTO) > 0) {
            throw new ValidationException("Il campo importo deve essere inferiore o uguale a " + MAX_IMPORTO + ".");
        }

        // Verifica massimo 2 cifre decimali
        if (importo.scale() > 2) {
            throw new ValidationException("Il campo importo non deve superare le 2 cifre decimali.");
        }
    }

    private void validaNumeroAvviso(String numeroAvviso) throws ValidationException {
        if (StringUtils.hasText(numeroAvviso)) {
            if (numeroAvviso.length() > 18) {
                throw new ValidationException("Il valore del campo numeroAvviso non rispetta la lunghezza massima di 18 caratteri.");
            }
            if (!Pattern.matches(PATTERN_NUMERO_AVVISO, numeroAvviso)) {
                throw new ValidationException("Il valore [" + numeroAvviso + "] del campo numeroAvviso non rispetta il pattern richiesto: " + PATTERN_NUMERO_AVVISO);
            }
        }
    }

    private void validaAnnoRiferimento(BigDecimal annoRiferimento) throws ValidationException {
        if (annoRiferimento != null) {
            String anno = annoRiferimento.toBigInteger().toString();
            if (!Pattern.matches(PATTERN_ANNO_RIFERIMENTO, anno)) {
                throw new ValidationException("Il valore [" + anno + "] del campo annoRiferimento non rispetta il pattern richiesto: " + PATTERN_ANNO_RIFERIMENTO);
            }
        }
    }

    private void validaCartellaPagamento(String cartellaPagamento) throws ValidationException {
        if (StringUtils.hasText(cartellaPagamento) && cartellaPagamento.length() > 35) {
            throw new ValidationException("Il valore del campo cartellaPagamento non rispetta la lunghezza massima di 35 caratteri.");
        }
    }

    private void validaDirezione(String direzione) throws ValidationException {
        if (StringUtils.hasText(direzione) && direzione.length() > 35) {
            throw new ValidationException("Il valore del campo direzione non rispetta la lunghezza massima di 35 caratteri.");
        }
    }

    private void validaDivisione(String divisione) throws ValidationException {
        if (StringUtils.hasText(divisione) && divisione.length() > 35) {
            throw new ValidationException("Il valore del campo divisione non rispetta la lunghezza massima di 35 caratteri.");
        }
    }

    private void validaDocumento(Documento documento) throws ValidationException {
        if (documento == null) {
            return;
        }

        // Identificativo documento
        if (StringUtils.hasText(documento.getIdentificativo()) && documento.getIdentificativo().length() > 35) {
            throw new ValidationException("Il valore del campo documento.identificativo non rispetta la lunghezza massima di 35 caratteri.");
        }

        // Descrizione documento
        if (StringUtils.hasText(documento.getDescrizione()) && documento.getDescrizione().length() > 255) {
            throw new ValidationException("Il valore del campo documento.descrizione non rispetta la lunghezza massima di 255 caratteri.");
        }

        // Rata
        if (documento.getRata() != null && documento.getRata().compareTo(BigDecimal.ONE) < 0) {
            throw new ValidationException("Il campo documento.rata deve essere superiore o uguale a 1.");
        }
    }

    private void validaVoci(List<VocePendenza> voci) throws ValidationException {
        if (voci == null || voci.isEmpty()) {
            throw new ValidationException("Il campo voci non deve essere vuoto.");
        }

        if (voci.size() > MAX_VOCI) {
            throw new ValidationException("Il campo voci deve avere massimo " + MAX_VOCI + " elementi.");
        }

        int index = 0;
        for (VocePendenza voce : voci) {
            validaVocePendenza(voce, index);
            index++;
        }
    }

    private void validaVocePendenza(VocePendenza voce, int index) throws ValidationException {
        String prefix = "voci[" + index + "]";

        if (voce == null) {
            throw new ValidationException("L'elemento in posizione " + index + " del campo voci è vuoto.");
        }

        validaIdVocePendenza(voce.getIdVocePendenza(), prefix);
        validaImportoVoce(voce.getImporto(), prefix);
        validaDescrizioneVoce(voce.getDescrizione(), prefix);
        validaLunghezzaMassima(voce.getDescrizioneCausaleRPT(), 140, prefix + ".descrizioneCausaleRPT");
        validaTipoAccredito(voce, prefix);
    }

    private void validaIdVocePendenza(String idVocePendenza, String prefix) throws ValidationException {
        if (!StringUtils.hasText(idVocePendenza)) {
            throw new ValidationException("Il campo " + prefix + ".idVocePendenza non deve essere vuoto.");
        }
        if (!Pattern.matches(PATTERN_ID_VOCE_PENDENZA, idVocePendenza)) {
            throw new ValidationException("Il valore [" + idVocePendenza + "] del campo " + prefix + ".idVocePendenza non rispetta il pattern richiesto: " + PATTERN_ID_VOCE_PENDENZA);
        }
    }

    private void validaImportoVoce(BigDecimal importo, String prefix) throws ValidationException {
        if (importo == null) {
            throw new ValidationException("Il campo " + prefix + ".importo non deve essere vuoto.");
        }
        if (importo.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Il campo " + prefix + ".importo deve essere superiore o uguale a 0.");
        }
        if (importo.compareTo(MAX_IMPORTO) > 0) {
            throw new ValidationException("Il campo " + prefix + ".importo deve essere inferiore o uguale a " + MAX_IMPORTO + ".");
        }
        if (importo.scale() > 2) {
            throw new ValidationException("Il campo " + prefix + ".importo non deve superare le 2 cifre decimali.");
        }
    }

    private void validaDescrizioneVoce(String descrizione, String prefix) throws ValidationException {
        if (!StringUtils.hasText(descrizione)) {
            throw new ValidationException("Il campo " + prefix + ".descrizione non deve essere vuoto.");
        }
        validaLunghezzaMassima(descrizione, 255, prefix + ".descrizione");
    }

    /**
     * Verifica la mutua esclusivita' tra codEntrata, tipoBollo e ibanAccredito
     * e i campi richiesti da ciascuna tipologia.
     */
    private void validaTipoAccredito(VocePendenza voce, String prefix) throws ValidationException {
        boolean hasCodEntrata = StringUtils.hasText(voce.getCodEntrata());
        boolean hasTipoBollo = StringUtils.hasText(voce.getTipoBollo());
        boolean hasIbanAccredito = StringUtils.hasText(voce.getIbanAccredito());

        if (hasCodEntrata) {
            // Se codEntrata è valorizzato, gli altri devono essere null
            if (hasTipoBollo || hasIbanAccredito) {
                throw new ValidationException("Valorizzato codEntrata. I campi tipoBollo e ibanAccredito devono essere vuoti.");
            }
        } else if (hasTipoBollo) {
            // Se tipoBollo è valorizzato, ibanAccredito deve essere null
            if (hasIbanAccredito) {
                throw new ValidationException("Valorizzato tipoBollo. Il campo ibanAccredito deve essere vuoto.");
            }
            validaVoceBollo(voce, prefix);
        } else if (hasIbanAccredito) {
            validaVoceIban(voce, prefix);
        } else {
            throw new ValidationException("Uno dei campi tra ibanAccredito, tipoBollo o codEntrata deve essere valorizzato per " + prefix + ".");
        }
    }

    private void validaVoceBollo(VocePendenza voce, String prefix) throws ValidationException {
        // hashDocumento e provinciaResidenza sono richiesti
        if (!StringUtils.hasText(voce.getHashDocumento())) {
            throw new ValidationException("Il campo " + prefix + ".hashDocumento non deve essere vuoto quando tipoBollo è valorizzato.");
        }
        if (!StringUtils.hasText(voce.getProvinciaResidenza())) {
            throw new ValidationException("Il campo " + prefix + ".provinciaResidenza non deve essere vuoto quando tipoBollo è valorizzato.");
        }
        if (!Pattern.matches(PATTERN_PROVINCIA, voce.getProvinciaResidenza())) {
            throw new ValidationException("Il valore [" + voce.getProvinciaResidenza() + "] del campo " + prefix + ".provinciaResidenza non rispetta il pattern richiesto: " + PATTERN_PROVINCIA);
        }
    }

    private void validaVoceIban(VocePendenza voce, String prefix) throws ValidationException {
        // Se ibanAccredito è valorizzato, tipoContabilita e codiceContabilita sono richiesti
        if (voce.getTipoContabilita() == null) {
            throw new ValidationException("Il campo " + prefix + ".tipoContabilita non deve essere vuoto quando ibanAccredito è valorizzato.");
        }
        if (!StringUtils.hasText(voce.getCodiceContabilita())) {
            throw new ValidationException("Il campo " + prefix + ".codiceContabilita non deve essere vuoto quando ibanAccredito è valorizzato.");
        }
    }

    private void validaAllegati(List<NuovoAllegatoPendenza> allegati) throws ValidationException {
        if (allegati == null || allegati.isEmpty()) {
            return;
        }

        int index = 0;
        for (NuovoAllegatoPendenza allegato : allegati) {
            String prefix = "allegati[" + index + "]";

            if (!StringUtils.hasText(allegato.getNome())) {
                throw new ValidationException("Il campo " + prefix + ".nome non deve essere vuoto.");
            }
            if (allegato.getNome().length() > 255) {
                throw new ValidationException("Il valore del campo " + prefix + ".nome non rispetta la lunghezza massima di 255 caratteri.");
            }

            if (StringUtils.hasText(allegato.getTipo()) && allegato.getTipo().length() > 255) {
                throw new ValidationException("Il valore del campo " + prefix + ".tipo non rispetta la lunghezza massima di 255 caratteri.");
            }

            if (StringUtils.hasText(allegato.getDescrizione()) && allegato.getDescrizione().length() > 255) {
                throw new ValidationException("Il valore del campo " + prefix + ".descrizione non rispetta la lunghezza massima di 255 caratteri.");
            }

            if (allegato.getContenuto() == null) {
                throw new ValidationException("Il campo " + prefix + ".contenuto non deve essere vuoto.");
            }

            index++;
        }
    }
}
