package com.example.buzzer.controller;

import com.example.buzzer.entity.ProductData;
import com.example.buzzer.service.ProductDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/pdf")
public class PdfUploadController {

    @Autowired
    private ProductDataService productDataService;

    // 上传PDF文件并解析数据
    @PostMapping("/upload")
    public ResponseEntity<?> uploadPdfFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            List<ProductData> savedData = productDataService.uploadAndProcessPdf(file);
            return ResponseEntity.ok(savedData);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing PDF file: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error: " + e.getMessage());
        }
    }

    // 获取所有数据
    @GetMapping("/data")
    public ResponseEntity<List<ProductData>> getAllProductData() {
        List<ProductData> data = productDataService.getAllProductData();
        return ResponseEntity.ok(data);
    }

    // 根据退货状态查询
    @GetMapping("/data/return-status")
    public ResponseEntity<List<ProductData>> getByReturnStatus(@RequestParam Boolean isReturn) {
        List<ProductData> data = productDataService.getProductDataByReturnStatus(isReturn);
        return ResponseEntity.ok(data);
    }

    // 根据日期范围查询
    @GetMapping("/data/date-range")
    public ResponseEntity<List<ProductData>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<ProductData> data = productDataService.getProductDataByDateRange(startDate, endDate);
        return ResponseEntity.ok(data);
    }

    // 根据款号查询
    @GetMapping("/data/product-code/{code}")
    public ResponseEntity<List<ProductData>> getByProductCode(@PathVariable String code) {
        List<ProductData> data = productDataService.getProductDataByProductCode(code);
        return ResponseEntity.ok(data);
    }
}