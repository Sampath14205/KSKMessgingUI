package com.ksk.portal.repo;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.ksk.portal.model.PortalUser;

public interface PortalUserRepository extends MongoRepository<PortalUser, String> {
    Optional<PortalUser> findByEmail(String email);

    Optional<PortalUser> findByBusinessId(String businessId);

    boolean existsByEmail(String email);
}
