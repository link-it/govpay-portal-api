package it.govpay.portal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.govpay.portal.entity.Rpt;

@Repository
public interface RptRepository extends JpaRepository<Rpt, Long> {

    Optional<Rpt> findByVersamentoId(Long versamentoId);

    /**
     * RPT con RT acquisita ({@code xml_rt IS NOT NULL}) piu' recente per il versamento.
     * Un filtro sulla sola data non basta: su PostgreSQL un {@code ORDER BY ... DESC} antepone
     * i NULL, quindi un tentativo di pagamento ancora pendente (nessuna RT, data nulla)
     * precederebbe una RT gia' disponibile e il mapper fallirebbe leggendo un XML nullo.
     */
    Optional<Rpt> findFirstByVersamentoIdAndXmlRtIsNotNullOrderByDataMsgRicevutaDesc(Long versamentoId);

    Optional<Rpt> findByCodDominioAndIuv(String codDominio, String iuv);

    Optional<Rpt> findByCodDominioAndIuvAndCcp(String codDominio, String iuv, String ccp);
}
