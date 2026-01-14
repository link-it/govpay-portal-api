package it.govpay.portal.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import it.govpay.pendenze.client.api.PendenzeApi;
import it.govpay.pendenze.client.model.NuovaPendenza;
import it.govpay.pendenze.client.model.PendenzaCreata;
import it.govpay.portal.entity.Versamento;
import it.govpay.portal.mapper.GovPayPendenzeMapper;

@Service
public class GovPayService {

    private static final Logger log = LoggerFactory.getLogger(GovPayService.class);

    private final PendenzeApi pendenzeApi;
    private final GovPayPendenzeMapper mapper;

    public GovPayService(PendenzeApi pendenzeApi, GovPayPendenzeMapper mapper) {
        this.pendenzeApi = pendenzeApi;
        this.mapper = mapper;
    }

    public PendenzaCreata addPendenza(String idA2A, String idPendenza, Versamento versamento) {
        return addPendenza(idA2A, idPendenza, versamento, false, null);
    }

    public PendenzaCreata addPendenza(String idA2A, String idPendenza, Versamento versamento,
            boolean stampaAvviso) {
        return addPendenza(idA2A, idPendenza, versamento, stampaAvviso, null);
    }

    public PendenzaCreata addPendenza(String idA2A, String idPendenza, Versamento versamento,
            boolean stampaAvviso, it.govpay.pendenze.client.model.AddPendenzaDataAvvisaturaParameter dataAvvisatura) {
        log.debug("Calling GovPay addPendenza for idA2A={}, idPendenza={}", idA2A, idPendenza);

        NuovaPendenza nuovaPendenza = mapper.toNuovaPendenza(versamento);

        try {
            PendenzaCreata result = pendenzeApi.addPendenza(idA2A, idPendenza, stampaAvviso, dataAvvisatura, nuovaPendenza);
            log.info("Pendenza created successfully: idDominio={}, numeroAvviso={}",
                    result.getIdDominio(), result.getNumeroAvviso());
            return result;
        } catch (RestClientException e) {
            log.error("Error calling GovPay addPendenza: {}", e.getMessage(), e);
            throw e;
        }
    }
}
