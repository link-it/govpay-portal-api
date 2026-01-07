package it.govpay.portal.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.govpay.portal.entity.TipoVersamentoDominio;
import it.govpay.portal.mapper.AnagraficaMapper;
import it.govpay.portal.model.Dominio;
import it.govpay.portal.model.ListaDomini;
import it.govpay.portal.model.ListaTipiPendenza;
import it.govpay.portal.model.Profilo;
import it.govpay.portal.model.TipoPendenza;
import it.govpay.portal.repository.DominioRepository;
import it.govpay.portal.repository.TipoVersamentoDominioRepository;

@Service
@Transactional(readOnly = true)
public class AnagraficaService {

    private final DominioRepository dominioRepository;
    private final TipoVersamentoDominioRepository tipoVersamentoDominioRepository;
    private final AnagraficaMapper anagraficaMapper;

    public AnagraficaService(
            DominioRepository dominioRepository,
            TipoVersamentoDominioRepository tipoVersamentoDominioRepository,
            AnagraficaMapper anagraficaMapper) {
        this.dominioRepository = dominioRepository;
        this.tipoVersamentoDominioRepository = tipoVersamentoDominioRepository;
        this.anagraficaMapper = anagraficaMapper;
    }

    public Profilo getProfilo() {
        // TODO: integrare con Spring Security per ottenere l'utente autenticato
        Profilo profilo = new Profilo();
        profilo.setNome("Utente Anonimo");
        return profilo;
    }

    public void logout() {
        // TODO: integrare con Spring Security per gestire il logout
    }

    public ListaDomini getDomini() {
        List<it.govpay.portal.entity.Dominio> entities = dominioRepository.findAll();

        List<Dominio> domini = entities.stream()
                .map(anagraficaMapper::toDominio)
                .collect(Collectors.toList());

        ListaDomini result = new ListaDomini();
        result.setRisultati(domini);

        return result;
    }

    public Optional<Dominio> getDominio(String idDominio) {
        return dominioRepository.findByCodDominio(idDominio)
                .map(anagraficaMapper::toDominio);
    }

    public ListaTipiPendenza getTipiPendenza(String idDominio) {
        Optional<it.govpay.portal.entity.Dominio> dominio = dominioRepository.findByCodDominio(idDominio);

        if (dominio.isEmpty()) {
            return new ListaTipiPendenza();
        }

        List<TipoVersamentoDominio> entities = tipoVersamentoDominioRepository
                .findByDominioIdAndAbilitatoAndPagAbilitato(dominio.get().getId(), true, true);

        List<TipoPendenza> tipiPendenza = entities.stream()
                .map(anagraficaMapper::toTipoPendenza)
                .collect(Collectors.toList());

        ListaTipiPendenza result = new ListaTipiPendenza();
        result.setRisultati(tipiPendenza);

        return result;
    }

    public Optional<TipoPendenza> getTipoPendenza(String idDominio, String idTipoPendenza) {
        return tipoVersamentoDominioRepository
                .findByDominioCodDominioAndTipoVersamentoCodTipoVersamento(idDominio, idTipoPendenza)
                .map(anagraficaMapper::toTipoPendenza);
    }

}
