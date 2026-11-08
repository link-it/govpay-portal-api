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

    /**
     * L'avviso si puo' pagare anche dal sito dell'ente ({@code creditor.pay_on_website}):
     * la stampa riporta "oppure sul sito di &lt;ente&gt;, ...". Va attivata quando e' in
     * esercizio il portale di pagamento dell'ente collegato a queste API; spenta, la
     * stampa riporta solo "oppure dal tuo Home Banking, ...".
     */
    private boolean payOnWebsite = false;

    @Data
    public static class Logo {
        /** Logo pagoPA delle ricevute: testo base64 dell'immagine, anche come URI data. */
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
