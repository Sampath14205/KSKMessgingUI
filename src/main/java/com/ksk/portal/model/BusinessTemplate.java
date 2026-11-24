package com.ksk.portal.model;

import java.time.Instant;
import java.util.*;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("portal_business_templates")
@CompoundIndex(name = "business_template_name", def = "{'businessId':1,'name':1}", unique = true)
public class BusinessTemplate {
    @Id
    private String id;
    private String businessId, imageUrl, name, languageCode, category, parameterFormat, headerType, headerText, headerMediaUrl, bodyText, footerText;
    private List<String> placeholders = new ArrayList<>(), examples = new ArrayList<>();
    private List<Map<String, Object>> buttons = new ArrayList<>(), components = new ArrayList<>();
    private Instant createdAt = Instant.now(), updatedAt = Instant.now();

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

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String value) { imageUrl = value; }

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

    public String getParameterFormat() {
        return parameterFormat;
    }

    public void setParameterFormat(String v) {
        parameterFormat = v;
    }

    public String getHeaderType() {
        return headerType;
    }

    public void setHeaderType(String v) {
        headerType = v;
    }

    public String getHeaderText() {
        return headerText;
    }

    public void setHeaderText(String v) {
        headerText = v;
    }

    public String getHeaderMediaUrl() {
        return headerMediaUrl;
    }

    public void setHeaderMediaUrl(String v) {
        headerMediaUrl = v;
    }

    public String getBodyText() {
        return bodyText;
    }

    public void setBodyText(String v) {
        bodyText = v;
    }

    public String getFooterText() {
        return footerText;
    }

    public void setFooterText(String v) {
        footerText = v;
    }

    public List<String> getPlaceholders() {
        return placeholders;
    }

    public void setPlaceholders(List<String> v) {
        placeholders = v;
    }

    public List<String> getExamples() {
        return examples;
    }

    public void setExamples(List<String> v) {
        examples = v;
    }

    public List<Map<String, Object>> getButtons() {
        return buttons;
    }

    public void setButtons(List<Map<String, Object>> v) {
        buttons = v;
    }

    public List<Map<String, Object>> getComponents() {
        return components;
    }

    public void setComponents(List<Map<String, Object>> v) {
        components = v;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant v) {
        createdAt = v;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant v) {
        updatedAt = v;
    }
}
