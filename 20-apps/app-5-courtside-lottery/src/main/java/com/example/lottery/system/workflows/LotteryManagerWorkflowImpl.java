package com.example.lottery.system.workflows;

import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.util.*;

public class LotteryManagerWorkflowImpl implements LotteryManagerWorkflow {
    private final Logger logger = Workflow.getLogger(LotteryManagerWorkflowImpl.class);

    private final Set<String> participants = new HashSet<>();
    private final List<String> winners = new ArrayList<>();
    private boolean isLotteryActive = false;

    @Override
    public List<String> start() {
        isLotteryActive = true;
        logger.info("Lottery workflow started");

        Workflow.await(() -> !isLotteryActive);
        logger.info("Lottery workflow finished");
        return Collections.unmodifiableList(winners);
    }

    @Override
    public void enter(List<String> userIds) {
        if (!isLotteryActive) {
            logger.info("Cannot enter lottery - lottery is not active");
            return;
        }

        int previousSize = participants.size();
        participants.addAll(userIds);
        int addedCount = participants.size() - previousSize;
        logger.info("Added {} users to the lottery", addedCount);
    }

    // Helper method to select winners
    private void selectWinners() {
        // Simple implementation: shuffle the list and select the first N participants
        List<String> shuffledParticipants = new ArrayList<>(participants);
        Collections.shuffle(shuffledParticipants);

        // Assuming we want to select a fixed number of winners
        int winnerCount = Math.min(3, shuffledParticipants.size());
        winners.addAll(shuffledParticipants.subList(0, winnerCount));

        logger.info("Selected {} winners from {} participants", winners.size(), participants.size());
    }

    @Override
    public void close() {
        if (isLotteryActive) {
            selectWinners();
            isLotteryActive = false;
        }
    }
}