package com.akaripm.controller;

import com.akaripm.model.*;
import com.akaripm.service.AkariService;
import com.akaripm.workflow.ProjectWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "*") // Allow requests from any origin
@RestController
@RequestMapping("/akari")
@Slf4j
public class AkariController {

    private final WorkflowClient workflowClient;
    private final AkariService akariService;

    public AkariController(WorkflowClient workflowClient, AkariService akariService) {
        this.workflowClient = workflowClient;
        this.akariService = akariService;
    }

    // Add a task to an existing project
    @PostMapping("/project/{projectName}")
    public String createProject(@PathVariable String projectName) {
        var projectId = UUID.randomUUID().toString();

        ProjectWorkflow projectWorkflow = workflowClient.newWorkflowStub(
                ProjectWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setWorkflowId(projectId)
                        .setTaskQueue("PROJECT_TASK_QUEUE")
                        .build()
        );

        try {
            var projectSpec = new ProjectSpec(
                    projectId,
                    projectName,
                    List.of(
                            new WorkerDTO("user-1", "Alice"),
                            new WorkerDTO("user-2", "Bob"),
                            new WorkerDTO("user-3", "Jane")
                    ),
                    List.of(
                            new TrackDTO(
                                    "BACKLOG",
                                    "BACKLOG",
                                    new ArrayList<>(),
                                    null, // desiredDueDate
                                    null, // calculatedDueDate
                                    null  // completedDueDate
                            )
                    )
            );

            WorkflowClient.start(() -> projectWorkflow.start(projectSpec));
            log.info("ProjectWorkflow started");
        } catch (io.temporal.client.WorkflowExecutionAlreadyStarted e) {
            // Workflow is already running, safe to ignore
            log.warn("workflow already started: {}", e.getMessage());
        }
        return "Task added to project ";
    }

    @GetMapping("/project")
    public List<ProjectNameProjectIdPair> getProjects() {
        return akariService.getNamesAndIds();
    }
}
