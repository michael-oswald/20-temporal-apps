package com.akaripm.activity;

import com.akaripm.model.ProjectDTO;
import com.akaripm.model.ProjectNameProjectIdPair;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

import java.util.List;

@ActivityInterface
public interface DynamoDbActivity {
    @ActivityMethod
    void save(ProjectDTO projectDTO);

    @ActivityMethod
    ProjectDTO get(String projectId);
}
