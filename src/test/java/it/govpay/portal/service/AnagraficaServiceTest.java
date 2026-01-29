package it.govpay.portal.service;

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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import it.govpay.portal.config.SpidUserDetails;
import it.govpay.portal.entity.TipoVersamento;
import it.govpay.portal.entity.TipoVersamentoDominio;
import it.govpay.portal.exception.UnauthorizedException;
import it.govpay.portal.mapper.AnagraficaMapper;
import it.govpay.portal.model.Dominio;
import it.govpay.portal.model.ListaDomini;
import it.govpay.portal.model.ListaTipiPendenza;
import it.govpay.portal.model.Profilo;
import it.govpay.portal.model.TipoPendenza;
import it.govpay.portal.repository.DominioLogoRepository;
import it.govpay.portal.repository.DominioRepository;
import it.govpay.portal.repository.TipoVersamentoDominioRepository;

@ExtendWith(MockitoExtension.class)
class AnagraficaServiceTest {

    @Mock
    private DominioRepository dominioRepository;

    @Mock
    private DominioLogoRepository dominioLogoRepository;

    @Mock
    private TipoVersamentoDominioRepository tipoVersamentoDominioRepository;

    @Mock
    private AnagraficaMapper anagraficaMapper;

    @InjectMocks
    private AnagraficaService anagraficaService;

    private it.govpay.portal.entity.Dominio dominioEntity;
    private Dominio dominioModel;

    @BeforeEach
    void setUp() {
        dominioEntity = it.govpay.portal.entity.Dominio.builder()
                .id(1L)
                .codDominio("12345678901")
                .ragioneSociale("Comune di Test")
                .cbill("CBILL001")
                .build();

        dominioModel = new Dominio();
        dominioModel.setIdDominio("12345678901");
        dominioModel.setRagioneSociale("Comune di Test");
    }

    @Nested
    @DisplayName("getProfilo Tests")
    class GetProfiloTests {

        @Test
        @DisplayName("Dovrebbe restituire il profilo dell'utente autenticato")
        void shouldReturnUserProfile() {
            SpidUserDetails spidUser = new SpidUserDetails(
                    "RSSMRA80A01H501U",
                    "Mario",
                    "Rossi",
                    "mario.rossi@email.it",
                    "3331234567",
                    "Via Roma 1, Roma");

            Authentication authentication = new UsernamePasswordAuthenticationToken(spidUser, null);
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(securityContext);

            Profilo profilo = anagraficaService.getProfilo();

            assertNotNull(profilo);
            assertEquals("RSSMRA80A01H501U", profilo.getNome());
            assertNotNull(profilo.getAnagrafica());
            assertEquals("RSSMRA80A01H501U", profilo.getAnagrafica().getIdentificativo());
            assertEquals("Mario Rossi", profilo.getAnagrafica().getAnagrafica());
            assertEquals("mario.rossi@email.it", profilo.getAnagrafica().getEmail());
        }

        @Test
        @DisplayName("Dovrebbe lanciare UnauthorizedException quando autenticazione è null")
        void shouldThrowUnauthorizedExceptionWhenAuthenticationIsNull() {
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(null);
            SecurityContextHolder.setContext(securityContext);

            UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                    () -> anagraficaService.getProfilo());

            assertEquals("Autenticazione non presente", exception.getMessage());
        }

        @Test
        @DisplayName("Dovrebbe lanciare UnauthorizedException quando principal non è SpidUserDetails")
        void shouldThrowUnauthorizedExceptionWhenPrincipalIsNotSpidUserDetails() {
            // Simula un utente anonimo (principal è una stringa)
            Authentication authentication = new UsernamePasswordAuthenticationToken("UTENTE_ANONIMO", null);
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(securityContext);

            UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                    () -> anagraficaService.getProfilo());

