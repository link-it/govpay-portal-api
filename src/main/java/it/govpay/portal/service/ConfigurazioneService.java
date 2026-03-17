package it.govpay.portal.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import it.govpay.common.configurazione.model.Hardening;

@Service
public class ConfigurazioneService {

    private static final Logger log = LoggerFactory.getLogger(ConfigurazioneService.class);

    private final it.govpay.common.configurazione.service.ConfigurazioneService commonConfigurazioneService;

    public ConfigurazioneService(
            it.govpay.common.configurazione.service.ConfigurazioneService commonConfigurazioneService) {
        this.commonConfigurazioneService = commonConfigurazioneService;
    }

    public Hardening getHardening() {
        try {
            return commonConfigurazioneService.getHardening()
                    .orElseGet(() -> {
                        log.debug("Configurazione hardening non trovata, restituisco configurazione disabilitata");
                        Hardening disabled = new Hardening();
                        disabled.setAbilitato(false);
                        return disabled;
                    });
        } catch (Exception e) {
            log.error("Errore durante la lettura della configurazione hardening: {}", e.getMessage(), e);
            Hardening disabled = new Hardening();
            disabled.setAbilitato(false);
            return disabled;
        }
    }
}
