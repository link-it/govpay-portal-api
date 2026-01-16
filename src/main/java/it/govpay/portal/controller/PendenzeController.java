package it.govpay.portal.controller;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.govpay.portal.model.Avviso;
import it.govpay.portal.model.LinguaSecondaria;
import it.govpay.portal.model.ListaPendenze;
import it.govpay.portal.model.Pendenza;
import it.govpay.portal.model.Ricevuta;
import it.govpay.portal.model.StatoPendenza;
import it.govpay.portal.service.PendenzeService;
import it.govpay.portal.service.StampeService;

@RestController
public class PendenzeController {

    private final PendenzeService pendenzeService;
    private final StampeService stampeService;

    public PendenzeController(PendenzeService pendenzeService, StampeService stampeService) {
        this.pendenzeService = pendenzeService;
        this.stampeService = stampeService;
    }

    @PostMapping(value = "/pendenze/{idDominio}/{idTipoPendenza}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Pendenza> creaPendenza(
            @PathVariable("idDominio") String idDominio,
            @PathVariable("idTipoPendenza") String idTipoPendenza,
            @RequestBody Map<String, Object> requestBody,
            @RequestParam(value = "idA2A", required = false) String idA2A,
            @RequestParam(value = "idPendenza", required = false) String idPendenza,
            @RequestParam(value = "gRecaptchaResponse", required = false) String gRecaptchaResponse) {
        // TODO: implementare la logica
        return ResponseEntity.ok(new Pendenza());
    }

    @GetMapping(value = "/pendenze/{idDominio}/{numeroAvviso}/avviso",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PDF_VALUE})
    public ResponseEntity<?> getAvviso(
            @PathVariable("idDominio") String idDominio,
            @PathVariable("numeroAvviso") String numeroAvviso,
            @RequestParam(value = "gRecaptchaResponse", required = false) String gRecaptchaResponse,
            @RequestParam(value = "idDebitore", required = false) String idDebitore,
            @RequestParam(value = "UUID", required = false) String uuid,
            @RequestParam(value = "linguaSecondaria", required = false) LinguaSecondaria linguaSecondaria,
            @RequestHeader(value = HttpHeaders.ACCEPT, required = false) String acceptHeader) {

        // Determina il content type richiesto
        MediaType requestedMediaType = parseAcceptHeader(acceptHeader);

        if (requestedMediaType == null) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body("Accept header richiesto: application/json o application/pdf");
        }

        if (MediaType.APPLICATION_PDF.equals(requestedMediaType) ||
            MediaType.APPLICATION_PDF.isCompatibleWith(requestedMediaType)) {
            // Genera il PDF tramite il servizio stampe
            return stampeService.generateAvvisoPdf(idDominio, numeroAvviso, linguaSecondaria)
                    .map(pdf -> ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_PDF)
                            .header(HttpHeaders.CONTENT_DISPOSITION,
                                    "attachment; filename=\"avviso_" + numeroAvviso + ".pdf\"")
                            .body(pdf))
                    .orElse(ResponseEntity.notFound().build());
        } else if (MediaType.APPLICATION_JSON.equals(requestedMediaType) ||
                   MediaType.APPLICATION_JSON.isCompatibleWith(requestedMediaType)) {
            // Restituisce il JSON dell'avviso
            return pendenzeService.getAvviso(idDominio, numeroAvviso)
                    .map(avviso -> ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body((Object) avviso))
                    .orElse(ResponseEntity.notFound().build());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body("Accept header non supportato. Valori ammessi: application/json, application/pdf");
        }
    }

    @GetMapping(value = "/pendenze/{idDominio}/{numeroAvviso}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Pendenza> getPendenza(
            @PathVariable("idDominio") String idDominio,
            @PathVariable("numeroAvviso") String numeroAvviso) {
        return pendenzeService.getPendenza(idDominio, numeroAvviso)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/pendenze/{idDominio}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ListaPendenze> getPendenze(
            @PathVariable("idDominio") String idDominio,
            @RequestParam(value = "stato", required = false) StatoPendenza stato) {
        ListaPendenze pendenze = pendenzeService.getPendenze(idDominio, stato);
        return ResponseEntity.ok(pendenze);
    }

    @GetMapping(value = "/pendenze/{idDominio}/{numeroAvviso}/ricevuta",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PDF_VALUE})
    public ResponseEntity<?> getRicevuta(
            @PathVariable("idDominio") String idDominio,
            @PathVariable("numeroAvviso") String numeroAvviso,
            @RequestHeader(value = HttpHeaders.ACCEPT, required = false) String acceptHeader) {

        // Determina il content type richiesto
        MediaType requestedMediaType = parseAcceptHeader(acceptHeader);

        if (requestedMediaType == null) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body("Accept header richiesto: application/json o application/pdf");
        }

        if (MediaType.APPLICATION_PDF.equals(requestedMediaType) ||
            MediaType.APPLICATION_PDF.isCompatibleWith(requestedMediaType)) {
            // Genera il PDF tramite il servizio stampe
            return stampeService.generateRicevutaPdf(idDominio, numeroAvviso)
                    .map(pdf -> ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_PDF)
                            .header(HttpHeaders.CONTENT_DISPOSITION,
                                    "attachment; filename=\"ricevuta_" + numeroAvviso + ".pdf\"")
                            .body(pdf))
                    .orElse(ResponseEntity.notFound().build());
        } else if (MediaType.APPLICATION_JSON.equals(requestedMediaType) ||
                   MediaType.APPLICATION_JSON.isCompatibleWith(requestedMediaType)) {
            // Restituisce il JSON della ricevuta
            return pendenzeService.getRicevuta(idDominio, numeroAvviso)
                    .map(ricevuta -> ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body((Object) ricevuta))
                    .orElse(ResponseEntity.notFound().build());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body("Accept header non supportato. Valori ammessi: application/json, application/pdf");
        }
    }

    /**
     * Analizza l'header Accept e restituisce il MediaType preferito.
     * Supporta solo application/json e application/pdf.
     */
    private MediaType parseAcceptHeader(String acceptHeader) {
        if (acceptHeader == null || acceptHeader.isBlank()) {
            return null;
        }

        try {
            for (MediaType mediaType : MediaType.parseMediaTypes(acceptHeader)) {
                if (MediaType.APPLICATION_PDF.isCompatibleWith(mediaType)) {
                    return MediaType.APPLICATION_PDF;
                }
                if (MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)) {
                    return MediaType.APPLICATION_JSON;
                }
            }
        } catch (Exception e) {
            // Header malformato
            return null;
        }

        return null;
    }
}
