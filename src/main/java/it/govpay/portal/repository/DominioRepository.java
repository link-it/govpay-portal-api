package it.govpay.portal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.govpay.portal.entity.Dominio;

@Repository
public interface DominioRepository extends JpaRepository<Dominio, Long> {

    Optional<Dominio> findByCodDominio(String codDominio);

}
