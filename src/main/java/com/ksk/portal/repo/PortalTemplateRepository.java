package com.ksk.portal.repo;
import com.ksk.portal.model.PortalTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.*;
public interface PortalTemplateRepository extends MongoRepository<PortalTemplate,String>{
 Optional<PortalTemplate> findByBusinessId(String businessId);
 List<PortalTemplate> findAllByOrderByCreatedDateDesc();
}
