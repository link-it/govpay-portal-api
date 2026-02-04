package it.govpay.portal.utils.trasformazioni;

/**
 * Costanti utilizzate nelle trasformazioni FreeMarker.
 * Basato su it.govpay.core.utils.trasformazioni.Costanti di GovPay.
 */
public class Costanti {

    private Costanti() {}

    // Context map keys
    public static final String MAP_DATE_OBJECT = "date";
    public static final String MAP_TRANSACTION_ID_OBJECT = "transactionId";
    public static final String MAP_CTX_OBJECT = "context";
    public static final String MAP_HEADER = "header";
    public static final String MAP_QUERY_PARAMETER = "queryParams";
    public static final String MAP_PATH_PARAMETER = "pathParams";

    // Extraction prefixes
    public static final String MAP_ELEMENT_URL_REGEXP = "urlRegExp";
    public static final String MAP_ELEMENT_URL_REGEXP_PREFIX = "{" + MAP_ELEMENT_URL_REGEXP + ":";
    public static final String MAP_ELEMENT_JSON_PATH = "jsonPath";
    public static final String MAP_ELEMENT_JSON_PATH_PREFIX = "{" + MAP_ELEMENT_JSON_PATH + ":";

    // Class loading (for FreeMarker templates)
    public static final String MAP_CLASS_LOAD_STATIC = "class";
    public static final String MAP_CLASS_NEW_INSTANCE = "new";

    // Domain objects
    public static final String MAP_VERSAMENTO = "versamento";
    public static final String MAP_DOMINIO = "dominio";
    public static final String MAP_APPLICAZIONE = "applicazione";
    public static final String MAP_RPT = "rpt";
    public static final String MAP_ID_TIPO_VERSAMENTO = "idTipoVersamento";
    public static final String MAP_ID_UNITA_OPERATIVA = "idUnitaOperativa";
    public static final String MAP_ID_DOMINIO = "idDominio";

    // Portal specific
    public static final String MAP_UTENTE = "utente";
    public static final String MAP_REQUEST_BODY = "requestBody";
    public static final String MAP_RESPONSE_BODY = "responseBody";

    // Content types
    public static final String MAP_CONTENT_TYPE_MESSAGGIO_PROMEMORIA = "contentTypePromemoria";
    public static final String MAP_CONTENT_TYPE_MESSAGGIO_PROMEMORIA_DEFAULT_VALUE = "text/html";
}
