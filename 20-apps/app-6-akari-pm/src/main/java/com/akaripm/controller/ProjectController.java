package com.akaripm.controller;

import com.akaripm.model.ProjectDTO;
import com.akaripm.model.TaskDTO;
import com.akaripm.model.TrackDTO;
import com.akaripm.model.WorkerDTO;
import com.akaripm.service.AkariService;
import com.akaripm.workflow.ProjectWorkflow;
import io.temporal.client.WorkflowClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*") // Allow requests from any origin
@RestController
@RequestMapping("/project")
public class ProjectController {

  private final WorkflowClient workflowClient;
  private final AkariService akariService;

  public ProjectController(WorkflowClient workflowClient, AkariService akariService) {
    this.workflowClient = workflowClient;
    this.akariService = akariService;
  }

  // Add a task to an existing project
  @PostMapping("/{projectId}/tasks")
  public String addTask(@PathVariable String projectId, @RequestBody TaskDTO task) {
    ProjectWorkflow workflow = getWorkflowStub(projectId);
    workflow.addTask(task, projectId);
    return "Task added to project " + projectId;
  }

  // Mark a user as on PTO
/*  @PostMapping("/{projectId}/pto")
  public String markPTO(@PathVariable String projectId, @RequestBody UserPTO pto) {
    ProjectWorkflow workflow = getWorkflowStub(projectId);
    workflow.markUserPTO(pto);
    return "Marked PTO for user " + pto.getUserId();
  }*/
  
  // Change a task's position within its current track
  @PostMapping("/{projectId}/tasks/changeOrder")
  public String changeTaskOrder(
      @PathVariable String projectId,
      @RequestParam String taskId,
      @RequestParam Integer newPosition) {
    ProjectWorkflow workflow = getWorkflowStub(projectId);
    workflow.changeTaskOrderOnTrack(taskId, newPosition, projectId);
    return "Task " + taskId + " moved to position " + newPosition + " in project " + projectId;
  }

  // Edit an existing task
  @PostMapping("/{projectId}/tasks/edit")
  public String editTask(@PathVariable String projectId, @RequestBody TaskDTO task) {
    ProjectWorkflow workflow = getWorkflowStub(projectId);
    workflow.editTask(task, projectId);
    return "Task edited in project " + projectId;
  }

  // Delete a task from the project
  @PostMapping("/{projectId}/tasks/delete")
  public String deleteTask(@PathVariable String projectId, @RequestParam String taskId) {
    ProjectWorkflow workflow = getWorkflowStub(projectId);
    workflow.deleteTask(taskId, projectId);
    return "Task " + taskId + " deleted from project " + projectId;
  }

  // Add a new track
  @PostMapping("/{projectId}/tracks")
  public String addTrack(@PathVariable String projectId, @RequestBody TrackDTO trackDTO) {
    ProjectWorkflow workflow = getWorkflowStub(projectId);
    workflow.addTrack(trackDTO, projectId);
    return "Track added to project " + projectId;
  }

  // Edit an existing track (trackName and desiredDueDate)
  @PostMapping("/{projectId}/tracks/edit")
  public String editTrack(@PathVariable String projectId, @RequestBody TrackDTO trackDTO) {
    ProjectWorkflow workflow = getWorkflowStub(projectId);
    workflow.editTrack(trackDTO, projectId);
    return "Track edited in project " + projectId;
  }

  // Delete a track from the project
  @PostMapping("/{projectId}/tracks/delete")
  public String deleteTrack(@PathVariable String projectId, @RequestParam String trackId) {
    ProjectWorkflow workflow = getWorkflowStub(projectId);
    workflow.deleteTrack(trackId, projectId);
    return "Track " + trackId + " deleted from project " + projectId;
  }

  // Add a new worker
  @PostMapping("/{projectId}/workers")
  public String addWorker(@PathVariable String projectId, @RequestBody WorkerDTO workerDTO) {
    // Generate UUID for worker if not provided
    if (workerDTO.getUserId() == null || workerDTO.getUserId().isEmpty()) {
      workerDTO.setUserId(java.util.UUID.randomUUID().toString());
    }
    ProjectWorkflow workflow = getWorkflowStub(projectId);
    workflow.addWorker(workerDTO, projectId);
    return "Worker added to project " + projectId;
  }

  // Assign a worker to a task
  @PostMapping("/{projectId}/assign")
  public String assignWorkerTask(@PathVariable String projectId, @RequestParam String workerId, @RequestParam String taskId) {
    ProjectWorkflow workflow = getWorkflowStub(projectId);
    workflow.assignWorkerTask(workerId, taskId, projectId);
    return "Worker " + workerId + " assigned to task " + taskId + " in project " + projectId;
  }

  // Change a task's track and optionally its position
  @PostMapping("/{projectId}/tasks/changeTrack")
  public String changeTaskTrack(
      @PathVariable String projectId,
      @RequestParam String taskId,
      @RequestParam String newTrackId) {
    ProjectWorkflow workflow = getWorkflowStub(projectId);
    workflow.changeTaskTrack(taskId, newTrackId, projectId);
    return "Task " + taskId + " moved to track " + newTrackId + " in project " + projectId;
  }

  // Cancel the project workflow
  @PostMapping("/{projectId}/cancel")
  public String cancel(@PathVariable String projectId) {
    ProjectWorkflow workflow = getWorkflowStub(projectId);
    workflow.cancel();
    return "Project " + projectId + " cancelled";
  }

  // Get project details (ProjectDTO)
  @GetMapping("/{projectId}/details")
  public ProjectDTO getProject(@PathVariable String projectId) {
    return akariService.getProjectById(projectId);
  }

  private ProjectWorkflow getWorkflowStub(String projectId) {
    // For workflow operations that modify state, use regular stub
    return workflowClient.newWorkflowStub(ProjectWorkflow.class, projectId);
  }

}
