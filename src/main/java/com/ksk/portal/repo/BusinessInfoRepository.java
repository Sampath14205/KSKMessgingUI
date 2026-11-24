package com.ksk.portal.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.ksk.portal.model.BusinessInfo;

public interface BusinessInfoRepository extends MongoRepository<BusinessInfo, String> {
    java.util.Optional<BusinessInfo> findByBusinessId(String businessId);
    java.util.List<BusinessInfo> findAllByOrderByCreatedAtDesc();
}
