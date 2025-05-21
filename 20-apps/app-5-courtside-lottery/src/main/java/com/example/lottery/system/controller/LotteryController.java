package com.example.lottery.system.controller;


import com.example.lottery.system.workflows.LotteryManagerWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowStub;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeoutException;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/lottery")
public class LotteryController {

    private final WorkflowClient workflowClient;

    public LotteryController(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    @PostMapping("/enter")
    public ResponseEntity<String> enterLottery(@RequestBody String csvUserIds) {
        String[] userIds = csvUserIds.split(",");
        if (userIds.length > 1000) {
            return ResponseEntity.badRequest().body("Too many users. Maximum is 1000");
        }

        var set = new HashSet<>(Arrays.asList(userIds));
        if (set.size() != userIds.length) {
            return ResponseEntity.badRequest().body("Duplicate user IDs found");
        }
        set = null; // garbage collect

        LotteryManagerWorkflow lotteryManagerWorkflow = workflowClient.newWorkflowStub(
                LotteryManagerWorkflow.class, "LotteryManagerWorkflow"
        );

        List<String> trimmedUserIds = Arrays.stream(userIds)
                .map(String::trim)
                .toList();

        lotteryManagerWorkflow.enter(trimmedUserIds);
        return ResponseEntity.ok(userIds.length + " users entered the lottery");
    }

    @PostMapping("/close")
    public ResponseEntity<String> closeLottery() {
        LotteryManagerWorkflow lotteryManagerWorkflow = workflowClient.newWorkflowStub(
                LotteryManagerWorkflow.class, "LotteryManagerWorkflow"
        );
        lotteryManagerWorkflow.close();
        return ResponseEntity.ok("Lottery closed");
    }

    @GetMapping("/winners")
    public ResponseEntity<?> getWinners() {
        try {
            WorkflowStub stub = workflowClient.newUntypedWorkflowStub("LotteryManagerWorkflow");
            // Use a timeout to prevent hanging
            List<String> result = stub.getResult(2, java.util.concurrent.TimeUnit.SECONDS, List.class);
            return ResponseEntity.ok(result);
        } catch (TimeoutException e) {
            return ResponseEntity.accepted().body("Lottery drawing is still in progress");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error retrieving winners: " + e.getMessage());
        }
    }
}