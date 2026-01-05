package it.govpay.portal.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import it.govpay.portal.api.AnagraficaApi;
import it.govpay.portal.model.Dominio;
import it.govpay.portal.model.ListaDomini;
import it.govpay.portal.model.ListaTipiPendenza;
import it.govpay.portal.model.Profilo;
import it.govpay.portal.model.TipoPendenza;

@RestController
public class AnagraficaController implements AnagraficaApi {

    @Override
    public ResponseEntity<Profilo> getProfilo() {
        // TODO: implementare la logica
        return ResponseEntity.ok(new Profilo());
    }

    @Override
    public ResponseEntity<Void> logout() {
        // TODO: implementare la logica di logout
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<ListaDomini> getDomini() {
        // TODO: implementare la logica
        return ResponseEntity.ok(new ListaDomini());
    }

    @Override
    public ResponseEntity<Dominio> getDominio(String idDominio) {
        // TODO: implementare la logica
        return ResponseEntity.ok(new Dominio());
    }

    @Override
    public ResponseEntity<ListaTipiPendenza> getTipiPendenza(String idDominio) {
        // TODO: implementare la logica
        return ResponseEntity.ok(new ListaTipiPendenza());
    }

    @Override
    public ResponseEntity<TipoPendenza> getTipoPendenza(String idDominio, String idTipoPendenza) {
        // TODO: implementare la logica
        return ResponseEntity.ok(new TipoPendenza());
    }

}
