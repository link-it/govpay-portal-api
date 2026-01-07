package it.govpay.portal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.govpay.portal.entity.IbanAccredito;

@Repository
public interface IbanAccreditoRepository extends JpaRepository<IbanAccredito, Long> {

    Optional<IbanAccredito> findByCodIban(String codIban);

    List<IbanAccredito> findByDominioId(Long idDominio);

    List<IbanAccredito> findByDominioIdAndAbilitato(Long idDominio, Boolean abilitato);

    Optional<IbanAccredito> findByDominioIdAndCodIban(Long idDominio, String codIban);

}
