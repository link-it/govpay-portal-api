package it.govpay.portal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "govpay.security")
public class SecurityProperties {

    private SpidHeaders spidHeaders = new SpidHeaders();

    public SpidHeaders getSpidHeaders() {
        return spidHeaders;
    }

    public void setSpidHeaders(SpidHeaders spidHeaders) {
        this.spidHeaders = spidHeaders;
    }

    public static class SpidHeaders {
        private String fiscalNumber = "X-SPID-FISCALNUMBER";
        private String name = "X-SPID-NAME";
        private String familyName = "X-SPID-FAMILYNAME";
        private String email = "X-SPID-EMAIL";
        private String mobilePhone = "X-SPID-MOBILEPHONE";
        private String address = "X-SPID-ADDRESS";

        public String getFiscalNumber() {
            return fiscalNumber;
        }

        public void setFiscalNumber(String fiscalNumber) {
            this.fiscalNumber = fiscalNumber;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFamilyName() {
            return familyName;
        }

        public void setFamilyName(String familyName) {
            this.familyName = familyName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getMobilePhone() {
            return mobilePhone;
        }

        public void setMobilePhone(String mobilePhone) {
            this.mobilePhone = mobilePhone;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }

}
