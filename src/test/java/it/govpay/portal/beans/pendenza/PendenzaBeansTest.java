package it.govpay.portal.beans.pendenza;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test per i bean di pendenza (POJO).
 */
class PendenzaBeansTest {

    @Nested
    @DisplayName("Test MapEntry")
    class MapEntryTest {
        @Test
        void testGettersAndSetters() {
            MapEntry entry = new MapEntry();

            entry.setKey("chiave");
            assertEquals("chiave", entry.getKey());

            entry.setValue("valore");
            assertEquals("valore", entry.getValue());
        }

        @Test
        void testKeyMethod() {
            MapEntry entry = new MapEntry();
            MapEntry result = entry.key("testKey");
            assertSame(entry, result);
            assertEquals("testKey", entry.getKey());
        }

        @Test
        void testValueMethod() {
            MapEntry entry = new MapEntry();
            MapEntry result = entry.value("testValue");
            assertSame(entry, result);
            assertEquals("testValue", entry.getValue());
        }

        @Test
        void testEqualsAndHashCode() {
            MapEntry entry1 = new MapEntry();
            entry1.setKey("key");
            entry1.setValue("value");

            MapEntry entry2 = new MapEntry();
            entry2.setKey("key");
            entry2.setValue("value");

            assertEquals(entry1, entry2);
            assertEquals(entry1.hashCode(), entry2.hashCode());
        }

        @Test
        void testToString() {
            MapEntry entry = new MapEntry();
            entry.setKey("key");
            entry.setValue("value");
            assertNotNull(entry.toString());
        }
    }

    @Nested
    @DisplayName("Test Metadata")
    class MetadataTest {
        @Test
        void testGettersAndSetters() {
            Metadata metadata = new Metadata();

            List<MapEntry> entries = Arrays.asList(new MapEntry().key("k").value("v"));
            metadata.setMapEntries(entries);
            assertEquals(entries, metadata.getMapEntries());
        }

        @Test
        void testMapEntriesMethod() {
            Metadata metadata = new Metadata();
            List<MapEntry> entries = Arrays.asList(new MapEntry());
            Metadata result = metadata.mapEntries(entries);
            assertSame(metadata, result);
            assertEquals(entries, metadata.getMapEntries());
        }

        @Test
        void testEqualsAndHashCode() {
            Metadata m1 = new Metadata();
            Metadata m2 = new Metadata();
            assertEquals(m1, m2);
            assertEquals(m1.hashCode(), m2.hashCode());
            assertEquals(m1, m1);
            assertNotEquals(m1, null);
            assertNotEquals(m1, "string");
        }

        @Test
        void testToString() {
            Metadata metadata = new Metadata();
            metadata.setMapEntries(Arrays.asList(new MapEntry().key("k")));
            String result = metadata.toString();
            assertNotNull(result);
            assertTrue(result.contains("Metadata"));
        }
    }

    @Nested
    @DisplayName("Test VoceDescrizioneImporto")
    class VoceDescrizioneImportoTest {
        @Test
        void testGettersAndSetters() {
            VoceDescrizioneImporto voce = new VoceDescrizioneImporto();

            voce.setVoce("Etichetta");
            assertEquals("Etichetta", voce.getVoce());

            voce.setImporto(new BigDecimal("100.00"));
            assertEquals(new BigDecimal("100.00"), voce.getImporto());
        }

        @Test
        void testVoceFluentMethod() {
            VoceDescrizioneImporto voce = new VoceDescrizioneImporto();
            VoceDescrizioneImporto result = voce.voce("test");
            assertSame(voce, result);
            assertEquals("test", voce.getVoce());
        }

        @Test
        void testImportoFluentMethod() {
            VoceDescrizioneImporto voce = new VoceDescrizioneImporto();
            VoceDescrizioneImporto result = voce.importo(new BigDecimal("50"));
            assertSame(voce, result);
            assertEquals(new BigDecimal("50"), voce.getImporto());
        }

        @Test
        void testEqualsAndHashCode() {
            VoceDescrizioneImporto v1 = new VoceDescrizioneImporto();
            v1.setVoce("v");
            VoceDescrizioneImporto v2 = new VoceDescrizioneImporto();
            v2.setVoce("v");
            assertEquals(v1, v2);
            assertEquals(v1.hashCode(), v2.hashCode());
            assertEquals(v1, v1);
            assertNotEquals(v1, null);
            assertNotEquals(v1, "string");
        }

        @Test
        void testToString() {
            VoceDescrizioneImporto voce = new VoceDescrizioneImporto();
            voce.setVoce("test");
            String result = voce.toString();
            assertNotNull(result);
            assertTrue(result.contains("VoceDescrizioneImporto"));
            assertTrue(result.contains("test"));
        }
    }

    @Nested
    @DisplayName("Test VincoloPagamento")
    class VincoloPagamentoTest {
        @Test
        void testGettersAndSetters() {
            VincoloPagamento vincolo = new VincoloPagamento();

            vincolo.setTipo("ENTRO");
            assertEquals("ENTRO", vincolo.getTipo());

            vincolo.setTipoEnum(TipoSogliaVincoloPagamento.ENTRO);
            assertEquals(TipoSogliaVincoloPagamento.ENTRO, vincolo.getTipoEnum());

            vincolo.setGiorni(new BigDecimal("30"));
            assertEquals(new BigDecimal("30"), vincolo.getGiorni());
        }

