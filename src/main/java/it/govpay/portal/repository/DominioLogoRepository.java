package it.govpay.portal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import it.govpay.portal.entity.DominioLogo;

@Repository
public interface DominioLogoRepository extends JpaRepository<DominioLogo, Long> {

    @Query("SELECT dl.logo FROM DominioLogo dl WHERE dl.codDominio = :codDominio")
    Optional<byte[]> findLogoByCodDominio(@Param("codDominio") String codDominio);
}
