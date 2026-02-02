package it.govpay.portal.utils.trasformazioni;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder per costruire il contesto delle trasformazioni FreeMarker.
 * Sostituisce i metodi fillDynamicMap di TrasformazioniUtils di GovPay.
 */
public class TransformationContext {

    private static final Logger log = LoggerFactory.getLogger(TransformationContext.class);

    private final Map<String, Object> context;

    private TransformationContext() {
        this.context = new HashMap<>();
    }

    /**
     * Crea un nuovo builder per il contesto di trasformazione.
     */
    public static TransformationContext builder() {
        return new TransformationContext();
    }

    /**
     * Aggiunge la data corrente al contesto.
     */
    public TransformationContext withDate() {
        context.put(Costanti.MAP_DATE_OBJECT, new Date());
        return this;
    }

    /**
     * Aggiunge una data specifica al contesto.
     */
    public TransformationContext withDate(Date date) {
        context.put(Costanti.MAP_DATE_OBJECT, date);
        return this;
    }

    /**
     * Aggiunge un ID transazione al contesto.
     */
    public TransformationContext withTransactionId(String transactionId) {
        context.put(Costanti.MAP_TRANSACTION_ID_OBJECT, transactionId);
        return this;
    }

    /**
     * Genera e aggiunge un ID transazione casuale al contesto.
     * L'UUID viene generato senza trattini per rispettare il pattern idPendenza GovPay (max 35 caratteri).
     */
    public TransformationContext withRandomTransactionId() {
        context.put(Costanti.MAP_TRANSACTION_ID_OBJECT, UUID.randomUUID().toString().replace("-", ""));
        return this;
    }

    /**
     * Aggiunge un contesto applicativo generico.
     */
    public TransformationContext withApplicationContext(Map<String, Object> appContext) {
        context.put(Costanti.MAP_CTX_OBJECT, appContext != null ? appContext : new HashMap<>());
        return this;
    }

    /**
     * Aggiunge gli headers HTTP al contesto.
     */
    public TransformationContext withHeaders(Map<String, String> headers) {
        if (headers != null && !headers.isEmpty()) {
            context.put(Costanti.MAP_HEADER, headers);
        }
        return this;
    }

    /**
     * Aggiunge i query parameters al contesto.
     */
    public TransformationContext withQueryParams(Map<String, String> queryParams) {
        if (queryParams != null && !queryParams.isEmpty()) {
            context.put(Costanti.MAP_QUERY_PARAMETER, queryParams);
        }
        return this;
    }

    /**
     * Aggiunge i query parameters (con valori multipli) al contesto.
     */
    public TransformationContext withQueryParamsMultiValue(Map<String, List<String>> queryParams) {
        if (queryParams != null && !queryParams.isEmpty()) {
            context.put(Costanti.MAP_QUERY_PARAMETER, convertMultiValueMap(queryParams));
        }
        return this;
    }

    /**
     * Aggiunge i path parameters al contesto.
     */
    public TransformationContext withPathParams(Map<String, String> pathParams) {
        if (pathParams != null && !pathParams.isEmpty()) {
            context.put(Costanti.MAP_PATH_PARAMETER, pathParams);
        }
        return this;
    }

    /**
     * Aggiunge un URL e crea il RegExpExtractor per l'estrazione di pattern.
     */
    public TransformationContext withUrl(String url) {
        if (url != null && !url.isEmpty()) {
            RegExpExtractor extractor = new RegExpExtractor(url);
            context.put(Costanti.MAP_ELEMENT_URL_REGEXP, extractor);
            context.put(Costanti.MAP_ELEMENT_URL_REGEXP.toLowerCase(), extractor);
        }
        return this;
    }

    /**
     * Aggiunge un contenuto JSON e crea il JsonPathExtractor per l'estrazione.
     */
    public TransformationContext withJson(String json) {
        if (json != null && !json.isEmpty()) {
            JsonPathExtractor extractor = new JsonPathExtractor(json);
            context.put(Costanti.MAP_ELEMENT_JSON_PATH, extractor);
            context.put(Costanti.MAP_ELEMENT_JSON_PATH.toLowerCase(), extractor);
        }
        return this;
    }

