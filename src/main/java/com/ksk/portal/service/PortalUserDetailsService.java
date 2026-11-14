package com.ksk.portal.service;

import com.ksk.portal.repo.PortalUserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class PortalUserDetailsService implements UserDetailsService {
    private final PortalUserRepository users;

    public PortalUserDetailsService(PortalUserRepository users) {
        this.users = users;
    }

    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var u = users.findByEmail(email.trim().toLowerCase()).orElseThrow(() -> new UsernameNotFoundException("Account not found"));
        return User.withUsername(u.getEmail()).password(u.getPasswordHash()).disabled(!u.isEnabled()).roles("BUSINESS").build();
    }
}
