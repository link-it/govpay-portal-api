package it.govpay.portal.controller;

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
import org.springframework.web.bind.annotation.RestController;

import eu.medsea.mimeutil.MimeUtil;
import it.govpay.portal.api.AnagraficaApi;
import it.govpay.portal.gde.Costanti;
import it.govpay.portal.gde.service.GdeService;
import it.govpay.portal.model.Dominio;
import it.govpay.portal.model.ListaDomini;
import it.govpay.portal.model.ListaTipiPendenza;
import it.govpay.portal.model.Profilo;
import it.govpay.portal.model.TipoPendenza;
import it.govpay.portal.service.AnagraficaService;
import jakarta.servlet.http.HttpServletRequest;

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

        try {
            Profilo profilo = anagraficaService.getProfilo();
            gdeService.saveEventOk(Costanti.OP_GET_PROFILO, startTime, OffsetDateTime.now(),
                    request, HttpStatus.OK.value(), null, null, profilo);
            return ResponseEntity.ok(profilo);
        } catch (Exception e) {
            gdeService.saveEventKo(Costanti.OP_GET_PROFILO, startTime, OffsetDateTime.now(),
                    request, HttpStatus.INTERNAL_SERVER_ERROR.value(), e, null, null);
            throw e;
        }
    }

    @Override
    public ResponseEntity<Void> logout() {
        OffsetDateTime startTime = OffsetDateTime.now();

        try {
            anagraficaService.logout();
            gdeService.saveEventOk(Costanti.OP_LOGOUT, startTime, OffsetDateTime.now(),
                    request, HttpStatus.OK.value(), null, null, null);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            gdeService.saveEventKo(Costanti.OP_LOGOUT, startTime, OffsetDateTime.now(),
                    request, HttpStatus.INTERNAL_SERVER_ERROR.value(), e, null, null);
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

}
