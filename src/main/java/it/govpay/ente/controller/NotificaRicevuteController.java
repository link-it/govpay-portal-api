package it.govpay.ente.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import it.govpay.ente.api.NotificaRicevuteApi;
import it.govpay.ente.model.Ricevuta;

@RestController
public class NotificaRicevuteController implements NotificaRicevuteApi {

    @Override
    public ResponseEntity<Void> notificaRicevuta(
            String idDominio,
            String iuv,
            String idRicevuta,
            String idSession,
            String idSessionePortale,
            String idCarrello,
            Ricevuta ricevuta) {
        // TODO: implementare la logica di notifica ricevuta di pagamento
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
