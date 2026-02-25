package it.govpay.portal.gde.mapper;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.govpay.gde.client.beans.CategoriaEvento;
import it.govpay.gde.client.beans.ComponenteEvento;
import it.govpay.gde.client.beans.DettaglioRichiesta;
import it.govpay.gde.client.beans.DettaglioRisposta;
import it.govpay.gde.client.beans.EsitoEvento;
import it.govpay.gde.client.beans.Header;
import it.govpay.gde.client.beans.NuovoEvento;
import it.govpay.gde.client.beans.RuoloEvento;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EventoPortalMapper {

    @Value("${govpay.portal.cluster-id}")
    private String clusterId;

    public NuovoEvento createEvento(String tipoEvento, String transactionId,
                                     OffsetDateTime dataStart, OffsetDateTime dataEnd) {
        NuovoEvento nuovoEvento = new NuovoEvento();

        nuovoEvento.setCategoriaEvento(CategoriaEvento.INTERFACCIA);
        nuovoEvento.setClusterId(clusterId);
        nuovoEvento.setDataEvento(dataStart);
        nuovoEvento.setDurataEvento(dataEnd.toEpochSecond() - dataStart.toEpochSecond());
        nuovoEvento.setRuolo(RuoloEvento.SERVER);
        nuovoEvento.setComponente(ComponenteEvento.API_PAGAMENTO);
        nuovoEvento.setTipoEvento(tipoEvento);
        nuovoEvento.setTransactionId(transactionId);

        return nuovoEvento;
    }

    public NuovoEvento createEventoOk(String tipoEvento, String transactionId,
                                       OffsetDateTime dataStart, OffsetDateTime dataEnd) {
        NuovoEvento nuovoEvento = createEvento(tipoEvento, transactionId, dataStart, dataEnd);
        nuovoEvento.setEsito(EsitoEvento.OK);
        return nuovoEvento;
    }

    public NuovoEvento createEventoKo(String tipoEvento, String transactionId,
                                       OffsetDateTime dataStart, OffsetDateTime dataEnd,
                                       int statusCode, Exception exception) {
        NuovoEvento nuovoEvento = createEvento(tipoEvento, transactionId, dataStart, dataEnd);

        if (statusCode >= 500) {
            nuovoEvento.setEsito(EsitoEvento.FAIL);
        } else {
            nuovoEvento.setEsito(EsitoEvento.KO);
        }

        nuovoEvento.setSottotipoEsito(String.valueOf(statusCode));
        if (exception != null) {
            nuovoEvento.setDettaglioEsito(exception.getMessage());
        }

        return nuovoEvento;
    }

    public void setParametriRichiesta(NuovoEvento nuovoEvento, HttpServletRequest request) {
        DettaglioRichiesta dettaglioRichiesta = new DettaglioRichiesta();
        dettaglioRichiesta.setDataOraRichiesta(nuovoEvento.getDataEvento());
        dettaglioRichiesta.setMethod(request.getMethod());

        String url = request.getRequestURL().toString();
        String queryString = request.getQueryString();
        if (queryString != null) {
            url = url + "?" + queryString;
        }
        dettaglioRichiesta.setUrl(url);

        dettaglioRichiesta.setHeaders(extractHeaders(request));

        nuovoEvento.setParametriRichiesta(dettaglioRichiesta);
    }

    public void setParametriRisposta(NuovoEvento nuovoEvento, OffsetDateTime dataEnd, int statusCode) {
        DettaglioRisposta dettaglioRisposta = new DettaglioRisposta();
        dettaglioRisposta.setDataOraRisposta(dataEnd);
        dettaglioRisposta.setStatus(BigDecimal.valueOf(statusCode));
        dettaglioRisposta.setHeaders(new ArrayList<>());

        nuovoEvento.setParametriRisposta(dettaglioRisposta);
    }

    private List<Header> extractHeaders(HttpServletRequest request) {
        List<Header> headers = new ArrayList<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                Header header = new Header();
                header.setNome(name);
                header.setValore(request.getHeader(name));
                headers.add(header);
            }
        }
        return headers;
    }
}
