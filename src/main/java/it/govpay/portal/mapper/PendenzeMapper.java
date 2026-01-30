package it.govpay.portal.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import it.govpay.portal.entity.CausaleUtils;
import it.govpay.portal.entity.SingoloVersamento;
import it.govpay.portal.entity.StatoVersamento;
import it.govpay.portal.entity.Versamento;
import it.govpay.portal.model.Avviso;
import it.govpay.portal.model.Dominio;
import it.govpay.portal.model.Pendenza;
import it.govpay.portal.model.Ricevuta;
import it.govpay.portal.model.Soggetto;
import it.govpay.portal.model.StatoAvviso;
import it.govpay.portal.model.StatoPendenza;
import it.govpay.portal.model.TassonomiaAvviso;
import it.govpay.portal.model.TipoSoggetto;
import it.govpay.portal.model.VocePendenza;
import it.govpay.portal.model.VoceRicevuta;
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
        pendenza.setCausale(CausaleUtils.getSimple(entity.getCausaleVersamento()));
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

        rsModel.setDescrizione(CausaleUtils.getSimple(versamento.getCausaleVersamento()));

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

    public Ricevuta toRicevuta(Versamento versamento) {
        if (versamento == null) {
            return null;
        }

        Ricevuta ricevuta = new Ricevuta();

        ricevuta.setOggettoDelPagamento(CausaleUtils.getSimple(versamento.getCausaleVersamento()));

        if (versamento.getDominio() != null) {
            Dominio dominio = new Dominio();
            dominio.setIdDominio(versamento.getDominio().getCodDominio());
            dominio.setRagioneSociale(versamento.getDominio().getRagioneSociale());
            ricevuta.setDominio(dominio);
        }

        ricevuta.setSoggetto(mapSoggettoPagatore(versamento));
        ricevuta.setIstitutoAttestante("N/D"); // TODO: da recuperare dal pagamento
        ricevuta.setImportoTotale(versamento.getImportoTotale() != null ?
                versamento.getImportoTotale().doubleValue() : 0.0);

        if (versamento.getDataPagamento() != null) {
            ricevuta.setDataOperazione(versamento.getDataPagamento().atOffset(ZoneOffset.UTC));
            ricevuta.setDataApplicativa(versamento.getDataPagamento().toLocalDate());
        }

        ricevuta.setStato(mapStatoRicevuta(versamento.getStatoVersamento()));
        ricevuta.setIuv(versamento.getIuvVersamento());
        ricevuta.setIdRicevuta(versamento.getIdSessione());

        List<VoceRicevuta> voci = new ArrayList<>();
        if (versamento.getSingoliVersamenti() != null) {
            for (SingoloVersamento sv : versamento.getSingoliVersamenti()) {
                VoceRicevuta voce = new VoceRicevuta();
                voce.setDescrizione(sv.getDescrizione() != null ? sv.getDescrizione() : "Voce di pagamento");
                voce.setIdRiscossione(String.valueOf(sv.getIndiceDati() != null ? sv.getIndiceDati() : 1));
                voce.setImporto(sv.getImportoSingoloVersamento() != null ?
                        sv.getImportoSingoloVersamento().doubleValue() : 0.0);
                voce.setStato("ESEGUITO");
                voci.add(voce);
            }
        }
        if (voci.isEmpty()) {
            VoceRicevuta voce = new VoceRicevuta();
            String causale = CausaleUtils.getSimple(versamento.getCausaleVersamento());
            voce.setDescrizione(causale != null ? causale : "Pagamento");
            voce.setIdRiscossione("1");
            voce.setImporto(versamento.getImportoTotale() != null ?
                    versamento.getImportoTotale().doubleValue() : 0.0);
            voce.setStato("ESEGUITO");
            voci.add(voce);
        }
        ricevuta.setElencoVoci(voci);

        return ricevuta;
    }

    private String mapStatoRicevuta(StatoVersamento stato) {
        if (stato == null) return "SCONOSCIUTO";
        return switch (stato) {
            case ESEGUITO, INCASSATO, ESEGUITO_ALTRO_CANALE, ESEGUITO_SENZA_RPT -> "ESEGUITO";
            case PARZIALMENTE_ESEGUITO -> "PARZIALMENTE_ESEGUITO";
            case NON_ESEGUITO -> "NON_ESEGUITO";
            case ANNULLATO -> "ANNULLATO";
            case ANOMALO -> "ANOMALO";
        };
    }

    /**
     * Converte la risposta della creazione pendenza dal client GovPay nel modello portal.
     */
    public Pendenza toPendenzaFromCreata(
            it.govpay.pendenze.client.model.PendenzaCreata pendenzaCreata,
            it.govpay.pendenze.client.model.NuovaPendenza nuovaPendenza) {

        Pendenza pendenza = new Pendenza();

        // Dati dalla risposta di creazione
        pendenza.setNumeroAvviso(pendenzaCreata.getNumeroAvviso());

        // Dati dal dominio
        if (pendenzaCreata.getIdDominio() != null) {
            Dominio dominio = new Dominio();
            dominio.setIdDominio(pendenzaCreata.getIdDominio());
            pendenza.setDominio(dominio);
        }

        // Dati dalla richiesta originale
        pendenza.setIdTipoPendenza(nuovaPendenza.getIdTipoPendenza());
        pendenza.setCausale(nuovaPendenza.getCausale());
        pendenza.setImporto(nuovaPendenza.getImporto());

        if (nuovaPendenza.getDataValidita() != null) {
            pendenza.setDataValidita(nuovaPendenza.getDataValidita());
        }
        if (nuovaPendenza.getDataScadenza() != null) {
            pendenza.setDataScadenza(nuovaPendenza.getDataScadenza());
        }
        if (nuovaPendenza.getAnnoRiferimento() != null) {
            pendenza.setAnnoRiferimento(nuovaPendenza.getAnnoRiferimento());
        }

        // UUID dalla risposta (se presente)
        pendenza.setUUID(pendenzaCreata.getUUID());

        // Stato iniziale
        pendenza.setStato(StatoPendenza.NON_ESEGUITA);

        // Data caricamento
        pendenza.setDataCaricamento(java.time.LocalDate.now());

        // Soggetto pagatore
        if (nuovaPendenza.getSoggettoPagatore() != null) {
            Soggetto soggetto = new Soggetto();
            it.govpay.pendenze.client.model.Soggetto sp = nuovaPendenza.getSoggettoPagatore();

            if (sp.getTipo() != null) {
                soggetto.setTipo(TipoSoggetto.fromValue(sp.getTipo().getValue()));
            }
            soggetto.setIdentificativo(sp.getIdentificativo());
            soggetto.setAnagrafica(sp.getAnagrafica());
            soggetto.setIndirizzo(sp.getIndirizzo());
            soggetto.setCivico(sp.getCivico());
            soggetto.setCap(sp.getCap());
            soggetto.setLocalita(sp.getLocalita());
            soggetto.setProvincia(sp.getProvincia());
            soggetto.setNazione(sp.getNazione());
            soggetto.setEmail(sp.getEmail());
            soggetto.setCellulare(sp.getCellulare());

            pendenza.setSoggettoPagatore(soggetto);
        }

        // Voci di pendenza
        if (nuovaPendenza.getVoci() != null && !nuovaPendenza.getVoci().isEmpty()) {
            List<VocePendenza> voci = new ArrayList<>();
            int indice = 1;
            for (it.govpay.pendenze.client.model.NuovaVocePendenza nv : nuovaPendenza.getVoci()) {
                VocePendenza voce = new VocePendenza();
                voce.setIdVocePendenza(nv.getIdVocePendenza());
                voce.setImporto(nv.getImporto());
                voce.setDescrizione(nv.getDescrizione());
                voce.setIndice(indice++);

                if (nv.getIdDominio() != null) {
                    Dominio d = new Dominio();
                    d.setIdDominio(nv.getIdDominio());
                    voce.setDominio(d);
                }

                voci.add(voce);
            }
            pendenza.setVoci(voci);
        }

        return pendenza;
    }

}