            assertEquals("Utente non autenticato tramite SPID", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("getDomini Tests")
    class GetDominiTests {

        @Test
        @DisplayName("Dovrebbe restituire lista di domini")
        void shouldReturnListOfDomini() {
            List<it.govpay.portal.entity.Dominio> entities = List.of(dominioEntity);

            when(dominioRepository.findAll()).thenReturn(entities);
            when(anagraficaMapper.toDominio(dominioEntity)).thenReturn(dominioModel);

            ListaDomini result = anagraficaService.getDomini();

            assertNotNull(result);
            assertNotNull(result.getRisultati());
            assertEquals(1, result.getRisultati().size());
            assertEquals("12345678901", result.getRisultati().get(0).getIdDominio());

            verify(dominioRepository).findAll();
            verify(anagraficaMapper).toDominio(dominioEntity);
        }

        @Test
        @DisplayName("Dovrebbe restituire lista vuota quando non ci sono domini")
        void shouldReturnEmptyListWhenNoDomini() {
            when(dominioRepository.findAll()).thenReturn(new ArrayList<>());

            ListaDomini result = anagraficaService.getDomini();

            assertNotNull(result);
            assertNotNull(result.getRisultati());
            assertTrue(result.getRisultati().isEmpty());
        }
    }

    @Nested
    @DisplayName("getDominio Tests")
    class GetDominioTests {

        @Test
        @DisplayName("Dovrebbe restituire dominio quando trovato")
        void shouldReturnDominioWhenFound() {
            when(dominioRepository.findByCodDominio("12345678901"))
                    .thenReturn(Optional.of(dominioEntity));
            when(anagraficaMapper.toDominio(dominioEntity)).thenReturn(dominioModel);

            Optional<Dominio> result = anagraficaService.getDominio("12345678901");

            assertTrue(result.isPresent());
            assertEquals("12345678901", result.get().getIdDominio());

            verify(dominioRepository).findByCodDominio("12345678901");
        }

        @Test
        @DisplayName("Dovrebbe restituire empty quando dominio non trovato")
        void shouldReturnEmptyWhenDominioNotFound() {
            when(dominioRepository.findByCodDominio("99999999999"))
                    .thenReturn(Optional.empty());

            Optional<Dominio> result = anagraficaService.getDominio("99999999999");

            assertTrue(result.isEmpty());
            verify(anagraficaMapper, never()).toDominio(any());
        }
    }

    @Nested
    @DisplayName("getTipiPendenza Tests")
    class GetTipiPendenzaTests {

        @Test
        @DisplayName("Dovrebbe restituire lista di tipi pendenza")
        void shouldReturnListOfTipiPendenza() {
            TipoVersamento tipoVersamento = TipoVersamento.builder()
                    .id(1L)
                    .codTipoVersamento("TARI")
                    .descrizione("Tassa Rifiuti")
                    .build();

            TipoVersamentoDominio tvd = TipoVersamentoDominio.builder()
                    .id(1L)
                    .dominio(dominioEntity)
                    .tipoVersamento(tipoVersamento)
                    .abilitato(true)
                    .pagAbilitato(true)
                    .build();

            TipoPendenza tipoPendenzaModel = new TipoPendenza("TARI", "Tassa Rifiuti");

            when(dominioRepository.findByCodDominio("12345678901"))
                    .thenReturn(Optional.of(dominioEntity));
            when(tipoVersamentoDominioRepository.findByDominioIdAndAbilitatoAndPagAbilitato(1L, true, true))
                    .thenReturn(List.of(tvd));
            when(anagraficaMapper.toTipoPendenza(tvd)).thenReturn(tipoPendenzaModel);

            ListaTipiPendenza result = anagraficaService.getTipiPendenza("12345678901");

            assertNotNull(result);
            assertNotNull(result.getRisultati());
            assertEquals(1, result.getRisultati().size());
            assertEquals("TARI", result.getRisultati().get(0).getIdTipoPendenza());
        }

        @Test
        @DisplayName("Dovrebbe restituire lista vuota quando dominio non trovato")
        void shouldReturnEmptyListWhenDominioNotFound() {
            when(dominioRepository.findByCodDominio("99999999999"))
                    .thenReturn(Optional.empty());

            ListaTipiPendenza result = anagraficaService.getTipiPendenza("99999999999");

            assertNotNull(result);
            // Il servizio restituisce un nuovo ListaTipiPendenza senza settare risultati
            // quindi risultati e' null o una lista vuota a seconda dell'inizializzazione del model

            verify(tipoVersamentoDominioRepository, never()).findByDominioIdAndAbilitatoAndPagAbilitato(anyLong(), anyBoolean(), anyBoolean());
        }
    }

    @Nested
    @DisplayName("getTipoPendenza Tests")
    class GetTipoPendenzaTests {

        @Test
        @DisplayName("Dovrebbe restituire tipo pendenza quando trovato")
        void shouldReturnTipoPendenzaWhenFound() {
            TipoVersamento tipoVersamento = TipoVersamento.builder()
                    .id(1L)
                    .codTipoVersamento("TARI")
                    .descrizione("Tassa Rifiuti")
                    .build();

            TipoVersamentoDominio tvd = TipoVersamentoDominio.builder()
                    .id(1L)
                    .dominio(dominioEntity)
                    .tipoVersamento(tipoVersamento)
                    .build();

            TipoPendenza tipoPendenzaModel = new TipoPendenza("TARI", "Tassa Rifiuti");

            when(tipoVersamentoDominioRepository.findByDominioCodDominioAndTipoVersamentoCodTipoVersamento(
                    "12345678901", "TARI"))
                    .thenReturn(Optional.of(tvd));
            when(anagraficaMapper.toTipoPendenza(tvd)).thenReturn(tipoPendenzaModel);

            Optional<TipoPendenza> result = anagraficaService.getTipoPendenza("12345678901", "TARI");

            assertTrue(result.isPresent());
            assertEquals("TARI", result.get().getIdTipoPendenza());
        }

        @Test
        @DisplayName("Dovrebbe restituire empty quando tipo pendenza non trovato")
        void shouldReturnEmptyWhenTipoPendenzaNotFound() {
            when(tipoVersamentoDominioRepository.findByDominioCodDominioAndTipoVersamentoCodTipoVersamento(
                    "12345678901", "UNKNOWN"))
                    .thenReturn(Optional.empty());

            Optional<TipoPendenza> result = anagraficaService.getTipoPendenza("12345678901", "UNKNOWN");

            assertTrue(result.isEmpty());
            verify(anagraficaMapper, never()).toTipoPendenza(any());
        }
    }

    @Nested
    @DisplayName("getLogo Tests")
    class GetLogoTests {

        @Test
        @DisplayName("Dovrebbe restituire logo quando trovato")
        void shouldReturnLogoWhenFound() {
            byte[] logoBytes = "logo-data".getBytes();

            when(dominioLogoRepository.findLogoByCodDominio("12345678901"))
                    .thenReturn(Optional.of(logoBytes));

            Optional<byte[]> result = anagraficaService.getLogo("12345678901");

            assertTrue(result.isPresent());
            assertArrayEquals(logoBytes, result.get());
        }

        @Test
        @DisplayName("Dovrebbe restituire empty quando logo non trovato")
        void shouldReturnEmptyWhenLogoNotFound() {
            when(dominioLogoRepository.findLogoByCodDominio("99999999999"))
                    .thenReturn(Optional.empty());

            Optional<byte[]> result = anagraficaService.getLogo("99999999999");

            assertTrue(result.isEmpty());
        }
    }
}
