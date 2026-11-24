package com.ksk.portal.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

@Service
public class KskImageUploadService {
    private final RestClient client;
    private final ObjectMapper mapper;

    public KskImageUploadService(RestClient.Builder builder,
                                 @Value("${app.ksk.base-url}") String baseUrl,
                                 ObjectMapper mapper) {
        this.client = builder.baseUrl(baseUrl.replaceAll("/$", "")).build();
        this.mapper = mapper;
    }

    public String upload(MultipartFile image, String businessId) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("businessId", businessId);
            body.add("image", getFileBytes(image));
            String response = client.post().uri(uriBuilder -> uriBuilder
                    .path("/api/images/upload")
                    .build())
                    .contentType(MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve().body(String.class);
            String url = extractUrl(response);
            if (url == null || url.isBlank()) {
                throw new IllegalStateException("KSK Messaging image upload API did not return an image URL. Response: " + response);
            }
            return url;
        } catch (Exception ex) {
            throw new IllegalStateException("Image upload failed through KSK_MESSAGING_BASE_URL /api/images/upload: " + ex.getMessage(), ex);
        }
    }

    private ByteArrayResource getFileBytes(MultipartFile image) {
        try {
            return new ByteArrayResource(image.getBytes()) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename();
                }
            };
        } catch (IOException e) {
            throw new RuntimeException("Failed to read image", e);
        }
    }

    private String extractUrl(String response) {
        if (response == null || response.isBlank()) return null;
        try {
            JsonNode root = mapper.readTree(response);
            String found = findUrl(root);
            if (found != null) return found;
        } catch (Exception ignored) {
            String plain = response.trim();
            if (plain.startsWith("https://") || plain.startsWith("http://")) return plain;
        }
        return null;
    }

    private String findUrl(JsonNode node) {
        if (node == null) return null;
        if (node.isObject()) {
            for (String key : new String[]{"imageUrl", "imageurl", "url", "fileUrl", "publicUrl", "location"}) {
                JsonNode value = node.get(key);
                if (value != null && value.isTextual() && !value.asText().isBlank()) return value.asText();
            }
            var fields = node.fields();
            while (fields.hasNext()) {
                String found = findUrl(fields.next().getValue());
                if (found != null) return found;
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                String found = findUrl(child);
                if (found != null) return found;
            }
        }
        return null;
    }
}
