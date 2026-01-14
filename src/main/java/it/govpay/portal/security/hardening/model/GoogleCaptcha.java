package it.govpay.portal.security.hardening.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleCaptcha {

    private String serverURL;
    private String siteKey;
    private String secretKey;
    private double soglia;
    private String responseParameter;
    private boolean denyOnFail;
    private int connectionTimeout;
    private int readTimeout;
}
