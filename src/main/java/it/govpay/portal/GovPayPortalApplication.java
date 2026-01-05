package it.govpay.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"it.govpay.portal", "it.govpay.ente"})
public class GovPayPortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(GovPayPortalApplication.class, args);
    }

}
