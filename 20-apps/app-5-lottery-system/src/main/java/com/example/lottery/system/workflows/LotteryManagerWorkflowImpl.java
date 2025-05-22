package com.example.lottery.system.workflows;

import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.util.*;

public class LotteryManagerWorkflowImpl implements LotteryManagerWorkflow {
    private final Logger logger = Workflow.getLogger(LotteryManagerWorkflowImpl.class);

    private final Set<String> participants = new HashSet<>();
    private final List<String> winners = new ArrayList<>();
    private boolean isLotteryActive = false;
    private int numWinners = 0;

  @Override
  public List<String> start(Integer numWinners, List<String> userIds) {
      this.numWinners = numWinners;
      isLotteryActive = true;
      logger.info("Lottery workflow started with {} winners to be selected", numWinners);

      // Method now handles the entire workflow
      Workflow.sleep(1000); // Give some time for simulation purposes

      participants.addAll(userIds);
      // Select winners directly
      selectWinners();
      isLotteryActive = false;

      logger.info("Lottery workflow finished, selected {} winners", winners.size());
      return Collections.unmodifiableList(winners);
  }

    // Helper method to select winners
    private void selectWinners() {
        // Simple implementation: shuffle the list and select the first N participants
        List<String> shuffledParticipants = new ArrayList<>(participants);
        Collections.shuffle(shuffledParticipants);

        // Assuming we want to select a fixed number of winners
        int winnerCount = Math.min(numWinners, shuffledParticipants.size());
        winners.addAll(shuffledParticipants.subList(0, winnerCount));

        logger.info("Selected {} winners from {} participants", winners.size(), participants.size());
    }
}