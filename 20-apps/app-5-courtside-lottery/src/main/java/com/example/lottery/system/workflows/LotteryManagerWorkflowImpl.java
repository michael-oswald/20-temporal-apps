package com.example.lottery.system.workflows;

import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LotteryManagerWorkflowImpl implements LotteryManagerWorkflow {
    private final Logger logger = Workflow.getLogger(LotteryManagerWorkflowImpl.class);
    
    private final List<String> participants = new ArrayList<>();
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
    public void enter(String userId) {
        if (!isLotteryActive) {
            logger.info("Cannot enter lottery - lottery is not active");
            return;
        }
        
        if (!participants.contains(userId)) {
            participants.add(userId);
            logger.info("User {} entered the lottery", userId);
            
            // Optionally run the lottery selection when certain conditions are met
            // For example: if (participants.size() >= MAX_PARTICIPANTS) { selectWinners(); }
        } else {
            logger.info("User {} is already participating in the lottery", userId);
        }
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