package com.example.email.workflows;

import com.example.email.Email;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.List;

@WorkflowInterface
public interface EmailBatchWorkflow {
    @WorkflowMethod
    void sendEmails(List<Email> emails);
}
