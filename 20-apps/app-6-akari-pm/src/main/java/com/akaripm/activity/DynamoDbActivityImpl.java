package com.akaripm.activity;

import com.akaripm.model.ProjectDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Service
public class DynamoDbActivityImpl implements DynamoDbActivity {

    private static final Logger logger = LoggerFactory.getLogger(DynamoDbActivityImpl.class);
    private static final String TABLE_NAME = "akaripm";

    private final DynamoDbTable<ProjectDTO> projectTable;

    @Autowired
    public DynamoDbActivityImpl(DynamoDbClient dynamoDbClient) {
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
        this.projectTable = enhancedClient.table(TABLE_NAME, TableSchema.fromBean(ProjectDTO.class));
    }

    @Override
    public void save(ProjectDTO projectDTO) {
        logger.info("Saving project: {}", projectDTO.getProjectId());
        projectTable.putItem(projectDTO);
    }

    @Override
    public ProjectDTO get(String projectId) {
        try {
            return projectTable.getItem(Key.builder().partitionValue(projectId).build());
        } catch (Exception e) {
            logger.error("Error retrieving project with ID: " + projectId, e);
            throw new RuntimeException("Failed to retrieve project", e);
        }
    }
}
