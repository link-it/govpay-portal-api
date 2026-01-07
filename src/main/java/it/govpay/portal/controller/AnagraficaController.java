package it.govpay.portal.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import it.govpay.portal.api.AnagraficaApi;
import it.govpay.portal.model.Dominio;
import it.govpay.portal.model.ListaDomini;
import it.govpay.portal.model.ListaTipiPendenza;
import it.govpay.portal.model.Profilo;
import it.govpay.portal.model.TipoPendenza;
import it.govpay.portal.service.AnagraficaService;

@RestController
public class AnagraficaController implements AnagraficaApi {

    private final AnagraficaService anagraficaService;

    public AnagraficaController(AnagraficaService anagraficaService) {
        this.anagraficaService = anagraficaService;
    }

    @Override
    public ResponseEntity<Profilo> getProfilo() {
        Profilo profilo = anagraficaService.getProfilo();
        return ResponseEntity.ok(profilo);
    }

    @Override
    public ResponseEntity<Void> logout() {
        anagraficaService.logout();
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<ListaDomini> getDomini() {
        ListaDomini result = anagraficaService.getDomini();
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Dominio> getDominio(String idDominio) {
        return anagraficaService.getDominio(idDominio)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<ListaTipiPendenza> getTipiPendenza(String idDominio) {
        ListaTipiPendenza result = anagraficaService.getTipiPendenza(idDominio);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<TipoPendenza> getTipoPendenza(String idDominio, String idTipoPendenza) {
        return anagraficaService.getTipoPendenza(idDominio, idTipoPendenza)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
