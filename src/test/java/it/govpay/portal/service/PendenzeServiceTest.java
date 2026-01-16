package it.govpay.portal.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import it.govpay.portal.config.SpidUserDetails;
import it.govpay.portal.entity.Dominio;
import it.govpay.portal.entity.StatoVersamento;
import it.govpay.portal.entity.Versamento;
import it.govpay.portal.mapper.PendenzeMapper;
import it.govpay.portal.model.Avviso;
import it.govpay.portal.model.ListaPendenze;
import it.govpay.portal.model.Pendenza;
import it.govpay.portal.model.Ricevuta;
import it.govpay.portal.model.StatoPendenza;
import it.govpay.portal.repository.VersamentoRepository;

@ExtendWith(MockitoExtension.class)
class PendenzeServiceTest {

    @Mock
    private VersamentoRepository versamentoRepository;

    @Mock
    private PendenzeMapper pendenzeMapper;

    @InjectMocks
    private PendenzeService pendenzeService;

    private Dominio dominio;
    private Versamento versamento;
    private SpidUserDetails spidUser;

    @BeforeEach
    void setUp() {
        dominio = Dominio.builder()
                .id(1L)
                .codDominio("12345678901")
                .ragioneSociale("Comune di Test")
                .build();

        versamento = Versamento.builder()
                .id(1L)
                .dominio(dominio)
                .numeroAvviso("123456789012345678")
                .iuvVersamento("01234567890123456")
                .importoTotale(150.50)
                .debitoreIdentificativo("RSSMRA80A01H501U")
                .debitoreAnagrafica("Mario Rossi")
                .statoVersamento(StatoVersamento.NON_ESEGUITO)
                .causaleVersamento("Pagamento TARI 2024")
                .singoliVersamenti(new ArrayList<>())
                .build();

        spidUser = new SpidUserDetails(
                "RSSMRA80A01H501U",
                "Mario",
                "Rossi",
                "mario.rossi@email.it",
                "3331234567",
                "Via Roma 1, Roma");
    }

    private void setupSecurityContext() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(spidUser, null);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Nested
    @DisplayName("getPendenze Tests")
    class GetPendenzeTests {

        @Test
        @DisplayName("Dovrebbe restituire lista di pendenze senza filtro stato")
        void shouldReturnPendenzeWithoutStateFilter() {
            setupSecurityContext();

            Pendenza pendenzaModel = new Pendenza();
            pendenzaModel.setNumeroAvviso("123456789012345678");
            pendenzaModel.setImporto(150.50);

            when(versamentoRepository.findByDominioCodDominioAndDebitoreIdentificativo(
                    "12345678901", "RSSMRA80A01H501U"))
                    .thenReturn(List.of(versamento));
            when(pendenzeMapper.toPendenza(versamento)).thenReturn(pendenzaModel);

            ListaPendenze result = pendenzeService.getPendenze("12345678901", null);

            assertNotNull(result);
            assertNotNull(result.getRisultati());
            assertEquals(1, result.getRisultati().size());
            assertEquals("123456789012345678", result.getRisultati().get(0).getNumeroAvviso());

            verify(versamentoRepository).findByDominioCodDominioAndDebitoreIdentificativo(
                    "12345678901", "RSSMRA80A01H501U");
            verify(versamentoRepository, never()).findByDominioCodDominioAndDebitoreIdentificativoAndStatoVersamento(
                    any(), any(), any());
        }

        @Test
        @DisplayName("Dovrebbe restituire lista di pendenze con filtro stato ESEGUITA")
        void shouldReturnPendenzeWithStateFilterEseguita() {
            setupSecurityContext();

            versamento.setStatoVersamento(StatoVersamento.ESEGUITO);

            Pendenza pendenzaModel = new Pendenza();
            pendenzaModel.setStato(StatoPendenza.ESEGUITA);

            when(versamentoRepository.findByDominioCodDominioAndDebitoreIdentificativoAndStatoVersamento(
                    "12345678901", "RSSMRA80A01H501U", "ESEGUITO"))
                    .thenReturn(List.of(versamento));
            when(pendenzeMapper.toPendenza(versamento)).thenReturn(pendenzaModel);

            ListaPendenze result = pendenzeService.getPendenze("12345678901", StatoPendenza.ESEGUITA);

            assertNotNull(result);
            assertNotNull(result.getRisultati());
            assertEquals(1, result.getRisultati().size());
            assertEquals(StatoPendenza.ESEGUITA, result.getRisultati().get(0).getStato());

            verify(versamentoRepository).findByDominioCodDominioAndDebitoreIdentificativoAndStatoVersamento(
                    "12345678901", "RSSMRA80A01H501U", "ESEGUITO");
        }

