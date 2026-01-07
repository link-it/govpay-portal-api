package it.govpay.ente.mapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import it.govpay.ente.model.NuovaPendenza;
import it.govpay.ente.model.NuovaVocePendenza;
import it.govpay.ente.model.PendenzaVerificata;
import it.govpay.ente.model.Soggetto;
import it.govpay.ente.model.StatoPendenzaVerificata;
import it.govpay.ente.model.TipoSoggetto;
import it.govpay.portal.entity.SingoloVersamento;
import it.govpay.portal.entity.Versamento;

@Component
public class VersamentoMapper {

    public PendenzaVerificata toPendenzaVerificata(Versamento versamento) {
        if (versamento == null) {
            return new PendenzaVerificata(StatoPendenzaVerificata.SCONOSCIUTA);
        }

        StatoPendenzaVerificata stato = mapStato(versamento.getStatoVersamento());
        PendenzaVerificata result = new PendenzaVerificata(stato);
        result.setDescrizioneStato(versamento.getDescrizioneStato());

        if (stato == StatoPendenzaVerificata.NON_ESEGUITA) {
            result.setPendenza(toNuovaPendenza(versamento));
        }

        return result;
    }

    public NuovaPendenza toNuovaPendenza(Versamento versamento) {
        NuovaPendenza pendenza = new NuovaPendenza();

        if (versamento.getApplicazione() != null) {
            pendenza.setIdA2A(versamento.getApplicazione().getCodApplicazione());
        }
        pendenza.setIdPendenza(versamento.getCodVersamentoEnte());

        if (versamento.getTipoVersamento() != null) {
            pendenza.setIdTipoPendenza(versamento.getTipoVersamento().getCodTipoVersamento());
        }

        if (versamento.getDominio() != null) {
            pendenza.setIdDominio(versamento.getDominio().getCodDominio());
        }

        if (versamento.getUo() != null) {
            pendenza.setIdUnitaOperativa(versamento.getUo().getCodUo());
        }

        pendenza.setCausale(versamento.getCausaleVersamento());
        pendenza.setSoggettoPagatore(toSoggetto(versamento));

        if (versamento.getImportoTotale() != null) {
            pendenza.setImporto(BigDecimal.valueOf(versamento.getImportoTotale()));
        }

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
                pendenza.setAnnoRiferimento(new BigDecimal(versamento.getCodAnnoTributario()));
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        pendenza.setCartellaPagamento(versamento.getCodBundlekey());

        if (versamento.getDataNotificaAvviso() != null) {
            pendenza.setDataNotificaAvviso(versamento.getDataNotificaAvviso().toLocalDate());
        }

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
        TipoSoggetto tipo = "G".equals(versamento.getDebitoreTipo())
            ? TipoSoggetto.G
            : TipoSoggetto.F;

        Soggetto soggetto = new Soggetto(tipo, versamento.getDebitoreIdentificativo());
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
        voce.setDescrizione(sv.getDescrizione());

        if (sv.getImportoSingoloVersamento() != null) {
            voce.setImporto(BigDecimal.valueOf(sv.getImportoSingoloVersamento()));
        }

        if (sv.getTributo() != null) {
            voce.setCodEntrata(sv.getTributo().getTipoTributo().getCodTributo());
        }

        if (sv.getIbanAccredito() != null) {
            voce.setIbanAccredito(sv.getIbanAccredito().getCodIban());
        }

        if (sv.getIbanAppoggio() != null) {
            voce.setIbanAppoggio(sv.getIbanAppoggio().getCodIban());
        }

        voce.setDescrizioneCausaleRPT(sv.getDescrizioneCausaleRpt());
        voce.setTipoBollo(sv.getTipoBollo() != null
            ? NuovaVocePendenza.TipoBolloEnum.fromValue(sv.getTipoBollo())
            : null);
        voce.setHashDocumento(sv.getHashDocumento());
        voce.setProvinciaResidenza(sv.getProvinciaResidenza());

        if (sv.getDominio() != null) {
            voce.setIdDominio(sv.getDominio().getCodDominio());
        }

        voce.setCodiceTassonomicoPagoPA(sv.getCodiceContabilita());

        return voce;
    }

    private StatoPendenzaVerificata mapStato(String statoVersamento) {
        if (statoVersamento == null) {
            return StatoPendenzaVerificata.SCONOSCIUTA;
        }

        return switch (statoVersamento) {
            case "NON_ESEGUITO" -> StatoPendenzaVerificata.NON_ESEGUITA;
            case "ANNULLATO" -> StatoPendenzaVerificata.ANNULLATA;
            case "SCADUTO" -> StatoPendenzaVerificata.SCADUTA;
            default -> StatoPendenzaVerificata.SCONOSCIUTA;
        };
    }

}
