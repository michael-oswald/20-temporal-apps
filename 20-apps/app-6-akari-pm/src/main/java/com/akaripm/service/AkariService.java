package com.akaripm.service;

import com.akaripm.activity.DynamoDbActivityImpl;
import com.akaripm.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AkariService {
    private static final String TABLE_NAME = "akaripm";
    private static final Logger logger = LoggerFactory.getLogger(AkariService.class);

    @Autowired
    private DynamoDbClient dynamoDbClient;
    private final DynamoDbTable<ProjectDTO> projectTable;

    @Autowired
    public AkariService(DynamoDbClient dynamoDbClient) {
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
        this.projectTable = enhancedClient.table(TABLE_NAME, TableSchema.fromBean(ProjectDTO.class));
    }

    public List<ProjectNameProjectIdPair> getNamesAndIds() {

        try {
            // Scan the table to get all items
            var scanResponse = dynamoDbClient.scan(builder -> builder
                    .tableName(TABLE_NAME)
                    .attributesToGet("projectId", "projectName")
                    .build());

            List<ProjectNameProjectIdPair> results = new ArrayList<>();

            // Process each item in the scan results
            for (Map<String, AttributeValue> item : scanResponse.items()) {
                String projectId = item.get("projectId").s();
                String projectName = item.get("projectName").s();
                results.add(new ProjectNameProjectIdPair(projectName, projectId));
            }

            return results;
        } catch (Exception e) {
            logger.error("Error retrieving project names and IDs", e);
            throw new RuntimeException("Failed to retrieve project names and IDs", e);
        }
    }

    public ProjectDTO getProjectById(String projectId) {
        ProjectDTO projectDto  = null;
        try {
            projectDto= projectTable.getItem(Key.builder().partitionValue(projectId).build());
        } catch (Exception e) {
            logger.error("Error retrieving project with ID: " + projectId, e);
            throw new RuntimeException("Failed to retrieve project", e);
        }

        for (TrackDTO track : projectDto.getTracks()) {
            int totalWorkDays = 0;

            // Sum up work days for tasks that are not completed
            totalWorkDays += track.getTasks().stream()
                    .filter(task -> {
                        TaskStatus status = task.getStatus();
                        return status == TaskStatus.BLOCKED ||
                                status == TaskStatus.IN_PROGRESS ||
                                status == TaskStatus.NOT_STARTED;
                    })
                    .mapToInt(TaskDTO::getDayEstimate)
                    .sum();

            // Calculate future date based on 5-day work week
            if (totalWorkDays > 0) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime calculatedDate = now;

                int daysAdded = 0;
                while (daysAdded < totalWorkDays) {
                    calculatedDate = calculatedDate.plusDays(1);
                    // Skip weekends (Saturday=6, Sunday=7)
                    if (calculatedDate.getDayOfWeek().getValue() < 6) {
                        daysAdded++;
                    }
                }

                track.setCalculatedDueDate(calculatedDate);
                track.setCalculatedNumBusinessDaysRemaining(totalWorkDays);
            }
        }
        return projectDto;
    }
}
