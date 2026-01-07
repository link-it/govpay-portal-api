package it.govpay.portal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import it.govpay.portal.entity.VersamentoAttesa;

@Repository
public interface VersamentoAttesaRepository extends JpaRepository<VersamentoAttesa, Long>, JpaSpecificationExecutor<VersamentoAttesa> {

    Optional<VersamentoAttesa> findByApplicazioneCodApplicazioneAndCodVersamentoEnte(
            String codApplicazione, String codVersamentoEnte);

    Optional<VersamentoAttesa> findByDominioCodDominioAndNumeroAvviso(String codDominio, String numeroAvviso);

    Optional<VersamentoAttesa> findByDominioCodDominioAndIuvVersamento(String codDominio, String iuvVersamento);

    List<VersamentoAttesa> findByDebitoreIdentificativo(String debitoreIdentificativo);

    List<VersamentoAttesa> findByStatoVersamento(String statoVersamento);

}
