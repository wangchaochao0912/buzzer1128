package com.example.buzzer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class JsonFileServiceTest {

    private JsonFileService jsonFileService;
    private final String testFileName = "test-data.json";
    private final String primaryKey = "id";
    private Path testFilePath;

    @BeforeEach
    void setUp() {
        // 初始化ObjectMapper和JsonFileService
        ObjectMapper objectMapper = new ObjectMapper();
        jsonFileService = new JsonFileService(objectMapper);

        // 构建测试文件路径
        testFilePath = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", testFileName);

        // 在测试前删除可能存在的测试文件
        try {
            Files.deleteIfExists(testFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testWriteDataWithoutValidation() throws IOException {
        // 创建测试数据
        Map<String, Object> testData = new HashMap<>();
        testData.put(primaryKey, 1);
        testData.put("name", "Test User");
        testData.put("age", 30);

        // 调用writeData方法，不进行校验
        jsonFileService.writeData(testFileName, testData, false, null);

        // 验证文件是否创建成功
        assertTrue(Files.exists(testFilePath));

        // 验证文件内容是否正确
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> dataFromFile = objectMapper.readValue(testFilePath.toFile(), List.class);

        assertEquals(1, dataFromFile.size());
        assertEquals(testData, dataFromFile.get(0));
    }

    @Test
    void testWriteDataWithValidation() throws IOException {
        // 创建测试数据
        Map<String, Object> testData = new HashMap<>();
        testData.put(primaryKey, 1);
        testData.put("name", "Test User");
        testData.put("age", 30);

        // 调用writeData方法，进行校验
        jsonFileService.writeData(testFileName, testData, true, primaryKey);

        // 验证文件是否创建成功
        assertTrue(Files.exists(testFilePath));

        // 尝试写入具有相同主键的重复数据，应抛出异常
        Map<String, Object> duplicateData = new HashMap<>();
        duplicateData.put(primaryKey, 1);
        duplicateData.put("name", "Duplicate User");
        duplicateData.put("age", 25);

        assertThrows(IllegalArgumentException.class, () -> {
            jsonFileService.writeData(testFileName, duplicateData, true, primaryKey);
        });
    }

    @Test
    void testWriteDataWithInvalidPrimaryKey() throws IOException {
        // 创建测试数据，不包含指定的主键
        Map<String, Object> testData = new HashMap<>();
        testData.put("name", "Test User");
        testData.put("age", 30);

        // 尝试写入数据，应抛出异常，因为数据中不包含指定的主键
        assertThrows(IllegalArgumentException.class, () -> {
            jsonFileService.writeData(testFileName, testData, true, primaryKey);
        });
    }

    @Test
    void testWriteDataWithEmptyPrimaryKey() throws IOException {
        // 创建测试数据
        Map<String, Object> testData = new HashMap<>();
        testData.put(primaryKey, 1);
        testData.put("name", "Test User");
        testData.put("age", 30);

        // 尝试写入数据，应抛出异常，因为主键字段名为空
        assertThrows(IllegalArgumentException.class, () -> {
            jsonFileService.writeData(testFileName, testData, true, "");
        });
    }
}
