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
@Table(name = "tipi_tributo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoTributo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cod_tributo")
    private String codTributo;

    @Column(name = "descrizione")
    private String descrizione;

    @Column(name = "tipo_contabilita")
    private String tipoContabilita;

    @Column(name = "cod_contabilita")
    private String codContabilita;

    @OneToMany(mappedBy = "tipoTributo", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Tributo> tributi = new ArrayList<>();

}
