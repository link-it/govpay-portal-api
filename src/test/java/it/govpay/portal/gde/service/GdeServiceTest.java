package it.govpay.portal.gde.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;

import it.govpay.common.client.service.ConnettoreService;
import it.govpay.common.configurazione.ConfigurazioneKeys;
import it.govpay.portal.gde.mapper.EventoPortalMapper;

class GdeServiceTest {

    private ConnettoreService connettoreService;
    private GdeService gdeService;

    @BeforeEach
    void setUp() {
        connettoreService = mock(ConnettoreService.class);
        gdeService = new GdeService(mock(ObjectMapper.class), Runnable::run, mock(EventoPortalMapper.class),
                connettoreService);
    }

    @Test
    void abilitatoConConnettore() {
        when(connettoreService.isAbilitato(ConfigurazioneKeys.COD_CONNETTORE_GDE)).thenReturn(true);

        assertTrue(gdeService.isAbilitato());
    }

    @Test
    void senzaConnettoreNonAbilitatoESenzaEccezioni() {
        when(connettoreService.isAbilitato(ConfigurazioneKeys.COD_CONNETTORE_GDE)).thenReturn(false);

        assertFalse(gdeService.isAbilitato());
        verify(connettoreService, never()).getConnettore(any());
    }
}
