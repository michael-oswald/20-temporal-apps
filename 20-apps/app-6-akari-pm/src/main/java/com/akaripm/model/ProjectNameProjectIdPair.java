package com.akaripm.model;

public class ProjectNameProjectIdPair {
    private String projectName;
    private String projectId;

    public ProjectNameProjectIdPair() {}

    public ProjectNameProjectIdPair(String projectName, String projectId) {
        this.projectName = projectName;
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
}
