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
@Table(name = "uo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Uo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cod_uo")
    private String codUo;

    @Column(name = "uo_codice_identificativo")
    private String uoCodiceIdentificativo;

    @Column(name = "uo_denominazione")
    private String uoDenominazione;

    @Column(name = "uo_indirizzo")
    private String uoIndirizzo;

    @Column(name = "uo_civico")
    private String uoCivico;

    @Column(name = "uo_cap")
    private String uoCap;

    @Column(name = "uo_localita")
    private String uoLocalita;

    @Column(name = "uo_provincia")
    private String uoProvincia;

    @Column(name = "uo_nazione")
    private String uoNazione;

    @Column(name = "uo_area")
    private String uoArea;

    @Column(name = "uo_url_sito_web")
    private String uoUrlSitoWeb;

    @Column(name = "uo_email")
    private String uoEmail;

    @Column(name = "uo_pec")
    private String uoPec;

    @Column(name = "uo_tel")
    private String uoTel;

    @Column(name = "uo_fax")
    private String uoFax;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_dominio")
    private Dominio dominio;

}
