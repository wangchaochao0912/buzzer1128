package com.example.buzzer.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class JsonFileService {

    private final ObjectMapper objectMapper;

    public JsonFileService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 将JSON数据写入指定文件中
     * @param filePath 文件路径
     * @param data 要写入的数据（JSON格式）
     * @param needValidation 是否需要校验
     * @param primaryKey 主键字段名（当needValidation为true时必填）
     * @throws IOException 当文件操作失败时抛出
     * @throws IllegalArgumentException 当数据校验失败时抛出
     */
    public void writeData(String fileName, Map<String, Object> data, boolean needValidation, String primaryKey) throws IOException {
        // 获取resources目录的路径
        Path resourcesPath = Paths.get(System.getProperty("user.dir"), "src", "main", "resources");
        
        // 确保resources目录存在
        if (!Files.exists(resourcesPath)) {
            Files.createDirectories(resourcesPath);
        }
        
        // 构建完整的文件路径
        Path filePath = resourcesPath.resolve(fileName);
        File file = filePath.toFile();
        
        // 检查文件是否存在，如果不存在则创建
        if (!file.exists()) {
            file.createNewFile();
        }

        // 读取文件中的现有数据
        List<Map<String, Object>> existingData = new ArrayList<>();
        if (file.length() > 0) {
            existingData = objectMapper.readValue(file, new TypeReference<List<Map<String, Object>>>() {});
        }

        // 如果需要校验，则检查主键是否存在
        if (needValidation) {
            if (primaryKey == null || primaryKey.isEmpty()) {
                throw new IllegalArgumentException("当需要校验时，主键字段名不能为空");
            }

            if (!data.containsKey(primaryKey)) {
                throw new IllegalArgumentException("数据中不包含指定的主键字段: " + primaryKey);
            }

            Object newPrimaryKeyValue = data.get(primaryKey);
            for (Map<String, Object> existingItem : existingData) {
                if (existingItem.containsKey(primaryKey) && existingItem.get(primaryKey).equals(newPrimaryKeyValue)) {
                    throw new IllegalArgumentException("数据中已经存在主键值为: " + newPrimaryKeyValue + " 的记录");
                }
            }
        }

        // 将新数据添加到现有数据列表中
        existingData.add(data);

        // 将更新后的数据写入文件
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, existingData);
    }
}
