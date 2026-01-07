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
@Table(name = "iban_accredito")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IbanAccredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cod_iban")
    private String codIban;

    @Column(name = "bic_accredito")
    private String bicAccredito;

    @Column(name = "postale")
    private Boolean postale;

    @Column(name = "abilitato")
    private Boolean abilitato;

    @Column(name = "descrizione")
    private String descrizione;

    @Column(name = "intestatario")
    private String intestatario;

    @Column(name = "aut_stampa_poste")
    private String autStampaPoste;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_dominio")
    private Dominio dominio;

    @OneToMany(mappedBy = "ibanAccredito", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Tributo> tributiAccredito = new ArrayList<>();

    @OneToMany(mappedBy = "ibanAppoggio", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Tributo> tributiAppoggio = new ArrayList<>();

}
