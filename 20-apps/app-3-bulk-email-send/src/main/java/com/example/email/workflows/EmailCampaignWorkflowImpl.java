package com.example.email.workflows;

import com.example.email.Email;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class EmailCampaignWorkflowImpl implements EmailCampaignWorkflow {

    @Override
    public void startEmailCampaign(List<Email> allEmails) {
        var batchSize = 100;
        var chunks = chunkList(allEmails, batchSize);
        List<Promise<Void>> children = new ArrayList<>();

        for (List<Email> chunk : chunks) {
            EmailBatchWorkflow child = Workflow.newChildWorkflowStub(EmailBatchWorkflow.class); // child workflow
            children.add(Async.procedure(() -> child.sendEmails(chunk)));
        }

        try {
            Promise.allOf(children).get(30, java.util.concurrent.TimeUnit.MINUTES); // Wait for all children to complete with 30m timeout
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    private List<List<Email>> chunkList(List<Email> emails, int size) {
        List<List<Email>> chunks = new ArrayList<>();
        for (int i = 0; i < emails.size(); i += size) {
            chunks.add(emails.subList(i, Math.min(i + size, emails.size())));
        }
        return chunks;
    }
}