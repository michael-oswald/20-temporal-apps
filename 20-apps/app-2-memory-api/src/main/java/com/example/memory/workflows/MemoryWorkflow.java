package com.example.memory.workflows;

import com.example.memory.controller.MemoryController;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface MemoryWorkflow {
    @WorkflowMethod
    void processWorkflow(MemoryController.MemoryRequest payload);
}
