package it.govpay.portal.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rpt")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rpt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_versamento")
    private Versamento versamento;

    @Column(name = "iuv")
    private String iuv;

    @Column(name = "ccp")
    private String ccp;

    @Column(name = "cod_dominio")
    private String codDominio;

    @Column(name = "versione")
    private String versione;

    @Column(name = "data_msg_ricevuta")
    private LocalDateTime dataMsgRicevuta;

    @Column(name = "xml_rt")
    private byte[] xmlRt;

    /**
     * Tracciato RPT originale. Serve a determinare la data operazione per SANP_230
     * ({@code dataOraMessaggioRichiesta}), che il legacy legge dall'RPT e non dalla RT.
     */
    @Column(name = "xml_rpt")
    private byte[] xmlRpt;

    /**
     * Ripiego per la data operazione SANP_230 quando {@code xmlRpt} non e' disponibile
     * (transazioni acquisite in standin): stessa fonte da cui il legacy popola quel campo
     * XML quando costruisce l'RPT in uscita.
     */
    @Column(name = "data_msg_richiesta")
    private LocalDateTime dataMsgRichiesta;

    @Column(name = "cod_esito_pagamento")
    private Integer codEsitoPagamento;

    @Column(name = "importo_totale_pagato")
    private Double importoTotalePagato;

    @Column(name = "cod_psp")
    private String codPsp;

    @Column(name = "denominazione_attestante")
    private String denominazioneAttestante;
}
