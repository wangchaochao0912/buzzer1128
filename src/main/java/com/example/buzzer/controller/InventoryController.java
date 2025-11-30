package com.example.buzzer.controller;

import com.example.buzzer.entity.Inventory;
import com.example.buzzer.service.InventoryService;
import com.example.buzzer.service.PdfParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private PdfParserService pdfParserService;

    @Autowired
    private InventoryService inventoryService;

    /**
     * 上传PDF文件并解析存储到数据库
     */
    @PostMapping("/upload-pdf")
    public ResponseEntity<?> uploadPdf(@RequestParam("file") MultipartFile file) {
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("上传的文件为空");
            }

            // 检查文件类型是否为PDF
            if (!file.getContentType().equals("application/pdf")) {
                return ResponseEntity.badRequest().body("只能上传PDF文件");
            }

            // 解析PDF文件
            List<Inventory> inventoryList = pdfParserService.parsePdf(file.getInputStream());

            // 保存到数据库
            List<Inventory> savedList = inventoryService.saveInventoryData(inventoryList);

            return ResponseEntity.ok("PDF文件解析成功，共保存了 " + savedList.size() + " 条记录");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("解析PDF文件时发生错误：" + e.getMessage());
        }
    }

    /**
     * 获取所有库存数据
     */
    @GetMapping
    public ResponseEntity<List<Inventory>> getAllInventory() {
        List<Inventory> inventoryList = inventoryService.getAllInventory();
        return ResponseEntity.ok(inventoryList);
    }
}