        @Test
        void testTipoFluentMethod() {
            VincoloPagamento vincolo = new VincoloPagamento();
            VincoloPagamento result = vincolo.tipo("OLTRE");
            assertSame(vincolo, result);
            assertEquals("OLTRE", vincolo.getTipo());
        }

        @Test
        void testTipoEnumFluentMethod() {
            VincoloPagamento vincolo = new VincoloPagamento();
            VincoloPagamento result = vincolo.tipoEnum(TipoSogliaVincoloPagamento.OLTRE);
            assertSame(vincolo, result);
            assertEquals(TipoSogliaVincoloPagamento.OLTRE, vincolo.getTipoEnum());
        }

        @Test
        void testGiorniMethod() {
            VincoloPagamento vincolo = new VincoloPagamento();
            VincoloPagamento result = vincolo.giorni(new BigDecimal("15"));
            assertSame(vincolo, result);
            assertEquals(new BigDecimal("15"), vincolo.getGiorni());
        }

        @Test
        void testEqualsAndHashCode() {
            VincoloPagamento v1 = new VincoloPagamento();
            v1.setTipo("ENTRO");
            VincoloPagamento v2 = new VincoloPagamento();
            v2.setTipo("ENTRO");
            assertEquals(v1, v2);
            assertEquals(v1.hashCode(), v2.hashCode());
            assertEquals(v1, v1);
            assertNotEquals(v1, null);
            assertNotEquals(v1, "string");
        }

        @Test
        void testToString() {
            VincoloPagamento vincolo = new VincoloPagamento();
            vincolo.setTipo("ENTRO");
            String result = vincolo.toString();
            assertNotNull(result);
            assertTrue(result.contains("VincoloPagamento"));
            assertTrue(result.contains("ENTRO"));
        }
    }

    @Nested
    @DisplayName("Test TipoSogliaVincoloPagamento")
    class TipoSogliaVincoloPagamentoTest {
        @Test
        void testEnumValues() {
            assertEquals(4, TipoSogliaVincoloPagamento.values().length);
            assertNotNull(TipoSogliaVincoloPagamento.ENTRO);
            assertNotNull(TipoSogliaVincoloPagamento.OLTRE);
            assertNotNull(TipoSogliaVincoloPagamento.SCONTATO);
            assertNotNull(TipoSogliaVincoloPagamento.RIDOTTO);
        }

        @Test
        void testToStringValues() {
            assertEquals("ENTRO", TipoSogliaVincoloPagamento.ENTRO.toString());
            assertEquals("OLTRE", TipoSogliaVincoloPagamento.OLTRE.toString());
            assertEquals("SCONTATO", TipoSogliaVincoloPagamento.SCONTATO.toString());
            assertEquals("RIDOTTO", TipoSogliaVincoloPagamento.RIDOTTO.toString());
        }

        @Test
        void testFromValue() {
            assertEquals(TipoSogliaVincoloPagamento.ENTRO, TipoSogliaVincoloPagamento.fromValue("ENTRO"));
            assertEquals(TipoSogliaVincoloPagamento.OLTRE, TipoSogliaVincoloPagamento.fromValue("OLTRE"));
            assertEquals(TipoSogliaVincoloPagamento.SCONTATO, TipoSogliaVincoloPagamento.fromValue("SCONTATO"));
            assertEquals(TipoSogliaVincoloPagamento.RIDOTTO, TipoSogliaVincoloPagamento.fromValue("RIDOTTO"));
        }

        @Test
        void testFromValueInvalid() {
            assertNull(TipoSogliaVincoloPagamento.fromValue("INVALID"));
        }
    }

    @Nested
    @DisplayName("Test TassonomiaAvviso")
    class TassonomiaAvvisoTest {
        @Test
        void testEnumValues() {
            assertTrue(TassonomiaAvviso.values().length > 0);
        }

        @Test
        void testToStringNotNull() {
            for (TassonomiaAvviso t : TassonomiaAvviso.values()) {
                assertNotNull(t.toString());
            }
        }

        @Test
        void testFromValue() {
            for (TassonomiaAvviso t : TassonomiaAvviso.values()) {
                assertEquals(t, TassonomiaAvviso.fromValue(t.toString()));
            }
        }

        @Test
        void testFromValueInvalid() {
            assertNull(TassonomiaAvviso.fromValue("VALORE_INESISTENTE"));
        }

        @Test
        void testSpecificValues() {
            assertEquals("Diritti e concessioni", TassonomiaAvviso.DIRITTI_E_CONCESSIONI.toString());
            assertEquals("Imposte e tasse", TassonomiaAvviso.IMPOSTE_E_TASSE.toString());
        }
    }

    @Nested
    @DisplayName("Test LinguaSecondaria")
    class LinguaSecondariaTest {
        @Test
        void testEnumValues() {
            assertEquals(5, LinguaSecondaria.values().length);
        }

