package com.ksk.portal.controller;

import com.ksk.portal.model.BusinessInfo;
import com.ksk.portal.model.BusinessTemplate;
import com.ksk.portal.repo.BusinessInfoRepository;
import com.ksk.portal.repo.BusinessTemplateRepository;
import com.ksk.portal.repo.PortalUserRepository;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

@Controller
public class AdminController {
    private final BusinessInfoRepository businesses;
    private final PortalUserRepository users;
    private final BusinessTemplateRepository templates;
    private final MongoTemplate mongo;

    public AdminController(BusinessInfoRepository businesses, PortalUserRepository users,
                           BusinessTemplateRepository templates, MongoTemplate mongo) {
        this.businesses = businesses;
        this.users = users;
        this.templates = templates;
        this.mongo = mongo;
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("businesses", businesses.findAllByOrderByCreatedAtDesc());
        model.addAttribute("templates", templates.findAllByOrderByCreatedAtDesc());
        return "admin-dashboard";
    }

    @GetMapping("/admin/api/configurations")
    @ResponseBody
    public List<Map<String, Object>> configs() {
        List<Document> docs = mongo.findAll(Document.class, "whatsapp_configurations");
        List<Map<String, Object>> out = new ArrayList<>();
        for (Document doc : docs) {
            Map<String, Object> item = new LinkedHashMap<>(doc);
            Object id = item.remove("_id");
            item.put("id", String.valueOf(id));
            out.add(item);
        }
        return out;
    }

    @PutMapping("/admin/api/business/{businessId}")
    @ResponseBody
    public BusinessInfo updateBusiness(@PathVariable String businessId, @RequestBody Map<String, Object> body) {
        BusinessInfo business = businesses.findByBusinessId(businessId)
                .orElseThrow(() -> new IllegalArgumentException("Business not found: " + businessId));
        if (body.containsKey("isActive")) {
            boolean active = Boolean.parseBoolean(String.valueOf(body.get("isActive")));
            business.setActive(active);
            users.findByBusinessId(businessId).ifPresent(user -> {
                user.setEnabled(active);
                users.save(user);
            });
        }
        if (body.containsKey("warrantyUpdatedAt")) {
            String date = String.valueOf(body.get("warrantyUpdatedAt"));
            if (date.isBlank()) throw new IllegalArgumentException("Select a warranty updated date.");
            business.setWarrantyUpdatedAt(LocalDate.parse(date).atStartOfDay(ZoneOffset.UTC).toInstant());
        }
        business.setUpdatedAt(Instant.now());
        return businesses.save(business);
    }

    @PostMapping("/admin/api/templates")
    @ResponseBody
    public BusinessTemplate createTemplate(@RequestBody Map<String, Object> body) {
        String businessId = value(body, "businessId");
        String templateName = value(body, "templateName");
        if (businessId.isBlank()) throw new IllegalArgumentException("Business ID is required.");
        if (templateName.isBlank()) throw new IllegalArgumentException("Template name is required.");
        if (!businesses.findByBusinessId(businessId).isPresent()) throw new IllegalArgumentException("Business not found.");
        BusinessTemplate template = new BusinessTemplate();
        template.setBusinessId(businessId);
        template.setName(templateName);
        template.setImageUrl(value(body, "imageUrl"));
        template.setCreatedAt(Instant.now());
        template.setUpdatedAt(Instant.now());
        return templates.save(template);
    }

    @PutMapping("/admin/api/templates/{id}")
    @ResponseBody
    public BusinessTemplate editTemplate(@PathVariable String id, @RequestBody Map<String, Object> body) {
        BusinessTemplate template = templates.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template not found."));
        if (body.containsKey("templateName")) {
            String name = value(body, "templateName");
            if (name.isBlank()) throw new IllegalArgumentException("Template name is required.");
            template.setName(name);
        }
        if (body.containsKey("imageUrl")) template.setImageUrl(value(body, "imageUrl"));
        template.setUpdatedAt(Instant.now());
        return templates.save(template);
    }

    @PostMapping("/admin/api/configurations")
    @ResponseBody
    public Map<String, Object> createConfig(@RequestBody Map<String, Object> body) {
        String businessId = value(body, "businessId");
        if (businessId.isBlank()) throw new IllegalArgumentException("Business ID is required.");
        body.remove("_id");
        body.put("businessId", businessId);
        body.put("updatedAt", Instant.now());
        mongo.save(new Document(body), "whatsapp_configurations");
        return body;
    }

    @PutMapping("/admin/api/configurations/{id}")
    @ResponseBody
    public Map<String, Object> updateConfig(@PathVariable String id, @RequestBody Map<String, Object> body) {
        Query query;
        try { query = Query.query(Criteria.where("_id").is(new ObjectId(id))); }
        catch (IllegalArgumentException ex) { query = Query.query(Criteria.where("_id").is(id)); }
        if (!mongo.exists(query, "whatsapp_configurations")) throw new IllegalArgumentException("WhatsApp configuration not found.");
        body.remove("_id");
        body.remove("id");
        body.put("updatedAt", Instant.now());
        Update update = new Update();
        body.forEach(update::set);
        mongo.updateFirst(query, update, "whatsapp_configurations");
        body.put("id", id);
        return body;
    }

    private String value(Map<String, Object> body, String key) {
        Object value = body.get(key);
        return value == null ? "" : String.valueOf(value).trim();
    }
}
