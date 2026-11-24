package com.ksk.portal.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("portal_templates")
public class PortalTemplate {
    @Id
    private String id;
    @Indexed(unique = true)
    private String businessId;
    private String languageCode;
    private String templateName;
    private String imageUrl;
    private Instant createdDate = Instant.now();
    private Instant updatedDate = Instant.now();

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

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String v) {
        templateName = v;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String v) {
        imageUrl = v;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant v) {
        createdDate = v;
    }

    public Instant getUpdatedDate() {
        return updatedDate;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public void setUpdatedDate(Instant v) {
        updatedDate = v;
    }

}
