package it.govpay.portal.security.hardening.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hardening {

    public static final String CONFIGURAZIONE_HARDENING = "hardening";

    private boolean abilitato;
    private GoogleCaptcha googleCaptcha;
}
