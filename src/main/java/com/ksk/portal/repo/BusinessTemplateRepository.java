package com.ksk.portal.repo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.ksk.portal.model.BusinessTemplate;

public interface BusinessTemplateRepository extends MongoRepository<BusinessTemplate, String> {
    List<BusinessTemplate> findByBusinessIdOrderByCreatedAtDesc(String businessId);
    List<BusinessTemplate> findAllByOrderByCreatedAtDesc();
    java.util.Optional<BusinessTemplate> findFirstByBusinessIdOrderByCreatedAtDesc(String businessId);
}
