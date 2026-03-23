package com.smartlaundry.washservice.session;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
public class WashSessionController {

    private final WashSessionService washSessionService;

    public WashSessionController(WashSessionService washSessionService) {
        this.washSessionService = washSessionService;
    }

    @GetMapping("/wash-sessions/me")
    public List<WashSessionService.WashSessionResponse> mySessions() {
        return washSessionService.getMine();
    }
}
