package com.yash.log.controller;


import com.yash.log.dto.AIFixRequest;
import com.yash.log.dto.AIFixResponse;
import com.yash.log.service.impl.AiFixServiceImpl;
import com.yash.log.service.services.AIFixService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/ai-fix")
@CrossOrigin("*")
public class AIFixesController {

    @Autowired
    private final AIFixService aiFixService;
    private static final Logger log = LoggerFactory.getLogger(AIFixesController.class);

    public AIFixesController(AiFixServiceImpl aiFixService) {
        this.aiFixService = aiFixService;
    }


    @PostMapping(value = "/analyse", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<AIFixResponse> analyse(@RequestBody AIFixRequest request) {
       log.info("Request paylod AIFixesController : {}",request);
       return  aiFixService.analyse(request);
    }




}
