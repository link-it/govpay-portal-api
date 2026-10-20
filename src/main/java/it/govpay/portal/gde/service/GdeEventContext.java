package it.govpay.portal.gde.service;

import java.time.OffsetDateTime;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Dati comuni dell'operazione da tracciare sul GDE: tipo evento, intervallo
 * temporale, richiesta HTTP e status code restituito.
 */
public record GdeEventContext(String tipoEvento, OffsetDateTime dataStart, OffsetDateTime dataEnd,
        HttpServletRequest request, int statusCode) {
}
