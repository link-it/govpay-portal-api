package it.govpay.portal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import it.govpay.stampe.client.ApiClient;
import it.govpay.stampe.client.api.PaymentNoticeApi;
import it.govpay.stampe.client.api.ReceiptApi;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "govpay.stampe")
@Data
public class GovPayStampeClientConfig {

    private String baseUrl;
    private Logo logo = new Logo();

    @Data
    public static class Logo {
        private String pagopa;
    }

    @Bean
    public PaymentNoticeApi paymentNoticeApi() {
        ApiClient apiClient = createApiClient();
        return new PaymentNoticeApi(apiClient);
    }

    @Bean
    public ReceiptApi receiptApi() {
        ApiClient apiClient = createApiClient();
        return new ReceiptApi(apiClient);
    }

    private ApiClient createApiClient() {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(baseUrl);
        return apiClient;
    }
}
