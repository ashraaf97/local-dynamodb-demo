package com.example.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest(classes = {StudentRepository.class, EmbeddedDynamoDBConfig.class})
class StudentRepositoryIT {

    private final StudentRepository studentRepository;

    @AfterAll
    static void teardownClass() {
        EmbeddedDynamoDBConfig.stopInMemoryDynamoDBServer();
    }

    @Test
    void testSave() {
        StudentEntity studentEntity = new StudentEntity()
                .setName("Alex")
                .setAge(21)
                .setClassId("1")
                .setSportsTeam("A");

        studentRepository.save(studentEntity);

        StudentEntity result = studentRepository.get(studentEntity.getName(), studentEntity.getAge());

        assertEquals(result, studentEntity);

        StudentEntity result2 = studentRepository.getByClassIdAndSportsTeam(studentEntity.getClassId(), studentEntity.getSportsTeam());

        assertEquals(result2, studentEntity);
    }

}