        @Test
        void testToStringValues() {
            assertEquals("de", LinguaSecondaria.DE.toString());
            assertEquals("en", LinguaSecondaria.EN.toString());
            assertEquals("fr", LinguaSecondaria.FR.toString());
            assertEquals("sl", LinguaSecondaria.SL.toString());
            assertEquals("false", LinguaSecondaria.FALSE.toString());
        }

        @Test
        void testFromValue() {
            assertEquals(LinguaSecondaria.DE, LinguaSecondaria.fromValue("de"));
            assertEquals(LinguaSecondaria.EN, LinguaSecondaria.fromValue("en"));
            assertEquals(LinguaSecondaria.FR, LinguaSecondaria.fromValue("fr"));
            assertEquals(LinguaSecondaria.SL, LinguaSecondaria.fromValue("sl"));
            assertEquals(LinguaSecondaria.FALSE, LinguaSecondaria.fromValue("false"));
        }

        @Test
        void testFromValueInvalid() {
            assertNull(LinguaSecondaria.fromValue("it"));
        }
    }

    @Nested
    @DisplayName("Test ProprietaPendenza")
    class ProprietaPendenzaTest {
        @Test
        void testGettersAndSetters() {
            ProprietaPendenza prop = new ProprietaPendenza();

            prop.setLinguaSecondaria(LinguaSecondaria.DE);
            assertEquals(LinguaSecondaria.DE, prop.getLinguaSecondaria());

            List<VoceDescrizioneImporto> voci = Arrays.asList(new VoceDescrizioneImporto());
            prop.setDescrizioneImporto(voci);
            assertEquals(voci, prop.getDescrizioneImporto());

            prop.setLineaTestoRicevuta1("Riga1");
            assertEquals("Riga1", prop.getLineaTestoRicevuta1());

            prop.setLineaTestoRicevuta2("Riga2");
            assertEquals("Riga2", prop.getLineaTestoRicevuta2());

            prop.setLinguaSecondariaCausale("CausaleDE");
            assertEquals("CausaleDE", prop.getLinguaSecondariaCausale());
        }

        @Test
        void testFluentMethods() {
            ProprietaPendenza prop = new ProprietaPendenza();

            ProprietaPendenza result = prop.linguaSecondaria(LinguaSecondaria.EN);
            assertSame(prop, result);

            result = prop.descrizioneImporto(Arrays.asList(new VoceDescrizioneImporto()));
            assertSame(prop, result);

            result = prop.lineaTestoRicevuta1("test1");
            assertSame(prop, result);

            result = prop.lineaTestoRicevuta2("test2");
            assertSame(prop, result);

            result = prop.linguaSecondariaCausale("causale");
            assertSame(prop, result);

            result = prop.informativaImportoAvviso("informativa");
            assertSame(prop, result);

            result = prop.linguaSecondariaInformativaImportoAvviso("informativa2");
            assertSame(prop, result);

            result = prop.dataScandenzaAvviso(new java.util.Date());
            assertSame(prop, result);
        }

        @Test
        void testAllGettersAndSetters() {
            ProprietaPendenza prop = new ProprietaPendenza();

            prop.setInformativaImportoAvviso("informativa");
            assertEquals("informativa", prop.getInformativaImportoAvviso());

            prop.setLinguaSecondariaInformativaImportoAvviso("infoSec");
            assertEquals("infoSec", prop.getLinguaSecondariaInformativaImportoAvviso());

            java.util.Date date = new java.util.Date();
            prop.setDataScandenzaAvviso(date);
            assertEquals(date, prop.getDataScandenzaAvviso());
        }

        @Test
        void testEqualsAndHashCode() {
            ProprietaPendenza p1 = new ProprietaPendenza();
            p1.setLinguaSecondaria(LinguaSecondaria.DE);
            ProprietaPendenza p2 = new ProprietaPendenza();
            p2.setLinguaSecondaria(LinguaSecondaria.DE);
            assertEquals(p1, p2);
            assertEquals(p1.hashCode(), p2.hashCode());
        }

        @Test
        void testToString() {
            ProprietaPendenza prop = new ProprietaPendenza();
            assertNotNull(prop.toString());
        }
    }

    @Nested
    @DisplayName("Test Soggetto")
    class SoggettoTest {
        @Test
        void testGettersAndSetters() {
            Soggetto soggetto = new Soggetto();

            soggetto.setTipo(Soggetto.TipoEnum.F);
            assertEquals(Soggetto.TipoEnum.F, soggetto.getTipo());

            soggetto.setIdentificativo("RSSMRA80A01H501U");
            assertEquals("RSSMRA80A01H501U", soggetto.getIdentificativo());

            soggetto.setAnagrafica("Mario Rossi");
            assertEquals("Mario Rossi", soggetto.getAnagrafica());

            soggetto.setIndirizzo("Via Roma 1");
            assertEquals("Via Roma 1", soggetto.getIndirizzo());

            soggetto.setCivico("10");
            assertEquals("10", soggetto.getCivico());

            soggetto.setCap("00100");
            assertEquals("00100", soggetto.getCap());

            soggetto.setLocalita("Roma");
            assertEquals("Roma", soggetto.getLocalita());

            soggetto.setProvincia("RM");
            assertEquals("RM", soggetto.getProvincia());

            soggetto.setNazione("IT");
            assertEquals("IT", soggetto.getNazione());

            soggetto.setEmail("mario.rossi@email.it");
            assertEquals("mario.rossi@email.it", soggetto.getEmail());

            soggetto.setCellulare("+39123456789");
            assertEquals("+39123456789", soggetto.getCellulare());
        }

