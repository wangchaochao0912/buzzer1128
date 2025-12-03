package com.example.buzzer.service;

import com.example.buzzer.model.GenericSaveRequest;
import java.io.IOException;

/**
 * 通用数据保存服务接口
 */
public interface GenericDataService {
    /**
     * 保存数据到文件
     * @param request 保存请求参数
     * @throws IOException 文件操作异常
     * @throws IllegalArgumentException 参数非法异常
     */
    void saveData(GenericSaveRequest request) throws IOException, IllegalArgumentException;
}