package com.example.demo;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.Data;

@Data
@DynamoDBTable(tableName = StudentEntity.TABLE_NAME)
public class StudentEntity {

    public static final String TABLE_NAME = "Student";
    public static final String CLASS_ID_AND_SPORTS_TEAM_INDEX = "ClassId_SportsTeam_Index";

    @DynamoDBHashKey
    private String name;

    @DynamoDBRangeKey
    private int age;

    @DynamoDBIndexHashKey(globalSecondaryIndexName = CLASS_ID_AND_SPORTS_TEAM_INDEX)
    private String classId;

    @DynamoDBIndexRangeKey(globalSecondaryIndexName = CLASS_ID_AND_SPORTS_TEAM_INDEX)
    private String sportsTeam;

}
