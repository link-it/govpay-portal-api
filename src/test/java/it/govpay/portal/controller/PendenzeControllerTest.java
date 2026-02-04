package it.govpay.portal.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import it.govpay.portal.model.Avviso;
import it.govpay.portal.model.LinguaSecondaria;
import it.govpay.portal.model.ListaPendenze;
import it.govpay.portal.model.Pendenza;
import it.govpay.portal.model.Ricevuta;
import it.govpay.portal.model.StatoPendenza;
import it.govpay.portal.service.PendenzeService;
import it.govpay.portal.service.StampeService;

@ExtendWith(MockitoExtension.class)
class PendenzeControllerTest {

    @Mock
    private PendenzeService pendenzeService;

    @Mock
    private StampeService stampeService;

    @InjectMocks
    private PendenzeController pendenzeController;

    private static final String ID_DOMINIO = "12345678901";
    private static final String NUMERO_AVVISO = "123456789012345678";

    @Nested
    @DisplayName("getAvviso Tests")
    class GetAvvisoTests {

        @Test
        @DisplayName("Dovrebbe restituire JSON quando Accept e' application/json")
        void shouldReturnJsonWhenAcceptIsJson() {
            Avviso avviso = new Avviso();
            avviso.setNumeroAvviso(NUMERO_AVVISO);
            avviso.setImporto(150.50);

            when(pendenzeService.getAvviso(ID_DOMINIO, NUMERO_AVVISO)).thenReturn(Optional.of(avviso));

            ResponseEntity<?> response = pendenzeController.getAvviso(
                    ID_DOMINIO, NUMERO_AVVISO, null, null, null, null,
                    MediaType.APPLICATION_JSON_VALUE);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
            assertInstanceOf(Avviso.class, response.getBody());
            assertEquals(NUMERO_AVVISO, ((Avviso) response.getBody()).getNumeroAvviso());

            verify(pendenzeService).getAvviso(ID_DOMINIO, NUMERO_AVVISO);
            verify(stampeService, never()).generateAvvisoPdf(any(), any(), any());
        }

        @Test
        @DisplayName("Dovrebbe restituire PDF quando Accept e' application/pdf")
        void shouldReturnPdfWhenAcceptIsPdf() {
            byte[] pdfContent = "PDF Content".getBytes();

            when(stampeService.generateAvvisoPdf(eq(ID_DOMINIO), eq(NUMERO_AVVISO), isNull()))
                    .thenReturn(Optional.of(pdfContent));

            ResponseEntity<?> response = pendenzeController.getAvviso(
                    ID_DOMINIO, NUMERO_AVVISO, null, null, null, null,
                    MediaType.APPLICATION_PDF_VALUE);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
            assertArrayEquals(pdfContent, (byte[]) response.getBody());
            assertTrue(response.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION).get(0)
                    .contains("avviso_" + NUMERO_AVVISO + ".pdf"));

            verify(stampeService).generateAvvisoPdf(ID_DOMINIO, NUMERO_AVVISO, null);
            verify(pendenzeService, never()).getAvviso(any(), any());
        }

        @Test
        @DisplayName("Dovrebbe passare linguaSecondaria per PDF bilingue")
        void shouldPassLinguaSecondariaForBilingualPdf() {
            byte[] pdfContent = "Bilingual PDF".getBytes();

            when(stampeService.generateAvvisoPdf(ID_DOMINIO, NUMERO_AVVISO, LinguaSecondaria.EN))
                    .thenReturn(Optional.of(pdfContent));

            ResponseEntity<?> response = pendenzeController.getAvviso(
                    ID_DOMINIO, NUMERO_AVVISO, null, null, null, LinguaSecondaria.EN,
                    MediaType.APPLICATION_PDF_VALUE);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(stampeService).generateAvvisoPdf(ID_DOMINIO, NUMERO_AVVISO, LinguaSecondaria.EN);
        }

