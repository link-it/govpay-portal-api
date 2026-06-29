package it.govpay.portal.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import it.govpay.portal.entity.Dominio;
import it.govpay.portal.entity.TipoVersamento;
import it.govpay.portal.entity.TipoVersamentoDominio;

@DataJpaTest
@ActiveProfiles("test")
class TipoVersamentoDominioRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TipoVersamentoDominioRepository repository;

    private Dominio dominio;

    @BeforeEach
    void setUp() {
        dominio = Dominio.builder()
                .codDominio("12345678901")
                .ragioneSociale("Comune di Test")
                .build();
        entityManager.persist(dominio);
    }

    @Test
    @DisplayName("findByDominioIdAndAbilitatoWithFormPortale dovrebbe restituire risultati ordinati per descrizione ASC")
    void shouldReturnResultsOrderedByDescrizioneAsc() {
        // Inserisco tipi versamento con descrizioni in ordine non alfabetico
        TipoVersamento tvZzz = TipoVersamento.builder()
                .codTipoVersamento("ZZZ")
                .descrizione("Zzz - Ultimo tipo")
                .pagFormTipo("formTipo")
                .pagFormDefinizione("formDef")
                .build();
        entityManager.persist(tvZzz);

        TipoVersamento tvAaa = TipoVersamento.builder()
                .codTipoVersamento("AAA")
                .descrizione("Aaa - Primo tipo")
                .pagFormTipo("formTipo")
                .pagFormDefinizione("formDef")
                .build();
        entityManager.persist(tvAaa);

        TipoVersamento tvMmm = TipoVersamento.builder()
                .codTipoVersamento("MMM")
                .descrizione("Mmm - Tipo intermedio")
                .pagFormTipo("formTipo")
                .pagFormDefinizione("formDef")
                .build();
        entityManager.persist(tvMmm);

        // Associo i tipi versamento al dominio con form portale definito a livello di tvd
        TipoVersamentoDominio tvdZzz = TipoVersamentoDominio.builder()
                .dominio(dominio)
                .tipoVersamento(tvZzz)
                .abilitato(true)
                .pagAbilitato(true)
                .pagFormTipo("formTipo")
                .pagFormDefinizione("formDef")
                .build();
        entityManager.persist(tvdZzz);

        TipoVersamentoDominio tvdAaa = TipoVersamentoDominio.builder()
                .dominio(dominio)
                .tipoVersamento(tvAaa)
                .abilitato(true)
                .pagAbilitato(true)
                .pagFormTipo("formTipo")
                .pagFormDefinizione("formDef")
                .build();
        entityManager.persist(tvdAaa);

        TipoVersamentoDominio tvdMmm = TipoVersamentoDominio.builder()
                .dominio(dominio)
                .tipoVersamento(tvMmm)
                .abilitato(true)
                .pagAbilitato(true)
                .pagFormTipo("formTipo")
                .pagFormDefinizione("formDef")
                .build();
        entityManager.persist(tvdMmm);

        entityManager.flush();

        List<TipoVersamentoDominio> result = repository.findByDominioIdAndAbilitatoWithFormPortale(dominio.getId(), true);

        assertEquals(3, result.size());
        assertEquals("Aaa - Primo tipo", result.get(0).getTipoVersamento().getDescrizione());
        assertEquals("Mmm - Tipo intermedio", result.get(1).getTipoVersamento().getDescrizione());
        assertEquals("Zzz - Ultimo tipo", result.get(2).getTipoVersamento().getDescrizione());
    }

    @Test
    @DisplayName("findByDominioIdAndAbilitatoWithFormPortale dovrebbe escludere tipi con pagAbilitato false o null")
    void shouldExcludeTipiWithPagAbilitatoFalseOrNull() {
        TipoVersamento tvAbilitato = TipoVersamento.builder()
                .codTipoVersamento("ABILITATO")
                .descrizione("Tipo abilitato")
                .pagFormTipo("formTipo")
                .pagFormDefinizione("formDef")
                .build();
        entityManager.persist(tvAbilitato);

        TipoVersamento tvDisabilitato = TipoVersamento.builder()
                .codTipoVersamento("DISABILITATO")
                .descrizione("Tipo disabilitato")
                .pagFormTipo("formTipo")
                .pagFormDefinizione("formDef")
                .build();
        entityManager.persist(tvDisabilitato);

        TipoVersamento tvNull = TipoVersamento.builder()
                .codTipoVersamento("NULL")
                .descrizione("Tipo con pagAbilitato null")
                .pagFormTipo("formTipo")
                .pagFormDefinizione("formDef")
                .build();
        entityManager.persist(tvNull);

        TipoVersamentoDominio tvdAbilitato = TipoVersamentoDominio.builder()
                .dominio(dominio)
                .tipoVersamento(tvAbilitato)
                .abilitato(true)
                .pagAbilitato(true)
                .pagFormTipo("formTipo")
                .pagFormDefinizione("formDef")
                .build();
        entityManager.persist(tvdAbilitato);

        TipoVersamentoDominio tvdDisabilitato = TipoVersamentoDominio.builder()
                .dominio(dominio)
                .tipoVersamento(tvDisabilitato)
                .abilitato(true)
                .pagAbilitato(false)
                .pagFormTipo("formTipo")
                .pagFormDefinizione("formDef")
                .build();
        entityManager.persist(tvdDisabilitato);

        TipoVersamentoDominio tvdNull = TipoVersamentoDominio.builder()
                .dominio(dominio)
                .tipoVersamento(tvNull)
                .abilitato(true)
                .pagFormTipo("formTipo")
                .pagFormDefinizione("formDef")
                .build();
        entityManager.persist(tvdNull);

        entityManager.flush();

        List<TipoVersamentoDominio> result = repository.findByDominioIdAndAbilitatoWithFormPortale(dominio.getId(), true);

        assertEquals(1, result.size());
        assertEquals("Tipo abilitato", result.get(0).getTipoVersamento().getDescrizione());
    }
}
