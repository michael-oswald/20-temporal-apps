package com.example.flight.reservation.activities;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface BookingActivity {
    @ActivityMethod
    void saveBookingToDatabase(String userId, String seatId);
    @ActivityMethod
    String getHeldSeatFromSeatManager(String userId);
}
