package com.example.memory.workflows;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface ReminderWorkflow {
    @WorkflowMethod
    void checkAndSendReminders();
}