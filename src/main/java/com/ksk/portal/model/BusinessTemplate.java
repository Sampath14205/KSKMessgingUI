package com.ksk.portal.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("portal_business_templates")
@CompoundIndex(name = "business_template_name", def = "{'businessId':1,'name':1}", unique = true)
public class BusinessTemplate {
    @Id
    private String id;
    private String businessId, name, languageCode, category, bodyText;
    private java.util.List<String> placeholders;
    private Instant createdAt = Instant.now();

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

    public String getName() {
        return name;
    }

    public void setName(String v) {
        name = v;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String v) {
        languageCode = v;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String v) {
        category = v;
    }

    public String getBodyText() {
        return bodyText;
    }

    public void setBodyText(String v) {
        bodyText = v;
    }

    public java.util.List<String> getPlaceholders() {
        return placeholders;
    }

    public void setPlaceholders(java.util.List<String> v) {
        placeholders = v;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
