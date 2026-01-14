package it.govpay.portal.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "versamenti")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Versamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_dominio")
    private Dominio dominio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_applicazione")
    private Applicazione applicazione;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_versamento")
    private TipoVersamento tipoVersamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_versamento_dominio")
    private TipoVersamentoDominio tipoVersamentoDominio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_uo")
    private Uo uo;

    @Column(name = "cod_versamento_ente", length = 35)
    private String codVersamentoEnte;

    @Column(name = "nome", length = 35)
    private String nome;

    @Column(name = "importo_totale")
    private Double importoTotale;

    @Enumerated(EnumType.STRING)
    @Column(name = "stato_versamento", length = 35)
    private StatoVersamento statoVersamento;

    @Column(name = "descrizione_stato", length = 255)
    private String descrizioneStato;

    @Column(name = "aggiornabile")
    private Boolean aggiornabile;

    @Column(name = "data_creazione")
    private LocalDateTime dataCreazione;

    @Column(name = "data_validita")
    private LocalDateTime dataValidita;

    @Column(name = "data_scadenza")
    private LocalDateTime dataScadenza;

    @Column(name = "data_ora_ultimo_aggiornamento")
    private LocalDateTime dataOraUltimoAggiornamento;

    @Column(name = "causale_versamento", length = 1024)
    private String causaleVersamento;

    @Column(name = "debitore_tipo")
    private String debitoreTipo;

    @Column(name = "debitore_identificativo", length = 35)
    private String debitoreIdentificativo;

    @Column(name = "debitore_anagrafica", length = 70)
    private String debitoreAnagrafica;

    @Column(name = "debitore_indirizzo", length = 70)
    private String debitoreIndirizzo;

    @Column(name = "debitore_civico", length = 16)
    private String debitoreCivico;

    @Column(name = "debitore_cap", length = 16)
    private String debitoreCap;

    @Column(name = "debitore_localita", length = 35)
    private String debitoreLocalita;

    @Column(name = "debitore_provincia", length = 35)
    private String debitoreProvincia;

    @Column(name = "debitore_nazione", length = 2)
    private String debitoreNazione;

    @Column(name = "debitore_email", length = 35)
    private String debitoreEmail;

    @Column(name = "debitore_telefono", length = 35)
    private String debitoreTelefono;

    @Column(name = "debitore_cellulare", length = 35)
    private String debitoreCellulare;

    @Column(name = "debitore_fax", length = 35)
    private String debitoreFax;

    @Column(name = "tassonomia_avviso", length = 35)
    private String tassonomiaAvviso;

    @Column(name = "tassonomia", length = 35)
    private String tassonomia;

    @Column(name = "cod_lotto", length = 35)
    private String codLotto;

    @Column(name = "cod_versamento_lotto", length = 35)
    private String codVersamentoLotto;

    @Column(name = "cod_anno_tributario", length = 35)
    private String codAnnoTributario;

    @Column(name = "cod_bundlekey", length = 256)
    private String codBundlekey;

    @Column(name = "dati_allegati", columnDefinition = "TEXT")
    private String datiAllegati;

    @Column(name = "anomalie", columnDefinition = "TEXT")
    private String anomalie;

    @Column(name = "iuv_versamento", length = 35)
    private String iuvVersamento;

    @Column(name = "numero_avviso", length = 35)
    private String numeroAvviso;

    @Column(name = "ack")
    private Boolean ack;

    @Column(name = "anomalo")
    private Boolean anomalo;

    @Column(name = "divisione", length = 35)
    private String divisione;

    @Column(name = "direzione", length = 35)
    private String direzione;

    @Column(name = "id_sessione", length = 35)
    private String idSessione;

    @Column(name = "data_pagamento")
    private LocalDateTime dataPagamento;

    @Column(name = "importo_pagato")
    private Double importoPagato;

    @Column(name = "importo_incassato")
    private Double importoIncassato;

    @Enumerated(EnumType.STRING)
    @Column(name = "stato_pagamento", length = 35)
    private StatoPagamento statoPagamento;

    @Column(name = "iuv_pagamento", length = 35)
    private String iuvPagamento;

    @Column(name = "src_iuv", length = 35)
    private String srcIuv;

    @Column(name = "src_debitore_identificativo", length = 35)
    private String srcDebitoreIdentificativo;

    @Column(name = "cod_rata", length = 35)
    private String codRata;

    @Column(name = "tipo", length = 35)
    private String tipo;

    @Column(name = "data_notifica_avviso")
    private LocalDateTime dataNotificaAvviso;

    @Column(name = "avviso_notificato")
    private Boolean avvisoNotificato;

    @Column(name = "avv_mail_data_prom_scadenza")
    private LocalDateTime avvMailDataPromScadenza;

    @Column(name = "avv_mail_prom_scad_notificato")
    private Boolean avvMailPromScadNotificato;

    @Column(name = "avv_app_io_data_prom_scadenza")
    private LocalDateTime avvAppIoDataPromScadenza;

    @Column(name = "avv_app_io_prom_scad_notificat")
    private Boolean avvAppIoPromScadNotificat;

    @Column(name = "proprieta", columnDefinition = "TEXT")
    private String proprieta;

    @Column(name = "id_documento")
    private Long idDocumento;

    @Column(name = "data_ultima_modifica_aca")
    private LocalDateTime dataUltimaModificaAca;

    @Column(name = "data_ultima_comunicazione_aca")
    private LocalDateTime dataUltimaComunicazioneAca;

    @OneToMany(mappedBy = "versamento", cascade = CascadeType.ALL)
    @Builder.Default
    private List<SingoloVersamento> singoliVersamenti = new ArrayList<>();

}
