package it.govpay.portal.gde.service;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.govpay.common.client.service.ConnettoreService;
import it.govpay.common.configurazione.ConfigurazioneKeys;
import it.govpay.common.gde.AbstractGdeService;
import it.govpay.common.gde.GdeEventInfo;
import it.govpay.gde.client.beans.NuovoEvento;
import it.govpay.portal.gde.mapper.EventoPortalMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GdeService extends AbstractGdeService {

    private final EventoPortalMapper eventoPortalMapper;
    private final ConnettoreService connettoreService;

    public GdeService(ObjectMapper objectMapper,
                      @Qualifier("asyncHttpExecutor") Executor asyncHttpExecutor,
                      EventoPortalMapper eventoPortalMapper,
                      ConnettoreService connettoreService) {
        super(objectMapper, asyncHttpExecutor, null);
        this.eventoPortalMapper = eventoPortalMapper;
        this.connettoreService = connettoreService;
    }

    @Override
    protected String getGdeEndpoint() {
        return connettoreService.getConnettore(ConfigurazioneKeys.COD_CONNETTORE_GDE).getUrl() + "/eventi";
    }

    @Override
    protected RestTemplate getGdeRestTemplate() {
        return connettoreService.getRestTemplate(ConfigurazioneKeys.COD_CONNETTORE_GDE);
    }

    @Override
    public boolean isAbilitato() {
        try {
            return connettoreService.getConnettore(ConfigurazioneKeys.COD_CONNETTORE_GDE).isAbilitato();
        } catch (IllegalArgumentException e) {
            log.debug("Connettore GDE non configurato: {}", e.getMessage());
            return false;
        }
    }

    @Override
    protected NuovoEvento convertToGdeEvent(GdeEventInfo eventInfo) {
        throw new UnsupportedOperationException(
                "GdeService usa sendEventAsync(NuovoEvento) direttamente, non il pattern GdeEventInfo");
    }

    private void sendEventAsync(NuovoEvento nuovoEvento) {
        if (!isAbilitato()) {
            log.debug("Connettore GDE disabilitato, evento {} non inviato", nuovoEvento.getTipoEvento());
            return;
        }
        CompletableFuture.runAsync(() -> {
            try {
                getGdeRestTemplate().postForEntity(getGdeEndpoint(), nuovoEvento, Void.class);
                log.debug("Evento {} inviato con successo al GDE", nuovoEvento.getTipoEvento());
            } catch (Exception ex) {
                log.warn("Impossibile inviare evento {} al GDE (il portale continua normalmente): {}",
                        nuovoEvento.getTipoEvento(), ex.getMessage());
                log.debug("Dettaglio errore GDE:", ex);
            }
        }, this.asyncExecutor);
    }

    public void saveEventOk(String tipoEvento, OffsetDateTime dataStart, OffsetDateTime dataEnd,
                             HttpServletRequest request, int statusCode, String idDominio,
                             Object requestBody, Object responseBody) {
        String transactionId = UUID.randomUUID().toString();
        NuovoEvento evento = eventoPortalMapper.createEventoOk(tipoEvento, transactionId, dataStart, dataEnd);

        if (idDominio != null) {
            evento.setIdDominio(idDominio);
        }

        eventoPortalMapper.setParametriRichiesta(evento, request, requestBody);
        eventoPortalMapper.setParametriRisposta(evento, dataEnd, statusCode, responseBody);

        sendEventAsync(evento);
    }

    public void saveEventKo(String tipoEvento, OffsetDateTime dataStart, OffsetDateTime dataEnd,
                             HttpServletRequest request, int statusCode, Exception exception,
                             String idDominio, Object requestBody) {
        String transactionId = UUID.randomUUID().toString();
        NuovoEvento evento = eventoPortalMapper.createEventoKo(tipoEvento, transactionId, dataStart, dataEnd,
                statusCode, exception);

        if (idDominio != null) {
            evento.setIdDominio(idDominio);
        }

        eventoPortalMapper.setParametriRichiesta(evento, request, requestBody);
        eventoPortalMapper.setParametriRisposta(evento, dataEnd, statusCode,
                exception != null ? exception.getMessage() : null);

        sendEventAsync(evento);
    }
}