        @Test
        @DisplayName("Dovrebbe restituire 404 quando avviso non trovato (JSON)")
        void shouldReturn404WhenAvvisoNotFoundJson() {
            when(pendenzeService.getAvviso(ID_DOMINIO, NUMERO_AVVISO)).thenReturn(Optional.empty());

            ResponseEntity<?> response = pendenzeController.getAvviso(
                    ID_DOMINIO, NUMERO_AVVISO, null, null, null, null,
                    MediaType.APPLICATION_JSON_VALUE);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @DisplayName("Dovrebbe restituire 404 quando avviso non trovato (PDF)")
        void shouldReturn404WhenAvvisoNotFoundPdf() {
            when(stampeService.generateAvvisoPdf(eq(ID_DOMINIO), eq(NUMERO_AVVISO), isNull()))
                    .thenReturn(Optional.empty());

            ResponseEntity<?> response = pendenzeController.getAvviso(
                    ID_DOMINIO, NUMERO_AVVISO, null, null, null, null,
                    MediaType.APPLICATION_PDF_VALUE);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @DisplayName("Dovrebbe restituire 406 quando Accept header mancante")
        void shouldReturn406WhenAcceptHeaderMissing() {
            ResponseEntity<?> response = pendenzeController.getAvviso(
                    ID_DOMINIO, NUMERO_AVVISO, null, null, null, null, null);

            assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        }

        @Test
        @DisplayName("Dovrebbe restituire 406 quando Accept header non supportato")
        void shouldReturn406WhenAcceptHeaderNotSupported() {
            ResponseEntity<?> response = pendenzeController.getAvviso(
                    ID_DOMINIO, NUMERO_AVVISO, null, null, null, null,
                    MediaType.APPLICATION_XML_VALUE);

            assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("getPendenza Tests")
    class GetPendenzaTests {

        @Test
        @DisplayName("Dovrebbe restituire 200 con pendenza")
        void shouldReturn200WithPendenza() {
            Pendenza pendenza = new Pendenza();
            pendenza.setNumeroAvviso(NUMERO_AVVISO);
            pendenza.setStato(StatoPendenza.NON_ESEGUITA);

            when(pendenzeService.getPendenza(ID_DOMINIO, NUMERO_AVVISO)).thenReturn(Optional.of(pendenza));

            ResponseEntity<Pendenza> response = pendenzeController.getPendenza(ID_DOMINIO, NUMERO_AVVISO);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(NUMERO_AVVISO, response.getBody().getNumeroAvviso());
        }

        @Test
        @DisplayName("Dovrebbe restituire 404 quando pendenza non trovata")
        void shouldReturn404WhenPendenzaNotFound() {
            when(pendenzeService.getPendenza(ID_DOMINIO, NUMERO_AVVISO)).thenReturn(Optional.empty());

            ResponseEntity<Pendenza> response = pendenzeController.getPendenza(ID_DOMINIO, NUMERO_AVVISO);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("getPendenze Tests")
    class GetPendenzeTests {

        @Test
        @DisplayName("Dovrebbe restituire 200 con lista pendenze")
        void shouldReturn200WithPendenze() {
            ListaPendenze lista = new ListaPendenze();
            List<Pendenza> pendenze = new ArrayList<>();
            Pendenza pendenza = new Pendenza();
            pendenza.setNumeroAvviso(NUMERO_AVVISO);
            pendenze.add(pendenza);
            lista.setRisultati(pendenze);

            when(pendenzeService.getPendenze(ID_DOMINIO, null)).thenReturn(lista);

            ResponseEntity<ListaPendenze> response = pendenzeController.getPendenze(ID_DOMINIO, null);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getRisultati().size());
        }

        @Test
        @DisplayName("Dovrebbe restituire 200 con filtro stato")
        void shouldReturn200WithStateFilter() {
            ListaPendenze lista = new ListaPendenze();
            lista.setRisultati(new ArrayList<>());

            when(pendenzeService.getPendenze(ID_DOMINIO, StatoPendenza.ESEGUITA)).thenReturn(lista);

            ResponseEntity<ListaPendenze> response = pendenzeController.getPendenze(ID_DOMINIO, StatoPendenza.ESEGUITA);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(pendenzeService).getPendenze(ID_DOMINIO, StatoPendenza.ESEGUITA);
        }
    }

    @Nested
    @DisplayName("getRicevuta Tests")
    class GetRicevutaTests {

        @Test
        @DisplayName("Dovrebbe restituire JSON quando Accept e' application/json")
        void shouldReturnJsonWhenAcceptIsJson() {
            Ricevuta ricevuta = new Ricevuta();
            ricevuta.setIuv("01234567890123456");
            ricevuta.setStato("ESEGUITO");

            when(pendenzeService.getRicevuta(ID_DOMINIO, NUMERO_AVVISO)).thenReturn(Optional.of(ricevuta));

            ResponseEntity<?> response = pendenzeController.getRicevuta(
                    ID_DOMINIO, NUMERO_AVVISO, MediaType.APPLICATION_JSON_VALUE);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
            assertInstanceOf(Ricevuta.class, response.getBody());
            assertEquals("01234567890123456", ((Ricevuta) response.getBody()).getIuv());

            verify(pendenzeService).getRicevuta(ID_DOMINIO, NUMERO_AVVISO);
            verify(stampeService, never()).generateRicevutaPdf(any(), any());
        }

        @Test
        @DisplayName("Dovrebbe restituire PDF quando Accept e' application/pdf")
        void shouldReturnPdfWhenAcceptIsPdf() {
            byte[] pdfContent = "Receipt PDF".getBytes();

            when(stampeService.generateRicevutaPdf(ID_DOMINIO, NUMERO_AVVISO))
                    .thenReturn(Optional.of(pdfContent));

            ResponseEntity<?> response = pendenzeController.getRicevuta(
                    ID_DOMINIO, NUMERO_AVVISO, MediaType.APPLICATION_PDF_VALUE);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
            assertArrayEquals(pdfContent, (byte[]) response.getBody());
            assertTrue(response.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION).get(0)
                    .contains("ricevuta_" + NUMERO_AVVISO + ".pdf"));

            verify(stampeService).generateRicevutaPdf(ID_DOMINIO, NUMERO_AVVISO);
            verify(pendenzeService, never()).getRicevuta(any(), any());
        }

        @Test
        @DisplayName("Dovrebbe restituire 404 quando ricevuta non trovata (JSON)")
        void shouldReturn404WhenRicevutaNotFoundJson() {
            when(pendenzeService.getRicevuta(ID_DOMINIO, NUMERO_AVVISO)).thenReturn(Optional.empty());

            ResponseEntity<?> response = pendenzeController.getRicevuta(
                    ID_DOMINIO, NUMERO_AVVISO, MediaType.APPLICATION_JSON_VALUE);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @DisplayName("Dovrebbe restituire 404 quando ricevuta non trovata (PDF)")
        void shouldReturn404WhenRicevutaNotFoundPdf() {
            when(stampeService.generateRicevutaPdf(ID_DOMINIO, NUMERO_AVVISO))
                    .thenReturn(Optional.empty());

            ResponseEntity<?> response = pendenzeController.getRicevuta(
                    ID_DOMINIO, NUMERO_AVVISO, MediaType.APPLICATION_PDF_VALUE);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @DisplayName("Dovrebbe restituire 406 quando Accept header mancante")
        void shouldReturn406WhenAcceptHeaderMissing() {
            ResponseEntity<?> response = pendenzeController.getRicevuta(
                    ID_DOMINIO, NUMERO_AVVISO, null);

            assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        }

        @Test
        @DisplayName("Dovrebbe restituire 406 quando Accept header non supportato")
        void shouldReturn406WhenAcceptHeaderNotSupported() {
            ResponseEntity<?> response = pendenzeController.getRicevuta(
                    ID_DOMINIO, NUMERO_AVVISO, MediaType.APPLICATION_XML_VALUE);

            assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        }
    }
}
