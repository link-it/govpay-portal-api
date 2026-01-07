package it.govpay.portal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.govpay.portal.entity.Applicazione;

@Repository
public interface ApplicazioneRepository extends JpaRepository<Applicazione, Long> {

    Optional<Applicazione> findByCodApplicazione(String codApplicazione);

}
