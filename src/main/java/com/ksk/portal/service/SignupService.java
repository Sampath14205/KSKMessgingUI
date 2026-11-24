package com.ksk.portal.service;

import java.util.UUID;

import com.ksk.portal.model.PortalUser;
import com.ksk.portal.model.BusinessInfo;
import com.ksk.portal.repo.BusinessInfoRepository;
import com.ksk.portal.repo.PortalUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SignupService {
    private final PortalUserRepository users;
    private final BusinessInfoRepository businesses;
    private final PasswordEncoder encoder;

    public SignupService(PortalUserRepository users, BusinessInfoRepository businesses, PasswordEncoder encoder) {
        this.users = users;
        this.businesses = businesses;
        this.encoder = encoder;
    }

    public void signup(String businessName, String contactName, String email, String phone, String password) {
        if (users.existsByEmail(email.trim().toLowerCase()))
            throw new IllegalArgumentException("An account with this email already exists.");
        PortalUser u = new PortalUser();
        u.setBusinessId("KSK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        u.setBusinessName(businessName.trim());
        u.setContactName(contactName.trim());
        u.setEmail(email);
        u.setPhone(phone);
        u.setPasswordHash(encoder.encode(password));
        users.save(u);
        BusinessInfo b = new BusinessInfo();
        b.setBusinessId(u.getBusinessId());
        b.setBusinessName(u.getBusinessName());
        b.setLegalName(u.getBusinessName());
        b.setEmail(u.getEmail());
        b.setPhone(u.getPhone());
        b.setActive(true);
        businesses.save(b);
    }

    public void changePassword(String email, String current, String next) {
        PortalUser u = users.findByEmail(email).orElseThrow();
        if (!encoder.matches(current, u.getPasswordHash()))
            throw new IllegalArgumentException("Current password is incorrect.");
        if (next == null || next.length() < 10)
            throw new IllegalArgumentException("New password must contain at least 10 characters.");
        u.setPasswordHash(encoder.encode(next));
        u.touch();
        users.save(u);
    }
}
