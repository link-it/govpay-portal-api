package it.govpay.portal.gde.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypes;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypesScanner;

import it.govpay.common.client.config.GovPayClientAutoConfiguration;
import it.govpay.common.entity.ConnettoreEntity;
import it.govpay.common.repository.ConnettoreEntityRepository;
import jakarta.persistence.EntityManager;

@Configuration
@ComponentScan(
    basePackages = "it.govpay.common.client",
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = GovPayClientAutoConfiguration.class
    )
)
public class GovPayCommonConfig {

    @Bean
    PersistenceManagedTypes persistenceManagedTypes(ResourceLoader resourceLoader) {
        PersistenceManagedTypes scanned = new PersistenceManagedTypesScanner(resourceLoader)
            .scan("it.govpay.portal.entity");
        List<String> classes = new ArrayList<>(scanned.getManagedClassNames());
        classes.add(ConnettoreEntity.class.getName());
        return PersistenceManagedTypes.of(classes, scanned.getManagedPackages());
    }

    @Bean
    ConnettoreEntityRepository connettoreEntityRepository(EntityManager entityManager) {
        return new JpaRepositoryFactory(entityManager).getRepository(ConnettoreEntityRepository.class);
    }
}
