package it.govpay.portal.mapper;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import it.govpay.pendenze.client.model.Contabilita;
import it.govpay.pendenze.client.model.LinguaSecondaria;
import it.govpay.pendenze.client.model.MapEntry;
import it.govpay.pendenze.client.model.Metadata;
import it.govpay.pendenze.client.model.NuovaPendenza;
import it.govpay.pendenze.client.model.NuovaVocePendenza;
import it.govpay.pendenze.client.model.NuovoAllegatoPendenza;
import it.govpay.pendenze.client.model.NuovoDocumento;
import it.govpay.pendenze.client.model.ProprietaPendenza;
import it.govpay.pendenze.client.model.QuotaContabilita;
import it.govpay.pendenze.client.model.Soggetto;
import it.govpay.pendenze.client.model.TassonomiaAvviso;
import it.govpay.pendenze.client.model.TipoContabilita;
import it.govpay.pendenze.client.model.TipoSoggetto;
import it.govpay.pendenze.client.model.TipoSogliaVincoloPagamento;
import it.govpay.pendenze.client.model.VincoloPagamento;
import it.govpay.pendenze.client.model.VoceDescrizioneImporto;
import it.govpay.portal.beans.pendenza.PendenzaPost;
import it.govpay.portal.beans.pendenza.VocePendenza;

/**
 * Mapper per convertire PendenzaPost (bean interno) in NuovaPendenza (bean client GovPay).
 */
@Component
public class PendenzaPostMapper {

    /**
     * Converte PendenzaPost in NuovaPendenza per l'invio a GovPay.
     */
    public NuovaPendenza toNuovaPendenza(PendenzaPost source) {
        if (source == null) {
            return null;
        }

        NuovaPendenza target = new NuovaPendenza();

        target.setIdDominio(source.getIdDominio());
        target.setIdTipoPendenza(source.getIdTipoPendenza());
        target.setIdUnitaOperativa(source.getIdUnitaOperativa());
        target.setCausale(source.getCausale());
        if(source.getSoggettoPagatore() != null) {
        	target.setSoggettoPagatore(toClientSoggetto(source.getSoggettoPagatore()));
        }

        if (source.getImporto() != null) {
            target.setImporto(source.getImporto().doubleValue());
        }

        target.setNumeroAvviso(source.getNumeroAvviso());
        target.setTassonomia(source.getTassonomia());
        target.setTassonomiaAvviso(toClientTassonomiaAvviso(source.getTassonomiaAvvisoEnum()));
        target.setDirezione(source.getDirezione());
        target.setDivisione(source.getDivisione());
        target.setDataValidita(toLocalDate(source.getDataValidita()));
        target.setDataScadenza(toLocalDate(source.getDataScadenza()));

        if (source.getAnnoRiferimento() != null) {
            target.setAnnoRiferimento(source.getAnnoRiferimento().intValue());
        }

        target.setCartellaPagamento(source.getCartellaPagamento());
        target.setDatiAllegati(source.getDatiAllegati());
        target.setDocumento(toClientDocumento(source.getDocumento()));
        target.setProprieta(toClientProprieta(source.getProprieta()));
        target.setVoci(toClientVoci(source.getVoci()));
        
        if(source.getAllegati() != null && !source.getAllegati().isEmpty()) {
        	target.setAllegati(toClientAllegati(source.getAllegati()));
        }

        return target;
    }

    private Soggetto toClientSoggetto(it.govpay.portal.beans.pendenza.Soggetto source) {
        Soggetto target = new Soggetto();

        if (source.getTipo() != null) {
            target.setTipo(TipoSoggetto.fromValue(source.getTipo().toString()));
        }

        target.setIdentificativo(source.getIdentificativo());
        target.setAnagrafica(source.getAnagrafica());
        target.setIndirizzo(source.getIndirizzo());
        target.setCivico(source.getCivico());
        target.setCap(source.getCap());
        target.setLocalita(source.getLocalita());
        target.setProvincia(source.getProvincia());
        target.setNazione(source.getNazione());
        target.setEmail(source.getEmail());
        target.setCellulare(source.getCellulare());

        return target;
    }

    private TassonomiaAvviso toClientTassonomiaAvviso(it.govpay.portal.beans.pendenza.TassonomiaAvviso source) {
        if (source == null) {
            return null;
        }
        return TassonomiaAvviso.fromValue(source.toString());
    }

    private NuovoDocumento toClientDocumento(it.govpay.portal.beans.pendenza.Documento source) {
        if (source == null) {
            return null;
        }

        NuovoDocumento target = new NuovoDocumento();
        target.setIdentificativo(source.getIdentificativo());
        target.setDescrizione(source.getDescrizione());

        if (source.getRata() != null) {
            target.setRata(source.getRata().intValue());
        }

        if (source.getSoglia() != null) {
            VincoloPagamento vincolo = new VincoloPagamento();
            if (source.getSoglia().getGiorni() != null) {
                vincolo.setGiorni(source.getSoglia().getGiorni().intValue());
            }
            if (source.getSoglia().getTipo() != null) {
                vincolo.setTipo(TipoSogliaVincoloPagamento.fromValue(source.getSoglia().getTipo()));
            }
            target.setSoglia(vincolo);
        }

        return target;
    }

