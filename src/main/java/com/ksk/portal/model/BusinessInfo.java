package com.ksk.portal.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("business_info")
public class BusinessInfo {
    @Id
    private String id;
    @Indexed(unique = true)
    private String businessId;
    private String businessName, legalName, email, phone, website, address;
    private boolean isActive = true;
    private Instant warrantyUpdatedAt = Instant.now();
    private Instant createdAt = Instant.now(), updatedAt = Instant.now();

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public Instant getWarrantyUpdatedAt() { return warrantyUpdatedAt; }
    public String getWarrantyDate() { return warrantyUpdatedAt == null ? "" : warrantyUpdatedAt.atZone(java.time.ZoneOffset.UTC).toLocalDate().toString(); }
    public void setWarrantyUpdatedAt(Instant value) { warrantyUpdatedAt = value; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant value) { updatedAt = value; }

    public String getId() {
        return id;
    }

    public void setId(String v) {
        id = v;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String v) {
        businessId = v;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String v) {
        businessName = v;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String v) {
        legalName = v;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String v) {
        email = v;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String v) {
        phone = v;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String v) {
        website = v;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String v) {
        address = v;
    }
}
