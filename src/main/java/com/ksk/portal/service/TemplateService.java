package com.ksk.portal.service;

import java.time.Instant;
import java.util.*;

import com.ksk.portal.model.BusinessTemplate;
import com.ksk.portal.repo.BusinessTemplateRepository;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class TemplateService {
    private final BusinessTemplateRepository repo;
    private final CurrentBusiness current;
    private final MongoTemplate mongo;

    public TemplateService(BusinessTemplateRepository repo, CurrentBusiness current, MongoTemplate mongo) {
        this.repo = repo;
        this.current = current;
        this.mongo = mongo;
    }

    public BusinessTemplate save(Map<String, Object> input) {
        BusinessTemplate t = new BusinessTemplate();
        apply(t, input);
        t.setBusinessId(current.id());
        t.setCreatedAt(Instant.now());
        t.setUpdatedAt(Instant.now());
        return repo.save(t);
    }

    public BusinessTemplate update(String id, Map<String, Object> input) {
        BusinessTemplate t = repo.findById(id).filter(x -> current.id().equals(x.getBusinessId()))
                .orElseThrow(() -> new IllegalArgumentException("Template not found."));
        apply(t, input);
        t.setUpdatedAt(Instant.now());
        return repo.save(t);
    }

    private void apply(BusinessTemplate t, Map<String, Object> input) {
        String name = string(input, "name");
        String body = string(input, "bodyText");
        if (name.isBlank() || body.isBlank()) throw new IllegalArgumentException("Template name and body are required.");
        t.setName(name.trim());
        t.setLanguageCode(or(string(input, "languageCode"), "en_US"));
        t.setCategory(or(string(input, "category"), "UTILITY").toUpperCase(Locale.ROOT));
        t.setParameterFormat(or(string(input, "parameterFormat"), "POSITIONAL").toUpperCase(Locale.ROOT));
        t.setHeaderType(or(string(input, "headerType"), "NONE").toUpperCase(Locale.ROOT));
        t.setHeaderText(string(input, "headerText"));
        t.setHeaderMediaUrl(string(input, "headerMediaUrl"));
        if (!t.getHeaderMediaUrl().isBlank() && !t.getHeaderMediaUrl().matches("(?i)^https://.+"))
            throw new IllegalArgumentException("Header media URL must be a public HTTPS URL.");
        t.setBodyText(body);
        t.setFooterText(string(input, "footerText"));
        Object buttons = input.get("buttons");
        t.setButtons(buttons instanceof List<?> ? (List<Map<String, Object>>) buttons : List.of());
        Object examples = input.get("examples");
        t.setExamples(examples instanceof List<?> ? (List<String>) examples : List.of());
        t.setPlaceholders(extractPlaceholders(body));
        List<Map<String, Object>> components = new ArrayList<>();
        if (!"NONE".equals(t.getHeaderType())) {
            Map<String, Object> h = new LinkedHashMap<>(); h.put("type", "HEADER"); h.put("format", t.getHeaderType());
            if (!t.getHeaderText().isBlank()) h.put("text", t.getHeaderText());
            if (!t.getHeaderMediaUrl().isBlank()) h.put("mediaUrl", t.getHeaderMediaUrl());
            components.add(h);
        }
        components.add(Map.of("type", "BODY", "text", body, "examples", t.getExamples()));
        if (!t.getFooterText().isBlank()) components.add(Map.of("type", "FOOTER", "text", t.getFooterText()));
        if (!t.getButtons().isEmpty()) components.add(Map.of("type", "BUTTONS", "buttons", t.getButtons()));
        Object customComponents = input.get("components");
        if (customComponents instanceof List<?> customList && !customList.isEmpty()) {
            t.setComponents((List<Map<String, Object>>) customList);
        } else {
            t.setComponents(components);
        }
    }

    private List<String> extractPlaceholders(String body) {
        List<String> values = new ArrayList<>();
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\{\\{\\s*(\\d+)\\s*}}") .matcher(body);
        while (m.find()) if (!values.contains(m.group(1))) values.add(m.group(1));
        return values;
    }

    private String string(Map<String, Object> m, String key) { Object v = m.get(key); return v == null ? "" : String.valueOf(v).trim(); }
    private String or(String v, String d) { return v.isBlank() ? d : v; }

    public List<BusinessTemplate> list() { return repo.findByBusinessIdOrderByCreatedAtDesc(current.id()); }

    public Map<String, Object> getWhatsAppConfig() {
        Document d = mongo.findOne(Query.query(Criteria.where("businessId").is(current.id())), Document.class, "whatsapp_configurations");
        Map<String, Object> result = d == null ? new LinkedHashMap<>() : new LinkedHashMap<>(d);
        result.remove("_id");
        result.putIfAbsent("businessId", current.id());
        result.putIfAbsent("graphUrl", "https://graph.facebook.com");
        result.putIfAbsent("graphVersion", "v23.0");
        return result;
    }

    public Map<String, Object> saveWhatsAppConfig(Map<String, Object> input) {
        Map<String, Object> values = new LinkedHashMap<>();
        input.forEach((k, v) -> {
            if (k == null || !k.matches("[A-Za-z][A-Za-z0-9_]{0,63}") || "_id".equals(k)) return;
            if (v instanceof String s) values.put(k, s.trim()); else if (v instanceof Number || v instanceof Boolean || v instanceof List<?> || v instanceof Map<?, ?>) values.put(k, v);
        });
        values.put("businessId", current.id());
        values.put("updatedAt", Instant.now());
        Query q = Query.query(Criteria.where("businessId").is(current.id()));
        // Persist the collection as flat fields so existing KSK service lookups remain compatible.
        Update update = new Update();
        Document existing = mongo.findOne(q, Document.class, "whatsapp_configurations");
        if (existing != null) {
            for (String existingKey : existing.keySet()) {
                if (!"_id".equals(existingKey) && !values.containsKey(existingKey)) update.unset(existingKey);
            }
        }
        values.forEach(update::set);
        mongo.upsert(q, update, "whatsapp_configurations");
        return getWhatsAppConfig();
    }
}
