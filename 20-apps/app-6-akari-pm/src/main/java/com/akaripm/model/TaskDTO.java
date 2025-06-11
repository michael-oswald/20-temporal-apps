package com.akaripm.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;

import java.time.Instant;

@DynamoDbBean
public class TaskDTO {
    private String taskId;
    private String trackId;
    private String name;
    private String assignedUserId;
    private Integer dayEstimate; // Estimated hours to complete the task
    private TaskStatus status;

    public TaskDTO() {}

    public TaskDTO(String taskId, String trackId, String name, String assignedUserId, Integer dayEstimate, TaskStatus status) {
        this.taskId = taskId;
        this.trackId = trackId;
        this.name = name;
        this.assignedUserId = assignedUserId;
        this.dayEstimate = dayEstimate;
        this.status = status;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAssignedUserId() {
        return assignedUserId;
    }

    public void setAssignedUserId(String assignedUserId) {
        this.assignedUserId = assignedUserId;
    }

    public Integer getDayEstimate() {
        return dayEstimate;
    }
    public void setDayEstimate(Integer dayEstimate) {
        this.dayEstimate = dayEstimate;
    }

    public TaskStatus getStatus() {
        return status;
    }
    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}


