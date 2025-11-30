package com.example.buzzer.service;

import com.example.buzzer.model.ProductTransaction;
import com.example.buzzer.repository.ProductTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class ProductTransactionService {

    @Autowired
    private PdfParserService pdfParserService;

    @Autowired
    private ProductTransactionRepository productTransactionRepository;

    public List<ProductTransaction> processPdfFile(MultipartFile file, boolean isTwoTables) throws IOException {
        // 将MultipartFile转换为File
        File tempFile = convertMultipartFileToFile(file);

        try {
            // 解析PDF文件
            List<ProductTransaction> transactions = pdfParserService.parsePdf(tempFile, isTwoTables);

            // 保存到数据库
            return productTransactionRepository.saveAll(transactions);
        } finally {
            // 删除临时文件
            tempFile.delete();
        }
    }

    private File convertMultipartFileToFile(MultipartFile file) throws IOException {
        // 创建临时文件
        File tempFile = File.createTempFile("upload-", ".pdf");

        // 复制MultipartFile的内容到临时文件
        Path tempPath = tempFile.toPath();
        Files.copy(file.getInputStream(), tempPath, StandardCopyOption.REPLACE_EXISTING);

        return tempFile;
    }
}