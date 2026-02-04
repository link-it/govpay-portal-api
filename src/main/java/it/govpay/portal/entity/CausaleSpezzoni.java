package it.govpay.portal.entity;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class CausaleSpezzoni implements Causale {

    private static final long serialVersionUID = 1L;
    private List<String> spezzoni;

    public CausaleSpezzoni() {
        this.spezzoni = new ArrayList<>();
    }

    @Override
    public String encode() {
        if (this.spezzoni == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder("02");
        for (String spezzone : this.spezzoni) {
            sb.append(" ").append(Base64.getEncoder().encodeToString(spezzone.getBytes(StandardCharsets.UTF_8)));
        }
        return sb.toString();
    }

    @Override
    public String getSimple() {
        if (this.spezzoni != null && !this.spezzoni.isEmpty()) {
            return this.spezzoni.get(0);
        }
        return "";
    }

    public List<String> getSpezzoni() {
        return this.spezzoni;
    }

    public void setSpezzoni(List<String> spezzoni) {
        this.spezzoni = spezzoni;
    }

    @Override
    public String toString() {
        return String.join("; ", this.spezzoni);
    }
}
