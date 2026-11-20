package com.ksk.portal.controller;

import com.ksk.portal.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@Controller
public class PageController {
    private final SignupService signup;
    private final CurrentBusiness current;

    public PageController(SignupService signup, CurrentBusiness current) {
        this.signup = signup;
        this.current = current;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @PostMapping("/signup")
    public String signupPost(@RequestParam String businessName, @RequestParam String contactName, @RequestParam String email, @RequestParam String phone, @RequestParam String password, Model m) {
        try {
            signup.signup(businessName, contactName, email, phone, password);
            return "redirect:/login?registered";
        } catch (Exception e) {
            m.addAttribute("error", e.getMessage());
            return "signup";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model m) {
        var u = current.user();
        m.addAttribute("business", u);
        return "dashboard";
    }

    @GetMapping("/settings")
    public String settings() {
        return "settings";
    }

    @GetMapping("/whatsapp-config")
    public String whatsappConfig() { return "whatsapp-config"; }
}
