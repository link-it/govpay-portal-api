package it.govpay.portal.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "domini")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dominio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cod_dominio")
    private String codDominio;

    @Column(name = "ragione_sociale")
    private String ragioneSociale;

    @Column(name = "aux_digit")
    private Integer auxDigit;

    @Column(name = "segregation_code")
    private Integer segregationCode;

    @Column(name = "iuv_prefix")
    private String iuvPrefix;

    @Column(name = "aut_stampa_poste")
    private String autStampaPoste;

    @OneToMany(mappedBy = "dominio", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Uo> unitaOrganizzative = new ArrayList<>();

    @OneToMany(mappedBy = "dominio", cascade = CascadeType.ALL)
    @Builder.Default
    private List<IbanAccredito> ibanAccredito = new ArrayList<>();

    @OneToMany(mappedBy = "dominio", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Tributo> tributi = new ArrayList<>();

    @OneToMany(mappedBy = "dominio", cascade = CascadeType.ALL)
    @Builder.Default
    private List<TipoVersamentoDominio> tipiVersamentoDominio = new ArrayList<>();

}
