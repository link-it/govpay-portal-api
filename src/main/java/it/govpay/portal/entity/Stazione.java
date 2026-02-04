package it.govpay.portal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stazioni")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stazione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cod_stazione")
    private String codStazione;

    @Column(name = "password")
    private String password;

    @Column(name = "abilitato")
    private Boolean abilitato;

    @Column(name = "application_code")
    private Integer applicationCode;

    @Column(name = "versione")
    private String versione;

    @Column(name = "id_intermediario")
    private Long idIntermediario;

}
