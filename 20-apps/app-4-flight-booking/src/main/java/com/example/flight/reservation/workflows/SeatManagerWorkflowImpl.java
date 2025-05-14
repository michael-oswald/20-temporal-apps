package com.example.flight.reservation.workflows;

import io.temporal.workflow.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SeatManagerWorkflowImpl implements SeatManagerWorkflow {

    private static final Logger log = LoggerFactory.getLogger(SeatManagerWorkflowImpl.class);
    private final Set<String> availableSeats = new LinkedHashSet<>();
    private final Map<String, String> heldSeats = new HashMap<>();
    private final Set<String> confirmedSeats = new HashSet<>();

    @Override
    public void start() {
        for (int row = 1; row <= 30; row++) {
            for (String letter : List.of("A", "B", "C", "D", "E", "F")) {
                availableSeats.add(row + letter);
            }
        }

        Workflow.await(() -> false); // Keep alive
    }

    @Override
    public void requestSeat(String userId) {
        if (heldSeats.containsKey(userId)) {
            log.info("held seat already found for user {}: {}", userId, heldSeats.get(userId));
            return;
        }

        Iterator<String> it = availableSeats.iterator();
        if (!it.hasNext()) throw new RuntimeException("No seats available");

        String seat = it.next();
        it.remove();
        heldSeats.put(userId, seat);
        if (!Workflow.isReplaying()) {
            log.info("seat {} held for user {}", seat, userId);
        }
    }

    @Override
    public void confirmSeat(String userId) {
        String seat = heldSeats.remove(userId);
        if (seat != null) confirmedSeats.add(seat);
    }

    @Override
    public void releaseSeat(String userId) {
        String seat = heldSeats.remove(userId);
        if (seat != null) availableSeats.add(seat);
    }

    @Override
    public List<String> getAvailableSeats() {
        return new ArrayList<>(availableSeats);
    }

    @Override
    public String getHeldSeat(String userId) {
        return heldSeats.get(userId);
    }

    @Override
    public Map<String, String> getSeatStatus() {
        Map<String, String> statusMap = new HashMap<>();

        for (String seat : availableSeats) {
            statusMap.put(seat, "AVAILABLE");
        }
        for (Map.Entry<String, String> entry : heldSeats.entrySet()) {
            statusMap.put(entry.getValue(), "HELD");
        }
        for (String seat : confirmedSeats) {
            statusMap.put(seat, "CONFIRMED");
        }

        return statusMap;
    }
}
