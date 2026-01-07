package it.govpay.portal.entity;

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
@Table(name = "sv_attesa")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SingoloVersamentoAttesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cod_singolo_versamento_ente", length = 70)
    private String codSingoloVersamentoEnte;

    @Column(name = "stato_singolo_versamento", length = 35)
    private String statoSingoloVersamento;

    @Column(name = "importo_singolo_versamento")
    private Double importoSingoloVersamento;

    @Column(name = "tipo_bollo", length = 2)
    private String tipoBollo;

    @Column(name = "hash_documento", length = 70)
    private String hashDocumento;

    @Column(name = "provincia_residenza", length = 2)
    private String provinciaResidenza;

    @Column(name = "tipo_contabilita")
    private String tipoContabilita;

    @Column(name = "codice_contabilita", length = 255)
    private String codiceContabilita;

    @Column(name = "descrizione", length = 256)
    private String descrizione;

    @Column(name = "dati_allegati", columnDefinition = "TEXT")
    private String datiAllegati;

    @Column(name = "contabilita", columnDefinition = "TEXT")
    private String contabilita;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "indice_dati")
    private Integer indiceDati;

    @Column(name = "descrizione_causale_rpt", length = 140)
    private String descrizioneCausaleRpt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_versamento")
    private VersamentoAttesa versamentoAttesa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tributo")
    private Tributo tributo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_iban_accredito")
    private IbanAccredito ibanAccredito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_iban_appoggio")
    private IbanAccredito ibanAppoggio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_dominio")
    private Dominio dominio;

}
