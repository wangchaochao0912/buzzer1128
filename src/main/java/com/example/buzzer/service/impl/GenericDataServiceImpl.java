package com.example.buzzer.service.impl;

import com.example.buzzer.model.GenericSaveRequest;
import com.example.buzzer.service.GenericDataService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * 通用数据保存服务实现类
 */
@Service
public class GenericDataServiceImpl implements GenericDataService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 源文件目录，使用项目resources目录
    private static final String BASE_DIR = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources";

    @Override
    public void saveData(GenericSaveRequest request) throws IOException, IllegalArgumentException {
        // 参数校验
        validateRequest(request);
        
        String filePath = BASE_DIR + File.separator + request.getFileName();
        File dataFile = new File(filePath);
        
        // 如果需要校验主键，则先检查是否已存在
        if (request.isNeedCheck()) {
            checkPrimaryKeyExists(dataFile, request);
        }
        
        // 将数据序列化为JSON字符串
        String jsonData = objectMapper.writeValueAsString(request.getData());
        
        // 追加写入文件
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath), StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND)) {
            writer.write(jsonData);
            writer.newLine(); // 每条数据一行，方便后续读取
        }
    }
    
    /**
     * 校验请求参数合法性
     */
    private void validateRequest(GenericSaveRequest request) {
        if (request.getFileName() == null || request.getFileName().trim().isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        if (request.getData() == null || request.getData().isEmpty()) {
            throw new IllegalArgumentException("保存数据不能为空");
        }
        if (request.isNeedCheck() && (request.getPrimaryKey() == null || request.getPrimaryKey().trim().isEmpty())) {
            throw new IllegalArgumentException("需要主键校验时，主键字段名不能为空");
        }
        if (request.isNeedCheck() && !request.getData().containsKey(request.getPrimaryKey())) {
            throw new IllegalArgumentException("数据中不包含指定的主键字段: " + request.getPrimaryKey());
        }
    }
    
    /**
     * 检查主键是否已存在
     */
    private void checkPrimaryKeyExists(File dataFile, GenericSaveRequest request) throws IOException {
        if (!dataFile.exists()) {
            return; // 文件不存在，无需校验
        }
        
        String primaryKey = request.getPrimaryKey();
        Object newValue = request.getData().get(primaryKey);
        
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(dataFile.getPath()), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                try {
                    Map<String, Object> existingData = objectMapper.readValue(line, Map.class);
                    Object existingValue = existingData.get(primaryKey);
                    if (newValue != null && newValue.equals(existingValue)) {
                        throw new IllegalArgumentException("主键值已存在: " + newValue);
                    }
                } catch (JsonProcessingException e) {
                    // 忽略格式错误的行
                    continue;
                }
            }
        }
    }
}