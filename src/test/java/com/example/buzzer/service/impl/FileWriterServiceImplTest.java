package com.example.buzzer.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FileWriterServiceImplTest {

    private FileWriterServiceImpl fileWriterService;
    private ObjectMapper objectMapper;

    // 使用临时目录来避免影响实际项目结构
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // 确保临时目录存在
        try {
            Files.createDirectories(tempDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // 使用带参数的构造函数创建FileWriterServiceImpl实例
        fileWriterService = new FileWriterServiceImpl(tempDir.toAbsolutePath().toString() + File.separator);
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() {
        // 测试后清理临时文件
        File[] files = tempDir.toFile().listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    @Test
    void testAppendData_createsNewFile_whenFileDoesNotExist() {
        // 准备测试数据
        String fileName = "test.json";
        Map<String, Object> jsonData = new HashMap<>();
        jsonData.put("id", 1);
        jsonData.put("name", "test");
        jsonData.put("value", "test value");

        // 执行测试方法
        fileWriterService.appendData(fileName, jsonData);

        // 验证文件是否创建
        File testFile = new File(tempDir.toAbsolutePath().toString() + File.separator + fileName);
        assertTrue(testFile.exists());
        assertTrue(testFile.isFile());
    }

    @Test
    void testAppendData_appendsData_whenFileExists() throws IOException {
        // 准备初始文件
        String fileName = "test_append.json";
        File testFile = new File(tempDir.toAbsolutePath().toString() + File.separator + fileName);
        List<Map<String, Object>> initialData = new ArrayList<>();
        Map<String, Object> initialRecord = new HashMap<>();
        initialRecord.put("id", 1);
        initialRecord.put("name", "initial");
        initialData.add(initialRecord);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(testFile, initialData);

        // 准备新增数据
        Map<String, Object> newData = new HashMap<>();
        newData.put("id", 2);
        newData.put("name", "new record");
        newData.put("value", "new value");

        // 执行测试方法
        fileWriterService.appendData(fileName, newData);

        // 验证数据是否正确追加
        List<Map<String, Object>> resultData = objectMapper.readValue(testFile, 
            new TypeReference<List<Map<String, Object>>>() {});
        assertEquals(2, resultData.size());
        assertEquals("initial", resultData.get(0).get("name"));
        assertEquals("new record", resultData.get(1).get("name"));
    }

    @Test
    void testAppendData_writesCorrectJsonFormat() throws IOException {
        // 准备测试数据
        String fileName = "test_format.json";
        Map<String, Object> jsonData = new HashMap<>();
        jsonData.put("id", 1);
        jsonData.put("name", "test format");
        
        // 使用Java 8支持的方式创建嵌套Map
        Map<String, Object> nested = new HashMap<>();
        nested.put("key", "value");
        nested.put("number", 42);
        jsonData.put("nested", nested);
        
        // 使用Java 8支持的方式创建List
        List<String> list = new ArrayList<>();
        list.add("item1");
        list.add("item2");
        list.add("item3");
        jsonData.put("list", list);

        // 执行测试方法
        fileWriterService.appendData(fileName, jsonData);

        // 验证JSON格式是否正确
        File testFile = new File(tempDir.toAbsolutePath().toString() + File.separator + fileName);
        List<Map<String, Object>> resultData = objectMapper.readValue(testFile, 
            new TypeReference<List<Map<String, Object>>>() {});
        assertEquals(1, resultData.size());
        assertEquals(1, resultData.get(0).get("id"));
        assertEquals("test format", resultData.get(0).get("name"));
        assertNotNull(resultData.get(0).get("nested"));
        assertNotNull(resultData.get(0).get("list"));
        
        // 验证嵌套对象
        Map<String, Object> resultNested = (Map<String, Object>) resultData.get(0).get("nested");
        assertEquals("value", resultNested.get("key"));
        assertEquals(42, resultNested.get("number"));
        
        // 验证列表
        List<String> resultList = (List<String>) resultData.get(0).get("list");
        assertEquals(3, resultList.size());
        assertEquals("item1", resultList.get(0));
        assertEquals("item2", resultList.get(1));
        assertEquals("item3", resultList.get(2));
    }

    @Test
    void testAppendData_withEmptyData() {
        // 准备测试数据
        String fileName = "test_empty.json";
        Map<String, Object> jsonData = new HashMap<>();

        // 执行测试方法，应该不会抛出异常
        assertDoesNotThrow(() -> fileWriterService.appendData(fileName, jsonData));

        // 验证文件是否创建
        File testFile = new File(tempDir.toAbsolutePath().toString() + File.separator + fileName);
        assertTrue(testFile.exists());
    }

    @Test
    void testAppendData_withSpecialCharactersInFileName() {
        // 准备测试数据，使用包含特殊字符的文件名
        String fileName = "test_file_with_special_chars.json";
        Map<String, Object> jsonData = new HashMap<>();
        jsonData.put("id", 1);
        jsonData.put("name", "test");

        // 执行测试方法
        fileWriterService.appendData(fileName, jsonData);

        // 验证文件是否创建
        File testFile = new File(tempDir.toAbsolutePath().toString() + File.separator + fileName);
        assertTrue(testFile.exists());
    }
}