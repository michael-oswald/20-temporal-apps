package com.example.email.workflows;

import com.example.email.Email;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.List;

@WorkflowInterface
public interface EmailCampaignWorkflow {
    @WorkflowMethod
    void startEmailCampaign(List<Email> allEmails);
}
