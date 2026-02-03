package it.govpay.portal.config;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Configurazione globale dell'ObjectMapper per la serializzazione/deserializzazione JSON.
 * <p>
 * Questa configurazione garantisce che le date vengano serializzate come stringhe ISO-8601
 * invece che come array (formato di default di Jackson).
 * <p>
 * Formato date: yyyy-MM-dd (solo data, senza orario)
 */
@Configuration
public class JacksonConfig {

    /** Pattern per la serializzazione delle date (solo data). */
    public static final String PATTERN_DATE_YYYY_MM_DD = "yyyy-MM-dd";

    @Value("${spring.jackson.time-zone:Europe/Rome}")
    private String timezone;

    /**
     * Crea un ObjectMapper configurato per la corretta gestione delle date.
     * <p>
     * Configurazione:
     * - Date serializzate come stringhe ISO-8601 (non come timestamps/array)
     * - Formato date: yyyy-MM-dd
     * - Timezone configurabile da properties
     * - Enum serializzati usando toString()
     *
     * @return ObjectMapper configurato
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // Configura il timezone
        objectMapper.setTimeZone(TimeZone.getTimeZone(timezone));

        // Configura il formato delle date per java.util.Date
        objectMapper.setDateFormat(new SimpleDateFormat(PATTERN_DATE_YYYY_MM_DD));

        // IMPORTANTE: disabilita la scrittura delle date come timestamps (array)
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Configura la serializzazione degli enum usando toString()
        objectMapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);

        // Registra il modulo per Java 8 Date/Time API (LocalDate, LocalDateTime, etc.)
        objectMapper.registerModule(new JavaTimeModule());

        return objectMapper;
    }
}