        @Test
        @DisplayName("Dovrebbe restituire lista di pendenze con filtro stato NON_ESEGUITA")
        void shouldReturnPendenzeWithStateFilterNonEseguita() {
            setupSecurityContext();

            Pendenza pendenzaModel = new Pendenza();
            pendenzaModel.setStato(StatoPendenza.NON_ESEGUITA);

            when(versamentoRepository.findByDominioCodDominioAndDebitoreIdentificativoAndStatoVersamento(
                    "12345678901", "RSSMRA80A01H501U", "NON_ESEGUITO"))
                    .thenReturn(List.of(versamento));
            when(pendenzeMapper.toPendenza(versamento)).thenReturn(pendenzaModel);

            ListaPendenze result = pendenzeService.getPendenze("12345678901", StatoPendenza.NON_ESEGUITA);

            assertNotNull(result);
            assertEquals(StatoPendenza.NON_ESEGUITA, result.getRisultati().get(0).getStato());
        }

        @Test
        @DisplayName("Dovrebbe restituire lista vuota quando non ci sono pendenze")
        void shouldReturnEmptyListWhenNoPendenze() {
            setupSecurityContext();

            when(versamentoRepository.findByDominioCodDominioAndDebitoreIdentificativo(
                    "12345678901", "RSSMRA80A01H501U"))
                    .thenReturn(new ArrayList<>());

            ListaPendenze result = pendenzeService.getPendenze("12345678901", null);

            assertNotNull(result);
            assertNotNull(result.getRisultati());
            assertTrue(result.getRisultati().isEmpty());
        }
    }

    @Nested
    @DisplayName("getPendenza Tests")
    class GetPendenzaTests {

        @Test
        @DisplayName("Dovrebbe restituire pendenza quando trovata e appartiene all'utente")
        void shouldReturnPendenzaWhenFoundAndBelongsToUser() {
            setupSecurityContext();

            Pendenza pendenzaModel = new Pendenza();
            pendenzaModel.setNumeroAvviso("123456789012345678");

            when(versamentoRepository.findByDominioCodDominioAndNumeroAvviso("12345678901", "123456789012345678"))
                    .thenReturn(Optional.of(versamento));
            when(pendenzeMapper.toPendenza(versamento)).thenReturn(pendenzaModel);

            Optional<Pendenza> result = pendenzeService.getPendenza("12345678901", "123456789012345678");

            assertTrue(result.isPresent());
            assertEquals("123456789012345678", result.get().getNumeroAvviso());
        }

        @Test
        @DisplayName("Dovrebbe restituire empty quando pendenza non trovata")
        void shouldReturnEmptyWhenPendenzaNotFound() {
            setupSecurityContext();

            when(versamentoRepository.findByDominioCodDominioAndNumeroAvviso("12345678901", "999999999999999999"))
                    .thenReturn(Optional.empty());

            Optional<Pendenza> result = pendenzeService.getPendenza("12345678901", "999999999999999999");

            assertTrue(result.isEmpty());
            verify(pendenzeMapper, never()).toPendenza(any());
        }

        @Test
        @DisplayName("Dovrebbe restituire empty quando pendenza non appartiene all'utente")
        void shouldReturnEmptyWhenPendenzaDoesNotBelongToUser() {
            setupSecurityContext();

            // Versamento di un altro utente
            versamento.setDebitoreIdentificativo("VRDLGI80A01H501U");

            when(versamentoRepository.findByDominioCodDominioAndNumeroAvviso("12345678901", "123456789012345678"))
                    .thenReturn(Optional.of(versamento));

            Optional<Pendenza> result = pendenzeService.getPendenza("12345678901", "123456789012345678");

            assertTrue(result.isEmpty());
            verify(pendenzeMapper, never()).toPendenza(any());
        }
    }

