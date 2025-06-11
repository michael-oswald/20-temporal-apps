// src/main/java/com/akaripm/workflow/ProjectWorkflowImpl.java
package com.akaripm.workflow;

import com.akaripm.activity.DynamoDbActivity;
import com.akaripm.model.*;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
public class ProjectWorkflowImpl implements ProjectWorkflow {

    private boolean finished = false;
    private ProjectDTO projectState = null;

    private final DynamoDbActivity dynamoDbActivity = Workflow.newActivityStub(
            DynamoDbActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .build()
    );

    @Override
    public void start(ProjectSpec spec) {
        // Initialize the project with the provided spec
        var projectDTO = new ProjectDTO(
                spec.projectId,
                spec.projectName,
                new ArrayList<>(),
                new ArrayList<>()
        );

        spec.teamMembers.forEach(user -> projectDTO.getWorkers().add(user));
        projectDTO.getTracks().addAll(spec.initialTracks);
        log.info("Starting project workflow with ID in start method: {}", spec.projectId);
        // save initial project state to DynamoDB
        try {
            dynamoDbActivity.save(projectDTO);
            log.info("Starting project workflow with ID in start method after dynamodb activity: {}", spec.projectId);
        } catch (Exception e) {
            log.error("Error during project initialization: {}", e.getMessage(), e);
            throw e;
        }
        Workflow.await(() -> finished);
    }

