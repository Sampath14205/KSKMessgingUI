package com.ksk.portal.service;

import com.ksk.portal.model.PortalUser;
import com.ksk.portal.repo.PortalUserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentBusiness {
    private final PortalUserRepository users;

    public CurrentBusiness(PortalUserRepository users) {
        this.users = users;
    }

    public PortalUser user() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return users.findByEmail(email).orElseThrow();
    }

    public String id() {
        return user().getBusinessId();
    }
}
