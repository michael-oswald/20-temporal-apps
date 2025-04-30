# simple webhook consumer
This project is a simple webhook consumer built with Java, Spring Boot, and Temporal. It demonstrates how to use Temporal workflows to process and manage webhook events reliably.

## Features
- Receives webhook events via REST API
- Processes events through Temporal workflows
- Provides durable execution with automatic retries
- Handles event history and status tracking

## Technologies
- Java
- Spring Boot
- Temporal
- DynamoDB (for event storage)
- Maven

## Sequence Diagram

```mermaid
sequenceDiagram
    participant Client
    participant WebhookAPI as Webhook API
    participant Workflow as Temporal Workflow
    participant Activity as Temporal Activity
    participant DynamoDB

    Client->>WebhookAPI: POST /webhook (event data)
    WebhookAPI->>DynamoDB: Store incoming event
    WebhookAPI->>Workflow: Start workflow execution
    WebhookAPI-->>Client: 202 Accepted (with tracking ID)
    
    Workflow->>Activity: Process webhook data
    Activity->>DynamoDB: Update event status
    
    alt Success
        Activity-->>Workflow: Success result
        Workflow->>DynamoDB: Mark event as processed
    else Failure
        Activity-->>Workflow: Failure
        Workflow->>Activity: Retry activity (with backoff)
    end
 ```

This is the first application in a series of 100 Temporal apps built to explore different use cases and learn the Temporal platform.
