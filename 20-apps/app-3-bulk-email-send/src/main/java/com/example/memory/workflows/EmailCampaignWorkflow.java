package com.example.memory.workflows;

import com.example.memory.Email;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.List;

@WorkflowInterface
public interface EmailCampaignWorkflow {
    @WorkflowMethod
    void startEmailCampaign(List<Email> allEmails);
}
