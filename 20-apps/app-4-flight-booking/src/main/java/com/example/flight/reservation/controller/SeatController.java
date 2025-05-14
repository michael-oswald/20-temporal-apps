package com.example.flight.reservation.controller;

import com.example.flight.reservation.workflows.SeatManagerWorkflow;
import io.temporal.client.WorkflowClient;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/seats")
public class SeatController {

    private final WorkflowClient workflowClient;

    public SeatController(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    @GetMapping("/status")
    public Map<String, String> getSeatStatus() {
        SeatManagerWorkflow seatManager = workflowClient.newWorkflowStub(
            SeatManagerWorkflow.class,
            "SeatManagerWorkflow"
        );
        return seatManager.getSeatStatus();
    }
}
