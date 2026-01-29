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
import it.govpay.portal.entity.SingoloVersamento;
import it.govpay.portal.entity.StatoPagamento;
import it.govpay.portal.entity.StatoSingoloVersamento;
import it.govpay.portal.entity.StatoVersamento;
import it.govpay.portal.entity.Versamento;
import it.govpay.portal.mapper.PendenzeMapper;
import it.govpay.portal.model.Avviso;
import it.govpay.portal.model.ListaPendenze;
import it.govpay.portal.model.Pendenza;
import it.govpay.portal.model.Ricevuta;
import it.govpay.portal.model.StatoPendenza;
import it.govpay.portal.model.VocePendenza;
import it.govpay.portal.model.VoceRicevuta;
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

    private SingoloVersamento singoloVersamento;

    @BeforeEach
    void setUp() {
        dominio = Dominio.builder()
                .id(1L)
                .codDominio("12345678901")
                .ragioneSociale("Comune di Test")
                .build();

        singoloVersamento = SingoloVersamento.builder()
                .id(1L)
                .codSingoloVersamentoEnte("SV001")
                .statoSingoloVersamento(StatoSingoloVersamento.NON_ESEGUITO)
                .importoSingoloVersamento(150.50)
                .descrizione("Quota fissa TARI 2024")
                .indiceDati(1)
                .dominio(dominio)
                .build();

        versamento = Versamento.builder()
                .id(1L)
                .dominio(dominio)
                .codVersamentoEnte("VER001")
                .numeroAvviso("123456789012345678")
                .iuvVersamento("01234567890123456")
                .importoTotale(150.50)
                .debitoreIdentificativo("RSSMRA80A01H501U")
                .debitoreAnagrafica("Mario Rossi")
                .statoVersamento(StatoVersamento.NON_ESEGUITO)
                .causaleVersamento("Pagamento TARI 2024")
                .aggiornabile(true)
                .dataCreazione(LocalDateTime.now())
                .dataOraUltimoAggiornamento(LocalDateTime.now())
                .ack(false)
                .anomalo(false)
                .importoPagato(0.0)
                .importoIncassato(0.0)
                .statoPagamento(StatoPagamento.NON_PAGATO)
                .srcDebitoreIdentificativo("RSSMRA80A01H501U")
                .tipo("SPONTANEO")
                .singoliVersamenti(List.of(singoloVersamento))
                .build();

        singoloVersamento.setVersamento(versamento);

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
        @DisplayName("Dovrebbe restituire lista di pendenze con voci senza filtro stato")
        void shouldReturnPendenzeWithVociWithoutStateFilter() {
            setupSecurityContext();

            VocePendenza vocePendenza = new VocePendenza();
            vocePendenza.setIdVocePendenza("SV001");
            vocePendenza.setDescrizione("Quota fissa TARI 2024");
            vocePendenza.setImporto(150.50);
            vocePendenza.setIndice(1);

            Pendenza pendenzaModel = new Pendenza();
            pendenzaModel.setNumeroAvviso("123456789012345678");
            pendenzaModel.setImporto(150.50);
            pendenzaModel.setVoci(List.of(vocePendenza));

            when(versamentoRepository.findByDominioCodDominioAndDebitoreIdentificativo(
                    "12345678901", "RSSMRA80A01H501U"))
                    .thenReturn(List.of(versamento));
            when(pendenzeMapper.toPendenza(versamento)).thenReturn(pendenzaModel);

            ListaPendenze result = pendenzeService.getPendenze("12345678901", null);

            assertNotNull(result);
            assertNotNull(result.getRisultati());
            assertEquals(1, result.getRisultati().size());

            Pendenza pendenza = result.getRisultati().get(0);
            assertEquals("123456789012345678", pendenza.getNumeroAvviso());

            // Verifica che ci sia almeno una voce
            assertNotNull(pendenza.getVoci());
            assertFalse(pendenza.getVoci().isEmpty());
            assertEquals(1, pendenza.getVoci().size());
            assertEquals("SV001", pendenza.getVoci().get(0).getIdVocePendenza());
            assertEquals("Quota fissa TARI 2024", pendenza.getVoci().get(0).getDescrizione());
            assertEquals(150.50, pendenza.getVoci().get(0).getImporto());

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
                    "12345678901", "RSSMRA80A01H501U", StatoVersamento.ESEGUITO))
                    .thenReturn(List.of(versamento));
            when(pendenzeMapper.toPendenza(versamento)).thenReturn(pendenzaModel);

            ListaPendenze result = pendenzeService.getPendenze("12345678901", StatoPendenza.ESEGUITA);

            assertNotNull(result);
            assertNotNull(result.getRisultati());
            assertEquals(1, result.getRisultati().size());
            assertEquals(StatoPendenza.ESEGUITA, result.getRisultati().get(0).getStato());

            verify(versamentoRepository).findByDominioCodDominioAndDebitoreIdentificativoAndStatoVersamento(
                    "12345678901", "RSSMRA80A01H501U", StatoVersamento.ESEGUITO);
        }

        @Test
        @DisplayName("Dovrebbe restituire lista di pendenze con filtro stato NON_ESEGUITA")
        void shouldReturnPendenzeWithStateFilterNonEseguita() {
            setupSecurityContext();

            Pendenza pendenzaModel = new Pendenza();
            pendenzaModel.setStato(StatoPendenza.NON_ESEGUITA);

            when(versamentoRepository.findByDominioCodDominioAndDebitoreIdentificativoAndStatoVersamento(
                    "12345678901", "RSSMRA80A01H501U", StatoVersamento.NON_ESEGUITO))
                    .thenReturn(List.of(versamento));
            when(pendenzeMapper.toPendenza(versamento)).thenReturn(pendenzaModel);

            ListaPendenze result = pendenzeService.getPendenze("12345678901", StatoPendenza.NON_ESEGUITA);

            assertNotNull(result);
            assertEquals(StatoPendenza.NON_ESEGUITA, result.getRisultati().get(0).getStato());

            verify(versamentoRepository).findByDominioCodDominioAndDebitoreIdentificativoAndStatoVersamento(
                    "12345678901", "RSSMRA80A01H501U", StatoVersamento.NON_ESEGUITO);
        }

        @Test
        @DisplayName("Dovrebbe restituire lista di pendenze con filtro stato ESEGUITA_PARZIALE")
        void shouldReturnPendenzeWithStateFilterEseguitaParziale() {
            setupSecurityContext();

            versamento.setStatoVersamento(StatoVersamento.PARZIALMENTE_ESEGUITO);

            Pendenza pendenzaModel = new Pendenza();
            pendenzaModel.setStato(StatoPendenza.ESEGUITA_PARZIALE);

            when(versamentoRepository.findByDominioCodDominioAndDebitoreIdentificativoAndStatoVersamento(
                    "12345678901", "RSSMRA80A01H501U", StatoVersamento.PARZIALMENTE_ESEGUITO))
                    .thenReturn(List.of(versamento));
            when(pendenzeMapper.toPendenza(versamento)).thenReturn(pendenzaModel);

            ListaPendenze result = pendenzeService.getPendenze("12345678901", StatoPendenza.ESEGUITA_PARZIALE);

            assertNotNull(result);
            assertEquals(StatoPendenza.ESEGUITA_PARZIALE, result.getRisultati().get(0).getStato());

            verify(versamentoRepository).findByDominioCodDominioAndDebitoreIdentificativoAndStatoVersamento(
                    "12345678901", "RSSMRA80A01H501U", StatoVersamento.PARZIALMENTE_ESEGUITO);
        }

        @Test
        @DisplayName("Dovrebbe restituire lista di pendenze con filtro stato ANNULLATA")
        void shouldReturnPendenzeWithStateFilterAnnullata() {
            setupSecurityContext();

            versamento.setStatoVersamento(StatoVersamento.ANNULLATO);

            Pendenza pendenzaModel = new Pendenza();
            pendenzaModel.setStato(StatoPendenza.ANNULLATA);

            when(versamentoRepository.findByDominioCodDominioAndDebitoreIdentificativoAndStatoVersamento(
                    "12345678901", "RSSMRA80A01H501U", StatoVersamento.ANNULLATO))
                    .thenReturn(List.of(versamento));
            when(pendenzeMapper.toPendenza(versamento)).thenReturn(pendenzaModel);

            ListaPendenze result = pendenzeService.getPendenze("12345678901", StatoPendenza.ANNULLATA);

            assertNotNull(result);
            assertEquals(StatoPendenza.ANNULLATA, result.getRisultati().get(0).getStato());

            verify(versamentoRepository).findByDominioCodDominioAndDebitoreIdentificativoAndStatoVersamento(
                    "12345678901", "RSSMRA80A01H501U", StatoVersamento.ANNULLATO);
        }

        @Test
        @DisplayName("Dovrebbe restituire lista di pendenze con filtro stato ANOMALA")
        void shouldReturnPendenzeWithStateFilterAnomala() {
            setupSecurityContext();

            versamento.setStatoVersamento(StatoVersamento.ANOMALO);

            Pendenza pendenzaModel = new Pendenza();
            pendenzaModel.setStato(StatoPendenza.ANOMALA);

            when(versamentoRepository.findByDominioCodDominioAndDebitoreIdentificativoAndStatoVersamento(
                    "12345678901", "RSSMRA80A01H501U", StatoVersamento.ANOMALO))
                    .thenReturn(List.of(versamento));
            when(pendenzeMapper.toPendenza(versamento)).thenReturn(pendenzaModel);

            ListaPendenze result = pendenzeService.getPendenze("12345678901", StatoPendenza.ANOMALA);

            assertNotNull(result);
            assertEquals(StatoPendenza.ANOMALA, result.getRisultati().get(0).getStato());

            verify(versamentoRepository).findByDominioCodDominioAndDebitoreIdentificativoAndStatoVersamento(
                    "12345678901", "RSSMRA80A01H501U", StatoVersamento.ANOMALO);
        }

        @Test
        @DisplayName("Dovrebbe restituire lista di pendenze scadute con filtro stato SCADUTA")
        void shouldReturnPendenzeWithStateFilterScaduta() {
            setupSecurityContext();

            // Pendenza scaduta: data scadenza nel passato e stato NON_ESEGUITO
            versamento.setStatoVersamento(StatoVersamento.NON_ESEGUITO);
            versamento.setDataScadenza(LocalDateTime.now().minusDays(10));

            Pendenza pendenzaModel = new Pendenza();
            pendenzaModel.setStato(StatoPendenza.SCADUTA);

            when(versamentoRepository.findByDominioCodDominioAndDebitoreIdentificativoAndStatoVersamentoAndDataScadenzaBefore(
                    eq("12345678901"), eq("RSSMRA80A01H501U"), eq(StatoVersamento.NON_ESEGUITO), any(LocalDateTime.class)))
                    .thenReturn(List.of(versamento));
            when(pendenzeMapper.toPendenza(versamento)).thenReturn(pendenzaModel);

            ListaPendenze result = pendenzeService.getPendenze("12345678901", StatoPendenza.SCADUTA);

            assertNotNull(result);
            assertEquals(1, result.getRisultati().size());
            assertEquals(StatoPendenza.SCADUTA, result.getRisultati().get(0).getStato());

            // Verifica che venga usata la query specifica per SCADUTA
            verify(versamentoRepository).findByDominioCodDominioAndDebitoreIdentificativoAndStatoVersamentoAndDataScadenzaBefore(
                    eq("12345678901"), eq("RSSMRA80A01H501U"), eq(StatoVersamento.NON_ESEGUITO), any(LocalDateTime.class));
            // Verifica che NON venga usata la query generica
            verify(versamentoRepository, never()).findByDominioCodDominioAndDebitoreIdentificativoAndStatoVersamento(
                    any(), any(), any());
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
        @DisplayName("Dovrebbe restituire pendenza con voci quando trovata e appartiene all'utente")
        void shouldReturnPendenzaWithVociWhenFoundAndBelongsToUser() {
            setupSecurityContext();

            VocePendenza vocePendenza = new VocePendenza();
            vocePendenza.setIdVocePendenza("SV001");
            vocePendenza.setDescrizione("Quota fissa TARI 2024");
            vocePendenza.setImporto(150.50);
            vocePendenza.setIndice(1);

            Pendenza pendenzaModel = new Pendenza();
            pendenzaModel.setNumeroAvviso("123456789012345678");
            pendenzaModel.setVoci(List.of(vocePendenza));

            when(versamentoRepository.findByDominioCodDominioAndNumeroAvviso("12345678901", "123456789012345678"))
                    .thenReturn(Optional.of(versamento));
            when(pendenzeMapper.toPendenza(versamento)).thenReturn(pendenzaModel);

            Optional<Pendenza> result = pendenzeService.getPendenza("12345678901", "123456789012345678");

            assertTrue(result.isPresent());
            assertEquals("123456789012345678", result.get().getNumeroAvviso());

            // Verifica che ci sia almeno una voce
            assertNotNull(result.get().getVoci());
            assertFalse(result.get().getVoci().isEmpty());
            assertEquals(1, result.get().getVoci().size());
            assertEquals("SV001", result.get().getVoci().get(0).getIdVocePendenza());
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
        @DisplayName("Dovrebbe restituire ricevuta con voci quando trovata")
        void shouldReturnRicevutaWithVociWhenFound() {
            versamento.setStatoVersamento(StatoVersamento.ESEGUITO);
            versamento.setDataPagamento(LocalDateTime.now());

            VoceRicevuta voceRicevuta = new VoceRicevuta();
            voceRicevuta.setIdRiscossione("1");
            voceRicevuta.setDescrizione("Quota fissa TARI 2024");
            voceRicevuta.setImporto(150.50);
            voceRicevuta.setStato("ESEGUITO");

            Ricevuta ricevutaModel = new Ricevuta();
            ricevutaModel.setIuv("01234567890123456");
            ricevutaModel.setStato("ESEGUITO");
            ricevutaModel.setElencoVoci(List.of(voceRicevuta));

            when(versamentoRepository.findByDominioCodDominioAndNumeroAvviso("12345678901", "123456789012345678"))
                    .thenReturn(Optional.of(versamento));
            when(pendenzeMapper.toRicevuta(versamento)).thenReturn(ricevutaModel);

            Optional<Ricevuta> result = pendenzeService.getRicevuta("12345678901", "123456789012345678");

            assertTrue(result.isPresent());
            assertEquals("01234567890123456", result.get().getIuv());
            assertEquals("ESEGUITO", result.get().getStato());

            // Verifica che ci sia almeno una voce
            assertNotNull(result.get().getElencoVoci());
            assertFalse(result.get().getElencoVoci().isEmpty());
            assertEquals(1, result.get().getElencoVoci().size());
            assertEquals("Quota fissa TARI 2024", result.get().getElencoVoci().get(0).getDescrizione());
            assertEquals(150.50, result.get().getElencoVoci().get(0).getImporto());
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
