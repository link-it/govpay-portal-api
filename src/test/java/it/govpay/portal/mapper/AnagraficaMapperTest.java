package it.govpay.portal.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.govpay.portal.entity.TipoVersamento;
import it.govpay.portal.entity.TipoVersamentoDominio;
import it.govpay.portal.entity.Uo;
import it.govpay.portal.model.Dominio;
import it.govpay.portal.model.TipoPendenza;

class AnagraficaMapperTest {

    private AnagraficaMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AnagraficaMapper(new ObjectMapper());
    }

    @Nested
    @DisplayName("toDominio Tests")
    class ToDominioTests {

        @Test
        @DisplayName("Null entity dovrebbe restituire null")
        void nullEntityShouldReturnNull() {
            assertNull(mapper.toDominio(null));
        }

        @Test
        @DisplayName("Dominio minimo dovrebbe essere mappato correttamente")
        void minimalDominioShouldBeMappedCorrectly() {
            it.govpay.portal.entity.Dominio entity = createMinimalDominioEntity();

            Dominio result = mapper.toDominio(entity);

            assertNotNull(result);
            assertEquals("12345678901", result.getIdDominio());
            assertEquals("Comune di Test", result.getRagioneSociale());
            assertEquals("ABCDE", result.getCbill());
        }

        @Test
        @DisplayName("Dominio con UO EC dovrebbe mappare anche indirizzo")
        void dominioWithUoEcShouldMapAddress() {
            it.govpay.portal.entity.Dominio entity = createDominioWithUoEc();

            Dominio result = mapper.toDominio(entity);

            assertNotNull(result);
            assertEquals("Via Roma", result.getIndirizzo());
            assertEquals("1", result.getCivico());
            assertEquals("00100", result.getCap());
            assertEquals("Roma", result.getLocalita());
            assertEquals("RM", result.getProvincia());
            assertEquals("IT", result.getNazione());
            assertEquals("comune@email.it", result.getEmail());
            assertEquals("comune@pec.it", result.getPec());
            assertEquals("0612345678", result.getTel());
            assertEquals("0612345679", result.getFax());
            assertEquals("https://www.comune.test.it", result.getWeb());
        }

        @Test
        @DisplayName("Dominio con UO non EC dovrebbe non mappare indirizzo")
        void dominioWithNonEcUoShouldNotMapAddress() {
            it.govpay.portal.entity.Dominio entity = createDominioWithNonEcUo();

            Dominio result = mapper.toDominio(entity);

            assertNotNull(result);
            assertNull(result.getIndirizzo());
            assertNull(result.getCivico());
        }

        @Test
        @DisplayName("Dominio senza UO dovrebbe funzionare")
        void dominioWithoutUoShouldWork() {
            it.govpay.portal.entity.Dominio entity = it.govpay.portal.entity.Dominio.builder()
                    .codDominio("12345678901")
                    .ragioneSociale("Comune di Test")
                    .unitaOrganizzative(new ArrayList<>())
                    .build();

            Dominio result = mapper.toDominio(entity);

            assertNotNull(result);
            assertEquals("12345678901", result.getIdDominio());
            assertNull(result.getIndirizzo());
        }
    }

    @Nested
    @DisplayName("toTipoPendenza Tests")
    class ToTipoPendenzaTests {

        @Test
        @DisplayName("Null entity dovrebbe restituire null")
        void nullEntityShouldReturnNull() {
            assertNull(mapper.toTipoPendenza(null));
        }

        @Test
        @DisplayName("TipoVersamentoDominio minimo dovrebbe essere mappato")
        void minimalTipoVersamentoDominioShouldBeMapped() {
            TipoVersamentoDominio entity = createMinimalTipoVersamentoDominio();

            TipoPendenza result = mapper.toTipoPendenza(entity);

            assertNotNull(result);
            assertEquals("TARI", result.getIdTipoPendenza());
            assertEquals("Tassa Rifiuti", result.getDescrizione());
            assertNull(result.getForm());
        }

        @Test
        @DisplayName("TipoVersamentoDominio senza TipoVersamento dovrebbe avere valori null")
        void tipoVersamentoDominioWithoutTipoVersamentoShouldHaveNullValues() {
            TipoVersamentoDominio entity = TipoVersamentoDominio.builder()
                    .tipoVersamento(null)
                    .build();

            TipoPendenza result = mapper.toTipoPendenza(entity);

            assertNotNull(result);
            assertNull(result.getIdTipoPendenza());
            assertNull(result.getDescrizione());
        }

        @Test
        @DisplayName("TipoVersamentoDominio con form proprio dovrebbe usare form dominio")
        void tipoVersamentoDominioWithOwnFormShouldUseDominioForm() {
            TipoVersamentoDominio entity = createTipoVersamentoDominioWithForm();

            TipoPendenza result = mapper.toTipoPendenza(entity);

            assertNotNull(result);
            assertNotNull(result.getForm());
            assertEquals("angular", result.getForm().getTipo());
        }

        @Test
        @DisplayName("TipoVersamentoDominio senza form proprio dovrebbe usare form TipoVersamento")
        void tipoVersamentoDominioWithoutOwnFormShouldUseTipoVersamentoForm() {
            TipoVersamento tipoVersamento = TipoVersamento.builder()
                    .codTipoVersamento("TARI")
                    .descrizione("Tassa Rifiuti")
                    .pagFormTipo("angular")
                    .pagFormDefinizione("{\"fields\":[]}")
                    .pagFormImpaginazione("{\"layout\":\"vertical\"}")
                    .build();

            TipoVersamentoDominio entity = TipoVersamentoDominio.builder()
                    .tipoVersamento(tipoVersamento)
                    .pagFormTipo(null)
                    .pagFormDefinizione(null)
                    .build();

            TipoPendenza result = mapper.toTipoPendenza(entity);

            assertNotNull(result);
            assertNotNull(result.getForm());
            assertEquals("angular", result.getForm().getTipo());
        }

        @Test
        @DisplayName("Form con JSON valido dovrebbe essere parsato")
        void formWithValidJsonShouldBeParsed() {
            TipoVersamentoDominio entity = TipoVersamentoDominio.builder()
                    .tipoVersamento(TipoVersamento.builder()
                            .codTipoVersamento("TARI")
                            .descrizione("Tassa Rifiuti")
                            .build())
                    .pagFormTipo("angular")
                    .pagFormDefinizione("{\"fields\":[{\"name\":\"importo\"}]}")
                    .pagFormImpaginazione("{\"layout\":\"vertical\"}")
                    .build();

            TipoPendenza result = mapper.toTipoPendenza(entity);

            assertNotNull(result);
            assertNotNull(result.getForm());
            assertNotNull(result.getForm().getDefinizione());
            assertNotNull(result.getForm().getImpaginazione());
        }

        @Test
        @DisplayName("Form con JSON non valido dovrebbe restituire stringa originale")
        void formWithInvalidJsonShouldReturnOriginalString() {
            TipoVersamentoDominio entity = TipoVersamentoDominio.builder()
                    .tipoVersamento(TipoVersamento.builder()
                            .codTipoVersamento("TARI")
                            .descrizione("Tassa Rifiuti")
                            .build())
                    .pagFormTipo("angular")
                    .pagFormDefinizione("invalid json")
                    .build();

            TipoPendenza result = mapper.toTipoPendenza(entity);

            assertNotNull(result);
            assertNotNull(result.getForm());
            assertEquals("invalid json", result.getForm().getDefinizione());
        }

        @Test
        @DisplayName("Form con stringa vuota dovrebbe restituire null")
        void formWithEmptyStringShouldReturnNull() {
            TipoVersamentoDominio entity = TipoVersamentoDominio.builder()
                    .tipoVersamento(TipoVersamento.builder()
                            .codTipoVersamento("TARI")
                            .descrizione("Tassa Rifiuti")
                            .build())
                    .pagFormTipo("angular")
                    .pagFormDefinizione("   ")
                    .build();

            TipoPendenza result = mapper.toTipoPendenza(entity);

            assertNotNull(result);
            assertNotNull(result.getForm());
            assertNull(result.getForm().getDefinizione());
        }

        @Test
        @DisplayName("Form con impaginazione null dovrebbe funzionare")
        void formWithNullImpaginazioneShouldWork() {
            TipoVersamentoDominio entity = TipoVersamentoDominio.builder()
                    .tipoVersamento(TipoVersamento.builder()
                            .codTipoVersamento("TARI")
                            .descrizione("Tassa Rifiuti")
                            .build())
                    .pagFormTipo("angular")
                    .pagFormDefinizione("{\"fields\":[]}")
                    .pagFormImpaginazione(null)
                    .build();

            TipoPendenza result = mapper.toTipoPendenza(entity);

            assertNotNull(result);
            assertNotNull(result.getForm());
            assertNull(result.getForm().getImpaginazione());
        }

        @Test
        @DisplayName("TipoVersamento con form ma senza pagFormTipo dominio dovrebbe usare form tipo versamento")
        void tipoVersamentoWithFormButNoPagFormTipoDominioShouldUseTipoVersamentoForm() {
            TipoVersamento tipoVersamento = TipoVersamento.builder()
                    .codTipoVersamento("IMU")
                    .descrizione("Imposta Municipale Unica")
                    .pagFormTipo("react")
                    .pagFormDefinizione("{\"type\":\"form\"}")
                    .build();

            TipoVersamentoDominio entity = TipoVersamentoDominio.builder()
                    .tipoVersamento(tipoVersamento)
                    .pagFormTipo(null)
                    .build();

            TipoPendenza result = mapper.toTipoPendenza(entity);

            assertNotNull(result);
            assertNotNull(result.getForm());
            assertEquals("react", result.getForm().getTipo());
        }
    }

    // Helper methods

    private it.govpay.portal.entity.Dominio createMinimalDominioEntity() {
        return it.govpay.portal.entity.Dominio.builder()
                .codDominio("12345678901")
                .ragioneSociale("Comune di Test")
                .cbill("ABCDE")
                .unitaOrganizzative(new ArrayList<>())
                .build();
    }

    private it.govpay.portal.entity.Dominio createDominioWithUoEc() {
        Uo uoEc = Uo.builder()
                .codUo("EC")
                .uoIndirizzo("Via Roma")
                .uoCivico("1")
                .uoCap("00100")
                .uoLocalita("Roma")
                .uoProvincia("RM")
                .uoNazione("IT")
                .uoEmail("comune@email.it")
                .uoPec("comune@pec.it")
                .uoTel("0612345678")
                .uoFax("0612345679")
                .uoUrlSitoWeb("https://www.comune.test.it")
                .build();

        return it.govpay.portal.entity.Dominio.builder()
                .codDominio("12345678901")
                .ragioneSociale("Comune di Test")
                .cbill("ABCDE")
                .unitaOrganizzative(List.of(uoEc))
                .build();
    }

    private it.govpay.portal.entity.Dominio createDominioWithNonEcUo() {
        Uo uoAltro = Uo.builder()
                .codUo("ALTRO")
                .uoIndirizzo("Via Milano")
                .build();

        return it.govpay.portal.entity.Dominio.builder()
                .codDominio("12345678901")
                .ragioneSociale("Comune di Test")
                .unitaOrganizzative(List.of(uoAltro))
                .build();
    }

    private TipoVersamentoDominio createMinimalTipoVersamentoDominio() {
        TipoVersamento tipoVersamento = TipoVersamento.builder()
                .codTipoVersamento("TARI")
                .descrizione("Tassa Rifiuti")
                .build();

        return TipoVersamentoDominio.builder()
                .tipoVersamento(tipoVersamento)
                .build();
    }

    private TipoVersamentoDominio createTipoVersamentoDominioWithForm() {
        TipoVersamento tipoVersamento = TipoVersamento.builder()
                .codTipoVersamento("TARI")
                .descrizione("Tassa Rifiuti")
                .build();

        return TipoVersamentoDominio.builder()
                .tipoVersamento(tipoVersamento)
                .pagFormTipo("angular")
                .pagFormDefinizione("{\"fields\":[{\"name\":\"importo\",\"type\":\"number\"}]}")
                .pagFormImpaginazione("{\"layout\":\"vertical\"}")
                .build();
    }
}