        @Test
        void testFluentMethods() {
            Soggetto soggetto = new Soggetto();

            assertSame(soggetto, soggetto.tipo(Soggetto.TipoEnum.F));
            assertSame(soggetto, soggetto.identificativo("CF"));
            assertSame(soggetto, soggetto.anagrafica("Nome"));
            assertSame(soggetto, soggetto.indirizzo("Via"));
            assertSame(soggetto, soggetto.civico("1"));
            assertSame(soggetto, soggetto.cap("00100"));
            assertSame(soggetto, soggetto.localita("Roma"));
            assertSame(soggetto, soggetto.provincia("RM"));
            assertSame(soggetto, soggetto.nazione("IT"));
            assertSame(soggetto, soggetto.email("test@test.it"));
            assertSame(soggetto, soggetto.cellulare("123"));
        }

        @Test
        void testEqualsAndHashCode() {
            Soggetto s1 = new Soggetto();
            s1.setIdentificativo("CF");
            Soggetto s2 = new Soggetto();
            s2.setIdentificativo("CF");
            assertEquals(s1, s2);
            assertEquals(s1.hashCode(), s2.hashCode());
        }

        @Test
        void testToString() {
            Soggetto soggetto = new Soggetto();
            assertNotNull(soggetto.toString());
        }

        @Test
        void testTipoEnumFromValue() {
            assertEquals(Soggetto.TipoEnum.F, Soggetto.TipoEnum.fromValue("F"));
            assertEquals(Soggetto.TipoEnum.G, Soggetto.TipoEnum.fromValue("G"));
        }

        @Test
        void testTipoEnumInvalid() {
            assertNull(Soggetto.TipoEnum.fromValue("X"));
        }

        @Test
        void testTipoEnumToStringValue() {
            assertEquals("F", Soggetto.TipoEnum.F.toString());
            assertEquals("G", Soggetto.TipoEnum.G.toString());
        }

        @Test
        void testTipoEnumToString() {
            assertEquals("F", Soggetto.TipoEnum.F.toString());
        }
    }

    @Nested
    @DisplayName("Test VocePendenza")
    class VocePendenzaTest {
        @Test
        void testGettersAndSetters() {
            VocePendenza voce = new VocePendenza();

            voce.setIdVocePendenza("VOCE001");
            assertEquals("VOCE001", voce.getIdVocePendenza());

            voce.setImporto(new BigDecimal("100.50"));
            assertEquals(new BigDecimal("100.50"), voce.getImporto());

            voce.setDescrizione("Descrizione voce");
            assertEquals("Descrizione voce", voce.getDescrizione());

            voce.setCodEntrata("ENTRATA001");
            assertEquals("ENTRATA001", voce.getCodEntrata());

            voce.setTipoBollo("01");
            assertEquals("01", voce.getTipoBollo());

            voce.setHashDocumento("hash123");
            assertEquals("hash123", voce.getHashDocumento());

            voce.setProvinciaResidenza("RM");
            assertEquals("RM", voce.getProvinciaResidenza());

            voce.setIbanAccredito("IT60X0542811101000000123456");
            assertEquals("IT60X0542811101000000123456", voce.getIbanAccredito());

            voce.setIbanAppoggio("IT60X0542811101000000123457");
            assertEquals("IT60X0542811101000000123457", voce.getIbanAppoggio());

            voce.setTipoContabilita(TipoContabilita.CAPITOLO);
            assertEquals(TipoContabilita.CAPITOLO, voce.getTipoContabilita());

            voce.setCodiceContabilita("COD001");
            assertEquals("COD001", voce.getCodiceContabilita());

            voce.setDescrizioneCausaleRPT("Causale RPT");
            assertEquals("Causale RPT", voce.getDescrizioneCausaleRPT());

            voce.setIdDominio("01234567890");
            assertEquals("01234567890", voce.getIdDominio());

            voce.setIndice(new BigDecimal("1"));
            assertEquals(new BigDecimal("1"), voce.getIndice());

            voce.setStato(VocePendenza.StatoEnum.ESEGUITO);
            assertEquals(VocePendenza.StatoEnum.ESEGUITO, voce.getStato());

            voce.setDatiAllegati("datiAllegati");
            assertEquals("datiAllegati", voce.getDatiAllegati());

            voce.setContabilita(new Contabilita());
            assertNotNull(voce.getContabilita());

            voce.setMetadata(new Metadata());
            assertNotNull(voce.getMetadata());
        }

