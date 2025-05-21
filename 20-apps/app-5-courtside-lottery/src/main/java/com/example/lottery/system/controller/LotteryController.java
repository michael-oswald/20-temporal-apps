package com.example.lottery.system.controller;


import com.example.lottery.system.workflows.LotteryManagerWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowStub;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/enter/{userId}")
    public ResponseEntity<String> enterLottery(@PathVariable String userId) {
        LotteryManagerWorkflow lotteryManagerWorkflow = workflowClient.newWorkflowStub(
                LotteryManagerWorkflow.class, "LotteryManagerWorkflow"
        );

        lotteryManagerWorkflow.enter(userId);
        return ResponseEntity.ok("Lottery request for user " + userId);
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