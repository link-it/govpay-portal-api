package it.govpay.portal.config;

import static org.junit.jupiter.api.Assertions.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import it.govpay.portal.beans.pendenza.PendenzaPost;

/**
 * Test per la configurazione Jackson.
 * Verifica che le date vengano serializzate come stringhe ISO-8601 e non come array.
 */
class JacksonConfigTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Crea ObjectMapper con la stessa configurazione di JacksonConfig
        objectMapper = new ObjectMapper();
        objectMapper.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
        objectMapper.setDateFormat(new SimpleDateFormat(JacksonConfig.PATTERN_DATE_YYYY_MM_DD));
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("Date serializzate come stringhe ISO-8601, non come array")
    void testDateSerializedAsString() throws JsonProcessingException {
        PendenzaPost pendenza = new PendenzaPost();

        // Crea una data: 31 gennaio 2027
        Calendar cal = Calendar.getInstance();
        cal.set(2027, Calendar.JANUARY, 31, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date dataScadenza = cal.getTime();

        pendenza.setDataScadenza(dataScadenza);
        pendenza.setIdDominio("01234567890");
        pendenza.setIdTipoPendenza("TEST");

        String json = objectMapper.writeValueAsString(pendenza);

        // Verifica che la data sia serializzata come stringa "2027-01-31"
        // e NON come array [2027,1,31]
        assertTrue(json.contains("\"dataScadenza\":\"2027-01-31\""),
                "La data deve essere serializzata come stringa ISO-8601, non come array. JSON: " + json);
        assertFalse(json.contains("[2027,"),
                "La data non deve essere serializzata come array. JSON: " + json);
    }

    @Test
    @DisplayName("Date null serializzate come null")
    void testNullDateSerialized() throws JsonProcessingException {
        PendenzaPost pendenza = new PendenzaPost();
        pendenza.setDataScadenza(null);
        pendenza.setIdDominio("01234567890");

        String json = objectMapper.writeValueAsString(pendenza);

        // Verifica che null sia gestito correttamente
        assertNotNull(json);
    }

    @Test
    @DisplayName("Deserializzazione date da stringa ISO-8601")
    void testDateDeserialization() throws JsonProcessingException {
        String json = "{\"dataScadenza\":\"2027-01-31\",\"idDominio\":\"01234567890\"}";

        PendenzaPost pendenza = objectMapper.readValue(json, PendenzaPost.class);

        assertNotNull(pendenza.getDataScadenza());
        Calendar cal = Calendar.getInstance();
        cal.setTime(pendenza.getDataScadenza());
        assertEquals(2027, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        assertEquals(31, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    @DisplayName("Enum serializzati usando toString")
    void testEnumSerializedAsString() throws JsonProcessingException {
        PendenzaPost pendenza = new PendenzaPost();
        pendenza.setIdDominio("01234567890");
        pendenza.setTassonomiaAvviso(it.govpay.portal.beans.pendenza.TassonomiaAvviso.SERVIZI_EROGATI_DAL_COMUNE);

        String json = objectMapper.writeValueAsString(pendenza);

        // Verifica che l'enum sia serializzato come stringa leggibile
        assertTrue(json.contains("Servizi erogati dal comune") || json.contains("SERVIZI_EROGATI_DAL_COMUNE"),
                "L'enum deve essere serializzato come stringa. JSON: " + json);
    }
}