        @Test
        void testFluentMethods() {
            VocePendenza voce = new VocePendenza();

            assertSame(voce, voce.indice(new BigDecimal("1")));
            assertSame(voce, voce.idVocePendenza("VOCE001"));
            assertSame(voce, voce.importo(new BigDecimal("100")));
            assertSame(voce, voce.descrizione("desc"));
            assertSame(voce, voce.stato(VocePendenza.StatoEnum.ESEGUITO));
            assertSame(voce, voce.datiAllegati("dati"));
            assertSame(voce, voce.descrizioneCausaleRPT("causale"));
            assertSame(voce, voce.contabilita(new Contabilita()));
            assertSame(voce, voce.metadata(new Metadata()));
            assertSame(voce, voce.idDominio("01234567890"));
            assertSame(voce, voce.hashDocumento("hash"));
            assertSame(voce, voce.tipoBollo("01"));
            assertSame(voce, voce.codEntrata("ENTRATA"));
            assertSame(voce, voce.provinciaResidenza("RM"));
            assertSame(voce, voce.codiceContabilita("COD001"));
            assertSame(voce, voce.ibanAccredito("IT60X0542811101000000123456"));
            assertSame(voce, voce.ibanAppoggio("IT60X0542811101000000123457"));
            assertSame(voce, voce.tipoContabilita(TipoContabilita.CAPITOLO));
        }

        @Test
        void testEqualsAndHashCode() {
            VocePendenza v1 = new VocePendenza();
            v1.setIdVocePendenza("VOCE001");
            v1.setImporto(new BigDecimal("100"));

            VocePendenza v2 = new VocePendenza();
            v2.setIdVocePendenza("VOCE001");
            v2.setImporto(new BigDecimal("100"));

            assertEquals(v1, v2);
            assertEquals(v1.hashCode(), v2.hashCode());
            assertEquals(v1, v1);
            assertNotEquals(v1, null);
            assertNotEquals(v1, "string");
        }

        @Test
        void testToString() {
            VocePendenza voce = new VocePendenza();
            voce.setIdVocePendenza("VOCE001");
            String result = voce.toString();
            assertNotNull(result);
            assertTrue(result.contains("VocePendenza"));
            assertTrue(result.contains("VOCE001"));
        }

        @Test
        void testStatoEnumFromValue() {
            assertEquals(VocePendenza.StatoEnum.ESEGUITO, VocePendenza.StatoEnum.fromValue("Eseguito"));
            assertEquals(VocePendenza.StatoEnum.NON_ESEGUITO, VocePendenza.StatoEnum.fromValue("Non eseguito"));
            assertEquals(VocePendenza.StatoEnum.ANOMALO, VocePendenza.StatoEnum.fromValue("Anomalo"));
            assertNull(VocePendenza.StatoEnum.fromValue("Invalid"));
        }

        @Test
        void testStatoEnumToString() {
            assertEquals("Eseguito", VocePendenza.StatoEnum.ESEGUITO.toString());
            assertEquals("Non eseguito", VocePendenza.StatoEnum.NON_ESEGUITO.toString());
            assertEquals("Anomalo", VocePendenza.StatoEnum.ANOMALO.toString());
        }
    }

    @Nested
    @DisplayName("Test PendenzaPost")
    class PendenzaPostTest {
        @Test
        void testGettersAndSetters() {
            PendenzaPost pendenza = new PendenzaPost();

            pendenza.setIdA2A("APP001");
            assertEquals("APP001", pendenza.getIdA2A());

            pendenza.setIdPendenza("PEND001");
            assertEquals("PEND001", pendenza.getIdPendenza());

            pendenza.setIdDominio("01234567890");
            assertEquals("01234567890", pendenza.getIdDominio());

            pendenza.setIdUnitaOperativa("UO001");
            assertEquals("UO001", pendenza.getIdUnitaOperativa());

            pendenza.setIdTipoPendenza("TIPO001");
            assertEquals("TIPO001", pendenza.getIdTipoPendenza());

            pendenza.setNome("Nome pendenza");
            assertEquals("Nome pendenza", pendenza.getNome());

            pendenza.setCausale("Causale pendenza");
            assertEquals("Causale pendenza", pendenza.getCausale());

            pendenza.setImporto(new BigDecimal("150.50"));
            assertEquals(new BigDecimal("150.50"), pendenza.getImporto());

            pendenza.setNumeroAvviso("301000000000000001");
            assertEquals("301000000000000001", pendenza.getNumeroAvviso());

            pendenza.setAnnoRiferimento(new BigDecimal("2024"));
            assertEquals(new BigDecimal("2024"), pendenza.getAnnoRiferimento());

            pendenza.setCartellaPagamento("CART001");
            assertEquals("CART001", pendenza.getCartellaPagamento());

            pendenza.setDirezione("DIR001");
            assertEquals("DIR001", pendenza.getDirezione());

            pendenza.setDivisione("DIV001");
            assertEquals("DIV001", pendenza.getDivisione());

            pendenza.setTassonomia("TASS001");
            assertEquals("TASS001", pendenza.getTassonomia());

            pendenza.setSoggettoPagatore(new Soggetto());
            assertNotNull(pendenza.getSoggettoPagatore());

            pendenza.setDocumento(new Documento());
            assertNotNull(pendenza.getDocumento());

            pendenza.setVoci(Arrays.asList(new VocePendenza()));
            assertNotNull(pendenza.getVoci());
            assertEquals(1, pendenza.getVoci().size());

            pendenza.setAllegati(Arrays.asList(new NuovoAllegatoPendenza()));
            assertNotNull(pendenza.getAllegati());

            pendenza.setDataCaricamento(new java.util.Date());
            assertNotNull(pendenza.getDataCaricamento());

            pendenza.setDataValidita(new java.util.Date());
            assertNotNull(pendenza.getDataValidita());

            pendenza.setDataScadenza(new java.util.Date());
            assertNotNull(pendenza.getDataScadenza());

            pendenza.setDatiAllegati("datiAllegati");
            assertEquals("datiAllegati", pendenza.getDatiAllegati());

            pendenza.setProprieta(new ProprietaPendenza());
            assertNotNull(pendenza.getProprieta());
        }

