package it.govpay.portal.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "tipi_vers_domini")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoVersamentoDominio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_dominio")
    private Dominio dominio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_versamento")
    private TipoVersamento tipoVersamento;

    @Column(name = "codifica_iuv")
    private String codificaIuv;

    @Column(name = "abilitato")
    private Boolean abilitato;

    @Column(name = "pag_form_tipo")
    private String pagFormTipo;

    @Column(name = "pag_form_definizione", columnDefinition = "TEXT")
    private String pagFormDefinizione;

    @Column(name = "pag_form_impaginazione", columnDefinition = "TEXT")
    private String pagFormImpaginazione;

    @Column(name = "pag_validazione_def", columnDefinition = "TEXT")
    private String pagValidazioneDef;

    @Column(name = "pag_trasformazione_tipo")
    private String pagTrasformazioneTipo;

    @Column(name = "pag_trasformazione_def", columnDefinition = "TEXT")
    private String pagTrasformazioneDef;

    @Column(name = "pag_cod_applicazione")
    private String pagCodApplicazione;

    @Column(name = "pag_abilitato")
    private Boolean pagAbilitato;

    @OneToMany(mappedBy = "tipoVersamentoDominio", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Versamento> versamenti = new ArrayList<>();

}
