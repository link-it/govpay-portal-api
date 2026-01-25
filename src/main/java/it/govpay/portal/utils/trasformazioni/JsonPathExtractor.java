package it.govpay.portal.utils.trasformazioni;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import it.govpay.portal.utils.trasformazioni.exception.JsonPathNotFoundException;
import it.govpay.portal.utils.trasformazioni.exception.JsonPathNotValidException;
import it.govpay.portal.utils.trasformazioni.exception.TrasformazioneException;

/**
 * Estrazione di valori da JSON tramite JsonPath.
 * Utilizza jayway json-path per l'estrazione.
 */
public class JsonPathExtractor {

    private static final Logger log = LoggerFactory.getLogger(JsonPathExtractor.class);

    private static final String NESSUN_MATCH_TROVATO = "Nessun match trovato per l''espressione jsonPath [{0}]";
    private static final String DOCUMENT_IS_NULL = "Documento JSON è null";
    private static final String PATTERN_IS_NULL = "Pattern è null";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final String jsonContent;

    public JsonPathExtractor(String jsonContent) {
        this.jsonContent = jsonContent;
    }

    /**
     * Verifica se il pattern trova un match nel JSON.
     */
    public boolean match(String pattern) throws TrasformazioneException {
        try {
            String v = read(pattern);
            return v != null && !v.isEmpty();
        } catch (JsonPathNotFoundException e) {
            return false;
        }
    }

    /**
     * Legge il primo valore corrispondente al pattern.
     */
    public String read(String pattern) throws TrasformazioneException, JsonPathNotFoundException {
        List<String> results = readList(pattern);
        if (results != null && !results.isEmpty()) {
            return results.get(0);
        }
        return null;
    }

    /**
     * Legge tutti i valori corrispondenti al pattern come lista di stringhe.
     */
    public List<String> readList(String pattern) throws TrasformazioneException, JsonPathNotFoundException {
        if (jsonContent == null) {
            throw new TrasformazioneException(DOCUMENT_IS_NULL);
        }
        if (pattern == null) {
            throw new TrasformazioneException(PATTERN_IS_NULL);
        }

        validate(pattern);

        try {
            List<String> result = new ArrayList<>();
            Object o = JsonPath.read(jsonContent, pattern);
            parseResult(o, result);

            if (result.isEmpty()) {
                throw new JsonPathNotFoundException(MessageFormat.format(NESSUN_MATCH_TROVATO, pattern));
            }
            return result;
        } catch (PathNotFoundException e) {
            throw new JsonPathNotFoundException(MessageFormat.format(NESSUN_MATCH_TROVATO, pattern), e);
        } catch (JsonPathNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new TrasformazioneException("Errore durante l'estrazione JsonPath: " + e.getMessage(), e);
        }
    }

    /**
     * Legge il valore corrispondente al pattern come JsonNode.
     */
    public JsonNode readAsNode(String pattern) throws TrasformazioneException, JsonPathNotFoundException {
        if (jsonContent == null) {
            throw new TrasformazioneException(DOCUMENT_IS_NULL);
        }
        if (pattern == null) {
            throw new TrasformazioneException(PATTERN_IS_NULL);
        }

        validate(pattern);

        try {
            Object result = JsonPath.read(jsonContent, pattern);
            if (result == null) {
                throw new JsonPathNotFoundException(MessageFormat.format(NESSUN_MATCH_TROVATO, pattern));
            }
            return convertToJsonNode(result);
        } catch (PathNotFoundException e) {
            throw new JsonPathNotFoundException(MessageFormat.format(NESSUN_MATCH_TROVATO, pattern), e);
        } catch (JsonPathNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new TrasformazioneException("Errore durante l'estrazione JsonPath: " + e.getMessage(), e);
        }
    }

    /**
     * Estrae un valore come stringa, gestendo sia match singoli che multipli.
     */
    public static String extractAsString(String jsonContent, String pattern) throws TrasformazioneException {
        try {
            JsonPathExtractor extractor = new JsonPathExtractor(jsonContent);
            return extractor.read(pattern);
        } catch (JsonPathNotFoundException e) {
            log.debug("Pattern {} non ha trovato risultati: {}", pattern, e.getMessage());
            return null;
        }
    }

    /**
     * Estrae tutti i valori come lista di stringhe.
     */
    public static List<String> extractAsList(String jsonContent, String pattern) throws TrasformazioneException {
        try {
            JsonPathExtractor extractor = new JsonPathExtractor(jsonContent);
            return extractor.readList(pattern);
        } catch (JsonPathNotFoundException e) {
            log.debug("Pattern {} non ha trovato risultati: {}", pattern, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Valida un pattern JsonPath.
     */
    public void validate(String pattern) throws JsonPathNotValidException {
        try {
            JsonPath.compile(pattern);
        } catch (Exception e) {
            throw new JsonPathNotValidException(
                    "Validazione del jsonPath [" + pattern + "] fallita: " + e.getMessage(), e);
        }
    }

    private void parseResult(Object o, List<String> result) throws TrasformazioneException {
        if (o == null) {
            return;
        }

        if (o instanceof List<?> list) {
            for (int i = 0; i < list.size(); i++) {
                Object item = list.get(i);
                if (item != null) {
                    result.add(convertToString(item));
                }
            }
        } else if (o instanceof Map<?, ?>) {
            result.add(mapToJsonString((Map<?, ?>) o));
        } else {
            result.add(convertToString(o));
        }
    }

    private String convertToString(Object o) throws TrasformazioneException {
        if (o instanceof String s) {
            return s;
        } else if (o instanceof Number || o instanceof Boolean) {
            return o.toString();
        } else if (o instanceof Map<?, ?> map) {
            return mapToJsonString(map);
        } else {
            try {
                return objectMapper.writeValueAsString(o);
            } catch (JsonProcessingException e) {
                return o.toString();
            }
        }
    }

    private String mapToJsonString(Map<?, ?> map) throws TrasformazioneException {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new TrasformazioneException("Errore conversione Map a JSON: " + e.getMessage(), e);
        }
    }

    private JsonNode convertToJsonNode(Object o) throws TrasformazioneException {
        try {
            if (o instanceof String s) {
                return new TextNode(s);
            } else {
                String json = objectMapper.writeValueAsString(o);
                return objectMapper.readTree(json);
            }
        } catch (JsonProcessingException e) {
            throw new TrasformazioneException("Errore conversione a JsonNode: " + e.getMessage(), e);
        }
    }
}
