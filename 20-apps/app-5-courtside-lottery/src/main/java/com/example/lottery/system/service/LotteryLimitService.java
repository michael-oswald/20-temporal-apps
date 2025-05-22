// src/main/java/com/example/lottery/system/service/LotteryLimitService.java
package com.example.lottery.system.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class LotteryLimitService {
    private static final int DAILY_LIMIT = 500;
    private int count = 0;
    private LocalDate currentDay = LocalDate.now();

    public synchronized boolean canStartNewLottery() {
        resetIfNewDay();
        return count < DAILY_LIMIT;
    }

    public synchronized int incrementAndGet() {
        resetIfNewDay();
        if (count < DAILY_LIMIT) {
            count++;
        }
        return count;
    }

    public synchronized int getRemaining() {
        resetIfNewDay();
        return DAILY_LIMIT - count;
    }

    private void resetIfNewDay() {
        if (!LocalDate.now().equals(currentDay)) {
            count = 0;
            currentDay = LocalDate.now();
        }
    }
}