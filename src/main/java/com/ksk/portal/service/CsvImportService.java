package com.ksk.portal.service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.ksk.portal.model.MessageJob;
import com.ksk.portal.repo.MessageJobRepository;
import org.apache.commons.csv.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CsvImportService {
    private final MessageJobRepository jobs;
    private final int maxRows;

    public CsvImportService(MessageJobRepository jobs, @Value("${app.csv.max-rows:50000}") int maxRows) {
        this.jobs = jobs;
        this.maxRows = maxRows;
    }

    public int importCsv(String businessId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Choose a non-empty CSV file.");
        String filename = Optional.ofNullable(file.getOriginalFilename()).orElse("upload.csv").replaceAll("[^A-Za-z0-9._-]", "_");
        if (!filename.toLowerCase(Locale.ROOT).endsWith(".csv"))
            throw new IllegalArgumentException("Only .csv files are accepted.");
        List<MessageJob> pending = new ArrayList<>();
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setIgnoreEmptyLines(true).setTrim(true).get().parse(reader)) {
            Map<String, Integer> h = new HashMap<>();
            parser.getHeaderMap().forEach((k, v) -> h.put(k.trim().toLowerCase(Locale.ROOT), v));
            if (!h.containsKey("to") || (!h.containsKey("body") && !h.containsKey("templatename")))
                throw new IllegalArgumentException("CSV needs 'to' and either 'body' or 'templateName' columns.");
            int rowNo = 1;
            for (CSVRecord row : parser) {
                rowNo++;
                if (pending.size() >= maxRows)
                    throw new IllegalArgumentException("CSV exceeds maximum rows: " + maxRows);
                String to = col(row, h.get("to")).replace("+", "");
                if (!to.matches("[0-9]{8,15}"))
                    throw new IllegalArgumentException("Invalid phone number at CSV row " + rowNo + ". Use country code and digits.");
                String template = col(row, h.get("templatename"));
                String body = col(row, h.get("body"));
                if (template.isBlank() && body.isBlank())
                    throw new IllegalArgumentException("CSV row " + rowNo + " requires body or templateName.");
                MessageJob j = new MessageJob();
                j.setBusinessId(businessId);
                j.setCsvFileName(filename);
                j.setPhoneNumber(to);
                if (!template.isBlank()) {
                    j.setMessageType("TEMPLATE");
                    j.setTemplateName(template);
                    j.setLanguageCode(or(col(row, h.get("languagecode")), "en_US"));
                    j.setTemplateParameters(split(col(row, h.get("parameters"))));
                } else {
                    j.setMessageType("TEXT");
                    j.setMessageBody(body);
                }
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
