package it.govpay.portal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.govpay.portal.entity.TipoVersamento;

@Repository
public interface TipoVersamentoRepository extends JpaRepository<TipoVersamento, Long> {

    Optional<TipoVersamento> findByCodTipoVersamento(String codTipoVersamento);

    List<TipoVersamento> findByAbilitato(Boolean abilitato);

}
