package com.example.lottery.system.workflows;

import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.List;

@WorkflowInterface
public interface LotteryManagerWorkflow {

    @WorkflowMethod
    List<String> start();

    @SignalMethod
    void close();

    @SignalMethod
    void enter(List<String> userIds);
}
