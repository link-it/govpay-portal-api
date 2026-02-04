package it.govpay.portal.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import it.govpay.portal.security.hardening.exception.ReCaptchaConfigurationException;
import it.govpay.portal.utils.trasformazioni.exception.TrasformazioneException;

/**
 * Test per le eccezioni custom.
 */
class ExceptionsTest {

    @Test
    @DisplayName("ValidationException con messaggio")
    void testValidationException() {
        ValidationException ex = new ValidationException("Campo non valido");
        assertEquals("Campo non valido", ex.getMessage());
    }

    @Test
    @DisplayName("ValidationException con messaggio e causa")
    void testValidationExceptionWithCause() {
        Exception cause = new RuntimeException("causa originale");
        ValidationException ex = new ValidationException("Campo non valido", cause);
        assertEquals("Campo non valido", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    @DisplayName("ValidationException con solo causa")
    void testValidationExceptionWithOnlyCause() {
        Exception cause = new RuntimeException("causa originale");
        ValidationException ex = new ValidationException(cause);
        assertEquals(cause, ex.getCause());
    }

    @Test
    @DisplayName("BadRequestException con messaggio")
    void testBadRequestException() {
        BadRequestException ex = new BadRequestException("Richiesta non valida");
        assertEquals("Richiesta non valida", ex.getMessage());
    }

    @Test
    @DisplayName("BadRequestException con messaggio e causa")
    void testBadRequestExceptionWithCause() {
        Exception cause = new RuntimeException("causa");
        BadRequestException ex = new BadRequestException("Richiesta non valida", cause);
        assertEquals("Richiesta non valida", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    @DisplayName("ForbiddenException con messaggio")
    void testForbiddenException() {
        ForbiddenException ex = new ForbiddenException("Accesso negato");
        assertEquals("Accesso negato", ex.getMessage());
    }

    @Test
    @DisplayName("ForbiddenException con messaggio e causa")
    void testForbiddenExceptionWithCause() {
        Exception cause = new RuntimeException("causa");
        ForbiddenException ex = new ForbiddenException("Accesso negato", cause);
        assertEquals("Accesso negato", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    @DisplayName("NotFoundException con messaggio")
    void testNotFoundException() {
        NotFoundException ex = new NotFoundException("Risorsa non trovata");
        assertEquals("Risorsa non trovata", ex.getMessage());
    }

    @Test
    @DisplayName("NotFoundException con messaggio e causa")
    void testNotFoundExceptionWithCause() {
        Exception cause = new RuntimeException("causa");
        NotFoundException ex = new NotFoundException("Risorsa non trovata", cause);
        assertEquals("Risorsa non trovata", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    @DisplayName("UnauthorizedException con messaggio")
    void testUnauthorizedException() {
        UnauthorizedException ex = new UnauthorizedException("Non autorizzato");
        assertEquals("Non autorizzato", ex.getMessage());
    }

    @Test
    @DisplayName("UnauthorizedException con messaggio e causa")
    void testUnauthorizedExceptionWithCause() {
        Exception cause = new RuntimeException("causa");
        UnauthorizedException ex = new UnauthorizedException("Non autorizzato", cause);
        assertEquals("Non autorizzato", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    @DisplayName("UnprocessableEntityException con messaggio")
    void testUnprocessableEntityException() {
        UnprocessableEntityException ex = new UnprocessableEntityException("Entità non processabile");
        assertEquals("Entità non processabile", ex.getMessage());
    }

    @Test
    @DisplayName("UnprocessableEntityException con messaggio e causa")
    void testUnprocessableEntityExceptionWithCause() {
        Exception cause = new RuntimeException("causa");
        UnprocessableEntityException ex = new UnprocessableEntityException("Entità non processabile", cause);
        assertEquals("Entità non processabile", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    @DisplayName("ReCaptchaConfigurationException con messaggio")
    void testReCaptchaConfigurationException() {
        ReCaptchaConfigurationException ex = new ReCaptchaConfigurationException("Configurazione reCAPTCHA non valida");
        assertEquals("Configurazione reCAPTCHA non valida", ex.getMessage());
    }

    @Test
    @DisplayName("ReCaptchaConfigurationException con messaggio e causa")
    void testReCaptchaConfigurationExceptionWithCause() {
        Exception cause = new RuntimeException("causa");
        ReCaptchaConfigurationException ex = new ReCaptchaConfigurationException("Configurazione reCAPTCHA non valida", cause);
        assertEquals("Configurazione reCAPTCHA non valida", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    @DisplayName("TrasformazioneException con messaggio")
    void testTrasformazioneException() {
        TrasformazioneException ex = new TrasformazioneException("Errore trasformazione");
        assertEquals("Errore trasformazione", ex.getMessage());
    }

    @Test
    @DisplayName("TrasformazioneException con messaggio e causa")
    void testTrasformazioneExceptionWithCause() {
        Exception cause = new RuntimeException("causa");
        TrasformazioneException ex = new TrasformazioneException("Errore trasformazione", cause);
        assertEquals("Errore trasformazione", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    @DisplayName("BadGatewayException con messaggio")
    void testBadGatewayException() {
        BadGatewayException ex = new BadGatewayException("Errore dal backend");
        assertEquals("Errore dal backend", ex.getMessage());
    }

    @Test
    @DisplayName("BadGatewayException con messaggio e causa")
    void testBadGatewayExceptionWithCause() {
        Exception cause = new RuntimeException("causa");
        BadGatewayException ex = new BadGatewayException("Errore dal backend", cause);
        assertEquals("Errore dal backend", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }
}
