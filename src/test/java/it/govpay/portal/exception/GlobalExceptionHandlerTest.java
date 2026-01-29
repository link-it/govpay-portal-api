package it.govpay.portal.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import it.govpay.portal.model.Errore;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("UnauthorizedException dovrebbe restituire 401")
    void shouldReturn401ForUnauthorizedException() {
        UnauthorizedException ex = new UnauthorizedException("Autenticazione non presente");

        ResponseEntity<Errore> response = handler.handleUnauthorizedException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("401", response.getBody().getCodice());
        assertEquals("AUTORIZZAZIONE", response.getBody().getCategoria());
        assertEquals("Non autorizzato", response.getBody().getDescrizione());
        assertEquals("Autenticazione non presente", response.getBody().getDettaglio());
    }

    @Test
    @DisplayName("ForbiddenException dovrebbe restituire 403")
    void shouldReturn403ForForbiddenException() {
        ForbiddenException ex = new ForbiddenException("Accesso non consentito");

        ResponseEntity<Errore> response = handler.handleForbiddenException(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("403", response.getBody().getCodice());
        assertEquals("AUTORIZZAZIONE", response.getBody().getCategoria());
        assertEquals("Accesso negato", response.getBody().getDescrizione());
        assertEquals("Accesso non consentito", response.getBody().getDettaglio());
    }

    @Test
    @DisplayName("NotFoundException dovrebbe restituire 404")
    void shouldReturn404ForNotFoundException() {
        NotFoundException ex = new NotFoundException("Risorsa non trovata");

        ResponseEntity<Errore> response = handler.handleNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("404", response.getBody().getCodice());
        assertEquals("RICHIESTA", response.getBody().getCategoria());
        assertEquals("Risorsa non trovata", response.getBody().getDescrizione());
        assertEquals("Risorsa non trovata", response.getBody().getDettaglio());
    }

    @Test
    @DisplayName("BadRequestException dovrebbe restituire 400")
    void shouldReturn400ForBadRequestException() {
        BadRequestException ex = new BadRequestException("Parametro non valido");

        ResponseEntity<Errore> response = handler.handleBadRequestException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("400", response.getBody().getCodice());
        assertEquals("RICHIESTA", response.getBody().getCategoria());
        assertEquals("Richiesta non valida", response.getBody().getDescrizione());
        assertEquals("Parametro non valido", response.getBody().getDettaglio());
    }

    @Test
    @DisplayName("UnprocessableEntityException dovrebbe restituire 422")
    void shouldReturn422ForUnprocessableEntityException() {
        UnprocessableEntityException ex = new UnprocessableEntityException("Dati non processabili");

        ResponseEntity<Errore> response = handler.handleUnprocessableEntityException(ex);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("422", response.getBody().getCodice());
        assertEquals("RICHIESTA", response.getBody().getCategoria());
        assertEquals("Entità non processabile", response.getBody().getDescrizione());
        assertEquals("Dati non processabili", response.getBody().getDettaglio());
    }

    @Test
    @DisplayName("Eccezione generica dovrebbe restituire 503")
    void shouldReturn503ForGenericException() {
        Exception ex = new RuntimeException("Errore interno");

        ResponseEntity<Errore> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("503", response.getBody().getCodice());
        assertEquals("INTERNO", response.getBody().getCategoria());
        assertEquals("Servizio non disponibile", response.getBody().getDescrizione());
        assertEquals("Errore interno del server", response.getBody().getDettaglio());
    }

    @Test
    @DisplayName("NullPointerException dovrebbe restituire 503")
    void shouldReturn503ForNullPointerException() {
        NullPointerException ex = new NullPointerException("Valore null");

        ResponseEntity<Errore> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("503", response.getBody().getCodice());
    }

    @Test
    @DisplayName("IllegalArgumentException dovrebbe restituire 503")
    void shouldReturn503ForIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Argomento non valido");

        ResponseEntity<Errore> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("503", response.getBody().getCodice());
    }
}
