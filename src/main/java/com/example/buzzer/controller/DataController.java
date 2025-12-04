package com.example.buzzer.controller;

import com.example.buzzer.dto.JsonDataRequest;
import com.example.buzzer.service.FileWriterService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DataController {
    
    @Autowired
    private FileWriterService fileWriterService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @PostMapping("/api/data")
    public ResponseEntity<String> saveData(@RequestBody JsonDataRequest request) {
        try {
            // 验证必要参数
            if (request.getFileName() == null || request.getFileName().isEmpty()) {
                return ResponseEntity.badRequest().body("文件名不能为空");
            }
            
            if (request.getJsonData() == null) {
                return ResponseEntity.badRequest().body("JSON数据不能为空");
            }
            
            // 将JsonNode转换为Map
            Map<String, Object> jsonData = objectMapper.convertValue(
                request.getJsonData(), 
                new TypeReference<Map<String, Object>>() {}
            );
            
            // 根据是否需要校验选择不同的方法
            if (request.isNeedValidation()) {
                if (request.getPrimaryKey() == null || request.getPrimaryKey().isEmpty()) {
                    return ResponseEntity.badRequest().body("校验模式下主键字段名不能为空");
                }
                fileWriterService.appendDataWithValidation(
                    request.getFileName(), 
                    jsonData, 
                    request.getPrimaryKey()
                );
            } else {
                fileWriterService.appendData(request.getFileName(), jsonData);
            }
            
            return ResponseEntity.ok("数据新增成功");
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("参数错误: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("操作失败: " + e.getMessage());
        }
    }
}