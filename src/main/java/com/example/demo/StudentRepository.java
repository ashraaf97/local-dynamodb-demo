package com.example.demo;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudentRepository {

    private final DynamoDBMapper mapper;

    public void save(StudentEntity studentEntity) {
        log.debug("Saving student: {}", studentEntity);
        mapper.save(studentEntity);
    }

    public StudentEntity get(String name, int age) {
        return mapper.load(StudentEntity.class, name, age);
    }

    public StudentEntity getByClassIdAndSportsTeam(String classId, String sportsTeam) {

        HashMap<String, AttributeValue> entityAttributeValue = new HashMap<>();
        entityAttributeValue.put(":v1", new AttributeValue().withS(classId));
        entityAttributeValue.put(":v2", new AttributeValue().withS(sportsTeam));

        DynamoDBQueryExpression<StudentEntity> queryExpression = new DynamoDBQueryExpression<StudentEntity>()
                .withIndexName(StudentEntity.CLASS_ID_AND_SPORTS_TEAM_INDEX)
                .withKeyConditionExpression("classId = :v1 AND  sportsTeam = :v2")
                .withExpressionAttributeValues(entityAttributeValue)
                .withConsistentRead(false)
                .withScanIndexForward(true);

        List<StudentEntity> list = IterableUtils.toList(mapper.query(StudentEntity.class, queryExpression));

        if (list.isEmpty()) {
            return null;
        }

        return list.get(0);
    }
}
