package it.govpay.portal.gde.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import tools.jackson.databind.ObjectMapper;

import it.govpay.common.client.config.GovPayClientAutoConfiguration;
import it.govpay.common.client.service.ConnettoreService;
import it.govpay.common.configurazione.config.ConfigurazioneAutoConfiguration;
import it.govpay.common.repository.ConfigurazioneRepository;

@Configuration
@ComponentScan(
    basePackages = {
        "it.govpay.common.client",
        "it.govpay.common.configurazione"
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {
            GovPayClientAutoConfiguration.class,
            ConfigurazioneAutoConfiguration.class,
            it.govpay.common.configurazione.service.ConfigurazioneService.class
        }
    )
)
public class GovPayCommonConfig {

    @Bean("commonConfigurazioneService")
    it.govpay.common.configurazione.service.ConfigurazioneService commonConfigurazioneService(
            ConfigurazioneRepository configurazioneRepository,
            ObjectMapper objectMapper,
            ConnettoreService connettoreService) {
        return new it.govpay.common.configurazione.service.ConfigurazioneService(
                configurazioneRepository, objectMapper, connettoreService);
    }
}
