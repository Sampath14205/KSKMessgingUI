package com.ksk.portal.service;

import java.util.*;

import com.ksk.portal.model.MessageJob;
import com.ksk.portal.model.SendTextRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class KskMessagingClient {
    private final RestClient client;

    public KskMessagingClient(RestClient.Builder builder, @Value("${app.ksk.base-url}") String baseUrl) {
        System.out.println("========baseUrl======="+baseUrl);
        this.client = builder.baseUrl(baseUrl).build();
    }

    public String send(MessageJob job, String businessName) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("businessId", job.getBusinessId());
        // payload.put("businessName", businessName);
        payload.put("to", job.getPhoneNumber());
        String path;
        SendTextRequest request = null;
        if ("TEMPLATE".equals(job.getMessageType())) {
            payload.put("templateName", job.getTemplateName());
            payload.put("languageCode", job.getLanguageCode() == null ? "en_US" : job.getLanguageCode());
            payload.put("bodyParameters", job.getTemplateParameters() == null ? List.of() : job.getTemplateParameters());
            path = "/api/whatsapp/send/template";
        } else {
            payload.put("body", job.getMessageBody());
            payload.put("previewUrl", false);
            request = new SendTextRequest(
                    job.getPhoneNumber(),
                    job.getMessageBody(),
                    job.getBusinessId(),
                    true
            );
            path = "/api/whatsapp/send/text";
        }
        System.out.println("==============="+path);
        return client.post().uri(path).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).body(request).retrieve().body(String.class);
    }
}
