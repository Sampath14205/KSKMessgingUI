package com.ksk.portal.service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.ksk.portal.model.MessageJob;
import com.ksk.portal.repo.MessageJobRepository;
import com.ksk.portal.repo.BusinessInfoRepository;
import com.ksk.portal.repo.BusinessTemplateRepository;
import com.ksk.portal.model.BusinessTemplate;
import org.apache.commons.csv.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CsvImportService {
    private final MessageJobRepository jobs;
    private final int maxRows;
    private final BusinessInfoRepository businesses;
    private final BusinessTemplateRepository templates;

    public CsvImportService(MessageJobRepository jobs, BusinessInfoRepository businesses, BusinessTemplateRepository templates, @Value("${app.csv.max-rows:50000}") int maxRows) {
        this.jobs = jobs; this.businesses = businesses; this.templates = templates;
        this.maxRows = maxRows;
    }

    public int importCsv(String businessId, MultipartFile file) throws IOException {
        var business = businesses.findByBusinessId(businessId).orElseThrow(() -> new IllegalStateException("Business information was not found. Please contact KSK support."));
        if (!business.isActive()) throw new IllegalStateException("Your business account is inactive. CSV upload is disabled. Please contact the administrator.");
        BusinessTemplate businessTemplate = templates.findFirstByBusinessIdOrderByCreatedAtDesc(businessId).orElseThrow(() -> new IllegalStateException("No template is configured for this business in portal_business_templates. Please ask the administrator to add a template before uploading a CSV file."));
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Choose a non-empty CSV file.");
        String filename = Optional.ofNullable(file.getOriginalFilename()).orElse("upload.csv").replaceAll("[^A-Za-z0-9._-]", "_");
        if (!filename.toLowerCase(Locale.ROOT).endsWith(".csv"))
            throw new IllegalArgumentException("Only .csv files are accepted.");
        List<MessageJob> pending = new ArrayList<>();
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setIgnoreEmptyLines(true).setTrim(true).get().parse(reader)) {
            Map<String, Integer> h = new HashMap<>();
            parser.getHeaderMap().forEach((k, v) -> h.put(k.trim().toLowerCase(Locale.ROOT), v));
            if (!h.containsKey("phonenumber") || !h.containsKey("message"))
                throw new IllegalArgumentException("CSV must contain exactly the required columns: phoneNumber,message.");
            int rowNo = 1;
            for (CSVRecord row : parser) {
                rowNo++;
                if (pending.size() >= maxRows)
                    throw new IllegalArgumentException("CSV exceeds maximum rows: " + maxRows);
                String to = col(row, h.get("phonenumber")).replace("+", "");
                if (!to.matches("[0-9]{8,15}"))
                    throw new IllegalArgumentException("Invalid phone number at CSV row " + rowNo + ". Use country code and digits.");
                String body = col(row, h.get("message"));
                if (body.isBlank()) throw new IllegalArgumentException("CSV row " + rowNo + " requires a message value.");
                MessageJob j = new MessageJob();
                j.setBusinessId(businessId);
                j.setCsvFileName(filename);
                j.setPhoneNumber(to);
                j.setMessageType("TEMPLATE");
                j.setTemplateName(businessTemplate.getName());
                j.setLanguageCode(or(businessTemplate.getLanguageCode(), "en_US"));
                j.setMessageBody(body);
                j.setTemplateParameters(split(body));
                String imageUrl = businessTemplate.getImageUrl();
                if (imageUrl == null || imageUrl.isBlank()) imageUrl = businessTemplate.getHeaderMediaUrl();
                if (imageUrl != null && !imageUrl.isBlank()) j.setImageUrl(imageUrl);
                j.setStatus("PENDING");
                pending.add(j);
            }
        }
        if (pending.isEmpty()) throw new IllegalArgumentException("CSV has no data rows.");
        jobs.saveAll(pending);
        return pending.size();
    }

    private String col(CSVRecord r, Integer i) {
        return i == null || i >= r.size() ? "" : r.get(i).trim();
    }

    private String or(String s, String d) {
        return s.isBlank() ? d : s;
    }

    private List<String> split(String s) {
        if (s.isBlank()) return List.of();
        return Arrays.stream(s.split("\\|", -1)).map(String::trim).toList();
    }
}
