package com.example.buzzer.model;

import lombok.Data;

import java.util.Map;

/**
 * 通用保存请求参数
 */
@Data
public class GenericSaveRequest {
    /**
     * 要写入的文件名
     */
    private String fileName;
    
    /**
     * 要保存的JSON数据
     */
    private Map<String, Object> data;
    
    /**
     * 是否需要主键校验
     */
    private boolean needCheck;
    
    /**
     * 主键字段名（当needCheck为true时必填）
     */
    private String primaryKey;
}