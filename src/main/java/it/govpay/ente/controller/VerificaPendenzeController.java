package it.govpay.ente.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import it.govpay.ente.api.VerificaPendenzeApi;
import it.govpay.ente.model.PendenzaVerificata;
import it.govpay.ente.service.VerificaPendenzeService;

@RestController
public class VerificaPendenzeController implements VerificaPendenzeApi {

    private final VerificaPendenzeService verificaPendenzeService;

    public VerificaPendenzeController(VerificaPendenzeService verificaPendenzeService) {
        this.verificaPendenzeService = verificaPendenzeService;
    }

    @Override
    public ResponseEntity<PendenzaVerificata> getAvviso(String idDominio, String numeroAvviso) {
        PendenzaVerificata result = verificaPendenzeService.verificaByAvviso(idDominio, numeroAvviso);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<PendenzaVerificata> verifyPendenza(String idA2A, String idPendenza) {
        PendenzaVerificata result = verificaPendenzeService.verificaByPendenza(idA2A, idPendenza);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<PendenzaVerificata> verifyPendenzaMod4(
            String idDominio,
            String idTipoPendenza,
            Object body) {
        // TODO: implementare la logica di acquisizione pendenza con dati custom
        return ResponseEntity.ok(new PendenzaVerificata());
    }

}
