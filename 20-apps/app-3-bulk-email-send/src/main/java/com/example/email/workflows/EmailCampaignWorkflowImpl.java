package com.example.email.workflows;

import com.example.email.Email;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;

import java.util.ArrayList;
import java.util.List;

public class EmailCampaignWorkflowImpl implements EmailCampaignWorkflow {

    @Override
    public void startEmailCampaign(List<Email> allEmails) {
        var batchSize = Math.max(10, allEmails.size() / 10); // 10 child workflows max
        var chunks = chunkList(allEmails, batchSize);
        var children = new ArrayList<Promise<Void>>();

        for (var chunk : chunks) {
            EmailBatchWorkflow child = Workflow.newChildWorkflowStub(EmailBatchWorkflow.class); // child workflow
            children.add(Async.procedure(() -> child.sendEmails(chunk)));
        }

        Promise.allOf(children).get(); // Waits indefinitely

    }

    private List<List<Email>> chunkList(List<Email> emails, int size) {
        var chunks = new ArrayList<List<Email>>();
        for (int i = 0; i < emails.size(); i += size) {
            chunks.add(emails.subList(i, Math.min(i + size, emails.size())));
        }
        return chunks;
    }
}