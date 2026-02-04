package it.govpay.portal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.govpay.portal.entity.SingoloVersamento;

@Repository
public interface SingoloVersamentoRepository extends JpaRepository<SingoloVersamento, Long> {

    List<SingoloVersamento> findByVersamentoId(Long idVersamento);

    Optional<SingoloVersamento> findByVersamentoIdAndIndiceDati(Long idVersamento, Integer indiceDati);

    Optional<SingoloVersamento> findByCodSingoloVersamentoEnte(String codSingoloVersamentoEnte);

}
