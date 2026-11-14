package com.ksk.portal.service;

import java.util.*;

import com.ksk.portal.model.BusinessTemplate;
import com.ksk.portal.repo.BusinessTemplateRepository;
import org.springframework.stereotype.Service;

@Service
public class TemplateService {
    private final BusinessTemplateRepository repo;
    private final CurrentBusiness current;

    public TemplateService(BusinessTemplateRepository repo, CurrentBusiness current) {
        this.repo = repo;
        this.current = current;
    }

    public BusinessTemplate save(String name, String language, String category, String body, String placeholders) {
        BusinessTemplate t = new BusinessTemplate();
        t.setBusinessId(current.id());
        t.setName(name.trim());
        t.setLanguageCode(language);
        t.setCategory(category);
        t.setBodyText(body);
        t.setPlaceholders(placeholders == null || placeholders.isBlank() ? List.of() : Arrays.stream(placeholders.split(",")).map(String::trim).toList());
        return repo.save(t);
    }

    public List<BusinessTemplate> list() {
        return repo.findByBusinessIdOrderByCreatedAtDesc(current.id());
    }
}
