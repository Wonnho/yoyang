package com.yoyang.api;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {

    @GetMapping("/api/v1/status")
    public Map<String, String> status() {
        return Map.of("status", "ok");
    }
}
