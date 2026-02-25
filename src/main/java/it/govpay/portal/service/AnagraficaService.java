package it.govpay.portal.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.govpay.portal.config.SpidUserDetails;
import it.govpay.portal.entity.TipoVersamentoDominio;
import it.govpay.portal.exception.UnauthorizedException;
import it.govpay.portal.mapper.AnagraficaMapper;
import it.govpay.portal.model.Dominio;
import it.govpay.portal.model.ListaDomini;
import it.govpay.portal.model.ListaTipiPendenza;
import it.govpay.portal.model.Profilo;
import it.govpay.portal.model.Soggetto;
import it.govpay.portal.model.TipoPendenza;
import it.govpay.portal.model.TipoSoggetto;
import it.govpay.portal.repository.DominioLogoRepository;
import it.govpay.portal.repository.DominioRepository;
import it.govpay.portal.repository.TipoVersamentoDominioRepository;

@Service
@Transactional(readOnly = true)
public class AnagraficaService {

    private final DominioRepository dominioRepository;
    private final DominioLogoRepository dominioLogoRepository;
    private final TipoVersamentoDominioRepository tipoVersamentoDominioRepository;
    private final AnagraficaMapper anagraficaMapper;

    public AnagraficaService(
            DominioRepository dominioRepository,
            DominioLogoRepository dominioLogoRepository,
            TipoVersamentoDominioRepository tipoVersamentoDominioRepository,
            AnagraficaMapper anagraficaMapper) {
        this.dominioRepository = dominioRepository;
        this.dominioLogoRepository = dominioLogoRepository;
        this.tipoVersamentoDominioRepository = tipoVersamentoDominioRepository;
        this.anagraficaMapper = anagraficaMapper;
    }

    public Profilo getProfilo() {
        SpidUserDetails spidUser = getAuthenticatedUser();

        Profilo profilo = new Profilo();
        profilo.setNome(spidUser.getFiscalNumber());
        profilo.setAnagrafica(popolaAnagraficaCittadino(spidUser));

        return profilo;
    }

    private Soggetto popolaAnagraficaCittadino(SpidUserDetails spidUser) {
        Soggetto anagrafica = new Soggetto();
        anagrafica.setTipo(TipoSoggetto.F);
        anagrafica.setIdentificativo(spidUser.getFiscalNumber());
        anagrafica.setAnagrafica(spidUser.getFullName());
        anagrafica.setEmail(spidUser.getEmail());
        anagrafica.setCellulare(spidUser.getMobilePhone());
        anagrafica.setIndirizzo(spidUser.getAddress());
        return anagrafica;
    }

    public void logout() {
        // Verifica che l'utente sia autenticato prima di procedere con il logout
        getAuthenticatedUser();
    }

    public ListaDomini getDomini() {
        List<it.govpay.portal.entity.Dominio> entities = dominioRepository.findAll();

        List<Dominio> domini = entities.stream()
                .map(anagraficaMapper::toDominio).toList();

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
                .map(anagraficaMapper::toTipoPendenzaIndex).toList();

        ListaTipiPendenza result = new ListaTipiPendenza();
        result.setRisultati(tipiPendenza);

        return result;
    }

    public Optional<TipoPendenza> getTipoPendenza(String idDominio, String idTipoPendenza) {
        return tipoVersamentoDominioRepository
                .findByDominioCodDominioAndTipoVersamentoCodTipoVersamento(idDominio, idTipoPendenza)
                .map(anagraficaMapper::toTipoPendenza);
    }

    public Optional<byte[]> getLogo(String idDominio) {
        return this.dominioLogoRepository.findLogoByCodDominio(idDominio);
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
