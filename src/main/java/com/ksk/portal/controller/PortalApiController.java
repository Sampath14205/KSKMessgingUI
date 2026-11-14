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

    public PortalApiController(CurrentBusiness c, CsvImportService csv, SignupService signup, MessageJobRepository jobs, TemplateService templates, KskMessagingClient sender, PortalUserRepository users) {
        this.current = c;
        this.csv = csv;
        this.signup = signup;
        this.jobs = jobs;
        this.templates = templates;
        this.sender = sender;
        this.users = users;
    }

    @PostMapping(value = "/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) throws Exception {
        return Map.of("success", true, "queuedMessages", csv.importCsv(current.id(), file), "message", "CSV rows saved for scheduled dispatch.");
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
    public BusinessTemplate createTemplate(@RequestParam String name, @RequestParam(defaultValue = "en_US") String languageCode, @RequestParam(defaultValue = "UTILITY") String category, @RequestParam String bodyText, @RequestParam(required = false) String placeholders) {
        return templates.save(name, languageCode, category, bodyText, placeholders);
    }

    @PostMapping("/quick-send")
    public Map<String, Object> quick(@RequestParam String to, @RequestParam String type, @RequestParam(required = false) String body, @RequestParam(required = false) String templateName, @RequestParam(defaultValue = "en_US") String languageCode, @RequestParam(required = false) String parameters) {
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
        String response = sender.send(j, current.user().getBusinessName());
        return Map.of("success", true, "response", response == null ? "" : response);
    }
}
