package it.govpay.portal.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import it.govpay.portal.model.Errore;

/**
 * Gestore globale delle eccezioni per restituire risposte strutturate.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

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
        errore.setCategoria("RICHIESTA");
        errore.setCodice("404");
        errore.setDescrizione("Risorsa non trovata");
        errore.setDettaglio(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errore);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Errore> handleBadRequestException(BadRequestException ex) {
        Errore errore = new Errore();
        errore.setCategoria("RICHIESTA");
        errore.setCodice("400");
        errore.setDescrizione("Richiesta non valida");
        errore.setDettaglio(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errore);
    }

    @ExceptionHandler(UnprocessableEntityException.class)
    public ResponseEntity<Errore> handleUnprocessableEntityException(UnprocessableEntityException ex) {
        Errore errore = new Errore();
        errore.setCategoria("RICHIESTA");
        errore.setCodice("422");
        errore.setDescrizione("Entità non processabile");
        errore.setDettaglio(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errore);
    }
}