        @Test
        void testFluentMethods() {
            PendenzaPost pendenza = new PendenzaPost();

            assertSame(pendenza, pendenza.idDominio("01234567890"));
            assertSame(pendenza, pendenza.idUnitaOperativa("UO001"));
            assertSame(pendenza, pendenza.idTipoPendenza("TIPO001"));
            assertSame(pendenza, pendenza.nome("Nome"));
            assertSame(pendenza, pendenza.causale("Causale"));
            assertSame(pendenza, pendenza.soggettoPagatore(new Soggetto()));
            assertSame(pendenza, pendenza.importo(new BigDecimal("100")));
            assertSame(pendenza, pendenza.numeroAvviso("301000000000000001"));
            assertSame(pendenza, pendenza.dataCaricamento(new java.util.Date()));
            assertSame(pendenza, pendenza.dataValidita(new java.util.Date()));
            assertSame(pendenza, pendenza.dataScadenza(new java.util.Date()));
            assertSame(pendenza, pendenza.annoRiferimento(new BigDecimal("2024")));
            assertSame(pendenza, pendenza.cartellaPagamento("CART001"));
            assertSame(pendenza, pendenza.datiAllegati("dati"));
            assertSame(pendenza, pendenza.tassonomia("TASS001"));
            assertSame(pendenza, pendenza.tassonomiaAvviso(TassonomiaAvviso.DIRITTI_E_CONCESSIONI));
            assertSame(pendenza, pendenza.direzione("DIR001"));
            assertSame(pendenza, pendenza.divisione("DIV001"));
            assertSame(pendenza, pendenza.documento(new Documento()));
            assertSame(pendenza, pendenza.proprieta(new ProprietaPendenza()));
            assertSame(pendenza, pendenza.voci(Arrays.asList(new VocePendenza())));
            assertSame(pendenza, pendenza.idA2A("APP001"));
            assertSame(pendenza, pendenza.idPendenza("PEND001"));
            assertSame(pendenza, pendenza.allegati(Arrays.asList(new NuovoAllegatoPendenza())));
        }

        @Test
        void testTassonomiaAvvisoMethods() {
            PendenzaPost pendenza = new PendenzaPost();

            // Test set with enum
            pendenza.setTassonomiaAvviso(TassonomiaAvviso.DIRITTI_E_CONCESSIONI);
            assertEquals(TassonomiaAvviso.DIRITTI_E_CONCESSIONI, pendenza.getTassonomiaAvvisoEnum());
            assertNotNull(pendenza.getTassonomiaAvviso());

            // Test set with string
            pendenza.setTassonomiaAvviso("Servizi erogati dal comune");
            assertEquals(TassonomiaAvviso.SERVIZI_EROGATI_DAL_COMUNE, pendenza.getTassonomiaAvvisoEnum());

            // Test null handling with enum setter
            pendenza.setTassonomiaAvviso((TassonomiaAvviso) null);
            assertNull(pendenza.getTassonomiaAvvisoEnum());
            assertNull(pendenza.getTassonomiaAvviso());

            // Test that string null doesn't change existing null
            PendenzaPost pendenza2 = new PendenzaPost();
            pendenza2.setTassonomiaAvviso((String) null);
            assertNull(pendenza2.getTassonomiaAvviso());
        }

        @Test
        void testEqualsAndHashCode() {
            PendenzaPost p1 = new PendenzaPost();
            p1.setIdDominio("01234567890");
            p1.setIdPendenza("PEND001");

            PendenzaPost p2 = new PendenzaPost();
            p2.setIdDominio("01234567890");
            p2.setIdPendenza("PEND001");

            assertEquals(p1, p2);
            assertEquals(p1.hashCode(), p2.hashCode());
            assertEquals(p1, p1);
            assertNotEquals(p1, null);
            assertNotEquals(p1, "string");
        }

        @Test
        void testToString() {
            PendenzaPost pendenza = new PendenzaPost();
            pendenza.setIdDominio("01234567890");
            pendenza.setIdPendenza("PEND001");
            String result = pendenza.toString();
            assertNotNull(result);
            assertTrue(result.contains("PendenzaPost"));
            assertTrue(result.contains("01234567890"));
        }
    }

