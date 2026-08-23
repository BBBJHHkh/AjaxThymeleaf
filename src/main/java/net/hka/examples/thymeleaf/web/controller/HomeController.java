package net.hka.examples.thymeleaf.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
class HomeController {

    @ModelAttribute("module")
    String module() {
        return "home";
    }

    @GetMapping("/")
    String index(Principal principal) {
        return principal != null ? "home/homeSignedIn" : "home/homeNotSignedIn";
    }

    @GetMapping("/thymeleaf")
    String thymeleafIndex() {
        return "redirect:/thymeleaf/excel-upload";
    }
}
