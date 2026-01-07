package it.govpay.ente.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.govpay.ente.mapper.VersamentoMapper;
import it.govpay.ente.model.PendenzaVerificata;
import it.govpay.ente.model.StatoPendenzaVerificata;
import it.govpay.portal.entity.Versamento;
import it.govpay.portal.repository.VersamentoRepository;

@Service
@Transactional(readOnly = true)
public class VerificaPendenzeService {

    private final VersamentoRepository versamentoRepository;
    private final VersamentoMapper versamentoMapper;

    public VerificaPendenzeService(VersamentoRepository versamentoRepository, VersamentoMapper versamentoMapper) {
        this.versamentoRepository = versamentoRepository;
        this.versamentoMapper = versamentoMapper;
    }

    public PendenzaVerificata verificaByAvviso(String idDominio, String numeroAvviso) {
        Optional<Versamento> versamento = versamentoRepository
                .findByDominioCodDominioAndNumeroAvviso(idDominio, numeroAvviso);

        return versamento
                .map(versamentoMapper::toPendenzaVerificata)
                .orElseGet(() -> {
                    PendenzaVerificata notFound = new PendenzaVerificata(StatoPendenzaVerificata.SCONOSCIUTA);
                    notFound.setDescrizioneStato("Pendenza non trovata per dominio " + idDominio + " e numero avviso " + numeroAvviso);
                    return notFound;
                });
    }

    public PendenzaVerificata verificaByPendenza(String idA2A, String idPendenza) {
        Optional<Versamento> versamento = versamentoRepository
                .findByApplicazioneCodApplicazioneAndCodVersamentoEnte(idA2A, idPendenza);

        return versamento
                .map(versamentoMapper::toPendenzaVerificata)
                .orElseGet(() -> {
                    PendenzaVerificata notFound = new PendenzaVerificata(StatoPendenzaVerificata.SCONOSCIUTA);
                    notFound.setDescrizioneStato("Pendenza non trovata per applicazione " + idA2A + " e id pendenza " + idPendenza);
                    return notFound;
                });
    }

}
