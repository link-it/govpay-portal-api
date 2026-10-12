package it.govpay.portal.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import it.govpay.portal.entity.Dominio;
import it.govpay.portal.entity.Rpt;
import it.govpay.portal.entity.Versamento;

@DataJpaTest
@ActiveProfiles("test")
class RptRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RptRepository repository;

    private Versamento versamento;

    @BeforeEach
    void setUp() {
        Dominio dominio = Dominio.builder()
                .codDominio("12345678901")
                .ragioneSociale("Comune di Test")
                .build();
        entityManager.persist(dominio);

        versamento = Versamento.builder()
                .dominio(dominio)
                .numeroAvviso("123456789012345678")
                .build();
        entityManager.persist(versamento);
    }

    @Test
    @DisplayName("findFirstByVersamentoIdAndXmlRtIsNotNullOrderByDataMsgRicevutaDesc ignora i tentativi senza RT acquisita")
    void shouldIgnoreAttemptsWithoutAcquiredRt() {
        // Tentativo di pagamento pendente: nessuna RT ancora acquisita, nessuna data di ricevuta.
        Rpt pendente = Rpt.builder()
                .versamento(versamento)
                .iuv("IUV_PENDENTE")
                .ccp("CCP_PENDENTE")
                .codDominio("12345678901")
                .versione("SANP_240")
                .build();
        entityManager.persist(pendente);

        // RT storica gia' acquisita, con data di ricevuta precedente al tentativo pendente.
        Rpt conRt = Rpt.builder()
                .versamento(versamento)
                .iuv("IUV_CON_RT")
                .ccp("CCP_CON_RT")
                .codDominio("12345678901")
                .versione("SANP_240")
                .dataMsgRicevuta(LocalDateTime.of(2026, 1, 1, 10, 0, 0))
                .xmlRt("<rt/>".getBytes())
                .build();
        entityManager.persist(conRt);

        entityManager.flush();

        Optional<Rpt> result = repository
                .findFirstByVersamentoIdAndXmlRtIsNotNullOrderByDataMsgRicevutaDesc(versamento.getId());

        assertTrue(result.isPresent());
        assertEquals("IUV_CON_RT", result.get().getIuv());
    }

    @Test
    @DisplayName("findFirstByVersamentoIdAndXmlRtIsNotNullOrderByDataMsgRicevutaDesc restituisce empty se nessuna RT e' acquisita")
    void shouldReturnEmptyWhenNoRtAcquired() {
        Rpt pendente = Rpt.builder()
                .versamento(versamento)
                .iuv("IUV_PENDENTE")
                .ccp("CCP_PENDENTE")
                .codDominio("12345678901")
                .versione("SANP_240")
                .build();
        entityManager.persist(pendente);
        entityManager.flush();

        Optional<Rpt> result = repository
                .findFirstByVersamentoIdAndXmlRtIsNotNullOrderByDataMsgRicevutaDesc(versamento.getId());

        assertTrue(result.isEmpty());
    }
}
