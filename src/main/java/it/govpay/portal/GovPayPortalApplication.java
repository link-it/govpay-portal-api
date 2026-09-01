package it.govpay.portal;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypes;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypesScanner;

import it.govpay.common.entity.ConfigurazioneEntity;
import it.govpay.common.entity.ConnettoreEntity;
import it.govpay.common.repository.ApplicazioneRepository;
import it.govpay.common.repository.DominioLogoRepository;
import it.govpay.common.repository.DominioRepository;
import it.govpay.common.repository.IntermediarioRepository;
import it.govpay.common.repository.StazioneRepository;

@SpringBootApplication(exclude = { DataJpaRepositoriesAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class })
@EnableJpaRepositories(
    basePackages = {
        "it.govpay.portal.repository",
        "it.govpay.common.repository"
    },
    // Il portale usa solo ConfigurazioneRepository e ConnettoreEntityRepository di govpay-common:
    // gli altri insistono su entità non registrate nella persistence unit e i loro nomi
    // collidono con gli omonimi repository di it.govpay.portal.repository.
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {
            ApplicazioneRepository.class,
            DominioRepository.class,
            DominioLogoRepository.class,
            IntermediarioRepository.class,
            StazioneRepository.class
        }
    )
)
public class GovPayPortalApplication extends SpringBootServletInitializer {

    /**
     * Persistence unit del portale: le entità proprie più le sole entità di govpay-common
     * effettivamente utilizzate. Sta qui, e non in una @Configuration a parte, perché deve
     * essere visibile anche agli slice test @DataJpaTest.
     */
    @Bean
    PersistenceManagedTypes persistenceManagedTypes(ResourceLoader resourceLoader) {
        PersistenceManagedTypes scanned = new PersistenceManagedTypesScanner(resourceLoader)
            .scan("it.govpay.portal.entity");
        List<String> classes = new ArrayList<>(scanned.getManagedClassNames());
        classes.add(ConnettoreEntity.class.getName());
        classes.add(ConfigurazioneEntity.class.getName());
        return PersistenceManagedTypes.of(classes, scanned.getManagedPackages());
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(GovPayPortalApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(GovPayPortalApplication.class, args);
    }

}
