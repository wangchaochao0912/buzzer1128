package com.example.buzzer.service;

import com.example.buzzer.entity.Transaction;
import com.example.buzzer.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class TransactionService {
    
    @Autowired
    private PdfParsingService pdfParsingService;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    /**
     * 上传并处理PDF文件
     * @param file PDF文件
     * @return 处理结果信息
     */
    @Transactional
    public String processPdfFile(MultipartFile file) {
        try {
            // 验证文件
            if (file.isEmpty()) {
                throw new IllegalArgumentException("上传的文件为空");
            }
            
            String fileName = file.getOriginalFilename();
            if (fileName == null || !fileName.toLowerCase().endsWith(".pdf")) {
                throw new IllegalArgumentException("只支持PDF文件上传");
            }
            
            // 解析PDF文件
            List<Transaction> transactions = pdfParsingService.parsePdfFile(file);
            
            if (transactions.isEmpty()) {
                return "警告：PDF文件中没有找到有效的表格数据";
            }
            
            // 保存到数据库
            int savedCount = 0;
            for (Transaction transaction : transactions) {
                try {
                    transactionRepository.save(transaction);
                    savedCount++;
                } catch (Exception e) {
                    log.error("保存交易记录失败: " + transaction.toString(), e);
                }
            }
            
            log.info("成功处理PDF文件: {}, 解析出{}条记录，成功保存{}条记录", 
                    fileName, transactions.size(), savedCount);
            
            return String.format("成功处理PDF文件，共解析出%d条记录，成功保存%d条记录", 
                    transactions.size(), savedCount);
            
        } catch (IOException e) {
            log.error("读取PDF文件失败", e);
            throw new RuntimeException("读取PDF文件失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("处理PDF文件失败", e);
            throw new RuntimeException("处理PDF文件失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取所有交易记录
     * @return 交易记录列表
     */
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
    
    /**
     * 根据文件名获取交易记录
     * @param fileName 文件名
     * @return 交易记录列表
     */
    public List<Transaction> getTransactionsByFileName(String fileName) {
        return transactionRepository.findBySourceFile(fileName);
    }
    
    /**
     * 根据交易类型获取交易记录
     * @param transactionType 交易类型 (PURCHASE/RETURN)
     * @return 交易记录列表
     */
    public List<Transaction> getTransactionsByType(String transactionType) {
        return transactionRepository.findByTransactionType(transactionType);
    }
    
    /**
     * 根据日期范围获取交易记录
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 交易记录列表
     */
    public List<Transaction> getTransactionsByDateRange(java.time.LocalDate startDate, 
                                                        java.time.LocalDate endDate) {
        return transactionRepository.findByTransactionDateBetween(startDate, endDate);
    }
}