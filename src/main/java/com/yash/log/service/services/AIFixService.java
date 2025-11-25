package com.yash.log.service.services;


import com.yash.log.dto.AIFixRequest;
import com.yash.log.dto.AIFixResponse;
import reactor.core.publisher.Mono;

public interface AIFixService {

    Mono<AIFixResponse> analyse(AIFixRequest request);
}
