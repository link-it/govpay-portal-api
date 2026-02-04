package it.govpay.portal.config;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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
    private RestTemplate createConfiguredRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // Configura l'ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
        objectMapper.setDateFormat(new SimpleDateFormat(JacksonConfig.PATTERN_DATE_YYYY_MM_DD));
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        objectMapper.registerModule(new JavaTimeModule());

        // Sostituisce il converter Jackson con uno configurato
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
        restTemplate.getMessageConverters().removeIf(MappingJackson2HttpMessageConverter.class::isInstance);
        restTemplate.getMessageConverters().add(0, converter);

        return restTemplate;
    }
}
