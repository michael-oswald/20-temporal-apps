package com.example.flight.reservation.workflows;

import io.temporal.workflow.ChildWorkflowOptions;
import io.temporal.workflow.Workflow;

import java.util.*;

public class SeatManagerWorkflowImpl implements SeatManagerWorkflow {

    private final Set<String> availableSeats = new LinkedHashSet<>();
    private final Set<String> confirmedSeats = new HashSet<>();
    private final Queue<String> bookingRequests = new LinkedList<>();
    private final Map<String, String> failedBookings = new HashMap<>();

    @Override
    public void start() {
        if (availableSeats.isEmpty()) {
            for (int row = 1; row <= 30; row++) {
                for (String letter : List.of("A", "B", "C", "D", "E", "F")) {
                    availableSeats.add(row + letter);
                }
            }
        }

        while (true) {
            Workflow.await(() -> !bookingRequests.isEmpty());
            String userId = bookingRequests.poll();

            Iterator<String> it = availableSeats.iterator();
            if (it.hasNext()) {
                String seat = it.next();
                it.remove();
                confirmedSeats.add(seat);

                BookingWorkflow booking = Workflow.newChildWorkflowStub(
                        BookingWorkflow.class,
                        ChildWorkflowOptions.newBuilder()
                                .setWorkflowId("BookingWorkflow_" + userId)
                                .setTaskQueue("BOOKING_TASK_QUEUE")
                                .build()
                );
                booking.book(userId, seat);
            } else {
                failedBookings.put(userId, "NO_SEATS_AVAILABLE");
            }
        }
    }

    @Override
    public void requestBooking(String userId) {
        bookingRequests.add(userId);
    }

    @Override
    public List<String> getAvailableSeats() {
        return new ArrayList<>(availableSeats);
    }

    @Override
    public Map<String, String> getSeatStatus() {
        Map<String, String> statusMap = new HashMap<>();
        for (String seat : availableSeats) {
            statusMap.put(seat, "AVAILABLE");
        }
        for (String seat : confirmedSeats) {
            statusMap.put(seat, "CONFIRMED");
        }
        return statusMap;
    }


    @Override
    public Map<String, String> getFailedBookings() {
        return new HashMap<>(failedBookings);
    }
}