package it.govpay.portal.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.govpay.portal.config.SpidUserDetails;
import it.govpay.portal.entity.Versamento;
import it.govpay.portal.mapper.PendenzeMapper;
import it.govpay.portal.model.Avviso;
import it.govpay.portal.model.ListaPendenze;
import it.govpay.portal.model.Pendenza;
import it.govpay.portal.model.Ricevuta;
import it.govpay.portal.model.StatoPendenza;
import it.govpay.portal.repository.VersamentoRepository;

@Service
@Transactional(readOnly = true)
public class PendenzeService {

    private final VersamentoRepository versamentoRepository;
    private final PendenzeMapper pendenzeMapper;

    public PendenzeService(VersamentoRepository versamentoRepository, PendenzeMapper pendenzeMapper) {
        this.versamentoRepository = versamentoRepository;
        this.pendenzeMapper = pendenzeMapper;
    }

    public ListaPendenze getPendenze(String idDominio, StatoPendenza stato) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SpidUserDetails spidUser = (SpidUserDetails) authentication.getPrincipal();
        String codiceFiscale = spidUser.getFiscalNumber();

        List<Versamento> versamenti;
        if (stato != null) {
            String statoVersamento = mapStatoPendenzaToStatoVersamento(stato);
            versamenti = versamentoRepository.findByDominioCodDominioAndDebitoreIdentificativoAndStatoVersamento(
                    idDominio, codiceFiscale, statoVersamento);
        } else {
            versamenti = versamentoRepository.findByDominioCodDominioAndDebitoreIdentificativo(
                    idDominio, codiceFiscale);
        }

        List<Pendenza> pendenze = versamenti.stream()
                .map(pendenzeMapper::toPendenza).toList();

        ListaPendenze result = new ListaPendenze();
        result.setRisultati(pendenze);

        return result;
    }

    public Optional<Pendenza> getPendenza(String idDominio, String numeroAvviso) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SpidUserDetails spidUser = (SpidUserDetails) authentication.getPrincipal();
        String codiceFiscale = spidUser.getFiscalNumber();

        return versamentoRepository.findByDominioCodDominioAndNumeroAvviso(idDominio, numeroAvviso)
                .filter(v -> codiceFiscale.equals(v.getDebitoreIdentificativo()))
                .map(pendenzeMapper::toPendenza);
    }

    public Optional<Avviso> getAvviso(String idDominio, String numeroAvviso) {
        return versamentoRepository.findByDominioCodDominioAndNumeroAvviso(idDominio, numeroAvviso)
                .map(versamento -> pendenzeMapper.toAvviso(versamento, versamento.getDominio()));
    }

    public Optional<Ricevuta> getRicevuta(String idDominio, String numeroAvviso) {
        return versamentoRepository.findByDominioCodDominioAndNumeroAvviso(idDominio, numeroAvviso)
                .map(pendenzeMapper::toRicevuta);
    }

    private String mapStatoPendenzaToStatoVersamento(StatoPendenza stato) {
        return switch (stato) {
            case ESEGUITA -> "ESEGUITO";
            case NON_ESEGUITA -> "NON_ESEGUITO";
            case ESEGUITA_PARZIALE -> "PARZIALMENTE_ESEGUITO";
            case ANNULLATA -> "ANNULLATO";
            case SCADUTA -> "SCADUTO";
            case ANOMALA -> "ANOMALO";
        };
    }

}
