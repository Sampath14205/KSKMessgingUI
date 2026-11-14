package com.ksk.portal.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("portal_business_users")
public class PortalUser {
    @Id
    private String id;
    @Indexed(unique = true)
    private String email;
    @Indexed(unique = true)
    private String businessId;
    private String businessName, contactName, phone, passwordHash;
    private boolean enabled = true;
    private Instant createdAt = Instant.now(), updatedAt = Instant.now();

    public String getId() {
        return id;
    }

    public void setId(String v) {
        id = v;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String v) {
        email = v == null ? null : v.trim().toLowerCase();
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

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String v) {
        contactName = v;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String v) {
        phone = v;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String v) {
        passwordHash = v;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean v) {
        enabled = v;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void touch() {
        updatedAt = Instant.now();
    }
}
