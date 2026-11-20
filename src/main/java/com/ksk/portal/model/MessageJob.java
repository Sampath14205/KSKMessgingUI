package com.ksk.portal.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("portal_message_jobs")
@CompoundIndex(name = "batch_claim_idx", def = "{'status':1,'nextAttemptAt':1,'createdAt':1}")
public class MessageJob {
    @Id
    private String id;
    private String businessId, csvFileName, phoneNumber, messageType, messageBody, templateName, languageCode, imageUrl, status, providerMessageId, lastError;
    private java.util.List<String> templateParameters;
    private Instant createdAt = Instant.now(), updatedAt = Instant.now(), nextAttemptAt = Instant.now(), sentAt;
    private int attempts;

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

    public String getCsvFileName() {
        return csvFileName;
    }

    public void setCsvFileName(String v) {
        csvFileName = v;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String v) {
        phoneNumber = v;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String v) {
        messageType = v;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String v) {
        messageBody = v;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String v) {
        templateName = v;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String v) {
        languageCode = v;
    }

    public java.util.List<String> getTemplateParameters() {
        return templateParameters;
    }

    public void setTemplateParameters(java.util.List<String> v) {
        templateParameters = v;
    }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String v) { imageUrl = v; }

    public String getStatus() {
        return status;
    }

    public void setStatus(String v) {
        status = v;
    }

    public String getProviderMessageId() {
        return providerMessageId;
    }

    public void setProviderMessageId(String v) {
        providerMessageId = v;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String v) {
        lastError = v;
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

    public Instant getNextAttemptAt() {
        return nextAttemptAt;
    }

    public void setNextAttemptAt(Instant v) {
        nextAttemptAt = v;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant v) {
        sentAt = v;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int v) {
        attempts = v;
    }
}
