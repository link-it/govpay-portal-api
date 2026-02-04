package it.govpay.portal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.govpay.portal.entity.Tributo;

@Repository
public interface TributoRepository extends JpaRepository<Tributo, Long> {

    List<Tributo> findByDominioId(Long idDominio);

    List<Tributo> findByDominioIdAndAbilitato(Long idDominio, Boolean abilitato);

    Optional<Tributo> findByDominioIdAndTipoTributoId(Long idDominio, Long idTipoTributo);

    Optional<Tributo> findByDominioCodDominioAndTipoTributoCodTributo(String codDominio, String codTributo);

}