    @Nested
    @DisplayName("Test Documento")
    class DocumentoTest {
        @Test
        void testGettersAndSetters() {
            Documento documento = new Documento();

            documento.setIdentificativo("DOC001");
            assertEquals("DOC001", documento.getIdentificativo());

            documento.setDescrizione("Descrizione documento");
            assertEquals("Descrizione documento", documento.getDescrizione());

            documento.setRata(BigDecimal.valueOf(3));
            assertEquals(BigDecimal.valueOf(3), documento.getRata());

            documento.setSoglia(new VincoloPagamento());
            assertNotNull(documento.getSoglia());
        }

        @Test
        void testFluentMethods() {
            Documento documento = new Documento();

            assertSame(documento, documento.identificativo("DOC001"));
            assertSame(documento, documento.descrizione("Descrizione"));
            assertSame(documento, documento.rata(BigDecimal.valueOf(3)));
            assertSame(documento, documento.soglia(new VincoloPagamento()));

            assertEquals("DOC001", documento.getIdentificativo());
            assertEquals("Descrizione", documento.getDescrizione());
        }

        @Test
        void testEqualsAndHashCode() {
            Documento d1 = new Documento();
            d1.setIdentificativo("DOC001");
            d1.setDescrizione("Descrizione");

            Documento d2 = new Documento();
            d2.setIdentificativo("DOC001");
            d2.setDescrizione("Descrizione");

            assertEquals(d1, d2);
            assertEquals(d1.hashCode(), d2.hashCode());
            assertEquals(d1, d1);
            assertNotEquals(d1, null);
            assertNotEquals(d1, "string");
        }

        @Test
        void testToString() {
            Documento documento = new Documento();
            documento.setIdentificativo("DOC001");
            String result = documento.toString();
            assertNotNull(result);
            assertTrue(result.contains("Documento"));
            assertTrue(result.contains("DOC001"));
        }
    }

    @Nested
    @DisplayName("Test NuovoAllegatoPendenza")
    class NuovoAllegatoPendenzaTest {
        @Test
        void testGettersAndSetters() {
            NuovoAllegatoPendenza allegato = new NuovoAllegatoPendenza();

            allegato.setNome("documento.pdf");
            assertEquals("documento.pdf", allegato.getNome());

            allegato.setTipo("application/pdf");
            assertEquals("application/pdf", allegato.getTipo());

            allegato.setDescrizione("Allegato di prova");
            assertEquals("Allegato di prova", allegato.getDescrizione());

            byte[] contenuto = new byte[]{1, 2, 3};
            allegato.setContenuto(contenuto);
            assertArrayEquals(contenuto, allegato.getContenuto());
        }

        @Test
        void testDefaultTipo() {
            NuovoAllegatoPendenza allegato = new NuovoAllegatoPendenza();
            assertEquals("application/octet-stream", allegato.getTipo());
        }

        @Test
        void testFluentMethods() {
            NuovoAllegatoPendenza allegato = new NuovoAllegatoPendenza();

            assertSame(allegato, allegato.nome("file.pdf"));
            assertSame(allegato, allegato.tipo("application/pdf"));
            assertSame(allegato, allegato.descrizione("Descrizione"));
            assertSame(allegato, allegato.contenuto(new byte[]{1, 2, 3}));

            assertEquals("file.pdf", allegato.getNome());
            assertEquals("application/pdf", allegato.getTipo());
            assertEquals("Descrizione", allegato.getDescrizione());
        }

        @Test
        void testToString() {
            NuovoAllegatoPendenza allegato = new NuovoAllegatoPendenza();
            allegato.setNome("file.pdf");
            String result = allegato.toString();
            assertNotNull(result);
            assertTrue(result.contains("NuovoAllegatoPendenza"));
            assertTrue(result.contains("file.pdf"));
        }

        @Test
        void testToStringWithNull() {
            NuovoAllegatoPendenza allegato = new NuovoAllegatoPendenza();
            String result = allegato.toString();
            assertNotNull(result);
            assertTrue(result.contains("null"));
        }
    }

    @Nested
    @DisplayName("Test TipoContabilita")
    class TipoContabilitaTest {
        @Test
        void testEnumValues() {
            assertNotNull(TipoContabilita.values());
            assertTrue(TipoContabilita.values().length > 0);
        }

        @Test
        void testFromValue() {
            assertEquals(TipoContabilita.CAPITOLO, TipoContabilita.fromValue("CAPITOLO"));
            assertEquals(TipoContabilita.SIOPE, TipoContabilita.fromValue("SIOPE"));
        }

        @Test
        void testFromValueInvalid() {
            // fromValue restituisce null per valori non validi
            assertNull(TipoContabilita.fromValue("INVALID"));
        }
    }

