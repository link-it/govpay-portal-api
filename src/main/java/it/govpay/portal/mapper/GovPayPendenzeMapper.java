package it.govpay.portal.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import it.govpay.pendenze.client.model.NuovaPendenza;
import it.govpay.pendenze.client.model.NuovaVocePendenza;
import it.govpay.pendenze.client.model.Soggetto;
import it.govpay.pendenze.client.model.TassonomiaAvviso;
import it.govpay.pendenze.client.model.TipoContabilita;
import it.govpay.pendenze.client.model.TipoSoggetto;
import it.govpay.portal.entity.SingoloVersamento;
import it.govpay.portal.entity.Versamento;

@Component
public class GovPayPendenzeMapper {

    public NuovaPendenza toNuovaPendenza(Versamento versamento) {
        if (versamento == null) {
            return null;
        }

        NuovaPendenza pendenza = new NuovaPendenza();

        pendenza.setIdDominio(versamento.getDominio() != null
                ? versamento.getDominio().getCodDominio()
                : null);

        pendenza.setIdTipoPendenza(versamento.getTipoVersamento() != null
                ? versamento.getTipoVersamento().getCodTipoVersamento()
                : null);

        if (versamento.getUo() != null) {
            pendenza.setIdUnitaOperativa(versamento.getUo().getCodUo());
        }

        pendenza.setCausale(versamento.getCausaleVersamento());
        pendenza.setImporto(versamento.getImportoTotale());
        pendenza.setNumeroAvviso(versamento.getNumeroAvviso());
        pendenza.setTassonomia(versamento.getTassonomia());
        pendenza.setDirezione(versamento.getDirezione());
        pendenza.setDivisione(versamento.getDivisione());

        if (versamento.getDataValidita() != null) {
            pendenza.setDataValidita(versamento.getDataValidita().toLocalDate());
        }
        if (versamento.getDataScadenza() != null) {
            pendenza.setDataScadenza(versamento.getDataScadenza().toLocalDate());
        }
        if (versamento.getCodAnnoTributario() != null) {
            try {
                pendenza.setAnnoRiferimento(Integer.parseInt(versamento.getCodAnnoTributario()));
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        if (versamento.getDataNotificaAvviso() != null) {
            pendenza.setDataNotificaAvviso(versamento.getDataNotificaAvviso().toLocalDate());
        }

        if (versamento.getTassonomiaAvviso() != null) {
            try {
                pendenza.setTassonomiaAvviso(TassonomiaAvviso.fromValue(versamento.getTassonomiaAvviso()));
            } catch (IllegalArgumentException e) {
                // ignore invalid tassonomia
            }
        }

        pendenza.setSoggettoPagatore(toSoggetto(versamento));

        List<NuovaVocePendenza> voci = new ArrayList<>();
        if (versamento.getSingoliVersamenti() != null) {
            for (SingoloVersamento sv : versamento.getSingoliVersamenti()) {
                voci.add(toNuovaVocePendenza(sv));
            }
        }
        pendenza.setVoci(voci);

        return pendenza;
    }

    private Soggetto toSoggetto(Versamento versamento) {
        Soggetto soggetto = new Soggetto();

        if ("F".equals(versamento.getDebitoreTipo())) {
            soggetto.setTipo(TipoSoggetto.F);
        } else if ("G".equals(versamento.getDebitoreTipo())) {
            soggetto.setTipo(TipoSoggetto.G);
        } else {
            soggetto.setTipo(TipoSoggetto.F);
        }

        soggetto.setIdentificativo(versamento.getDebitoreIdentificativo());
        soggetto.setAnagrafica(versamento.getDebitoreAnagrafica());
        soggetto.setIndirizzo(versamento.getDebitoreIndirizzo());
        soggetto.setCivico(versamento.getDebitoreCivico());
        soggetto.setCap(versamento.getDebitoreCap());
        soggetto.setLocalita(versamento.getDebitoreLocalita());
        soggetto.setProvincia(versamento.getDebitoreProvincia());
        soggetto.setNazione(versamento.getDebitoreNazione());
        soggetto.setEmail(versamento.getDebitoreEmail());
        soggetto.setCellulare(versamento.getDebitoreCellulare());

        return soggetto;
    }

    private NuovaVocePendenza toNuovaVocePendenza(SingoloVersamento sv) {
        NuovaVocePendenza voce = new NuovaVocePendenza();

        voce.setIdVocePendenza(sv.getCodSingoloVersamentoEnte());
        voce.setImporto(sv.getImportoSingoloVersamento());
        voce.setDescrizione(sv.getDescrizione());
        voce.setDescrizioneCausaleRPT(sv.getDescrizioneCausaleRpt());

        if (sv.getDominio() != null) {
            voce.setIdDominio(sv.getDominio().getCodDominio());
        }

        if (sv.getTributo() != null) {
            voce.setCodEntrata(sv.getTributo().getTipoTributo() != null
                    ? sv.getTributo().getTipoTributo().getCodTributo()
                    : null);
        }

        if (sv.getIbanAccredito() != null) {
            voce.setIbanAccredito(sv.getIbanAccredito().getCodIban());
        }

        if (sv.getIbanAppoggio() != null) {
            voce.setIbanAppoggio(sv.getIbanAppoggio().getCodIban());
        }

        if (sv.getTipoContabilita() != null) {
            try {
                voce.setTipoContabilita(TipoContabilita.fromValue(sv.getTipoContabilita()));
            } catch (IllegalArgumentException e) {
                // ignore invalid tipo contabilita
            }
        }

        voce.setCodiceContabilita(sv.getCodiceContabilita());

        if (sv.getTipoBollo() != null) {
            voce.setTipoBollo(NuovaVocePendenza.TipoBolloEnum.fromValue(sv.getTipoBollo()));
            voce.setHashDocumento(sv.getHashDocumento());
            voce.setProvinciaResidenza(sv.getProvinciaResidenza());
        }

        return voce;
    }
}
