package com.example.memory.activities;

import com.example.memory.workflows.MemoryWorkflowImpl;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface DynamoDbActivity {
    @ActivityMethod
    void saveMemory(MemoryWorkflowImpl.Memory memory);
}