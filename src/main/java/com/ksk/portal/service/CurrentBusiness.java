package com.ksk.portal.service;

import com.ksk.portal.model.PortalUser;
import com.ksk.portal.repo.PortalUserRepository;
import com.ksk.portal.repo.BusinessInfoRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentBusiness {
    private final PortalUserRepository users;
    private final BusinessInfoRepository businesses;
    public CurrentBusiness(PortalUserRepository users, BusinessInfoRepository businesses) { this.users = users; this.businesses = businesses; }
    public PortalUser user() { String email = SecurityContextHolder.getContext().getAuthentication().getName(); return users.findByEmail(email).orElseThrow(); }
    public String id() { return user().getBusinessId(); }
    public void requireActive() { var info = businesses.findByBusinessId(id()).orElseThrow(() -> new IllegalStateException("Business information was not found. Please contact KSK support.")); if (!info.isActive()) throw new IllegalStateException("Your business account is inactive. Please contact the administrator to reactivate it."); }
}
