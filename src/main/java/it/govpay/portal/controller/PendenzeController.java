package it.govpay.portal.controller;

import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import it.govpay.portal.api.PendenzeApi;
import it.govpay.portal.model.Avviso;
import it.govpay.portal.model.LinguaSecondaria;
import it.govpay.portal.model.ListaPendenze;
import it.govpay.portal.model.ListaRicevute;
import it.govpay.portal.model.Pendenza;
import it.govpay.portal.model.StatoPendenza;

@RestController
public class PendenzeController implements PendenzeApi {

    @Override
    public ResponseEntity<ListaPendenze> getPendenze(
            StatoPendenza stato,
            String idA2A,
            String idPendenza,
            String idDominio,
            String iuv) {
        // TODO: implementare la logica
        return ResponseEntity.ok(new ListaPendenze());
    }

    @Override
    public ResponseEntity<Pendenza> getPendenza(String idA2A, String idPendenza) {
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
        // TODO: implementare la logica
        return ResponseEntity.ok(new Avviso());
    }

    @Override
    public ResponseEntity<Resource> getRicevutaPDF(String idDominio, String numeroAvviso) {
        // TODO: implementare la logica
        return ResponseEntity.ok().build();
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
    public ResponseEntity<ListaRicevute> getRicevute() {
        // TODO: implementare la logica
        return ResponseEntity.ok(new ListaRicevute());
    }

}