    @Override
    public void addTask(TaskDTO task, String projectId) {
        if (task.getTaskId() == null || task.getTaskId().isEmpty()) {
            task.setTaskId(UUID.randomUUID().toString());
        }
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.NOT_STARTED); // Default status if not set
        }

        var projectDto = dynamoDbActivity.get(projectId);

        // Add the task to the appropriate track other than backlog
        var trackDTOOptional = projectDto.getTracks().stream().filter(trackDTO -> trackDTO.getTrackId().equals(task.getTrackId())).findAny();
        if (trackDTOOptional.isEmpty()) {
            return;
        }
        trackDTOOptional.get().getTasks().add(task);

        dynamoDbActivity.save(projectDto);
    }

    @Override
    public void changeTaskTrack(String taskId, String newTrackId, String projectId) {

        var projectDto = dynamoDbActivity.get(projectId);

        TaskDTO taskToMove = null;
        TrackDTO sourceTrack = null;

        // Find the task and its source track
        for (TrackDTO track : projectDto.getTracks()) {
            for (TaskDTO task : track.getTasks()) {
                if (task.getTaskId().equals(taskId)) {
                    taskToMove = task;
                    sourceTrack = track;
                    break;
                }
            }
            if (taskToMove != null) break;
        }

        if (taskToMove == null) {
            return;
        }

        // Find the destination track
        TrackDTO destTrack = projectDto.getTracks().stream()
                .filter(track -> track.getTrackId().equals(newTrackId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Destination track not found: " + newTrackId));

        // Remove from source track and add to destination track
        sourceTrack.getTasks().remove(taskToMove);
        taskToMove.setTrackId(newTrackId);


        destTrack.getTasks().add(taskToMove); // add to task list to new track
        dynamoDbActivity.save(projectDto);

    }

    @Override
    public void cancel() {
        this.finished = true;
    }

    @Override
    public void addTrack(TrackDTO trackDTO, String projectId) {
        if (trackDTO.getTrackId() == null || trackDTO.getTrackId().isEmpty()) {
            trackDTO.setTrackId(UUID.randomUUID().toString());
        }
        var projectDto = dynamoDbActivity.get(projectId);

        // Check if a track with this ID already exists
        boolean trackExists = projectDto.getTracks().stream()
                .anyMatch(track -> track.getTrackId().equals(trackDTO.getTrackId()));

        if (trackExists) {
            return;
        }

        // Add the new track to the tracks list
        if (trackDTO.getTasks() == null) {
            trackDTO.setTasks(new ArrayList<>()); // Initialize tasks if null
        }
        projectDto.getTracks().add(trackDTO);
        dynamoDbActivity.save(projectDto);
    }

    @Override
    public void addWorker(WorkerDTO workerDTO, String projectId) {
        var projectDto = dynamoDbActivity.get(projectId);

        // Check if a worker with this ID already exists
        boolean workerExists = projectDto.getWorkers().stream()
                .anyMatch(worker -> worker.getUserId().equals(workerDTO.getUserId()));

        if (workerExists) {
            return; // Do not add if worker already exists
        }

        // Add the new worker to the workers map
        projectDto.getWorkers().add(workerDTO);
        dynamoDbActivity.save(projectDto);
    }

    @Override
    public void assignWorkerTask(String workerId, String taskId, String projectId) {

        var projectDto = dynamoDbActivity.get(projectId);
        // Verify worker exists
        boolean workerExists = projectDto.getWorkers().stream()
                .anyMatch(worker -> worker.getUserId().equals(workerId));

        if (!workerExists) {
            log.error("Worker with ID {} not found", workerId);
            return; // Do not assign if worker does not exist
        }

        // Find the task across all tracks
        TaskDTO taskToAssign = null;
        for (TrackDTO track : projectDto.getTracks()) {
            for (TaskDTO task : track.getTasks()) {
                if (task.getTaskId().equals(taskId)) {
                    taskToAssign = task;
                    break;
                }
            }
            if (taskToAssign != null) break;
        }

        // Throw exception if task not found
        if (taskToAssign == null) {
            log.error("Task with ID {} not found", taskId);
            return;
        }

        // Assign the worker to the task
        taskToAssign.setAssignedUserId(workerId);
        // Save the updated project state
        dynamoDbActivity.save(projectDto);
    }

    @Override
    public void editTask(TaskDTO updatedTask, String projectId) {
        // Find the task to edit across all tracks
        var projectDto = dynamoDbActivity.get(projectId);
        boolean taskFound = false;
        for (TrackDTO track : projectDto.getTracks()) {
            for (int i = 0; i < track.getTasks().size(); i++) {
                TaskDTO existingTask = track.getTasks().get(i);
                if (existingTask.getTaskId().equals(updatedTask.getTaskId())) {
                    // Replace the existing task with the updated one
                    // Preserve the track ID to prevent unintended track changes
                    updatedTask.setTrackId(existingTask.getTrackId());
                    track.getTasks().set(i, updatedTask);
                    taskFound = true;
                    break;
                }
            }
            if (taskFound) break;
        }

        // Throw exception if task not found
        if (!taskFound) {
            throw new RuntimeException("Task with ID " + updatedTask.getTaskId() + " not found");
        }
        // Save the updated project state
        dynamoDbActivity.save(projectDto);
    }

    @Override
    public void editTrack(TrackDTO trackDTO, String projectId) {
        var projectDto = dynamoDbActivity.get(projectId);

        // Find the track to edit
        TrackDTO existingTrack = projectDto.getTracks().stream()
                .filter(track -> track.getTrackId().equals(trackDTO.getTrackId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Track with ID " + trackDTO.getTrackId() + " not found"));

        // Update the track name if provided
        if (trackDTO.getTrackName() != null) {
            existingTrack.setTrackName(trackDTO.getTrackName());
        }

        // Update the desired due date if provided
        if (trackDTO.getDesiredDueDate() != null) {
            existingTrack.setDesiredDueDate(trackDTO.getDesiredDueDate());
        }

        // Save the updated project state
        dynamoDbActivity.save(projectDto);
    }

    @Override
    public void changeTaskOrderOnTrack(String taskId, Integer newPosition, String projectId) {
        var projectDto = dynamoDbActivity.get(projectId);

        // Find the task and its track
        TaskDTO taskToMove = null;
        TrackDTO track = null;

        // Search for the task across all tracks
        for (TrackDTO t : projectDto.getTracks()) {
            for (TaskDTO task : t.getTasks()) {
                if (task.getTaskId().equals(taskId)) {
                    taskToMove = task;
                    track = t;
                    break;
                }
            }
            if (taskToMove != null) break;
        }

        if (taskToMove == null) {
            return;
        }

        // Validate the new position
        if (newPosition < 0 || newPosition >= track.getTasks().size()) {
            return;
        }

        // Remove the task from its current position and add it at the new position
        track.getTasks().remove(taskToMove);
        track.getTasks().add(newPosition, taskToMove);
        // Save the updated project state
        dynamoDbActivity.save(projectDto);
    }

    @Override
    public void deleteTask(String taskId, String projectId) {
        var projectDto = dynamoDbActivity.get(projectId);
        boolean taskFound = false;

        // Create a copy of the project DTO to make modifications to
        // This is crucial for deterministic workflow execution
        ProjectDTO updatedProjectDto = new ProjectDTO(
            projectDto.getProjectId(),
            projectDto.getProjectName(),
            new ArrayList<>(projectDto.getWorkers()),
            new ArrayList<>()
        );

        // Copy tracks but exclude the task to be deleted
        for (TrackDTO originalTrack : projectDto.getTracks()) {
            TrackDTO newTrack = new TrackDTO(
                originalTrack.getTrackId(),
                originalTrack.getTrackName(),
                new ArrayList<>(),
                originalTrack.getDesiredDueDate(),
                originalTrack.getCalculatedDueDate(),
                originalTrack.getCompletedDueDate()
            );

            // Copy all tasks except the one to delete
            for (TaskDTO task : originalTrack.getTasks()) {
                if (!task.getTaskId().equals(taskId)) {
                    newTrack.getTasks().add(task);
                } else {
                    taskFound = true;
                }
            }

            updatedProjectDto.getTracks().add(newTrack);
        }

        // Only save if the task was actually found and removed
        if (taskFound) {
            // Save the updated project state
            dynamoDbActivity.save(updatedProjectDto);
            log.info("Task with ID {} successfully deleted from project {}", taskId, projectId);
        } else {
            log.warn("Task with ID {} not found in project {}", taskId, projectId);
        }
    }

    @Override
    public void deleteTrack(String trackId, String projectId) {
        var projectDto = dynamoDbActivity.get(projectId);
        boolean trackFound = false;

        // Create a copy of the project DTO to make modifications to
        // This is crucial for deterministic workflow execution
        ProjectDTO updatedProjectDto = new ProjectDTO(
            projectDto.getProjectId(),
            projectDto.getProjectName(),
            new ArrayList<>(projectDto.getWorkers()),
            new ArrayList<>()
        );

        // Copy all tracks except the one to delete
        for (TrackDTO track : projectDto.getTracks()) {
            if (!track.getTrackId().equals(trackId)) {
                updatedProjectDto.getTracks().add(track);
            } else {
                trackFound = true;
            }
        }

        // Only save if the track was actually found and removed
        if (trackFound) {
            // Save the updated project state
            dynamoDbActivity.save(updatedProjectDto);
            log.info("Track with ID {} successfully deleted from project {}", trackId, projectId);
        } else {
            log.warn("Track with ID {} not found in project {}", trackId, projectId);
        }
    }
}
