package it.govpay.portal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.govpay.portal.entity.Uo;

@Repository
public interface UoRepository extends JpaRepository<Uo, Long> {

    Optional<Uo> findByCodUo(String codUo);

    List<Uo> findByDominioId(Long idDominio);

    Optional<Uo> findByDominioIdAndCodUo(Long idDominio, String codUo);

}
