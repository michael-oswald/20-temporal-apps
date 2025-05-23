package com.example.lottery.system.controller;


import com.example.lottery.system.model.LotteryRequest;
import com.example.lottery.system.service.LotteryLimitService;
import com.example.lottery.system.workflows.LotteryManagerWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/lottery")
public class LotteryController {

    private final WorkflowClient workflowClient;
    private final LotteryLimitService lotteryLimitService;

    public LotteryController(@Lazy WorkflowClient workflowClient, LotteryLimitService lotteryLimitService) {
        this.workflowClient = workflowClient;
        this.lotteryLimitService = lotteryLimitService;
    }

    @PostMapping("/enterSimple")
    public ResponseEntity<?> enterSimple(@RequestBody LotteryRequest lotteryRequest) {
        if (lotteryRequest == null || lotteryRequest.csvUserIds == null || lotteryRequest.csvUserIds.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid request");
        }

        String[] userIds = lotteryRequest.csvUserIds.split(",");
        if (userIds.length > 1000) {
            return ResponseEntity.badRequest().body("Too many users. Maximum is 1000");
        }

        var set = new HashSet<>(Arrays.asList(userIds));
        if (set.size() != userIds.length) {
            return ResponseEntity.badRequest().body("Duplicate user IDs found");
        }

        int numWinners = lotteryRequest.numWinners;
        if (numWinners <= 0 || numWinners > userIds.length) {
            return ResponseEntity.badRequest().body("Invalid number of winners");
        }

        List<String> trimmedUserIds = Arrays.stream(userIds)
                .map(String::trim)
                .toList();

        // Shuffle and pick winners
        List<String> shuffled = new java.util.ArrayList<>(trimmedUserIds);
        java.util.Collections.shuffle(shuffled);
        List<String> winners = shuffled.subList(0, numWinners);

        return ResponseEntity.ok(winners);
    }

    @PostMapping("/enter")
    public ResponseEntity<?> enterLottery(@RequestBody LotteryRequest lotteryRequest) {
        if (lotteryRequest == null || lotteryRequest.csvUserIds == null || lotteryRequest.csvUserIds.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid request");
        }

        if (!lotteryLimitService.canStartNewLottery()) {
            return ResponseEntity.status(429).body("Daily lottery entry limit reached");
        }

        String[] userIds = lotteryRequest.csvUserIds.split(",");
        if (userIds.length > 1000) {
            return ResponseEntity.badRequest().body("Too many users. Maximum is 1000");
        }

        var set = new HashSet<>(Arrays.asList(userIds));
        if (set.size() != userIds.length) {
            return ResponseEntity.badRequest().body("Duplicate user IDs found");
        }
        set = null; // garbage collect

        String workflowId = "LMW" + UUID.randomUUID();
        LotteryManagerWorkflow lotteryManagerWorkflow = workflowClient.newWorkflowStub(
                LotteryManagerWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setWorkflowId(workflowId)
                        .setTaskQueue("LOTTERY_TASK_QUEUE")
                        .build()
        );
        List<String> trimmedUserIds = Arrays.stream(userIds)
                .map(String::trim)
                .toList();
        try {
            WorkflowClient.start(lotteryManagerWorkflow::start, lotteryRequest.numWinners, trimmedUserIds);
            //log.info("LotteryManagerWorkflow started");
        } catch (io.temporal.client.WorkflowExecutionAlreadyStarted e) {
            // Workflow is already running, safe to ignore
        }

        // update the daily lottery limit
        lotteryLimitService.incrementAndGet();

        try {
            WorkflowStub stub = workflowClient.newUntypedWorkflowStub(workflowId);
            // Use a timeout to prevent hanging
            List<String> result = stub.getResult(2, java.util.concurrent.TimeUnit.SECONDS, List.class);
            return ResponseEntity.ok(result);
        } catch (TimeoutException e) {
            return ResponseEntity.accepted().body("Lottery drawing is still in progress");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error retrieving winners: " + e.getMessage());
        }
    }

    @GetMapping("/remaining")
    public ResponseEntity<Integer> getRemaining() {
        int remaining = lotteryLimitService.getRemaining();
        return ResponseEntity.ok(remaining);
    }
}