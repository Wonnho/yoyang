package com.yoyang.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AboutController {

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/about.html")
    public String redirectLegacyAbout() {
        return "redirect:/about";
    }

    @GetMapping("/services")
    public String services() {
        return "services";
    }

    @GetMapping("/caregivers")
    public String caregivers() {
        return "caregivers";
    }
}
