package com.example.buzzer.service;

import java.util.Map;

public interface FileWriterService {
    
    /**
     * 新增JSON数据到文件，不做校验
     * @param fileName 文件名
     * @param jsonData JSON数据
     */
    void appendData(String fileName, Map<String, Object> jsonData);
    
    /**
     * 新增JSON数据到文件，带主键校验
     * @param fileName 文件名
     * @param jsonData JSON数据
     * @param primaryKey 主键字段名
     */
    void appendDataWithValidation(String fileName, Map<String, Object> jsonData, String primaryKey);
}