package it.govpay.portal.config;

public class SpidUserDetails {

    private final String fiscalNumber;
    private final String name;
    private final String familyName;
    private final String email;
    private final String mobilePhone;
    private final String address;

    public SpidUserDetails(String fiscalNumber, String name, String familyName,
                           String email, String mobilePhone, String address) {
        this.fiscalNumber = fiscalNumber;
        this.name = name;
        this.familyName = familyName;
        this.email = email;
        this.mobilePhone = mobilePhone;
        this.address = address;
    }

    public String getFiscalNumber() {
        return fiscalNumber;
    }

    public String getName() {
        return name;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getEmail() {
        return email;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public String getAddress() {
        return address;
    }

    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        if (name != null && !name.isBlank()) {
            sb.append(name);
        }
        if (familyName != null && !familyName.isBlank()) {
            if (!sb.isEmpty()) {
                sb.append(" ");
            }
            sb.append(familyName);
        }
        return sb.toString();
    }

}
