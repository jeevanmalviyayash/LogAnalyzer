package com.yash.log.dto;

import java.time.LocalDate;

public class DailyErrorCountDto {

    private LocalDate date;
    private long count;

    public DailyErrorCountDto(LocalDate date, long count) {
        this.date = date;
        this.count = count;
    }

    public LocalDate getDate() {
        return date;
    }

    public long getCount() {
        return count;
    }
}

