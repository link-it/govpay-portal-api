package it.govpay.portal.controller;

import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Optional;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import eu.medsea.mimeutil.MimeUtil;
import it.govpay.portal.api.AnagraficaApi;
import it.govpay.portal.config.SpidUserDetails;
import it.govpay.portal.exception.NotFoundException;
import it.govpay.portal.gde.Costanti;
import it.govpay.portal.gde.service.GdeService;
import lombok.extern.slf4j.Slf4j;
import it.govpay.portal.model.Dominio;
import it.govpay.portal.model.ListaDomini;
import it.govpay.portal.model.ListaTipiPendenza;
import it.govpay.portal.model.Profilo;
import it.govpay.portal.model.TipoPendenza;
import it.govpay.portal.service.AnagraficaService;
import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@RestController
public class AnagraficaController implements AnagraficaApi {

	private static final Integer CACHE_LOGO = 2 * 60 * 60;

    private final AnagraficaService anagraficaService;
    private final GdeService gdeService;
    private final HttpServletRequest request;

    public AnagraficaController(AnagraficaService anagraficaService, GdeService gdeService,
                                HttpServletRequest request) {
        this.anagraficaService = anagraficaService;
        this.gdeService = gdeService;
        this.request = request;
    }

    @Override
    public ResponseEntity<Profilo> getProfilo() {
        OffsetDateTime startTime = OffsetDateTime.now();
        String principal = getPrincipalName();

        try {
            Profilo profilo = anagraficaService.getProfilo();
            gdeService.saveEventOk(Costanti.OP_GET_PROFILO, startTime, OffsetDateTime.now(),
                    request, HttpStatus.OK.value(), null, null, profilo, principal);
            return ResponseEntity.ok(profilo);
        } catch (Exception e) {
            gdeService.saveEventKo(Costanti.OP_GET_PROFILO, startTime, OffsetDateTime.now(),
                    request, HttpStatus.INTERNAL_SERVER_ERROR.value(), e, null, null, principal);
            throw e;
        }
    }

    @Override
    public ResponseEntity<Profilo> login() {
        String principal = getPrincipalName();

        try {
            Profilo profilo = anagraficaService.login();
            log.info("Login effettuato con successo per l'utente [{}]", principal);
            return ResponseEntity.ok(profilo);
        } catch (Exception e) {
            log.error("Errore durante il login dell'utente [{}]: {}", principal, e.getMessage());
            throw e;
        }
    }

    @Override
    public ResponseEntity<Void> loginWithRedirect(String urlID) {
        String principal = getPrincipalName();

        try {
            Optional<String> redirectUrl = anagraficaService.getLoginRedirectUrl(urlID);

            if (redirectUrl.isEmpty()) {
                throw new NotFoundException("URL-ID non registrato: " + urlID);
            }

            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(redirectUrl.get());
            request.getParameterMap().forEach((key, values) -> {
                for (String value : values) {
                    builder.queryParam(key, value);
                }
            });
            URI location = builder.build().toUri();

            log.info("Login con redirect effettuato per l'utente [{}], redirect verso [{}]", principal, location);
            return ResponseEntity.status(HttpStatus.SEE_OTHER)
                    .location(location)
                    .build();
        } catch (Exception e) {
            log.error("Errore durante il login con redirect dell'utente [{}]: {}", principal, e.getMessage());
            throw e;
        }
    }

    @Override
    public ResponseEntity<Void> logout() {
        OffsetDateTime startTime = OffsetDateTime.now();
        String principal = getPrincipalName();

        try {
            anagraficaService.logout();
            gdeService.saveEventOk(Costanti.OP_LOGOUT, startTime, OffsetDateTime.now(),
                    request, HttpStatus.OK.value(), null, null, null, principal);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            gdeService.saveEventKo(Costanti.OP_LOGOUT, startTime, OffsetDateTime.now(),
                    request, HttpStatus.INTERNAL_SERVER_ERROR.value(), e, null, null, principal);
            throw e;
        }
    }

    @Override
    public ResponseEntity<Void> logoutWithRedirect(String urlID) {
        OffsetDateTime startTime = OffsetDateTime.now();
        String principal = getPrincipalName();

        try {
            Optional<String> redirectUrl = anagraficaService.getLogoutRedirectUrl(urlID);

            if (redirectUrl.isEmpty()) {
                throw new NotFoundException("URL-ID non registrato: " + urlID);
            }

            anagraficaService.logout();

            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(redirectUrl.get());
            request.getParameterMap().forEach((key, values) -> {
                for (String value : values) {
                    builder.queryParam(key, value);
                }
            });
            URI location = builder.build().toUri();

            gdeService.saveEventOk(Costanti.OP_LOGOUT, startTime, OffsetDateTime.now(),
                    request, HttpStatus.SEE_OTHER.value(), null, null, null, principal);
            return ResponseEntity.status(HttpStatus.SEE_OTHER)
                    .location(location)
                    .build();
        } catch (Exception e) {
            gdeService.saveEventKo(Costanti.OP_LOGOUT, startTime, OffsetDateTime.now(),
                    request, HttpStatus.INTERNAL_SERVER_ERROR.value(), e, null, null, principal);
            throw e;
        }
    }

