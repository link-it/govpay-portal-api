package it.govpay.portal.controller;

import java.time.Duration;
import java.util.Collection;
import java.util.Optional;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import eu.medsea.mimeutil.MimeUtil;
import it.govpay.portal.api.AnagraficaApi;
import it.govpay.portal.model.Dominio;
import it.govpay.portal.model.ListaDomini;
import it.govpay.portal.model.ListaTipiPendenza;
import it.govpay.portal.model.Profilo;
import it.govpay.portal.model.TipoPendenza;
import it.govpay.portal.service.AnagraficaService;

@RestController
public class AnagraficaController implements AnagraficaApi {
	
	private static final Integer CACHE_LOGO = 2 * 60 * 60;

    private final AnagraficaService anagraficaService;

    public AnagraficaController(AnagraficaService anagraficaService) {
        this.anagraficaService = anagraficaService;
    }

    @Override
    public ResponseEntity<Profilo> getProfilo() {
        Profilo profilo = anagraficaService.getProfilo();
        return ResponseEntity.ok(profilo);
    }

    @Override
    public ResponseEntity<Void> logout() {
        anagraficaService.logout();
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<ListaDomini> getDomini() {
        ListaDomini result = anagraficaService.getDomini();
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Dominio> getDominio(String idDominio) {
        return anagraficaService.getDominio(idDominio)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<Resource> getLogo(String idDominio) {
    	Optional<byte[]> logo = anagraficaService.getLogo(idDominio);
    	
		if (logo.isEmpty() || logo.get() == null || logo.get().length == 0) {
			return ResponseEntity.notFound().build();
    	}
		
		byte[] logoBytes = logo.get();
		
		MimeUtil.registerMimeDetector(eu.medsea.mimeutil.detector.MagicMimeMimeDetector.class.getName());
		
		Collection<?> mimeTypes = MimeUtil.getMimeTypes(logoBytes);

		String mimeType = MimeUtil.getFirstMimeType(mimeTypes.toString()).toString();
                	
        ByteArrayResource resource = new ByteArrayResource(logoBytes);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofSeconds(CACHE_LOGO)).cachePublic())
                .contentType(MediaType.parseMediaType(mimeType))
                .contentLength(logoBytes.length)
                .body((Resource) resource);
    }

    @Override
    public ResponseEntity<ListaTipiPendenza> getTipiPendenza(String idDominio, String gruppo, String descriione) {
        ListaTipiPendenza result = anagraficaService.getTipiPendenza(idDominio);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<TipoPendenza> getTipoPendenza(String idDominio, String idTipoPendenza) {
        return anagraficaService.getTipoPendenza(idDominio, idTipoPendenza)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
