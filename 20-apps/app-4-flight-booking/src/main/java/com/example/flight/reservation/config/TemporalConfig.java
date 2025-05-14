package com.example.flight.reservation.config;


import com.example.flight.reservation.activities.BookingActivityImpl;
import com.example.flight.reservation.workflows.BookingWorkflowImpl;
import com.example.flight.reservation.workflows.SeatManagerWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemporalConfig {

    @Value("${temporal.server.address:127.0.0.1:7233}")
    private String temporalServerAddress;

    @Value("${temporal.namespace:default}")
    private String temporalNamespace;

    @Value("${temporal.taskqueue:BOOKING_TASK_QUEUE}")
    private String bookingTaskQueue;

    @Value("${temporal.taskqueue:SEAT_TASK_QUEUE}")
    private String seatTaskQueue;

    @Bean
    public WorkflowServiceStubs workflowServiceStubs() {
        return WorkflowServiceStubs.newInstance(
                                WorkflowServiceStubsOptions.newBuilder()
                                        .setTarget(temporalServerAddress)
                                        .build());
    }

    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs workflowServiceStubs) {
        return WorkflowClient.newInstance(workflowServiceStubs,
                WorkflowClientOptions.newBuilder()
                        .setNamespace(temporalNamespace)
                        .build());
    }

    @Bean
    public WorkerFactory workerFactory(WorkflowClient workflowClient, BookingActivityImpl bookingActivity) {
        WorkerFactory workerFactory = WorkerFactory.newInstance(workflowClient);

        Worker seatWorker = workerFactory.newWorker(seatTaskQueue);
        seatWorker.registerWorkflowImplementationTypes(SeatManagerWorkflowImpl.class);

        Worker bookingWorker = workerFactory.newWorker(bookingTaskQueue);
        bookingWorker.registerWorkflowImplementationTypes(BookingWorkflowImpl.class);
        bookingWorker.registerActivitiesImplementations(bookingActivity);

        workerFactory.start();
        return workerFactory;
    }

    @Bean
    public BookingActivityImpl bookingActivity() {
        return new BookingActivityImpl();
    }
}