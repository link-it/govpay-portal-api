package it.govpay.portal.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import it.govpay.portal.gde.service.GdeService;
import it.govpay.portal.model.Dominio;
import it.govpay.portal.model.ListaDomini;
import it.govpay.portal.model.ListaTipiPendenza;
import it.govpay.portal.model.Profilo;
import it.govpay.portal.model.Soggetto;
import it.govpay.portal.model.TipoPendenza;
import it.govpay.portal.model.TipoSoggetto;
import it.govpay.portal.service.AnagraficaService;
import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class AnagraficaControllerTest {

    @Mock
    private AnagraficaService anagraficaService;

    @Mock
    private GdeService gdeService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AnagraficaController anagraficaController;

    @Nested
    @DisplayName("getProfilo Tests")
    class GetProfiloTests {

        @Test
        @DisplayName("Dovrebbe restituire 200 con profilo utente")
        void shouldReturn200WithProfilo() {
            Profilo profilo = new Profilo();
            profilo.setNome("RSSMRA80A01H501U");
            Soggetto anagrafica = new Soggetto();
            anagrafica.setTipo(TipoSoggetto.F);
            anagrafica.setIdentificativo("RSSMRA80A01H501U");
            anagrafica.setAnagrafica("Mario Rossi");
            profilo.setAnagrafica(anagrafica);

            when(anagraficaService.getProfilo()).thenReturn(profilo);

            ResponseEntity<Profilo> response = anagraficaController.getProfilo();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("RSSMRA80A01H501U", response.getBody().getNome());
            assertEquals("Mario Rossi", response.getBody().getAnagrafica().getAnagrafica());
        }
    }

    @Nested
    @DisplayName("logout Tests")
    class LogoutTests {

        @Test
        @DisplayName("Dovrebbe restituire 200 per logout")
        void shouldReturn200ForLogout() {
            doNothing().when(anagraficaService).logout();

            ResponseEntity<Void> response = anagraficaController.logout();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(anagraficaService).logout();
        }
    }

    @Nested
    @DisplayName("getDomini Tests")
    class GetDominiTests {

        @Test
        @DisplayName("Dovrebbe restituire 200 con lista domini")
        void shouldReturn200WithDomini() {
            ListaDomini listaDomini = new ListaDomini();
            List<Dominio> domini = new ArrayList<>();
            Dominio dominio = new Dominio();
            dominio.setIdDominio("12345678901");
            dominio.setRagioneSociale("Comune di Test");
            domini.add(dominio);
            listaDomini.setRisultati(domini);

            when(anagraficaService.getDomini()).thenReturn(listaDomini);

            ResponseEntity<ListaDomini> response = anagraficaController.getDomini();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getRisultati().size());
            assertEquals("12345678901", response.getBody().getRisultati().get(0).getIdDominio());
        }

        @Test
        @DisplayName("Dovrebbe restituire 200 con lista vuota")
        void shouldReturn200WithEmptyList() {
            ListaDomini listaDomini = new ListaDomini();
            listaDomini.setRisultati(new ArrayList<>());

            when(anagraficaService.getDomini()).thenReturn(listaDomini);

            ResponseEntity<ListaDomini> response = anagraficaController.getDomini();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().getRisultati().isEmpty());
        }
    }

    @Nested
    @DisplayName("getDominio Tests")
    class GetDominioTests {

        @Test
        @DisplayName("Dovrebbe restituire 200 quando dominio trovato")
        void shouldReturn200WhenDominioFound() {
            Dominio dominio = new Dominio();
            dominio.setIdDominio("12345678901");
            dominio.setRagioneSociale("Comune di Test");

            when(anagraficaService.getDominio("12345678901")).thenReturn(Optional.of(dominio));

            ResponseEntity<Dominio> response = anagraficaController.getDominio("12345678901");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("12345678901", response.getBody().getIdDominio());
        }

        @Test
        @DisplayName("Dovrebbe restituire 404 quando dominio non trovato")
        void shouldReturn404WhenDominioNotFound() {
            when(anagraficaService.getDominio("99999999999")).thenReturn(Optional.empty());

            ResponseEntity<Dominio> response = anagraficaController.getDominio("99999999999");

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNull(response.getBody());
        }
    }

    @Nested
    @DisplayName("getLogo Tests")
    class GetLogoTests {

        @Test
        @DisplayName("Dovrebbe restituire 404 quando logo non trovato")
        void shouldReturn404WhenLogoNotFound() {
            when(anagraficaService.getLogo("12345678901")).thenReturn(Optional.empty());

            ResponseEntity<Resource> response = anagraficaController.getLogo("12345678901");

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @DisplayName("Dovrebbe restituire 404 quando logo vuoto")
        void shouldReturn404WhenLogoEmpty() {
            when(anagraficaService.getLogo("12345678901")).thenReturn(Optional.of(new byte[0]));

            ResponseEntity<Resource> response = anagraficaController.getLogo("12345678901");

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @DisplayName("Dovrebbe restituire 404 quando logo null")
        void shouldReturn404WhenLogoNull() {
            when(anagraficaService.getLogo("12345678901")).thenReturn(Optional.ofNullable(null));

            ResponseEntity<Resource> response = anagraficaController.getLogo("12345678901");

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("getTipiPendenza Tests")
    class GetTipiPendenzaTests {

        @Test
        @DisplayName("Dovrebbe restituire 200 con lista tipi pendenza")
        void shouldReturn200WithTipiPendenza() {
            ListaTipiPendenza listaTipi = new ListaTipiPendenza();
            List<TipoPendenza> tipi = new ArrayList<>();
            TipoPendenza tipo = new TipoPendenza("TARI", "Tassa Rifiuti");
            tipi.add(tipo);
            listaTipi.setRisultati(tipi);

            when(anagraficaService.getTipiPendenza("12345678901")).thenReturn(listaTipi);

            ResponseEntity<ListaTipiPendenza> response = anagraficaController.getTipiPendenza(
                    "12345678901", null, null);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getRisultati().size());
            assertEquals("TARI", response.getBody().getRisultati().get(0).getIdTipoPendenza());
        }
    }

    @Nested
    @DisplayName("getTipoPendenza Tests")
    class GetTipoPendenzaTests {

        @Test
        @DisplayName("Dovrebbe restituire 200 quando tipo pendenza trovato")
        void shouldReturn200WhenTipoPendenzaFound() {
            TipoPendenza tipo = new TipoPendenza("TARI", "Tassa Rifiuti");

            when(anagraficaService.getTipoPendenza("12345678901", "TARI")).thenReturn(Optional.of(tipo));

            ResponseEntity<TipoPendenza> response = anagraficaController.getTipoPendenza("12345678901", "TARI");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("TARI", response.getBody().getIdTipoPendenza());
        }

        @Test
        @DisplayName("Dovrebbe restituire 404 quando tipo pendenza non trovato")
        void shouldReturn404WhenTipoPendenzaNotFound() {
            when(anagraficaService.getTipoPendenza("12345678901", "UNKNOWN")).thenReturn(Optional.empty());

            ResponseEntity<TipoPendenza> response = anagraficaController.getTipoPendenza("12345678901", "UNKNOWN");

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNull(response.getBody());
        }
    }
}
