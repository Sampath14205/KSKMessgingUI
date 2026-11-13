package com.example.csvwhatsapp.controller;

import org.apache.commons.csv.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
public class BulkSendController {
    private final RestClient client;
    private final String bulkApiUrl;
    private final int maxRows;

    public BulkSendController(RestClient.Builder builder,
      @Value("${whatsapp.bulk-api-url:https://kskmessagingservice-production.up.railway.app/api/whatsapp/send/bulk/text}") String bulkApiUrl,
      @Value("${app.max-csv-rows:5000}") int maxRows) {
        this.client = builder.build();
        this.bulkApiUrl = bulkApiUrl;
        this.maxRows = maxRows;
    }

    @PostMapping(value="/api/csv/send-bulk", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> sendCsv(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("error","Please select a non-empty CSV file."));
        String name = file.getOriginalFilename();
        if (name == null || !name.toLowerCase(Locale.ROOT).endsWith(".csv"))
            return ResponseEntity.badRequest().body(Map.of("error","Please upload a .csv file."));

        List<Map<String,Object>> messages = new ArrayList<>();
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true)
               .setIgnoreEmptyLines(true).setTrim(true).get().parse(reader)) {
            Map<String,Integer> headers = new HashMap<>();
            parser.getHeaderMap().forEach((k,v) -> headers.put(k.trim().toLowerCase(Locale.ROOT),v));
            if (!headers.containsKey("to") || !headers.containsKey("body") || !headers.containsKey("previewurl"))
                return ResponseEntity.badRequest().body(Map.of("error","CSV headers must be: to,body,previewUrl","receivedHeaders",parser.getHeaderNames()));

            int rowNum=1;
            for (CSVRecord row : parser) {
                rowNum++;
                String to = column(row,headers.get("to"));
                String body = column(row,headers.get("body"));
                if (to.isBlank() || body.isBlank())
                    return ResponseEntity.badRequest().body(Map.of("error","CSV row "+rowNum+" has an empty 'to' or 'body'."));
                if (!to.matches("\\+?[0-9]{8,15}"))
                    return ResponseEntity.badRequest().body(Map.of("error","CSV row "+rowNum+" has an invalid phone number. Use country code and digits, e.g. 918001234567."));
                Map<String,Object> message = new LinkedHashMap<>();
                message.put("to",to.startsWith("+") ? to.substring(1) : to);
                message.put("body",body);
                message.put("previewUrl",true); // Always true, as requested.
                messages.add(message);
                if (messages.size()>maxRows)
                    return ResponseEntity.badRequest().body(Map.of("error","CSV exceeds maximum of "+maxRows+" data rows."));
            }
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error","Unable to read CSV: "+safe(ex)));
        }
        if (messages.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error","CSV contains no message rows."));

        try {
            ResponseEntity<String> upstream = client.post().uri(bulkApiUrl)
              .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
              .body(Map.of("messages",messages)).retrieve().toEntity(String.class);
            Map<String,Object> out = new LinkedHashMap<>();
            out.put("success",upstream.getStatusCode().is2xxSuccessful());
            out.put("sentToApi",messages.size());
            out.put("upstreamStatus",upstream.getStatusCode().value());
            out.put("upstreamResponse",upstream.getBody()==null ? "" : upstream.getBody());
            return ResponseEntity.status(upstream.getStatusCode()).body(out);
        } catch (org.springframework.web.client.RestClientResponseException ex) {
            Map<String,Object> out = new LinkedHashMap<>();
            out.put("success",false); out.put("sentToApi",messages.size());
            out.put("upstreamStatus",ex.getStatusCode().value());
            out.put("upstreamResponse",ex.getResponseBodyAsString());
            return ResponseEntity.status(ex.getStatusCode()).body(out);
        } catch (Exception ex) {
            return ResponseEntity.status(502).body(Map.of("success",false,"sentToApi",messages.size(),"error","Bulk API call failed: "+safe(ex)));
        }
    }
    private static String column(CSVRecord row,Integer index) {
        return index==null || index<0 || index>=row.size() ? "" : row.get(index).trim();
    }
    private static String safe(Exception ex) {
        return ex.getMessage()==null || ex.getMessage().isBlank() ? ex.getClass().getSimpleName() : ex.getMessage();
    }
}
