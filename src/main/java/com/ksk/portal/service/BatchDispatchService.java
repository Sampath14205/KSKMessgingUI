package com.ksk.portal.service;

import java.time.Instant;
import java.util.*;

import com.ksk.portal.model.*;
import com.ksk.portal.repo.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class BatchDispatchService {
    private final MongoTemplate mongo;
    private final PortalUserRepository users;
    private final KskMessagingClient sender;
    private final int pageSize, maxAttempts, retryDelay;
    @Value("${app.batch.enabled:true}")
    private boolean enabled;

    public BatchDispatchService(MongoTemplate mongo, PortalUserRepository users, KskMessagingClient sender,
                                @Value("${app.batch.page-size:5000}") int pageSize, @Value("${app.batch.max-attempts:3}") int maxAttempts,
                                @Value("${app.batch.retry-delay-seconds:300}") int retryDelay) {
        this.mongo = mongo;
        this.users = users;
        this.sender = sender;
        this.pageSize = pageSize;
        this.maxAttempts = maxAttempts;
        this.retryDelay = retryDelay;
    }

    @Scheduled(cron = "${app.batch.cron:0 */5 * * * *}")
    public void dispatch() {
        if (!enabled) return;
        for (int i = 0; i < pageSize; i++) {
            MessageJob job = claim();
            if (job == null) break;
            sendOne(job);
        }
    }

    private MessageJob claim() {
        Query q = new Query(Criteria.where("status").is("PENDING").and("nextAttemptAt").lte(Instant.now())).with(Sort.by(Sort.Direction.ASC, "createdAt")).limit(1);
        Update u = new Update().set("status", "PROCESSING").set("updatedAt", Instant.now()).inc("attempts", 1);
        return mongo.findAndModify(q, u, FindAndModifyOptions.options().returnNew(true), MessageJob.class);
    }

    private void sendOne(MessageJob j) {
        try {
            PortalUser business = users.findByBusinessId(j.getBusinessId()).orElseThrow(() -> new IllegalStateException("Business account not found"));
            String response = sender.send(j, business.getBusinessName());
            j.setStatus("SENT");
            j.setLastError(null);
            j.setSentAt(Instant.now());
            j.setProviderMessageId(response == null ? "" : response.substring(0, Math.min(500, response.length())));
        } catch (Exception ex) {
            j.setLastError(safe(ex));
            if (j.getAttempts() < maxAttempts) {
                j.setStatus("PENDING");
                j.setNextAttemptAt(Instant.now().plusSeconds(retryDelay));
            } else j.setStatus("FAILED");
        }
        j.touch();
        mongo.save(j);
    }

    private String safe(Exception e) {
        String s = e.getMessage();
        return s == null ? e.getClass().getSimpleName() : s.substring(0, Math.min(500, s.length()));
    }
}
