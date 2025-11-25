package com.yash.log.dto;

import com.yash.log.dto.ErrorCategory;

public class ErrorCategoryStatDto {

    private ErrorCategory category;
    private long count;

    public ErrorCategoryStatDto(ErrorCategory category, long count) {
        this.category = category;
        this.count = count;
    }

    public ErrorCategory getCategory() {
        return category;
    }

    public long getCount() {
        return count;
    }
}