    private ProprietaPendenza toClientProprieta(it.govpay.portal.beans.pendenza.ProprietaPendenza source) {
        if (source == null) {
            return null;
        }

        ProprietaPendenza target = new ProprietaPendenza();

        if (source.getLinguaSecondaria() != null) {
            target.setLinguaSecondaria(LinguaSecondaria.fromValue(source.getLinguaSecondaria().toString()));
        }

        if (source.getDescrizioneImporto() != null) {
            List<VoceDescrizioneImporto> voci = new ArrayList<>();
            for (it.govpay.portal.beans.pendenza.VoceDescrizioneImporto v : source.getDescrizioneImporto()) {
                VoceDescrizioneImporto voce = new VoceDescrizioneImporto();
                voce.setVoce(v.getVoce());
                if (v.getImporto() != null) {
                    voce.setImporto(v.getImporto().doubleValue());
                }
                voci.add(voce);
            }
            target.setDescrizioneImporto(voci);
        }

        target.setLineaTestoRicevuta1(source.getLineaTestoRicevuta1());
        target.setLineaTestoRicevuta2(source.getLineaTestoRicevuta2());
        target.setLinguaSecondariaCausale(source.getLinguaSecondariaCausale());
        target.setInformativaImportoAvviso(source.getInformativaImportoAvviso());
        target.setLinguaSecondariaInformativaImportoAvviso(source.getLinguaSecondariaInformativaImportoAvviso());

        return target;
    }

    private List<NuovaVocePendenza> toClientVoci(List<VocePendenza> source) {
        if (source == null || source.isEmpty()) {
            return new ArrayList<>();
        }

        List<NuovaVocePendenza> result = new ArrayList<>();
        for (VocePendenza v : source) {
            result.add(toClientVoce(v));
        }
        return result;
    }

    private NuovaVocePendenza toClientVoce(VocePendenza source) {
        if (source == null) {
            return null;
        }

        NuovaVocePendenza target = new NuovaVocePendenza();

        target.setIdVocePendenza(source.getIdVocePendenza());

        if (source.getImporto() != null) {
            target.setImporto(source.getImporto().doubleValue());
        }

        target.setDescrizione(source.getDescrizione());
        target.setDatiAllegati(source.getDatiAllegati());
        target.setDescrizioneCausaleRPT(source.getDescrizioneCausaleRPT());
        target.setIdDominio(source.getIdDominio());
        target.setCodEntrata(source.getCodEntrata());
        target.setIbanAccredito(source.getIbanAccredito());
        target.setIbanAppoggio(source.getIbanAppoggio());
        target.setCodiceContabilita(source.getCodiceContabilita());
        target.setHashDocumento(source.getHashDocumento());
        target.setProvinciaResidenza(source.getProvinciaResidenza());

        if (source.getTipoContabilita() != null) {
            target.setTipoContabilita(TipoContabilita.fromValue(source.getTipoContabilita().toString()));
        }

        if (source.getTipoBollo() != null) {
            target.setTipoBollo(NuovaVocePendenza.TipoBolloEnum.fromValue(source.getTipoBollo()));
        }

        target.setContabilita(toClientContabilita(source.getContabilita()));
        target.setMetadata(toClientMetadata(source.getMetadata()));

        return target;
    }

    private Contabilita toClientContabilita(it.govpay.portal.beans.pendenza.Contabilita source) {
        if (source == null) {
            return null;
        }

        Contabilita target = new Contabilita();
        target.setProprietaCustom(source.getProprietaCustom());

        if (source.getQuote() != null) {
            List<QuotaContabilita> quote = new ArrayList<>();
            for (it.govpay.portal.beans.pendenza.QuotaContabilita q : source.getQuote()) {
                QuotaContabilita quota = new QuotaContabilita();
                quota.setCapitolo(q.getCapitolo());
                if (q.getAnnoEsercizio() != null) {
                    quota.setAnnoEsercizio(q.getAnnoEsercizio().intValue());
                }
                if (q.getImporto() != null) {
                    quota.setImporto(q.getImporto().doubleValue());
                }
                quota.setAccertamento(q.getAccertamento());
                quota.setProprietaCustom(q.getProprietaCustom());
                quota.setTitolo(q.getTitolo());
                quota.setTipologia(q.getTipologia());
                quota.setCategoria(q.getCategoria());
                quota.setArticolo(q.getArticolo());
                quote.add(quota);
            }
            target.setQuote(quote);
        }

        return target;
    }

    private Metadata toClientMetadata(it.govpay.portal.beans.pendenza.Metadata source) {
        if (source == null) {
            return null;
        }

        Metadata target = new Metadata();

        if (source.getMapEntries() != null) {
            List<MapEntry> entries = new ArrayList<>();
            for (it.govpay.portal.beans.pendenza.MapEntry e : source.getMapEntries()) {
                MapEntry entry = new MapEntry();
                entry.setKey(e.getKey());
                entry.setValue(e.getValue());
                entries.add(entry);
            }
            target.setMapEntries(entries);
        }

        return target;
    }

    private List<NuovoAllegatoPendenza> toClientAllegati(List<it.govpay.portal.beans.pendenza.NuovoAllegatoPendenza> source) {
        List<NuovoAllegatoPendenza> result = new ArrayList<>();
        for (it.govpay.portal.beans.pendenza.NuovoAllegatoPendenza a : source) {
            NuovoAllegatoPendenza allegato = new NuovoAllegatoPendenza();
            allegato.setNome(a.getNome());
            allegato.setTipo(a.getTipo());
            allegato.setDescrizione(a.getDescrizione());
            allegato.setContenuto(a.getContenuto());
            result.add(allegato);
        }
        return result;
    }

    private LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