    @Nested
    @DisplayName("getAvviso Tests")
    class GetAvvisoTests {

        @Test
        @DisplayName("Dovrebbe restituire avviso quando trovato")
        void shouldReturnAvvisoWhenFound() {
            Avviso avvisoModel = new Avviso();
            avvisoModel.setNumeroAvviso("123456789012345678");
            avvisoModel.setImporto(150.50);

            when(versamentoRepository.findByDominioCodDominioAndNumeroAvviso("12345678901", "123456789012345678"))
                    .thenReturn(Optional.of(versamento));
            when(pendenzeMapper.toAvviso(versamento, dominio)).thenReturn(avvisoModel);

            Optional<Avviso> result = pendenzeService.getAvviso("12345678901", "123456789012345678");

            assertTrue(result.isPresent());
            assertEquals("123456789012345678", result.get().getNumeroAvviso());
            assertEquals(150.50, result.get().getImporto());
        }

        @Test
        @DisplayName("Dovrebbe restituire empty quando avviso non trovato")
        void shouldReturnEmptyWhenAvvisoNotFound() {
            when(versamentoRepository.findByDominioCodDominioAndNumeroAvviso("12345678901", "999999999999999999"))
                    .thenReturn(Optional.empty());

            Optional<Avviso> result = pendenzeService.getAvviso("12345678901", "999999999999999999");

            assertTrue(result.isEmpty());
            verify(pendenzeMapper, never()).toAvviso(any(), any());
        }
    }

    @Nested
    @DisplayName("getRicevuta Tests")
    class GetRicevutaTests {

        @Test
        @DisplayName("Dovrebbe restituire ricevuta quando trovata")
        void shouldReturnRicevutaWhenFound() {
            versamento.setStatoVersamento(StatoVersamento.ESEGUITO);
            versamento.setDataPagamento(LocalDateTime.now());

            Ricevuta ricevutaModel = new Ricevuta();
            ricevutaModel.setIuv("01234567890123456");
            ricevutaModel.setStato("ESEGUITO");

            when(versamentoRepository.findByDominioCodDominioAndNumeroAvviso("12345678901", "123456789012345678"))
                    .thenReturn(Optional.of(versamento));
            when(pendenzeMapper.toRicevuta(versamento)).thenReturn(ricevutaModel);

            Optional<Ricevuta> result = pendenzeService.getRicevuta("12345678901", "123456789012345678");

            assertTrue(result.isPresent());
            assertEquals("01234567890123456", result.get().getIuv());
            assertEquals("ESEGUITO", result.get().getStato());
        }

        @Test
        @DisplayName("Dovrebbe restituire empty quando ricevuta non trovata")
        void shouldReturnEmptyWhenRicevutaNotFound() {
            when(versamentoRepository.findByDominioCodDominioAndNumeroAvviso("12345678901", "999999999999999999"))
                    .thenReturn(Optional.empty());

            Optional<Ricevuta> result = pendenzeService.getRicevuta("12345678901", "999999999999999999");

            assertTrue(result.isEmpty());
            verify(pendenzeMapper, never()).toRicevuta(any());
        }

        @Test
        @DisplayName("Dovrebbe restituire ricevuta anche per versamento non eseguito")
        void shouldReturnRicevutaForNonExecutedPayment() {
            versamento.setStatoVersamento(StatoVersamento.NON_ESEGUITO);

            Ricevuta ricevutaModel = new Ricevuta();
            ricevutaModel.setIuv("01234567890123456");
            ricevutaModel.setStato("NON_ESEGUITO");

            when(versamentoRepository.findByDominioCodDominioAndNumeroAvviso("12345678901", "123456789012345678"))
                    .thenReturn(Optional.of(versamento));
            when(pendenzeMapper.toRicevuta(versamento)).thenReturn(ricevutaModel);

            Optional<Ricevuta> result = pendenzeService.getRicevuta("12345678901", "123456789012345678");

            assertTrue(result.isPresent());
            assertEquals("NON_ESEGUITO", result.get().getStato());
        }
    }
}
