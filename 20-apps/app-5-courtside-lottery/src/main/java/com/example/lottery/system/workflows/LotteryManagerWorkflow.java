package com.example.lottery.system.workflows;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.List;

@WorkflowInterface
public interface LotteryManagerWorkflow {

    @WorkflowMethod
    List<String> start(Integer numWinners, List<String> userIds);
}
