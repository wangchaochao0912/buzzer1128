package com.example.buzzer.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class JsonDataRequest {
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * JSON数据
     */
    private JsonNode jsonData;
    
    /**
     * 是否需要校验
     */
    private boolean needValidation;
    
    /**
     * 主键字段名（当needValidation为true时必填）
     */
    private String primaryKey;
}