package com.yash.log.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class AIFixMessage {

    private  String role;
    private String content;
}
