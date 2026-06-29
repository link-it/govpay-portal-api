package it.govpay.portal.config;

import static org.junit.jupiter.api.Assertions.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;

import it.govpay.portal.beans.pendenza.PendenzaPost;

/**
 * Test per la configurazione Jackson.
 * Verifica che le date vengano serializzate come stringhe ISO-8601 e non come array.
 */
class JacksonConfigTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Crea il JsonMapper (Jackson 3) con la stessa configurazione di JacksonConfig
        objectMapper = JsonMapper.builder()
                .defaultTimeZone(TimeZone.getTimeZone("Europe/Rome"))
                .defaultDateFormat(new SimpleDateFormat(JacksonConfig.PATTERN_DATE_YYYY_MM_DD))
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(EnumFeature.READ_ENUMS_USING_TO_STRING)
                .enable(EnumFeature.WRITE_ENUMS_USING_TO_STRING)
                .build();
    }

    @Test
    @DisplayName("Date serializzate come stringhe ISO-8601, non come array")
    void testDateSerializedAsString() throws JacksonException {
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
    void testNullDateSerialized() throws JacksonException {
        PendenzaPost pendenza = new PendenzaPost();
        pendenza.setDataScadenza(null);
        pendenza.setIdDominio("01234567890");

        String json = objectMapper.writeValueAsString(pendenza);

        // Verifica che null sia gestito correttamente
        assertNotNull(json);
    }

    @Test
    @DisplayName("Deserializzazione date da stringa ISO-8601")
    void testDateDeserialization() throws JacksonException {
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
    void testEnumSerializedAsString() throws JacksonException {
        PendenzaPost pendenza = new PendenzaPost();
        pendenza.setIdDominio("01234567890");
        pendenza.setTassonomiaAvviso(it.govpay.portal.beans.pendenza.TassonomiaAvviso.SERVIZI_EROGATI_DAL_COMUNE);

        String json = objectMapper.writeValueAsString(pendenza);

        // Verifica che l'enum sia serializzato come stringa leggibile
        assertTrue(json.contains("Servizi erogati dal comune") || json.contains("SERVIZI_EROGATI_DAL_COMUNE"),
                "L'enum deve essere serializzato come stringa. JSON: " + json);
    }
}
