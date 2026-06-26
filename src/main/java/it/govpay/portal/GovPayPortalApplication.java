package it.govpay.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(exclude = { DataJpaRepositoriesAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class })
@EnableJpaRepositories(basePackages = "it.govpay.portal.repository")
public class GovPayPortalApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(GovPayPortalApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(GovPayPortalApplication.class, args);
    }

}
