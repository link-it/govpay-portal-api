package it.govpay.portal.entity;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CausaleSemplice implements Causale {

    private static final long serialVersionUID = 1L;
    private String causale;

    public CausaleSemplice() {
    }

    public CausaleSemplice(String causale) {
        this.causale = causale;
    }

    @Override
    public String encode() {
        if (this.causale == null) {
            return null;
        }
        return "01 " + Base64.getEncoder().encodeToString(this.causale.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String getSimple() {
        return this.causale;
    }

    public String getCausale() {
        return this.causale;
    }

    public void setCausale(String causale) {
        this.causale = causale;
    }

    @Override
    public String toString() {
        return this.causale;
    }
}
