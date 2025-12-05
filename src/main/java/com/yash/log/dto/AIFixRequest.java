package com.yash.log.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
@Data
@NoArgsConstructor
@ToString
public class AIFixRequest {

    private String model;
    private List<AIFixMessage> messages;
    private Double temperature;
    private Integer max_tokens;
    private Boolean stream;
}
