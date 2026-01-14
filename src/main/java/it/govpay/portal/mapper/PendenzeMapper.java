package it.govpay.portal.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import it.govpay.portal.entity.SingoloVersamento;
import it.govpay.portal.entity.StatoVersamento;
import it.govpay.portal.entity.Versamento;
import it.govpay.portal.model.Avviso;
import it.govpay.portal.model.Dominio;
import it.govpay.portal.model.Pendenza;
import it.govpay.portal.model.Soggetto;
import it.govpay.portal.model.StatoAvviso;
import it.govpay.portal.model.StatoPendenza;
import it.govpay.portal.model.TassonomiaAvviso;
import it.govpay.portal.model.TipoSoggetto;
import it.govpay.portal.model.VocePendenza;
import it.govpay.portal.util.IuvUtils;

@Component
public class PendenzeMapper {

    public Pendenza toPendenza(Versamento entity) {
        if (entity == null) {
            return null;
        }

        Pendenza pendenza = new Pendenza();

        pendenza.setIdA2A(entity.getApplicazione() != null
                ? entity.getApplicazione().getCodApplicazione()
                : null);
        pendenza.setIdPendenza(entity.getCodVersamentoEnte());
        pendenza.setIdTipoPendenza(entity.getTipoVersamento() != null
                ? entity.getTipoVersamento().getCodTipoVersamento()
                : null);

        if (entity.getDominio() != null) {
            Dominio dominio = new Dominio();
            dominio.setIdDominio(entity.getDominio().getCodDominio());
            dominio.setRagioneSociale(entity.getDominio().getRagioneSociale());
            pendenza.setDominio(dominio);
        }

        pendenza.setStato(mapStatoVersamento(entity.getStatoVersamento()));
        pendenza.setIuv(entity.getIuvVersamento());
        pendenza.setCausale(entity.getCausaleVersamento());
        pendenza.setImporto(entity.getImportoTotale());
        pendenza.setNumeroAvviso(entity.getNumeroAvviso());

        if (entity.getDataCreazione() != null) {
            pendenza.setDataCaricamento(entity.getDataCreazione().toLocalDate());
        }
        if (entity.getDataValidita() != null) {
            pendenza.setDataValidita(entity.getDataValidita().toLocalDate());
        }
        if (entity.getDataScadenza() != null) {
            pendenza.setDataScadenza(entity.getDataScadenza().toLocalDate());
        }
        if (entity.getDataPagamento() != null) {
            pendenza.setDataPagamento(entity.getDataPagamento().toLocalDate());
        }

        if (entity.getCodAnnoTributario() != null) {
            try {
                pendenza.setAnnoRiferimento(Integer.parseInt(entity.getCodAnnoTributario()));
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        pendenza.setSoggettoPagatore(mapSoggettoPagatore(entity));

        List<VocePendenza> voci = entity.getSingoliVersamenti().stream()
                .map(this::toVocePendenza).toList();
        pendenza.setVoci(voci);

        return pendenza;
    }

    private Soggetto mapSoggettoPagatore(Versamento entity) {
        Soggetto soggetto = new Soggetto();

        if ("F".equals(entity.getDebitoreTipo())) {
            soggetto.setTipo(TipoSoggetto.F);
        } else if ("G".equals(entity.getDebitoreTipo())) {
            soggetto.setTipo(TipoSoggetto.G);
        }

        soggetto.setIdentificativo(entity.getDebitoreIdentificativo());
        soggetto.setAnagrafica(entity.getDebitoreAnagrafica());
        soggetto.setIndirizzo(entity.getDebitoreIndirizzo());
        soggetto.setCivico(entity.getDebitoreCivico());
        soggetto.setCap(entity.getDebitoreCap());
        soggetto.setLocalita(entity.getDebitoreLocalita());
        soggetto.setProvincia(entity.getDebitoreProvincia());
        soggetto.setNazione(entity.getDebitoreNazione());
        soggetto.setEmail(entity.getDebitoreEmail());
        soggetto.setCellulare(entity.getDebitoreCellulare());

        return soggetto;
    }

    private StatoPendenza mapStatoVersamento(StatoVersamento statoVersamento) {
        if (statoVersamento == null) {
            return null;
        }
        return switch (statoVersamento) {
            case ESEGUITO -> StatoPendenza.ESEGUITA;
            case NON_ESEGUITO -> StatoPendenza.NON_ESEGUITA;
            case PARZIALMENTE_ESEGUITO -> StatoPendenza.ESEGUITA_PARZIALE;
            case ANNULLATO -> StatoPendenza.ANNULLATA;
            case INCASSATO -> StatoPendenza.ESEGUITA;
            case ANOMALO -> StatoPendenza.ANOMALA;
            default -> StatoPendenza.NON_ESEGUITA;
        };
    }

    private VocePendenza toVocePendenza(SingoloVersamento entity) {
        VocePendenza voce = new VocePendenza();
        voce.setIdVocePendenza(entity.getCodSingoloVersamentoEnte());
        voce.setImporto(entity.getImportoSingoloVersamento());
        voce.setDescrizione(entity.getDescrizione());
        voce.setIndice(entity.getIndiceDati());

        if (entity.getDominio() != null) {
            Dominio dominio = new Dominio();
            dominio.setIdDominio(entity.getDominio().getCodDominio());
            dominio.setRagioneSociale(entity.getDominio().getRagioneSociale());
            voce.setDominio(dominio);
        }

        return voce;
    }

    public Avviso toAvviso(Versamento versamento, it.govpay.portal.entity.Dominio dominio) {
        if (versamento == null) {
            return null;
        }

        String numeroAvviso = versamento.getNumeroAvviso();
        
        Avviso rsModel = new Avviso();

        rsModel.setDescrizione(versamento.getCausaleVersamento());

        if (versamento.getDataScadenza() != null) {
            rsModel.setDataScadenza(versamento.getDataScadenza().toLocalDate());
        }
        if (versamento.getDataPagamento() != null) {
            rsModel.setDataPagamento(versamento.getDataPagamento().toLocalDate());
        }
        if (versamento.getDataValidita() != null) {
            rsModel.setDataValidita(versamento.getDataValidita().toLocalDate());
        }

        rsModel.setIdDominio(versamento.getDominio() != null ? versamento.getDominio().getCodDominio() : null);
        rsModel.setImporto(versamento.getImportoTotale());
        
		rsModel.setNumeroAvviso(numeroAvviso);

        if (versamento.getTassonomiaAvviso() != null) {
            try {
                rsModel.setTassonomiaAvviso(TassonomiaAvviso.fromValue(versamento.getTassonomiaAvviso()));
            } catch (IllegalArgumentException e) {
                // ignore invalid tassonomia
            }
        }

        BigDecimal importo = versamento.getImportoTotale() != null
        		? BigDecimal.valueOf(versamento.getImportoTotale())
        		: BigDecimal.ZERO;
        rsModel.setBarcode(IuvUtils.buildBarCode(dominio.getGln(), dominio.getAuxDigit(),
        		dominio.getStazione().getApplicationCode(), versamento.getIuvVersamento(), importo, numeroAvviso));
        rsModel.setQrcode(IuvUtils.buildQrCode002(dominio.getCodDominio(), dominio.getAuxDigit(),
        		dominio.getStazione().getApplicationCode(), versamento.getIuvVersamento(), importo, numeroAvviso));


        StatoAvviso statoPendenza = getStatoPendenza(versamento);

        rsModel.setStato(statoPendenza);

        return rsModel;
    }

	private StatoAvviso getStatoPendenza(Versamento versamento) {
		StatoAvviso statoPendenza = null;
        StatoVersamento statoVersamento = versamento.getStatoVersamento();

        if (statoVersamento != null) {
            switch (statoVersamento) {
                case ANNULLATO:
                    statoPendenza = StatoAvviso.ANNULLATA;
                    break;
                case ESEGUITO, ESEGUITO_ALTRO_CANALE, PARZIALMENTE_ESEGUITO:
                    statoPendenza = StatoAvviso.DUPLICATA;
                    break;
                case NON_ESEGUITO:
                    if (versamento.getDataScadenza() != null &&
                            versamento.getDataScadenza().toLocalDate().isBefore(LocalDate.now())) {
                        statoPendenza = StatoAvviso.SCADUTA;
                    } else {
                        statoPendenza = StatoAvviso.NON_ESEGUITA;
                    }
                    break;
                default:
                    statoPendenza = StatoAvviso.SCONOSCIUTA;
                    break;
            }
        } else {
            statoPendenza = StatoAvviso.SCONOSCIUTA;
        }
		return statoPendenza;
	}

}
