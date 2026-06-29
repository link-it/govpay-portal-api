package it.govpay.portal.config;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.cfg.EnumFeature;

/**
 * Configurazione globale del JsonMapper (Jackson 3) per la serializzazione/deserializzazione JSON.
 * <p>
 * Personalizza il JsonMapper primario di Spring Boot tramite {@link JsonMapperBuilderCustomizer}:
 * in questo modo la configurazione si applica al mapper effettivamente usato dai message converter
 * HTTP, dall'eventuale iniezione di {@code ObjectMapper} nei bean e dalle risposte JSON.
 * <p>
 * Le date vengono serializzate come stringhe ISO-8601 (mai come array/timestamp). Il pattern
 * {@code yyyy-MM-dd} si applica ai tipi {@code java.util.Date}; i tipi {@code java.time} usano il
 * supporto nativo di Jackson 3 (ISO-8601).
 * <p>
 * L'inclusione/esclusione dei null è gestita dalla property {@code spring.jackson.default-property-inclusion}
 * (impostata a {@code non_null} in application.properties), applicata da Spring Boot al builder.
 */
@Configuration
public class JacksonConfig {

    /** Pattern per la serializzazione delle date (solo data). */
    public static final String PATTERN_DATE_YYYY_MM_DD = "yyyy-MM-dd";

    @Value("${portal.time-zone:Europe/Rome}")
    private String timezone;

    /**
     * Personalizza il JsonMapper primario di Spring Boot (Jackson 3).
     * <p>
     * Configurazione:
     * - Date serializzate come stringhe ISO-8601 (non come timestamps/array)
     * - Formato date per java.util.Date: yyyy-MM-dd
     * - Timezone configurabile da properties (portal.time-zone)
     * - Enum serializzati/deserializzati usando toString()
     *
     * @return il customizer del JsonMapper
     */
    @Bean
    public JsonMapperBuilderCustomizer portalJsonMapperBuilderCustomizer() {
        return builder -> builder
                .defaultDateFormat(new SimpleDateFormat(PATTERN_DATE_YYYY_MM_DD))
                .defaultTimeZone(TimeZone.getTimeZone(this.timezone))
                .enable(EnumFeature.READ_ENUMS_USING_TO_STRING)
                .enable(EnumFeature.WRITE_ENUMS_USING_TO_STRING)
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
