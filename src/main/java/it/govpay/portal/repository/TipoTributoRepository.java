package it.govpay.portal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.govpay.portal.entity.TipoTributo;

@Repository
public interface TipoTributoRepository extends JpaRepository<TipoTributo, Long> {

    Optional<TipoTributo> findByCodTributo(String codTributo);

}
