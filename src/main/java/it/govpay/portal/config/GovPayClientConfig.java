package it.govpay.portal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import it.govpay.pendenze.client.ApiClient;
import it.govpay.pendenze.client.api.PendenzeApi;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "govpay.pendenze")
@Data
public class GovPayClientConfig {

    private String baseUrl;
    private String username;
    private String password;

    @Bean
    public PendenzeApi pendenzeApi() {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(baseUrl);
        apiClient.setUsername(username);
        apiClient.setPassword(password);
        return new PendenzeApi(apiClient);
    }
}
