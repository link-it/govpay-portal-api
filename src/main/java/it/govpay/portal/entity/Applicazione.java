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
@Table(name = "applicazioni")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Applicazione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cod_applicazione")
    private String codApplicazione;

    @Column(name = "cod_applicazione_iuv")
    private String codApplicazioneIuv;

    @OneToMany(mappedBy = "applicazione", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Versamento> versamenti = new ArrayList<>();

}
