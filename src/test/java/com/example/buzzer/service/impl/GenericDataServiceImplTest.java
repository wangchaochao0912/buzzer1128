package com.example.buzzer.service.impl;

import com.example.buzzer.model.GenericSaveRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class GenericDataServiceImplTest {

    @InjectMocks
    private GenericDataServiceImpl genericDataService;
    
    private static final String TEST_FILE_NAME = "test-unit.txt";
    private static final String TEST_FILE_PATH = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + TEST_FILE_NAME;

    @BeforeEach
    public void setUp() throws IOException {
        // 创建resources目录（如果不存在）
        File resourcesDir = new File(System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources");
        if (!resourcesDir.exists()) {
            resourcesDir.mkdirs();
        }
        
        // 删除测试文件（如果存在）
        File testFile = new File(TEST_FILE_PATH);
        if (testFile.exists()) {
            Files.delete(Paths.get(TEST_FILE_PATH));
        }
    }

    @AfterEach
    public void tearDown() throws IOException {
        // 清理测试文件
        File testFile = new File(TEST_FILE_PATH);
        if (testFile.exists()) {
            Files.delete(Paths.get(TEST_FILE_PATH));
        }
    }

    @Test
    public void testSaveDataWithoutCheck() throws IOException {
        // 创建请求
        GenericSaveRequest request = new GenericSaveRequest();
        request.setFileName(TEST_FILE_NAME);
        request.setNeedCheck(false);
        
        Map<String, Object> data = new HashMap<>();
        data.put("id", "1001");
        data.put("name", "测试数据1");
        data.put("value", "demo1");
        request.setData(data);
        
        // 调用方法
        genericDataService.saveData(request);
        
        // 验证文件存在且内容正确
        File testFile = new File(TEST_FILE_PATH);
        assertTrue(testFile.exists());
        
        String content = new String(Files.readAllBytes(Paths.get(TEST_FILE_PATH)));
        assertTrue(content.contains("\"id\":\"1001\""));
        assertTrue(content.contains("\"name\":\"测试数据1\""));
    }

    @Test
    public void testSaveDataWithCheckSuccess() throws IOException {
        // 创建请求
        GenericSaveRequest request = new GenericSaveRequest();
        request.setFileName(TEST_FILE_NAME);
        request.setNeedCheck(true);
        request.setPrimaryKey("id");
        
        Map<String, Object> data = new HashMap<>();
        data.put("id", "1002");
        data.put("name", "测试数据2");
        request.setData(data);
        
        // 调用方法
        genericDataService.saveData(request);
        
        // 验证文件存在
        File testFile = new File(TEST_FILE_PATH);
        assertTrue(testFile.exists());
    }

    @Test
    public void testSaveDataWithCheckDuplicateKey() throws IOException {
        // 先保存一条数据
        GenericSaveRequest request1 = new GenericSaveRequest();
        request1.setFileName(TEST_FILE_NAME);
        request1.setNeedCheck(true);
        request1.setPrimaryKey("id");
        
        Map<String, Object> data1 = new HashMap<>();
        data1.put("id", "1003");
        data1.put("name", "测试数据3");
        request1.setData(data1);
        genericDataService.saveData(request1);
        
        // 再次保存相同主键的数据，应该抛出异常
        GenericSaveRequest request2 = new GenericSaveRequest();
        request2.setFileName(TEST_FILE_NAME);
        request2.setNeedCheck(true);
        request2.setPrimaryKey("id");
        
        Map<String, Object> data2 = new HashMap<>();
        data2.put("id", "1003");
        data2.put("name", "测试数据3重复");
        request2.setData(data2);
        
        assertThrows(IllegalArgumentException.class, () -> {
            genericDataService.saveData(request2);
        });
    }

    @Test
    public void testSaveDataWithEmptyFileName() throws IOException {
        GenericSaveRequest request = new GenericSaveRequest();
        request.setFileName("");
        request.setNeedCheck(false);
        
        Map<String, Object> data = new HashMap<>();
        data.put("id", "1004");
        request.setData(data);
        
        assertThrows(IllegalArgumentException.class, () -> {
            genericDataService.saveData(request);
        });
    }

    @Test
    public void testSaveDataWithEmptyData() throws IOException {
        GenericSaveRequest request = new GenericSaveRequest();
        request.setFileName(TEST_FILE_NAME);
        request.setNeedCheck(false);
        request.setData(new HashMap<>());
        
        assertThrows(IllegalArgumentException.class, () -> {
            genericDataService.saveData(request);
        });
    }

    @Test
    public void testSaveDataWithCheckWithoutPrimaryKey() throws IOException {
        GenericSaveRequest request = new GenericSaveRequest();
        request.setFileName(TEST_FILE_NAME);
        request.setNeedCheck(true);
        request.setPrimaryKey(null); // 主键为空
        
        Map<String, Object> data = new HashMap<>();
        data.put("id", "1005");
        request.setData(data);
        
        assertThrows(IllegalArgumentException.class, () -> {
            genericDataService.saveData(request);
        });
    }

    @Test
    public void testSaveDataWithCheckPrimaryKeyNotExists() throws IOException {
        GenericSaveRequest request = new GenericSaveRequest();
        request.setFileName(TEST_FILE_NAME);
        request.setNeedCheck(true);
        request.setPrimaryKey("non_exist_key");
        
        Map<String, Object> data = new HashMap<>();
        data.put("id", "1006");
        request.setData(data);
        
        assertThrows(IllegalArgumentException.class, () -> {
            genericDataService.saveData(request);
        });
    }
}