    /**
     * Aggiunge il body della richiesta al contesto.
     */
    public TransformationContext withRequestBody(String requestBody) {
        if (requestBody != null) {
            context.put(Costanti.MAP_REQUEST_BODY, requestBody);
        }
        return this;
    }

    /**
     * Aggiunge il body della risposta al contesto.
     */
    public TransformationContext withResponseBody(String responseBody) {
        if (responseBody != null) {
            context.put(Costanti.MAP_RESPONSE_BODY, responseBody);
        }
        return this;
    }

    /**
     * Aggiunge l'ID del dominio al contesto.
     */
    public TransformationContext withIdDominio(String idDominio) {
        if (idDominio != null) {
            context.put(Costanti.MAP_ID_DOMINIO, idDominio);
        }
        return this;
    }

    /**
     * Aggiunge l'ID del tipo versamento al contesto.
     */
    public TransformationContext withIdTipoVersamento(String idTipoVersamento) {
        if (idTipoVersamento != null) {
            context.put(Costanti.MAP_ID_TIPO_VERSAMENTO, idTipoVersamento);
        }
        return this;
    }

    /**
     * Aggiunge l'ID dell'unità operativa al contesto.
     */
    public TransformationContext withIdUnitaOperativa(String idUnitaOperativa) {
        if (idUnitaOperativa != null) {
            context.put(Costanti.MAP_ID_UNITA_OPERATIVA, idUnitaOperativa);
        }
        return this;
    }

    /**
     * Aggiunge un oggetto versamento al contesto.
     */
    public TransformationContext withVersamento(Object versamento) {
        if (versamento != null) {
            context.put(Costanti.MAP_VERSAMENTO, versamento);
        }
        return this;
    }

    /**
     * Aggiunge un oggetto dominio al contesto.
     */
    public TransformationContext withDominio(Object dominio) {
        if (dominio != null) {
            context.put(Costanti.MAP_DOMINIO, dominio);
        }
        return this;
    }

    /**
     * Aggiunge un oggetto applicazione al contesto.
     */
    public TransformationContext withApplicazione(Object applicazione) {
        if (applicazione != null) {
            context.put(Costanti.MAP_APPLICAZIONE, applicazione);
        }
        return this;
    }

    /**
     * Aggiunge un oggetto RPT al contesto.
     */
    public TransformationContext withRpt(Object rpt) {
        if (rpt != null) {
            context.put(Costanti.MAP_RPT, rpt);
        }
        return this;
    }

    /**
     * Aggiunge le informazioni dell'utente al contesto.
     */
    public TransformationContext withUtente(Object utente) {
        if (utente != null) {
            context.put(Costanti.MAP_UTENTE, utente);
        }
        return this;
    }

    /**
     * Aggiunge un oggetto generico al contesto con chiave personalizzata.
     */
    public TransformationContext with(String key, Object value) {
        if (key != null && value != null) {
            context.put(key, value);
        }
        return this;
    }

    /**
     * Aggiunge tutti gli elementi di una mappa al contesto.
     */
    public TransformationContext withAll(Map<String, Object> values) {
        if (values != null) {
            context.putAll(values);
        }
        return this;
    }

    /**
     * Costruisce e restituisce la mappa del contesto.
     */
    public Map<String, Object> build() {
        // Assicura che i valori di default siano presenti
        if (!context.containsKey(Costanti.MAP_DATE_OBJECT)) {
            context.put(Costanti.MAP_DATE_OBJECT, new Date());
        }
        if (!context.containsKey(Costanti.MAP_CTX_OBJECT)) {
            context.put(Costanti.MAP_CTX_OBJECT, new HashMap<String, Object>());
        }
        return new HashMap<>(context);
    }

    /**
     * Converte una mappa con valori multipli in una mappa semplice.
     */
    private Map<String, String> convertMultiValueMap(Map<String, List<String>> multiMap) {
        Map<String, String> result = new HashMap<>();
        if (multiMap == null) {
            return result;
        }
        for (Map.Entry<String, List<String>> entry : multiMap.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                result.put(entry.getKey(), String.join(",", entry.getValue()));
            }
        }
        return result;
    }

    /**
     * Restituisce una rappresentazione della mappa corrente (per debug).
     */
    @Override
    public String toString() {
        return "TransformationContext{keys=" + context.keySet() + "}";
    }
}
