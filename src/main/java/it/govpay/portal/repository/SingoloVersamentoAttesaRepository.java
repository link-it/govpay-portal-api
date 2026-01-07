package it.govpay.portal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.govpay.portal.entity.SingoloVersamentoAttesa;

@Repository
public interface SingoloVersamentoAttesaRepository extends JpaRepository<SingoloVersamentoAttesa, Long> {

    List<SingoloVersamentoAttesa> findByVersamentoAttesaId(Long idVersamentoAttesa);

    Optional<SingoloVersamentoAttesa> findByVersamentoAttesaIdAndIndiceDati(Long idVersamentoAttesa, Integer indiceDati);

    Optional<SingoloVersamentoAttesa> findByCodSingoloVersamentoEnte(String codSingoloVersamentoEnte);

}
