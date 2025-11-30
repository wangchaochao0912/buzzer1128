package com.example.buzzer.controller;

import com.example.buzzer.model.ProductTransaction;
import com.example.buzzer.service.ProductTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/pdf")
public class PdfUploadController {

    @Autowired
    private ProductTransactionService productTransactionService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadPdfFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "isTwoTables", defaultValue = "false") boolean isTwoTables) {

        try {
            // 验证文件是否为空
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("上传的文件为空");
            }

            // 验证文件类型是否为PDF
            if (!file.getContentType().equals("application/pdf")) {
                return ResponseEntity.badRequest().body("请上传PDF格式的文件");
            }

            // 处理PDF文件
            List<ProductTransaction> transactions = productTransactionService.processPdfFile(file, isTwoTables);

            // 返回处理结果
            return ResponseEntity.ok().body("PDF文件解析成功，共保存了 " + transactions.size() + " 条记录");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("PDF文件解析失败: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("PDF文件格式不正确: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("处理PDF文件时发生错误: " + e.getMessage());
        }
    }
}