package com.ksk.portal.repo;

import java.util.*;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.ksk.portal.model.MessageJob;

public interface MessageJobRepository extends MongoRepository<MessageJob, String> {
    List<MessageJob> findTop200ByBusinessIdOrderByCreatedAtDesc(String businessId);

    long countByBusinessIdAndStatus(String businessId, String status);
}
