package com.example.buzzer.controller;

import com.example.buzzer.entity.Transaction;
import com.example.buzzer.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@Slf4j
public class TransactionController {
    
    @Autowired
    private TransactionService transactionService;
    
    /**
     * 上传PDF文件并解析交易数据
     * @param file PDF文件
     * @return 处理结果
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadPdfFile(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String result = transactionService.processPdfFile(file);
            response.put("success", true);
            response.put("message", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("上传PDF文件失败", e);
            response.put("success", false);
            response.put("message", "处理PDF文件失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取所有交易记录
     * @return 交易记录列表
     */
    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        List<Transaction> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * 根据文件名获取交易记录
     * @param fileName 文件名
     * @return 交易记录列表
     */
    @GetMapping("/by-file")
    public ResponseEntity<List<Transaction>> getTransactionsByFileName(@RequestParam String fileName) {
        List<Transaction> transactions = transactionService.getTransactionsByFileName(fileName);
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * 根据交易类型获取交易记录
     * @param transactionType 交易类型 (PURCHASE/RETURN)
     * @return 交易记录列表
     */
    @GetMapping("/by-type")
    public ResponseEntity<List<Transaction>> getTransactionsByType(@RequestParam String transactionType) {
        List<Transaction> transactions = transactionService.getTransactionsByType(transactionType);
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * 根据日期范围获取交易记录
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 交易记录列表
     */
    @GetMapping("/by-date-range")
    public ResponseEntity<List<Transaction>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Transaction> transactions = transactionService.getTransactionsByDateRange(startDate, endDate);
        return ResponseEntity.ok(transactions);
    }
}