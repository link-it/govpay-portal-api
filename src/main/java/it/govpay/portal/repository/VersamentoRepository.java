package it.govpay.portal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import it.govpay.portal.entity.StatoVersamento;
import it.govpay.portal.entity.Versamento;

@Repository
public interface VersamentoRepository extends JpaRepository<Versamento, Long>, JpaSpecificationExecutor<Versamento> {

    Optional<Versamento> findByApplicazioneCodApplicazioneAndCodVersamentoEnte(
            String codApplicazione, String codVersamentoEnte);

    Optional<Versamento> findByDominioCodDominioAndNumeroAvviso(String codDominio, String numeroAvviso);

    Optional<Versamento> findByDominioCodDominioAndIuvVersamento(String codDominio, String iuvVersamento);

    List<Versamento> findByDebitoreIdentificativo(String debitoreIdentificativo);

    List<Versamento> findByStatoVersamento(StatoVersamento statoVersamento);

    List<Versamento> findByDominioIdAndStatoVersamento(Long idDominio, StatoVersamento statoVersamento);

    List<Versamento> findByDominioCodDominioAndDebitoreIdentificativo(
            String codDominio, String debitoreIdentificativo);

    List<Versamento> findByDominioCodDominioAndDebitoreIdentificativoAndStatoVersamento(
            String codDominio, String debitoreIdentificativo, StatoVersamento statoVersamento);

    Optional<Versamento> findByDominioCodDominioAndNumeroAvvisoAndIdSessione(
            String codDominio, String numeroAvviso, String idSessione);

    Optional<Versamento> findByDominioCodDominioAndIuvVersamentoAndIdSessione(
            String codDominio, String iuvVersamento, String idSessione);
}
