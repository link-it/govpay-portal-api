package it.govpay.portal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.govpay.portal.entity.Configurazione;

@Repository
public interface ConfigurazioneRepository extends JpaRepository<Configurazione, Long> {

    Optional<Configurazione> findByNome(String nome);
}
