// src/main/java/com/akaripm/workflow/ProjectWorkflow.java
package com.akaripm.workflow;

import com.akaripm.model.*;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.List;
import java.util.Optional;

@WorkflowInterface
public interface ProjectWorkflow {
    @WorkflowMethod
    void start(ProjectSpec spec);

    @SignalMethod
    void addTask(TaskDTO task, String projectId);

    @SignalMethod
    void editTask(TaskDTO task, String projectId);

    @SignalMethod
    void addTrack(TrackDTO trackDTO, String projectId);

    @SignalMethod
    void editTrack(TrackDTO trackDTO, String projectId);

    @SignalMethod
    void addWorker(WorkerDTO workerDTO, String projectId);

    @SignalMethod
    void assignWorkerTask(String workerId, String taskId, String projectId);

    @SignalMethod
    void changeTaskOrderOnTrack(String taskId, Integer newPosition, String projectId);

    @SignalMethod
    void changeTaskTrack(String taskId, String newTrackId, String projectId);

    @SignalMethod
    void deleteTask(String taskId, String projectId);

    @SignalMethod
    void deleteTrack(String trackId, String projectId);

    //TODO signal method to change Track Ordering... can come later

    @SignalMethod
    void cancel();

    //@QueryMethod
    //ProjectDTO getProject(String projectId); // Add this to get project details
}
