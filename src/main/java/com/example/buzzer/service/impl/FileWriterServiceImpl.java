package com.example.buzzer.service.impl;

import com.example.buzzer.service.FileWriterService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FileWriterServiceImpl implements FileWriterService {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String DEFAULT_BASE_DIR = "src/main/resources/"; // 默认文件目录
    private static String baseDir = DEFAULT_BASE_DIR; // 可配置的文件目录
    
    // 用于测试的构造函数
    FileWriterServiceImpl() {
        // 空构造函数，保持与Spring的兼容性
    }
    
    // 用于测试的构造函数，可以指定baseDir
    FileWriterServiceImpl(String baseDir) {
        this.baseDir = baseDir;
    }
    
    // 用于测试的setter方法
    static void setBaseDir(String baseDir) {
        FileWriterServiceImpl.baseDir = baseDir;
    }
    
    @Override
    public void appendData(String fileName, Map<String, Object> jsonData) {
        try {
            // 确保目录存在
            Files.createDirectories(Paths.get(baseDir));
            
            // 构建文件路径
            String filePath = baseDir + fileName;
            File file = new File(filePath);
            
            // 读取现有数据
            List<Map<String, Object>> dataList;
            if (file.exists()) {
                dataList = objectMapper.readValue(file, new TypeReference<List<Map<String, Object>>>() {});
            } else {
                dataList = new ArrayList<>();
            }
            
            // 新增数据
            dataList.add(jsonData);
            
            // 写回文件
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, dataList);
            
        } catch (IOException e) {
            throw new RuntimeException("写入文件失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void appendDataWithValidation(String fileName, Map<String, Object> jsonData, String primaryKey) {
        try {
            // 确保目录存在
            Files.createDirectories(Paths.get(baseDir));
            
            // 构建文件路径
            String filePath = baseDir + fileName;
            File file = new File(filePath);
            
            // 读取现有数据
            List<Map<String, Object>> dataList;
            if (file.exists()) {
                dataList = objectMapper.readValue(file, new TypeReference<List<Map<String, Object>>>() {});
            } else {
                dataList = new ArrayList<>();
            }
            
            // 校验主键是否存在
            Object primaryKeyValue = jsonData.get(primaryKey);
            if (primaryKeyValue == null) {
                throw new IllegalArgumentException("数据中缺少主键字段: " + primaryKey);
            }
            
            // 检查主键是否重复
            Optional<Map<String, Object>> existingRecord = dataList.stream()
                .filter(record -> primaryKeyValue.equals(record.get(primaryKey)))
                .findFirst();
            
            if (existingRecord.isPresent()) {
                throw new RuntimeException("主键已存在: " + primaryKeyValue);
            }
            
            // 新增数据
            dataList.add(jsonData);
            
            // 写回文件
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, dataList);
            
        } catch (IOException e) {
            throw new RuntimeException("写入文件失败: " + e.getMessage(), e);
        }
    }
}