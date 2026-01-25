package it.govpay.portal.utils.trasformazioni;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.govpay.portal.utils.trasformazioni.exception.TrasformazioneException;

/**
 * Estrazione di valori da stringhe (URL, testo) tramite espressioni regolari.
 * Sostituisce org.openspcoop2.utils.regexp.RegularExpressionEngine.
 */
public class RegExpExtractor {

    private static final Logger log = LoggerFactory.getLogger(RegExpExtractor.class);

    private static final String ERROR_MSG_ESTRAZIONE_FALLITA = "Estrazione ''{0}'' fallita: {1}";
    private static final String DEBUG_MSG_NESSUN_RISULTATO = "Estrazione ''{0}'' non ha trovato risultati";

    private final String content;

    public RegExpExtractor(String content) {
        this.content = content;
    }

    /**
     * Verifica se il pattern trova un match completo nella stringa.
     */
    public boolean match(String regex) throws TrasformazioneException {
        String v = read(regex);
        return v != null && !v.isEmpty();
    }

    /**
     * Estrae il primo gruppo catturato con un match completo (matches()).
     * Se il pattern non ha gruppi, restituisce l'intera stringa se c'è match.
     */
    public String read(String regex) throws TrasformazioneException {
        if (content == null) {
            return null;
        }

        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(content);

            if (matcher.matches()) {
                if (matcher.groupCount() > 0) {
                    return matcher.group(1);
                }
                return matcher.group(0);
            }

            log.debug(MessageFormat.format(DEBUG_MSG_NESSUN_RISULTATO, regex));
            return null;

        } catch (PatternSyntaxException e) {
            throw new TrasformazioneException(
                    MessageFormat.format(ERROR_MSG_ESTRAZIONE_FALLITA, regex, "Pattern non valido: " + e.getMessage()), e);
        } catch (Exception e) {
            throw new TrasformazioneException(
                    MessageFormat.format(ERROR_MSG_ESTRAZIONE_FALLITA, regex, e.getMessage()), e);
        }
    }

    /**
     * Estrae tutti i gruppi catturati con un match completo (matches()).
     */
    public List<String> readList(String regex) throws TrasformazioneException {
        List<String> results = new ArrayList<>();

        if (content == null) {
            return results;
        }

        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(content);

            if (matcher.matches()) {
                if (matcher.groupCount() > 0) {
                    for (int i = 1; i <= matcher.groupCount(); i++) {
                        String group = matcher.group(i);
                        if (group != null) {
                            results.add(group);
                        }
                    }
                } else {
                    results.add(matcher.group(0));
                }
            }

            if (results.isEmpty()) {
                log.debug(MessageFormat.format(DEBUG_MSG_NESSUN_RISULTATO, regex));
            }

            return results;

        } catch (PatternSyntaxException e) {
            throw new TrasformazioneException(
                    MessageFormat.format(ERROR_MSG_ESTRAZIONE_FALLITA, regex, "Pattern non valido: " + e.getMessage()), e);
        } catch (Exception e) {
            throw new TrasformazioneException(
                    MessageFormat.format(ERROR_MSG_ESTRAZIONE_FALLITA, regex, e.getMessage()), e);
        }
    }

    /**
     * Verifica se il pattern trova almeno un match parziale (find()).
     */
    public boolean found(String regex) throws TrasformazioneException {
        String v = find(regex);
        return v != null && !v.isEmpty();
    }

    /**
     * Cerca il primo match parziale (find()) e restituisce il primo gruppo catturato.
     * Se il pattern non ha gruppi, restituisce il match completo.
     */
    public String find(String regex) throws TrasformazioneException {
        if (content == null) {
            return null;
        }

        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(content);

            if (matcher.find()) {
                if (matcher.groupCount() > 0) {
                    return matcher.group(1);
                }
                return matcher.group(0);
            }

            log.debug(MessageFormat.format(DEBUG_MSG_NESSUN_RISULTATO, regex));
            return null;

        } catch (PatternSyntaxException e) {
            throw new TrasformazioneException(
                    MessageFormat.format(ERROR_MSG_ESTRAZIONE_FALLITA, regex, "Pattern non valido: " + e.getMessage()), e);
        } catch (Exception e) {
            throw new TrasformazioneException(
                    MessageFormat.format(ERROR_MSG_ESTRAZIONE_FALLITA, regex, e.getMessage()), e);
        }
    }

    /**
     * Trova tutti i match parziali (find()) e restituisce i gruppi catturati.
     * Se il pattern non ha gruppi, restituisce tutti i match completi.
     */
    public List<String> findAll(String regex) throws TrasformazioneException {
        List<String> results = new ArrayList<>();

        if (content == null) {
            return results;
        }

        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(content);

            while (matcher.find()) {
                if (matcher.groupCount() > 0) {
                    String group = matcher.group(1);
                    if (group != null) {
                        results.add(group);
                    }
                } else {
                    results.add(matcher.group(0));
                }
            }

            if (results.isEmpty()) {
                log.debug(MessageFormat.format(DEBUG_MSG_NESSUN_RISULTATO, regex));
            }

            return results;

        } catch (PatternSyntaxException e) {
            throw new TrasformazioneException(
                    MessageFormat.format(ERROR_MSG_ESTRAZIONE_FALLITA, regex, "Pattern non valido: " + e.getMessage()), e);
        } catch (Exception e) {
            throw new TrasformazioneException(
                    MessageFormat.format(ERROR_MSG_ESTRAZIONE_FALLITA, regex, e.getMessage()), e);
        }
    }

    /**
     * Sostituisce tutte le occorrenze del pattern con la stringa di sostituzione.
     */
    public String replaceAll(String regex, String replacement) throws TrasformazioneException {
        if (content == null) {
            return null;
        }

        try {
            return content.replaceAll(regex, replacement);
        } catch (PatternSyntaxException e) {
            throw new TrasformazioneException(
                    MessageFormat.format(ERROR_MSG_ESTRAZIONE_FALLITA, regex, "Pattern non valido: " + e.getMessage()), e);
        } catch (Exception e) {
            throw new TrasformazioneException(
                    MessageFormat.format(ERROR_MSG_ESTRAZIONE_FALLITA, regex, e.getMessage()), e);
        }
    }

    /**
     * Verifica se il pattern è una espressione regolare valida.
     */
    public static boolean isValidPattern(String regex) {
        try {
            Pattern.compile(regex);
            return true;
        } catch (PatternSyntaxException e) {
            return false;
        }
    }
}