    @Nested
    @DisplayName("Test Contabilita")
    class ContabilitaTest {
        @Test
        void testGettersAndSetters() {
            Contabilita contabilita = new Contabilita();

            contabilita.setQuote(Arrays.asList(new QuotaContabilita()));
            assertNotNull(contabilita.getQuote());
            assertEquals(1, contabilita.getQuote().size());

            contabilita.setProprietaCustom("custom");
            assertEquals("custom", contabilita.getProprietaCustom());
        }

        @Test
        void testFluentMethods() {
            Contabilita contabilita = new Contabilita();

            assertSame(contabilita, contabilita.quote(Arrays.asList(new QuotaContabilita())));
            assertSame(contabilita, contabilita.proprietaCustom("custom"));

            assertEquals("custom", contabilita.getProprietaCustom());
        }

        @Test
        void testEqualsAndHashCode() {
            Contabilita c1 = new Contabilita();
            c1.setProprietaCustom("custom");

            Contabilita c2 = new Contabilita();
            c2.setProprietaCustom("custom");

            assertEquals(c1, c2);
            assertEquals(c1.hashCode(), c2.hashCode());
            assertEquals(c1, c1);
            assertNotEquals(c1, null);
            assertNotEquals(c1, "string");
        }

        @Test
        void testToString() {
            Contabilita contabilita = new Contabilita();
            contabilita.setProprietaCustom("custom");
            String result = contabilita.toString();
            assertNotNull(result);
            assertTrue(result.contains("Contabilita"));
            assertTrue(result.contains("custom"));
        }
    }

    @Nested
    @DisplayName("Test QuotaContabilita")
    class QuotaContabilitaTest {
        @Test
        void testGettersAndSetters() {
            QuotaContabilita quota = new QuotaContabilita();

            quota.setCapitolo("CAP001");
            assertEquals("CAP001", quota.getCapitolo());

            quota.setAnnoEsercizio(new BigDecimal("2024"));
            assertEquals(new BigDecimal("2024"), quota.getAnnoEsercizio());

            quota.setImporto(new BigDecimal("150.50"));
            assertEquals(new BigDecimal("150.50"), quota.getImporto());

            quota.setAccertamento("ACC001");
            assertEquals("ACC001", quota.getAccertamento());

            quota.setProprietaCustom("custom");
            assertEquals("custom", quota.getProprietaCustom());

            quota.setTitolo("TITOLO001");
            assertEquals("TITOLO001", quota.getTitolo());

            quota.setTipologia("TIPOLOGIA001");
            assertEquals("TIPOLOGIA001", quota.getTipologia());

            quota.setCategoria("CATEGORIA001");
            assertEquals("CATEGORIA001", quota.getCategoria());

            quota.setArticolo("ARTICOLO001");
            assertEquals("ARTICOLO001", quota.getArticolo());
        }

        @Test
        void testFluentMethods() {
            QuotaContabilita quota = new QuotaContabilita();

            assertSame(quota, quota.capitolo("CAP001"));
            assertSame(quota, quota.annoEsercizio(new BigDecimal("2024")));
            assertSame(quota, quota.importo(new BigDecimal("100")));
            assertSame(quota, quota.accertamento("ACC001"));
            assertSame(quota, quota.proprietaCustom("custom"));
            assertSame(quota, quota.titolo("TITOLO001"));
            assertSame(quota, quota.tipologia("TIPOLOGIA001"));
            assertSame(quota, quota.categoria("CATEGORIA001"));
            assertSame(quota, quota.articolo("ARTICOLO001"));

            assertEquals("CAP001", quota.getCapitolo());
            assertEquals("TITOLO001", quota.getTitolo());
        }

        @Test
        void testEqualsAndHashCode() {
            QuotaContabilita q1 = new QuotaContabilita();
            q1.setCapitolo("CAP001");
            q1.setImporto(new BigDecimal("100"));

            QuotaContabilita q2 = new QuotaContabilita();
            q2.setCapitolo("CAP001");
            q2.setImporto(new BigDecimal("100"));

            assertEquals(q1, q2);
            assertEquals(q1.hashCode(), q2.hashCode());
            assertEquals(q1, q1);
            assertNotEquals(q1, null);
            assertNotEquals(q1, "string");
        }

        @Test
        void testToString() {
            QuotaContabilita quota = new QuotaContabilita();
            quota.setCapitolo("CAP001");
            String result = quota.toString();
            assertNotNull(result);
            assertTrue(result.contains("QuotaContabilita"));
            assertTrue(result.contains("CAP001"));
        }
    }

    @Nested
    @DisplayName("Test TipoContabilita enum completo")
    class TipoContabilitaEnumTest {
        @Test
        void testAllEnumValues() {
            for (TipoContabilita tipo : TipoContabilita.values()) {
                assertNotNull(tipo.toString());
                assertEquals(tipo, TipoContabilita.fromValue(tipo.toString()));
            }
        }

        @Test
        void testSpecificValues() {
            assertEquals("CAPITOLO", TipoContabilita.CAPITOLO.toString());
            assertEquals("SIOPE", TipoContabilita.SIOPE.toString());
            assertEquals("SPECIALE", TipoContabilita.SPECIALE.toString());
            assertEquals("ALTRO", TipoContabilita.ALTRO.toString());
        }
    }
}
