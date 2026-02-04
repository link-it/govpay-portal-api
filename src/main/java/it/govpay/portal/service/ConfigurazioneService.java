package it.govpay.portal.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.govpay.portal.entity.Configurazione;
import it.govpay.portal.repository.ConfigurazioneRepository;
import it.govpay.portal.security.hardening.model.Hardening;

@Service
public class ConfigurazioneService {

    private static final Logger log = LoggerFactory.getLogger(ConfigurazioneService.class);

    private final ConfigurazioneRepository configurazioneRepository;
    private final ObjectMapper objectMapper;

    public ConfigurazioneService(ConfigurazioneRepository configurazioneRepository, ObjectMapper objectMapper) {
        this.configurazioneRepository = configurazioneRepository;
        this.objectMapper = objectMapper;
    }

    public Hardening getHardening() {
        Optional<Configurazione> configOpt = configurazioneRepository.findByNome(Hardening.CONFIGURAZIONE_HARDENING);

        if (configOpt.isEmpty() || configOpt.get().getValore() == null) {
            log.debug("Configurazione hardening non trovata, restituisco configurazione disabilitata");
            return Hardening.builder().abilitato(false).build();
        }

        try {
            Hardening hardening = objectMapper.readValue(configOpt.get().getValore(), Hardening.class);
            log.debug("Configurazione hardening letta: abilitato={}", hardening.isAbilitato());
            return hardening;
        } catch (Exception e) {
            log.error("Errore durante il parsing della configurazione hardening: {}", e.getMessage(), e);
            return Hardening.builder().abilitato(false).build();
        }
    }
}
