package com.akaripm.model;

import software.amazon.awssdk.enhanced.dynamodb.internal.converter.attribute.LocalDateTimeAttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@DynamoDbBean
public class TrackDTO {
    private String trackId;
    private String trackName;
    private List<TaskDTO> tasks;
    private LocalDateTime desiredDueDate;
    private LocalDateTime calculatedDueDate;
    private LocalDateTime completedDueDate;
    private Integer calculatedNumBusinessDaysRemaining;

    public TrackDTO() {
    }

    public TrackDTO(String trackId, String trackName, List<TaskDTO> tasks, LocalDateTime desiredDueDate, LocalDateTime calculatedDueDate, LocalDateTime completedDueDate) {
        this.trackId = trackId;
        this.trackName = trackName;
        this.tasks = tasks;
        this.desiredDueDate = desiredDueDate;
        this.calculatedDueDate = calculatedDueDate;
        this.completedDueDate = completedDueDate;
    }

    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public List<TaskDTO> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskDTO> tasks) {
        this.tasks = tasks;
    }

    @DynamoDbConvertedBy(LocalDateTimeAttributeConverter.class)
    public LocalDateTime getDesiredDueDate() {
        return desiredDueDate;
    }

    public void setDesiredDueDate(LocalDateTime desiredDueDate) {
        this.desiredDueDate = desiredDueDate;
    }

    @DynamoDbConvertedBy(LocalDateTimeAttributeConverter.class)
    public LocalDateTime getCalculatedDueDate() {
        return calculatedDueDate;
    }

    public void setCalculatedDueDate(LocalDateTime calculatedDueDate) {
        this.calculatedDueDate = calculatedDueDate;
    }

    @DynamoDbConvertedBy(LocalDateTimeAttributeConverter.class)
    public LocalDateTime getCompletedDueDate() {
        return completedDueDate;
    }

    public void setCompletedDueDate(LocalDateTime completedDueDate) {
        this.completedDueDate = completedDueDate;
    }

    public Integer getCalculatedNumBusinessDaysRemaining() {
        return calculatedNumBusinessDaysRemaining;
    }

    public void setCalculatedNumBusinessDaysRemaining(Integer calculatedNumBusinessDaysRemaining) {
        this.calculatedNumBusinessDaysRemaining = calculatedNumBusinessDaysRemaining;
    }
}
