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
@Table(name = "tributi")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tributo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "abilitato")
    private Boolean abilitato;

    @Column(name = "tipo_contabilita")
    private String tipoContabilita;

    @Column(name = "codice_contabilita")
    private String codiceContabilita;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_dominio")
    private Dominio dominio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_iban_accredito")
    private IbanAccredito ibanAccredito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_iban_appoggio")
    private IbanAccredito ibanAppoggio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_tributo")
    private TipoTributo tipoTributo;

    @OneToMany(mappedBy = "tributo", cascade = CascadeType.ALL)
    @Builder.Default
    private List<SingoloVersamento> singoliVersamenti = new ArrayList<>();

}
