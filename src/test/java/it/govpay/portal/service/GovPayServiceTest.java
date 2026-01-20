package it.govpay.portal.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

import it.govpay.pendenze.client.api.PendenzeApi;
import it.govpay.pendenze.client.model.NuovaPendenza;
import it.govpay.pendenze.client.model.PendenzaCreata;
import it.govpay.portal.entity.Dominio;
import it.govpay.portal.entity.Versamento;
import it.govpay.portal.mapper.GovPayPendenzeMapper;

@ExtendWith(MockitoExtension.class)
class GovPayServiceTest {

    @Mock
    private PendenzeApi pendenzeApi;

    @Mock
    private GovPayPendenzeMapper mapper;

    private GovPayService service;

    @BeforeEach
    void setUp() {
        service = new GovPayService(pendenzeApi, mapper);
    }

    @Nested
    @DisplayName("addPendenza Tests")
    class AddPendenzaTests {

        @Test
        @DisplayName("addPendenza con successo dovrebbe restituire PendenzaCreata")
        void successfulAddPendenzaShouldReturnPendenzaCreata() {
            String idA2A = "APP001";
            String idPendenza = "PEN001";
            Versamento versamento = createMinimalVersamento();

            NuovaPendenza nuovaPendenza = new NuovaPendenza();
            when(mapper.toNuovaPendenza(versamento)).thenReturn(nuovaPendenza);

            PendenzaCreata expectedResult = new PendenzaCreata();
            expectedResult.setIdDominio("12345678901");
            expectedResult.setNumeroAvviso("301234567890123456");

            when(pendenzeApi.addPendenza(eq(idA2A), eq(idPendenza), eq(false), isNull(), any(NuovaPendenza.class)))
                    .thenReturn(expectedResult);

            PendenzaCreata result = service.addPendenza(idA2A, idPendenza, versamento);

            assertNotNull(result);
            assertEquals("12345678901", result.getIdDominio());
            assertEquals("301234567890123456", result.getNumeroAvviso());
            verify(pendenzeApi).addPendenza(idA2A, idPendenza, false, null, nuovaPendenza);
        }

        @Test
        @DisplayName("addPendenza con stampaAvviso=true dovrebbe passare il parametro")
        void addPendenzaWithStampaAvvisoShouldPassParameter() {
            String idA2A = "APP001";
            String idPendenza = "PEN001";
            Versamento versamento = createMinimalVersamento();

            NuovaPendenza nuovaPendenza = new NuovaPendenza();
            when(mapper.toNuovaPendenza(versamento)).thenReturn(nuovaPendenza);

            PendenzaCreata expectedResult = new PendenzaCreata();
            when(pendenzeApi.addPendenza(eq(idA2A), eq(idPendenza), eq(true), isNull(), any(NuovaPendenza.class)))
                    .thenReturn(expectedResult);

            PendenzaCreata result = service.addPendenza(idA2A, idPendenza, versamento, true);

            assertNotNull(result);
            verify(pendenzeApi).addPendenza(idA2A, idPendenza, true, null, nuovaPendenza);
        }

        @Test
        @DisplayName("addPendenza con dataAvvisatura dovrebbe passare il parametro")
        void addPendenzaWithDataAvvisaturaShouldPassParameter() {
            String idA2A = "APP001";
            String idPendenza = "PEN001";
            Versamento versamento = createMinimalVersamento();
            it.govpay.pendenze.client.model.AddPendenzaDataAvvisaturaParameter dataAvvisatura =
                    new it.govpay.pendenze.client.model.AddPendenzaDataAvvisaturaParameter();

            NuovaPendenza nuovaPendenza = new NuovaPendenza();
            when(mapper.toNuovaPendenza(versamento)).thenReturn(nuovaPendenza);

            PendenzaCreata expectedResult = new PendenzaCreata();
            when(pendenzeApi.addPendenza(eq(idA2A), eq(idPendenza), eq(true), eq(dataAvvisatura), any(NuovaPendenza.class)))
                    .thenReturn(expectedResult);

            PendenzaCreata result = service.addPendenza(idA2A, idPendenza, versamento, true, dataAvvisatura);

            assertNotNull(result);
            verify(pendenzeApi).addPendenza(idA2A, idPendenza, true, dataAvvisatura, nuovaPendenza);
        }

        @Test
        @DisplayName("addPendenza con errore RestClient dovrebbe propagare eccezione")
        void addPendenzaWithRestClientErrorShouldPropagateException() {
            String idA2A = "APP001";
            String idPendenza = "PEN001";
            Versamento versamento = createMinimalVersamento();

            NuovaPendenza nuovaPendenza = new NuovaPendenza();
            when(mapper.toNuovaPendenza(versamento)).thenReturn(nuovaPendenza);

            when(pendenzeApi.addPendenza(any(), any(), anyBoolean(), any(), any()))
                    .thenThrow(new RestClientException("Connection refused"));

            RestClientException exception = assertThrows(
                    RestClientException.class,
                    () -> service.addPendenza(idA2A, idPendenza, versamento)
            );

            assertEquals("Connection refused", exception.getMessage());
        }

        @Test
        @DisplayName("addPendenza senza parametri opzionali dovrebbe usare default")
        void addPendenzaWithoutOptionalParamsShouldUseDefaults() {
            String idA2A = "APP001";
            String idPendenza = "PEN001";
            Versamento versamento = createMinimalVersamento();

            NuovaPendenza nuovaPendenza = new NuovaPendenza();
            when(mapper.toNuovaPendenza(versamento)).thenReturn(nuovaPendenza);

            PendenzaCreata expectedResult = new PendenzaCreata();
            when(pendenzeApi.addPendenza(eq(idA2A), eq(idPendenza), eq(false), isNull(), any(NuovaPendenza.class)))
                    .thenReturn(expectedResult);

            service.addPendenza(idA2A, idPendenza, versamento);

            verify(pendenzeApi).addPendenza(idA2A, idPendenza, false, null, nuovaPendenza);
        }

        @Test
        @DisplayName("mapper dovrebbe essere invocato con versamento")
        void mapperShouldBeCalledWithVersamento() {
            String idA2A = "APP001";
            String idPendenza = "PEN001";
            Versamento versamento = createMinimalVersamento();

            NuovaPendenza nuovaPendenza = new NuovaPendenza();
            when(mapper.toNuovaPendenza(versamento)).thenReturn(nuovaPendenza);

            PendenzaCreata expectedResult = new PendenzaCreata();
            when(pendenzeApi.addPendenza(any(), any(), anyBoolean(), any(), any()))
                    .thenReturn(expectedResult);

            service.addPendenza(idA2A, idPendenza, versamento);

            verify(mapper).toNuovaPendenza(versamento);
        }
    }

    private Versamento createMinimalVersamento() {
        Dominio dominio = Dominio.builder()
                .codDominio("12345678901")
                .ragioneSociale("Comune di Test")
                .build();

        return Versamento.builder()
                .dominio(dominio)
                .codVersamentoEnte("COD_VERS_001")
                .singoliVersamenti(new ArrayList<>())
                .build();
    }
}
