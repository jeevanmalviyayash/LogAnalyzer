package com.yash.log.controller;


import com.yash.log.dto.AIFixRequest;
import com.yash.log.dto.AIFixResponse;
import com.yash.log.service.services.AIFixService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/ai/assitant")
public class AIFixesController {

    private static final Logger log = LoggerFactory.getLogger(AIFixesController.class);

    private final AIFixService aiFixService;

    public AIFixesController(AIFixService aiFixService) {
        this.aiFixService = aiFixService;
    }

    @PostMapping
    public ResponseEntity<AIFixResponse> analyse(@RequestBody AIFixRequest request) {
        log.info("Request payload AIFixesController : {}", request);


        return ResponseEntity.ok(aiFixService.analyse(request));
    }




    }