    @Override
    public ResponseEntity<ListaDomini> findDomini() {
        OffsetDateTime startTime = OffsetDateTime.now();

        try {
            ListaDomini result = anagraficaService.getDomini();
            gdeService.saveEventOk(Costanti.OP_GET_DOMINI, startTime, OffsetDateTime.now(),
                    request, HttpStatus.OK.value(), null, null, result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            gdeService.saveEventKo(Costanti.OP_GET_DOMINI, startTime, OffsetDateTime.now(),
                    request, HttpStatus.INTERNAL_SERVER_ERROR.value(), e, null, null);
            throw e;
        }
    }

    @Override
    public ResponseEntity<Dominio> getDominio(String idDominio) {
        OffsetDateTime startTime = OffsetDateTime.now();

        try {
            ResponseEntity<Dominio> response = anagraficaService.getDominio(idDominio)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

            int statusCode = response.getStatusCode().value();
            if (response.getStatusCode().is2xxSuccessful()) {
                gdeService.saveEventOk(Costanti.OP_GET_DOMINIO, startTime, OffsetDateTime.now(),
                        request, statusCode, idDominio, null, response.getBody());
            } else {
                gdeService.saveEventKo(Costanti.OP_GET_DOMINIO, startTime, OffsetDateTime.now(),
                        request, statusCode, null, idDominio, null);
            }
            return response;
        } catch (Exception e) {
            gdeService.saveEventKo(Costanti.OP_GET_DOMINIO, startTime, OffsetDateTime.now(),
                    request, HttpStatus.INTERNAL_SERVER_ERROR.value(), e, idDominio, null);
            throw e;
        }
    }

    @Override
    public ResponseEntity<Resource> getLogo(String idDominio) {
        OffsetDateTime startTime = OffsetDateTime.now();

        try {
            Optional<byte[]> logo = anagraficaService.getLogo(idDominio);

            if (logo.isEmpty() || logo.get() == null || logo.get().length == 0) {
                gdeService.saveEventKo(Costanti.OP_GET_LOGO, startTime, OffsetDateTime.now(),
                        request, HttpStatus.NOT_FOUND.value(), null, idDominio, null);
                return ResponseEntity.notFound().build();
            }

            byte[] logoBytes = logo.get();

            MimeUtil.registerMimeDetector(eu.medsea.mimeutil.detector.MagicMimeMimeDetector.class.getName());

            Collection<?> mimeTypes = MimeUtil.getMimeTypes(logoBytes);

            String mimeType = MimeUtil.getFirstMimeType(mimeTypes.toString()).toString();

            ByteArrayResource resource = new ByteArrayResource(logoBytes);

            gdeService.saveEventOk(Costanti.OP_GET_LOGO, startTime, OffsetDateTime.now(),
                    request, HttpStatus.OK.value(), idDominio, null, null);

            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(Duration.ofSeconds(CACHE_LOGO)).cachePublic())
                    .contentType(MediaType.parseMediaType(mimeType))
                    .contentLength(logoBytes.length)
                    .body((Resource) resource);
        } catch (Exception e) {
            gdeService.saveEventKo(Costanti.OP_GET_LOGO, startTime, OffsetDateTime.now(),
                    request, HttpStatus.INTERNAL_SERVER_ERROR.value(), e, idDominio, null);
            throw e;
        }
    }

    @Override
    public ResponseEntity<ListaTipiPendenza> findTipiPendenza(String idDominio, String gruppo, String descriione) {
        OffsetDateTime startTime = OffsetDateTime.now();

        try {
            ListaTipiPendenza result = anagraficaService.getTipiPendenza(idDominio);
            gdeService.saveEventOk(Costanti.OP_GET_TIPI_PENDENZA, startTime, OffsetDateTime.now(),
                    request, HttpStatus.OK.value(), idDominio, null, result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            gdeService.saveEventKo(Costanti.OP_GET_TIPI_PENDENZA, startTime, OffsetDateTime.now(),
                    request, HttpStatus.INTERNAL_SERVER_ERROR.value(), e, idDominio, null);
            throw e;
        }
    }

    @Override
    public ResponseEntity<TipoPendenza> getTipoPendenza(String idDominio, String idTipoPendenza) {
        OffsetDateTime startTime = OffsetDateTime.now();

        try {
            ResponseEntity<TipoPendenza> response = anagraficaService.getTipoPendenza(idDominio, idTipoPendenza)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

            int statusCode = response.getStatusCode().value();
            if (response.getStatusCode().is2xxSuccessful()) {
                gdeService.saveEventOk(Costanti.OP_GET_TIPO_PENDENZA, startTime, OffsetDateTime.now(),
                        request, statusCode, idDominio, null, response.getBody());
            } else {
                gdeService.saveEventKo(Costanti.OP_GET_TIPO_PENDENZA, startTime, OffsetDateTime.now(),
                        request, statusCode, null, idDominio, null);
            }
            return response;
        } catch (Exception e) {
            gdeService.saveEventKo(Costanti.OP_GET_TIPO_PENDENZA, startTime, OffsetDateTime.now(),
                    request, HttpStatus.INTERNAL_SERVER_ERROR.value(), e, idDominio, null);
            throw e;
        }
    }

    private String getPrincipalName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SpidUserDetails spidUser) {
            return spidUser.getFiscalNumber();
        }
        return null;
    }

}
