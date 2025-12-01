package com.yash.log.dto;

public class ErrorCategoryStatDto {

    private String category;
    private long count;

    public ErrorCategoryStatDto(String category, long count) {
        this.category = category;
        this.count = count;
    }

    public String getCategory() {
        return category;
    }

    public long getCount() {
        return count;
    }
}

