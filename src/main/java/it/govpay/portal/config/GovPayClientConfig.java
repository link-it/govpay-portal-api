package it.govpay.portal.config;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;

import it.govpay.pendenze.client.ApiClient;
import it.govpay.pendenze.client.api.PendenzeApi;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "govpay.pendenze")
@Data
public class GovPayClientConfig {

    private String baseUrl;
    private String username;
    private String password;

    @Value("${portal.time-zone:Europe/Rome}")
    private String timezone;

    @Bean
    public PendenzeApi pendenzeApi() {
        // Crea un RestTemplate con ObjectMapper configurato per le date
        RestTemplate restTemplate = createConfiguredRestTemplate();

        ApiClient apiClient = new ApiClient(restTemplate);
        apiClient.setBasePath(baseUrl);
        apiClient.setUsername(username);
        apiClient.setPassword(password);
        return new PendenzeApi(apiClient);
    }

    /**
     * Crea un RestTemplate con ObjectMapper configurato per serializzare le date
     * come stringhe ISO-8601 invece che come array.
     */
    @SuppressWarnings("removal")
    private RestTemplate createConfiguredRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // Configura il JsonMapper (Jackson 3, immutabile: configurazione sul builder).
        // Il supporto java.time e' nativo in Jackson 3, quindi non serve alcun modulo aggiuntivo.
        JsonMapper jsonMapper = JsonMapper.builder()
                .defaultDateFormat(new SimpleDateFormat(JacksonConfig.PATTERN_DATE_YYYY_MM_DD))
                .defaultTimeZone(TimeZone.getTimeZone(timezone))
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(EnumFeature.READ_ENUMS_USING_TO_STRING)
                .enable(EnumFeature.WRITE_ENUMS_USING_TO_STRING)
                .build();

        // Sostituisce i converter Jackson con uno configurato (Jackson 3).
        // Rimuove sia l'eventuale converter Jackson 2 sia quello Jackson 3 di default.
        restTemplate.getMessageConverters().removeIf(
                c -> c instanceof org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
                        || c instanceof JacksonJsonHttpMessageConverter);
        restTemplate.getMessageConverters().add(0, new JacksonJsonHttpMessageConverter(jsonMapper));

        return restTemplate;
    }
}
