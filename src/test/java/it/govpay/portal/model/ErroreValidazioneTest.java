package it.govpay.portal.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import it.govpay.portal.model.ErroreValidazione.DettaglioErrore;

/**
 * Test per ErroreValidazione e DettaglioErrore.
 */
class ErroreValidazioneTest {

    @Nested
    @DisplayName("Test ErroreValidazione")
    class ErroreValidazioneMainTest {

        @Test
        @DisplayName("Test getters e setters")
        void testGettersAndSetters() {
            ErroreValidazione errore = new ErroreValidazione();

            errore.setCategoria("RICHIESTA");
            assertEquals("RICHIESTA", errore.getCategoria());

            errore.setCodice("400");
            assertEquals("400", errore.getCodice());

            errore.setDescrizione("Errore di validazione");
            assertEquals("Errore di validazione", errore.getDescrizione());

            List<DettaglioErrore> dettagli = new ArrayList<>();
            dettagli.add(new DettaglioErrore("campo1", "messaggio1"));
            errore.setDettagli(dettagli);
            assertEquals(1, errore.getDettagli().size());
        }

        @Test
        @DisplayName("Test addDettaglio")
        void testAddDettaglio() {
            ErroreValidazione errore = new ErroreValidazione();

            errore.addDettaglio("idDominio", "Il campo idDominio non deve essere vuoto");
            errore.addDettaglio("importo", "L'importo deve essere positivo");

            assertEquals(2, errore.getDettagli().size());
            assertEquals("idDominio", errore.getDettagli().get(0).getCampo());
            assertEquals("Il campo idDominio non deve essere vuoto", errore.getDettagli().get(0).getMessaggio());
            assertEquals("importo", errore.getDettagli().get(1).getCampo());
        }

        @Test
        @DisplayName("Test dettagli inizializzati")
        void testDettagliInitialized() {
            ErroreValidazione errore = new ErroreValidazione();
            assertNotNull(errore.getDettagli());
            assertTrue(errore.getDettagli().isEmpty());
        }

        @Test
        @DisplayName("Test completo")
        void testCompleto() {
            ErroreValidazione errore = new ErroreValidazione();
            errore.setCategoria("RICHIESTA");
            errore.setCodice("SINTASSI");
            errore.setDescrizione("Richiesta non valida");
            errore.addDettaglio("idA2A", "Campo obbligatorio");
            errore.addDettaglio("idPendenza", "Campo obbligatorio");

            assertEquals("RICHIESTA", errore.getCategoria());
            assertEquals("SINTASSI", errore.getCodice());
            assertEquals("Richiesta non valida", errore.getDescrizione());
            assertEquals(2, errore.getDettagli().size());
        }
    }

    @Nested
    @DisplayName("Test DettaglioErrore")
    class DettaglioErroreTest {

        @Test
        @DisplayName("Test costruttore default")
        void testDefaultConstructor() {
            DettaglioErrore dettaglio = new DettaglioErrore();
            assertNull(dettaglio.getCampo());
            assertNull(dettaglio.getMessaggio());
        }

        @Test
        @DisplayName("Test costruttore con parametri")
        void testParameterizedConstructor() {
            DettaglioErrore dettaglio = new DettaglioErrore("campo", "messaggio");
            assertEquals("campo", dettaglio.getCampo());
            assertEquals("messaggio", dettaglio.getMessaggio());
        }

        @Test
        @DisplayName("Test getters e setters")
        void testGettersAndSetters() {
            DettaglioErrore dettaglio = new DettaglioErrore();

            dettaglio.setCampo("idDominio");
            assertEquals("idDominio", dettaglio.getCampo());

            dettaglio.setMessaggio("Il campo non deve essere vuoto");
            assertEquals("Il campo non deve essere vuoto", dettaglio.getMessaggio());
        }

        @Test
        @DisplayName("Test valori null")
        void testNullValues() {
            DettaglioErrore dettaglio = new DettaglioErrore(null, null);
            assertNull(dettaglio.getCampo());
            assertNull(dettaglio.getMessaggio());

            dettaglio.setCampo(null);
            dettaglio.setMessaggio(null);
            assertNull(dettaglio.getCampo());
            assertNull(dettaglio.getMessaggio());
        }
    }
}
