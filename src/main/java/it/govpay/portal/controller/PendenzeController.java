package it.govpay.portal.controller;

import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import it.govpay.portal.api.PendenzeApi;
import it.govpay.portal.model.Avviso;
import it.govpay.portal.model.LinguaSecondaria;
import it.govpay.portal.model.ListaPendenze;
import it.govpay.portal.model.Pendenza;
import it.govpay.portal.model.StatoPendenza;
import it.govpay.portal.service.PendenzeService;

@RestController
public class PendenzeController implements PendenzeApi {

    private final PendenzeService pendenzeService;

    public PendenzeController(PendenzeService pendenzeService) {
        this.pendenzeService = pendenzeService;
    }

    @Override
    public ResponseEntity<Pendenza> creaPendenza(
            String idDominio,
            String idTipoPendenza,
            Map<String, Object> requestBody,
            String idA2A,
            String idPendenza,
            String gRecaptchaResponse) {
        // TODO: implementare la logica
        return ResponseEntity.ok(new Pendenza());
    }

    @Override
    public ResponseEntity<Avviso> getAvviso(
            String idDominio,
            String numeroAvviso,
            String gRecaptchaResponse,
            String idDebitore,
            String UUID,
            LinguaSecondaria linguaSecondaria) {
        return pendenzeService.getAvviso(idDominio, numeroAvviso)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<Pendenza> getPendenza(String idDominio, String numeroAvviso) {
        return pendenzeService.getPendenza(idDominio, numeroAvviso)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<ListaPendenze> getPendenze(String idDominio, StatoPendenza stato) {
        ListaPendenze pendenze = pendenzeService.getPendenze(idDominio, stato);
        return ResponseEntity.ok(pendenze);
    }

    @Override
    public ResponseEntity<Resource> getRicevuta(String idDominio, String numeroAvviso) {
        // TODO: implementare la logica
        return ResponseEntity.ok().build();
    }

}
