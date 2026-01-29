package it.govpay.portal.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.govpay.portal.config.SpidUserDetails;
import it.govpay.portal.entity.StatoVersamento;
import it.govpay.portal.entity.Versamento;
import it.govpay.portal.exception.UnauthorizedException;
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
        SpidUserDetails spidUser = getAuthenticatedUser();
        String codiceFiscale = spidUser.getFiscalNumber();

        List<Versamento> versamenti;
        if (stato == null) {
            versamenti = versamentoRepository.findByDominioCodDominioAndDebitoreIdentificativoOrderByDataCreazioneDesc(
                    idDominio, codiceFiscale);
        } else if (stato == StatoPendenza.SCADUTA) {
            // SCADUTA è uno stato derivato: pendenze non pagate con data scadenza passata
            versamenti = versamentoRepository.findByDominioCodDominioAndDebitoreIdentificativoAndStatoVersamentoAndDataScadenzaBeforeOrderByDataCreazioneDesc(
                    idDominio, codiceFiscale, StatoVersamento.NON_ESEGUITO, LocalDateTime.now());
        } else {
            StatoVersamento statoVersamento = mapStatoPendenzaToStatoVersamento(stato);
            versamenti = versamentoRepository.findByDominioCodDominioAndDebitoreIdentificativoAndStatoVersamentoOrderByDataCreazioneDesc(
                    idDominio, codiceFiscale, statoVersamento);
        }

        List<Pendenza> pendenze = versamenti.stream()
                .map(pendenzeMapper::toPendenza).toList();

        ListaPendenze result = new ListaPendenze();
        result.setRisultati(pendenze);

        return result;
    }

    public Optional<Pendenza> getPendenza(String idDominio, String numeroAvviso) {
        SpidUserDetails spidUser = getAuthenticatedUser();
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

    private StatoVersamento mapStatoPendenzaToStatoVersamento(StatoPendenza stato) {
        return switch (stato) {
            case ESEGUITA -> StatoVersamento.ESEGUITO;
            case NON_ESEGUITA -> StatoVersamento.NON_ESEGUITO;
            case ESEGUITA_PARZIALE -> StatoVersamento.PARZIALMENTE_ESEGUITO;
            case ANNULLATA -> StatoVersamento.ANNULLATO;
            case SCADUTA -> throw new IllegalArgumentException("SCADUTA deve essere gestito separatamente");
            case ANOMALA -> StatoVersamento.ANOMALO;
        };
    }

    private SpidUserDetails getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedException("Autenticazione non presente");
        }

        if (!(authentication.getPrincipal() instanceof SpidUserDetails)) {
            throw new UnauthorizedException("Utente non autenticato tramite SPID");
        }

        return (SpidUserDetails) authentication.getPrincipal();
    }

}
