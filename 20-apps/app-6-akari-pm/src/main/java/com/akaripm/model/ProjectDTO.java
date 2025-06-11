package com.akaripm.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import java.util.ArrayList;
import java.util.List;

@DynamoDbBean
public class ProjectDTO {
    private String projectId;
    private String projectName;
    private List<WorkerDTO> workers = new ArrayList<>();
    private List<TrackDTO> tracks = new ArrayList<>();

    public ProjectDTO() {}

    public ProjectDTO(String projectId, String projectName, List<WorkerDTO> workers, List<TrackDTO> tracks) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.workers = workers;
        this.tracks = tracks;
    }

    @DynamoDbPartitionKey
    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public List<WorkerDTO> getWorkers() {
        return workers;
    }

    public void setWorkers(List<WorkerDTO> workers) {
        this.workers = workers;
    }

    public List<TrackDTO> getTracks() {
        return tracks;
    }

    public void setTracks(List<TrackDTO> tracks) {
        this.tracks = tracks;
    }
}
