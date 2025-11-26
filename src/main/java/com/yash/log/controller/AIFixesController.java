package com.yash.log.controller;


import com.yash.log.dto.AIFixRequest;
import com.yash.log.dto.AIFixResponse;
import com.yash.log.service.impl.AiFixServiceImpl;
import com.yash.log.service.services.AIFixService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class AIFixesController {

    private static final Logger log = LoggerFactory.getLogger(AIFixesController.class);

    @Autowired
    private  AIFixService aiFixService;

    @PostMapping(value = "/ai-assitant", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<AIFixResponse>> analyse(@RequestBody AIFixRequest request) {
        log.info("Request payload AIFixesController : {}", request);

        return aiFixService.analyse(request)
                .map(response -> ResponseEntity.ok(response))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }




}
