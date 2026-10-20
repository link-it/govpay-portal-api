package it.govpay.portal.service;

import java.util.Map;

/**
 * Header, parametri query e parametri path della richiesta HTTP,
 * resi disponibili alla trasformazione della pendenza.
 */
public record ParametriRichiesta(Map<String, String> headers, Map<String, String> queryParams,
        Map<String, String> pathParams) {
}
