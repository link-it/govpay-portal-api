package it.govpay.ente.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import it.govpay.ente.api.VerificaPendenzeApi;
import it.govpay.ente.model.PendenzaVerificata;

@RestController
public class VerificaPendenzeController implements VerificaPendenzeApi {

    @Override
    public ResponseEntity<PendenzaVerificata> getAvviso(String idDominio, String numeroAvviso) {
        // TODO: implementare la logica di verifica pendenza da avviso
        return ResponseEntity.ok(new PendenzaVerificata());
    }

    @Override
    public ResponseEntity<PendenzaVerificata> verifyPendenza(String idA2A, String idPendenza) {
        // TODO: implementare la logica di verifica pendenza da identificativo
        return ResponseEntity.ok(new PendenzaVerificata());
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
