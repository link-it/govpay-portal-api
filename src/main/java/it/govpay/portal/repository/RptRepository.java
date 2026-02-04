package it.govpay.portal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.govpay.portal.entity.Rpt;

@Repository
public interface RptRepository extends JpaRepository<Rpt, Long> {

    Optional<Rpt> findByVersamentoId(Long versamentoId);

    Optional<Rpt> findFirstByVersamentoIdOrderByDataMsgRicevutaDesc(Long versamentoId);

    Optional<Rpt> findByCodDominioAndIuv(String codDominio, String iuv);

    Optional<Rpt> findByCodDominioAndIuvAndCcp(String codDominio, String iuv, String ccp);
}
