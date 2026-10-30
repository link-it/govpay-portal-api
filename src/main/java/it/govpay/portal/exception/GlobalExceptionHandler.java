package it.govpay.portal.exception;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import it.govpay.portal.model.Errore;

/**
 * Gestore globale delle eccezioni per restituire risposte strutturate.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String CATEGORIA_RICHIESTA = "RICHIESTA";

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Errore> handleUnauthorizedException(UnauthorizedException ex) {
        Errore errore = new Errore();
        errore.setCategoria("AUTORIZZAZIONE");
        errore.setCodice("401");
        errore.setDescrizione("Non autorizzato");
        errore.setDettaglio(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errore);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Errore> handleForbiddenException(ForbiddenException ex) {
        Errore errore = new Errore();
        errore.setCategoria("AUTORIZZAZIONE");
        errore.setCodice("403");
        errore.setDescrizione("Accesso negato");
        errore.setDettaglio(ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errore);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Errore> handleNotFoundException(NotFoundException ex) {
        Errore errore = new Errore();
        errore.setCategoria(CATEGORIA_RICHIESTA);
        errore.setCodice("404");
        errore.setDescrizione("Risorsa non trovata");
        errore.setDettaglio(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errore);
    }

    /**
     * Path senza controller ne' risorsa statica (es. /actuator/health sulla porta
     * applicativa): 404, non l'errore interno del gestore generico.
     */
    @ExceptionHandler({ NoResourceFoundException.class, NoHandlerFoundException.class })
    public ResponseEntity<Errore> handleNoResourceFoundException(Exception ex) {
        Errore errore = new Errore();
        errore.setCategoria(CATEGORIA_RICHIESTA);
        errore.setCodice("404");
        errore.setDescrizione("Risorsa non trovata");
        errore.setDettaglio(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errore);
    }

    /**
     * Errori del client rilevati da Spring MVC prima di arrivare al controller:
     * metodo HTTP non supportato. Senza questo gestore finirebbero nel gestore
     * generico, con 503.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Errore> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        Errore errore = new Errore();
        errore.setCategoria(CATEGORIA_RICHIESTA);
        errore.setCodice("405");
        errore.setDescrizione("Metodo non consentito");
        errore.setDettaglio(ex.getMessage());
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED);
        if (ex.getSupportedHttpMethods() != null) {
            builder.allow(ex.getSupportedHttpMethods().toArray(new HttpMethod[0]));
        }
        return builder.body(errore);
    }

    /** Content-Type della richiesta non supportato. */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Errore> handleMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex) {
        Errore errore = new Errore();
        errore.setCategoria(CATEGORIA_RICHIESTA);
        errore.setCodice("415");
        errore.setDescrizione("Tipo di contenuto non supportato");
        errore.setDettaglio(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(errore);
    }

    /**
     * Parametri, header o body mancanti o non validi: 400.
     */
    @ExceptionHandler({
        MissingServletRequestParameterException.class,
        MissingRequestHeaderException.class,
        MethodArgumentTypeMismatchException.class,
        HttpMessageNotReadableException.class,
        MethodArgumentNotValidException.class,
        HandlerMethodValidationException.class
    })
    public ResponseEntity<Errore> handleRichiestaNonValida(Exception ex) {
        Errore errore = new Errore();
        errore.setCategoria(CATEGORIA_RICHIESTA);
        errore.setCodice("400");
        errore.setDescrizione("Richiesta non valida");
        errore.setDettaglio(ex instanceof HttpMessageNotReadableException
                ? "Body della richiesta non leggibile"
                : ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errore);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Errore> handleBadRequestException(BadRequestException ex) {
        Errore errore = new Errore();
        errore.setCategoria(CATEGORIA_RICHIESTA);
        errore.setCodice("400");
        errore.setDescrizione("Richiesta non valida");
        errore.setDettaglio(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errore);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Errore> handleValidationException(ValidationException ex) {
        Errore errore = new Errore();
        errore.setCategoria(CATEGORIA_RICHIESTA);
        errore.setCodice("VALIDAZIONE");
        errore.setDescrizione("Errore di validazione");
        errore.setDettaglio(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errore);
    }

    @ExceptionHandler(UnprocessableEntityException.class)
    public ResponseEntity<Errore> handleUnprocessableEntityException(UnprocessableEntityException ex) {
        Errore errore = new Errore();
        errore.setCategoria(CATEGORIA_RICHIESTA);
        errore.setCodice("422");
        errore.setDescrizione("Entità non processabile");
        errore.setDettaglio(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errore);
    }

    @ExceptionHandler(BadGatewayException.class)
    public ResponseEntity<Errore> handleBadGatewayException(BadGatewayException ex) {
        Errore errore = new Errore();
        errore.setCategoria("BACKEND");
        errore.setCodice("502");
        errore.setDescrizione("Errore dal servizio GovPay");
        errore.setDettaglio(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errore);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Errore> handleGenericException(Exception ex) {
        Errore errore = new Errore();
        errore.setCategoria("INTERNO");
        errore.setCodice("503");
        errore.setDescrizione("Servizio non disponibile");
        errore.setDettaglio("Errore interno del server");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errore);
    }
}
