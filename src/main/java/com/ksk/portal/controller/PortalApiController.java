package com.ksk.portal.controller;

import java.security.Principal;
import java.util.*;

import com.ksk.portal.service.*;
import com.ksk.portal.repo.*;
import com.ksk.portal.model.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.*;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api")
@Validated
public class PortalApiController {
    private final CurrentBusiness current;
    private final CsvImportService csv;
    private final SignupService signup;
    private final MessageJobRepository jobs;
    private final TemplateService templates;
    private final KskMessagingClient sender;
    private final PortalUserRepository users;
    private final KskImageUploadService imageUploadService;
    private final BusinessTemplateRepository templateRepo;

    public PortalApiController(CurrentBusiness c, CsvImportService csv, SignupService signup, MessageJobRepository jobs, TemplateService templates, KskMessagingClient sender, PortalUserRepository users, KskImageUploadService imageUploadService, BusinessTemplateRepository templateRepo) {
        this.current = c;
        this.csv = csv;
        this.signup = signup;
        this.jobs = jobs;
        this.templates = templates;
        this.sender = sender;
        this.users = users; this.imageUploadService = imageUploadService; this.templateRepo = templateRepo;
    }

    @PostMapping(value = "/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) throws Exception {
        current.requireActive();
        return Map.of("success", true, "queuedMessages", csv.importCsv(current.id(), file), "message", "CSV rows saved for scheduled dispatch.");
    }


    @PostMapping(value="/image-upload", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String,Object> uploadImage(@RequestParam("file") MultipartFile file) throws Exception {
        current.requireActive();
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Choose an image to upload.");
        String type = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!type.startsWith("image/")) throw new IllegalArgumentException("Only image files are allowed.");
        if (file.getSize() > 10L * 1024 * 1024) throw new IllegalArgumentException("Image size must not exceed 10 MB.");
        BusinessTemplate template = templateRepo.findFirstByBusinessIdOrderByCreatedAtDesc(current.id())
                .orElseThrow(() -> new IllegalStateException("No template is configured for this business. Ask the administrator to create a template first."));
        String url = imageUploadService.upload(file,current.id());
        template.setImageUrl(url);
        template.setUpdatedAt(java.time.Instant.now());
        templateRepo.save(template);
        return Map.of("success", true, "imageUrl", url, "message", "Image uploaded through KSK Messaging Service and saved to your business template.");
    }

    @GetMapping("/image-url")
    public Map<String,Object> imageUrl() {
        return templateRepo.findFirstByBusinessIdOrderByCreatedAtDesc(current.id())
                .map(t -> Map.<String,Object>of("templateName", t.getName() == null ? "" : t.getName(), "imageUrl", t.getImageUrl() == null ? "" : t.getImageUrl()))
                .orElse(Map.of("imageUrl", ""));
    }

    @GetMapping("/history")
    public List<MessageJob> history() {
        return jobs.findTop200ByBusinessIdOrderByCreatedAtDesc(current.id());
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        String b = current.id();
        return Map.of("pending", jobs.countByBusinessIdAndStatus(b, "PENDING"), "processing", jobs.countByBusinessIdAndStatus(b, "PROCESSING"), "sent", jobs.countByBusinessIdAndStatus(b, "SENT"), "failed", jobs.countByBusinessIdAndStatus(b, "FAILED"));
    }

    @PostMapping("/password")
    public Map<String, Object> password(Principal p, @RequestParam String currentPassword, @RequestParam String newPassword) {
        signup.changePassword(p.getName(), currentPassword, newPassword);
        return Map.of("success", true);
    }

    @GetMapping("/templates")
    public List<BusinessTemplate> templates() {
        return templates.list();
    }

    @PostMapping("/templates")
    public BusinessTemplate createTemplate(@RequestBody Map<String, Object> request) {
        return templates.save(request);
    }

    @PutMapping("/templates/{id}")
    public BusinessTemplate updateTemplate(@PathVariable String id, @RequestBody Map<String, Object> request) {
        return templates.update(id, request);
    }

    @GetMapping("/whatsapp-config")
    public Map<String, Object> whatsappConfig() { return templates.getWhatsAppConfig(); }

    @PutMapping("/whatsapp-config")
    public Map<String, Object> saveWhatsAppConfig(@RequestBody Map<String, Object> request) {
        return templates.saveWhatsAppConfig(request);
    }

    @PostMapping("/quick-send")
    public Map<String, Object> quick(@RequestParam String to, @RequestParam String type, @RequestParam(required = false) String body, @RequestParam(required = false) String templateName, @RequestParam(defaultValue = "en_US") String languageCode, @RequestParam(required = false) String parameters, @RequestParam(required = false) String imageUrl) {
        current.requireActive();
        String phone = to.replace("+", "");
        if (!phone.matches("[0-9]{8,15}"))
            throw new IllegalArgumentException("Enter a valid phone number with country code.");
        MessageJob j = new MessageJob();
        j.setBusinessId(current.id());
        j.setPhoneNumber(phone);
        j.setMessageType("TEMPLATE".equalsIgnoreCase(type) ? "TEMPLATE" : "TEXT");
        j.setMessageBody(body);
        j.setTemplateName(templateName);
        j.setLanguageCode(languageCode);
        j.setTemplateParameters(parameters == null ? List.of() : Arrays.stream(parameters.split("\\|")).map(String::trim).toList());
        if (imageUrl != null && !imageUrl.isBlank()) {
            if (!imageUrl.matches("(?i)^https://.+")) throw new IllegalArgumentException("Image URL must be a public HTTPS URL.");
            j.setImageUrl(imageUrl);
        }
        String response = sender.send(j, current.user().getBusinessName());
        return Map.of("success", true, "response", response == null ? "" : response);
    }
}
