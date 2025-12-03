package com.example.buzzer.controller;

import com.example.buzzer.model.GenericSaveRequest;
import com.example.buzzer.service.GenericDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 通用数据保存控制器
 */
@RestController
public class GenericDataController {

    @Autowired
    private GenericDataService genericDataService;

    /**
     * 通用数据保存接口
     * @param request 保存请求
     * @return 响应结果
     */
    @PostMapping("/api/save-data")
    public ResponseEntity<String> saveData(@RequestBody GenericSaveRequest request) {
        try {
            genericDataService.saveData(request);
            return ResponseEntity.ok("数据保存成功");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("参数错误: " + e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("文件写入失败: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("保存失败: " + e.getMessage());
        }
    }
}