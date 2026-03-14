package it.govpay.portal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import it.govpay.portal.entity.TipoVersamentoDominio;

@Repository
public interface TipoVersamentoDominioRepository extends JpaRepository<TipoVersamentoDominio, Long> {

    List<TipoVersamentoDominio> findByDominioId(Long idDominio);

    List<TipoVersamentoDominio> findByDominioIdAndAbilitato(Long idDominio, Boolean abilitato);

    @Query("SELECT tvd FROM TipoVersamentoDominio tvd JOIN FETCH tvd.tipoVersamento tv " +
           "WHERE tvd.dominio.id = :idDominio AND tvd.abilitato = :abilitato " +
           "AND ((tvd.pagFormDefinizione IS NOT NULL AND tvd.pagFormTipo IS NOT NULL) " +
           "  OR (tv.pagFormDefinizione IS NOT NULL AND tv.pagFormTipo IS NOT NULL " +
           "      AND tvd.pagFormDefinizione IS NULL AND tvd.pagFormTipo IS NULL))")
    List<TipoVersamentoDominio> findByDominioIdAndAbilitatoWithFormPortale(
            @Param("idDominio") Long idDominio, @Param("abilitato") Boolean abilitato);

    Optional<TipoVersamentoDominio> findByDominioIdAndTipoVersamentoId(Long idDominio, Long idTipoVersamento);

    Optional<TipoVersamentoDominio> findByDominioCodDominioAndTipoVersamentoCodTipoVersamento(
            String codDominio, String codTipoVersamento);

}
