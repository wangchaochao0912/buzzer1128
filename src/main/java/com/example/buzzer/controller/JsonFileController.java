package com.example.buzzer.controller;

import com.example.buzzer.service.JsonFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/json-file")
public class JsonFileController {

    private final JsonFileService jsonFileService;

    @Autowired
    public JsonFileController(JsonFileService jsonFileService) {
        this.jsonFileService = jsonFileService;
    }

    /**
     * 处理JSON数据写入请求
     * @param requestData 请求数据
     * @return 响应结果
     */
    @PostMapping("/write")
    public ResponseEntity<String> writeData(@RequestBody Map<String, Object> requestData) {
        try {
            // 从请求数据中获取参数
            String filePath = (String) requestData.get("filePath");
            Map<String, Object> data = (Map<String, Object>) requestData.get("data");
            boolean needValidation = requestData.containsKey("needValidation") ? (boolean) requestData.get("needValidation") : false;
            String primaryKey = requestData.containsKey("primaryKey") ? (String) requestData.get("primaryKey") : null;

            // 检查必要参数是否存在
            if (filePath == null || filePath.isEmpty()) {
                return new ResponseEntity<>("文件路径不能为空", HttpStatus.BAD_REQUEST);
            }

            if (data == null || data.isEmpty()) {
                return new ResponseEntity<>("要写入的数据不能为空", HttpStatus.BAD_REQUEST);
            }

            // 调用Service层方法写入数据
            jsonFileService.writeData(filePath, data, needValidation, primaryKey);

            return new ResponseEntity<>("数据写入成功", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            return new ResponseEntity<>("文件操作失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>("数据写入失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
