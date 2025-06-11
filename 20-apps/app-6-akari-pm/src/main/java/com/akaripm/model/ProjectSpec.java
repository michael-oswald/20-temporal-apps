package com.akaripm.model;

import java.util.List;

public class ProjectSpec {
  public String projectId;
  public String projectName;
  public List<WorkerDTO> teamMembers;
  public List<TrackDTO> initialTracks;

  public ProjectSpec() {}

  public ProjectSpec(String projectId, String projectName, List<WorkerDTO> teamMembers, List<TrackDTO> initialTracks) {
      this.projectId = projectId;
      this.projectName = projectName;
      this.teamMembers = teamMembers;
      this.initialTracks = initialTracks;
  }
}