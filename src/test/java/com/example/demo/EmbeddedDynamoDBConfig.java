package com.example.demo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded;
import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import com.amazonaws.services.dynamodbv2.model.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;

@Configuration
@Slf4j
public class EmbeddedDynamoDBConfig {

    public static final String COUNTER_HASH_KEY = "id";

    private static DynamoDBProxyServer server;

    static {
        try {
            startInMemoryDynamoDBServer();
        } catch (Exception e) {
            log.error("Failed to boot in memory dynamo");
        }
    }

    @SneakyThrows
    public static void startInMemoryDynamoDBServer() {
        System.setProperty("sqlite4java.library.path", "native-libs");
        String port = getAvailablePort();
        server = ServerRunner.createServerFromCommandLineArgs(new String[]{"-inMemory", "-port", port});
        server.start();
    }

    @SneakyThrows
    public static void stopInMemoryDynamoDBServer() {
        server.stop();
    }

    private static String getAvailablePort() {
        try (final ServerSocket serverSocket = new ServerSocket(0)) {
            return String.valueOf(serverSocket.getLocalPort());
        } catch (IOException e) {
            throw new RuntimeException("Available port was not found", e);
        }
    }

    @Bean
    @Primary
    public DynamoDBMapper ddbIterationOnlyMapper(AmazonDynamoDB ddbClient) {

        DynamoDBMapperConfig mapperConfig = DynamoDBMapperConfig.builder()
                .withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT)
                .withPaginationLoadingStrategy(DynamoDBMapperConfig.PaginationLoadingStrategy.ITERATION_ONLY)
                .build();

        return new DynamoDBMapper(ddbClient, mapperConfig);
    }

    @Bean
    @Primary
    public AmazonDynamoDB getDynamoDBEmbedded() {
        AmazonDynamoDB amazonDynamoDB = DynamoDBEmbedded
                .create()
                .amazonDynamoDB();

        createStudentTable(amazonDynamoDB);

        return amazonDynamoDB;
    }

    private void createStudentTable(AmazonDynamoDB amazonDynamoDB) {
        CreateTableRequest createTableRequest = new CreateTableRequest()
                .withTableName(StudentEntity.TABLE_NAME)
                .withAttributeDefinitions(
                        new AttributeDefinition("name", ScalarAttributeType.S),
                        new AttributeDefinition("age", ScalarAttributeType.N),
                        new AttributeDefinition("classId", ScalarAttributeType.S),
                        new AttributeDefinition("sportsTeam", ScalarAttributeType.S)
                )
                .withKeySchema(new KeySchemaElement("name", KeyType.HASH), new KeySchemaElement("age", KeyType.RANGE))
                .withProvisionedThroughput(new ProvisionedThroughput(100L, 100L));

        GlobalSecondaryIndex classIdAndSportTeamIndex = createIndex(StudentEntity.CLASS_ID_AND_SPORTS_TEAM_INDEX, "classId", "sportsTeam");
        createTableRequest.setGlobalSecondaryIndexes(Collections.singletonList(classIdAndSportTeamIndex));
        amazonDynamoDB.createTable(createTableRequest);
    }

    private GlobalSecondaryIndex createIndex(String indexName, String hashKeyAttributeName, String rangeKeyAttributeName) {
        GlobalSecondaryIndex index = (new GlobalSecondaryIndex()).withIndexName(indexName).withProvisionedThroughput((new ProvisionedThroughput()).withReadCapacityUnits(1L).withWriteCapacityUnits(1L)).withProjection((new Projection()).withProjectionType(ProjectionType.ALL));
        ArrayList<KeySchemaElement> indexKeySchema = new ArrayList();
        indexKeySchema.add((new KeySchemaElement()).withAttributeName(hashKeyAttributeName).withKeyType(KeyType.HASH));
        if (rangeKeyAttributeName != null) {
            indexKeySchema.add((new KeySchemaElement()).withAttributeName(rangeKeyAttributeName).withKeyType(KeyType.RANGE));
        }

        index.setKeySchema(indexKeySchema);
        return index;
    }
}
