package it.govpay.portal.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
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

    @Lob
    @Column(name = "xml_rt")
    private byte[] xmlRt;
}
