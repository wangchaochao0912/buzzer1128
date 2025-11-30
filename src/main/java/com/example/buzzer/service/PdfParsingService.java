package com.example.buzzer.service;

import com.example.buzzer.entity.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;
import technology.tabula.extractors.BasicExtractionAlgorithm;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PdfParsingService {
    
    @Autowired
    private HeaderMappingService headerMappingService;
    
    @Autowired
    private DataCleaningService dataCleaningService;
    
    /**
     * 解析PDF文件中的表格数据
     * @param file PDF文件
     * @return 解析出的交易记录列表
     */
    public List<Transaction> parsePdfFile(MultipartFile file) throws IOException {
        // 创建临时文件
        Path tempDir = Files.createTempDirectory("pdf-upload-");
        Path tempFile = tempDir.resolve(file.getOriginalFilename());
        file.transferTo(tempFile.toFile());
        
        try {
            // 使用Tabula提取表格
            PDDocument document = PDDocument.load(tempFile.toFile());
            ObjectExtractor oe = new ObjectExtractor(document);
            List<Table> tables = new ArrayList<>();
            
            // 提取所有页面的表格
            for (int pageNum = 1; pageNum <= document.getNumberOfPages(); pageNum++) {
                Page page = oe.extract(pageNum);
                BasicExtractionAlgorithm algorithm = new BasicExtractionAlgorithm();
                List<Table> pageTables = algorithm.extract(page);
                tables.addAll(pageTables);
            }
            
            // 解析表格数据
            return parseTables(tables, file.getOriginalFilename());
        } finally {
            // 清理临时文件
            try {
                Files.deleteIfExists(tempFile);
                Files.deleteIfExists(tempDir);
            } catch (IOException e) {
                log.warn("Failed to delete temporary file: " + tempFile, e);
            }
        }
    }
    
    /**
     * 解析表格数据
     * @param tables 表格列表
     * @param fileName 文件名
     * @return 解析出的交易记录列表
     */
    private List<Transaction> parseTables(List<Table> tables, String fileName) {
        List<Transaction> transactions = new ArrayList<>();
        
        if (tables.isEmpty()) {
            log.warn("No tables found in the PDF file");
            return transactions;
        }
        
        // 判断是单表格还是双表格
        if (tables.size() == 1) {
            // 单表格：根据金额正负判断进货/退货
            transactions.addAll(parseSingleTable(tables.get(0), fileName));
        } else {
            // 双表格：第一个表格为进货，第二个表格为退货
            transactions.addAll(parsePurchaseTable(tables.get(0), fileName));
            if (tables.size() > 1) {
                transactions.addAll(parseReturnTable(tables.get(1), fileName));
            }
        }
        
        return transactions;
    }
    
    /**
     * 解析单表格数据
     * @param table 表格
     * @param fileName 文件名
     * @return 解析出的交易记录列表
     */
    private List<Transaction> parseSingleTable(Table table, String fileName) {
        List<Transaction> transactions = new ArrayList<>();
        
        List<List<RectangularTextContainer>> rows = table.getRows();
        if (rows.isEmpty()) {
            return transactions;
        }
        
        // 解析表头
        List<String> headers = extractHeaders(rows.get(0));
        Map<HeaderMappingService.StandardField, Integer> headerMapping = 
                headerMappingService.createHeaderMapping(headers);
        
        // 解析数据行
        for (int i = 1; i < rows.size(); i++) {
            List<RectangularTextContainer> row = rows.get(i);
            Transaction transaction = parseTableRow(row, headerMapping, fileName);
            
            if (transaction != null) {
                // 根据金额判断交易类型
                if (transaction.getTotalAmount() != null) {
                    if (transaction.getTotalAmount().compareTo(BigDecimal.ZERO) < 0) {
                        transaction.setTransactionType("RETURN");
                    } else {
                        transaction.setTransactionType("PURCHASE");
                    }
                }
                transactions.add(transaction);
            }
        }
        
        return transactions;
    }
    
    /**
     * 解析进货表格数据
     * @param table 表格
     * @param fileName 文件名
     * @return 解析出的交易记录列表
     */
    private List<Transaction> parsePurchaseTable(Table table, String fileName) {
        List<Transaction> transactions = new ArrayList<>();
        
        List<List<RectangularTextContainer>> rows = table.getRows();
        if (rows.isEmpty()) {
            return transactions;
        }
        
        // 解析表头
        List<String> headers = extractHeaders(rows.get(0));
        Map<HeaderMappingService.StandardField, Integer> headerMapping = 
                headerMappingService.createHeaderMapping(headers);
        
        // 解析数据行
        for (int i = 1; i < rows.size(); i++) {
            List<RectangularTextContainer> row = rows.get(i);
            Transaction transaction = parseTableRow(row, headerMapping, fileName);
            
            if (transaction != null) {
                transaction.setTransactionType("PURCHASE");
                transactions.add(transaction);
            }
        }
        
        return transactions;
    }
    
    /**
     * 解析退货表格数据
     * @param table 表格
     * @param fileName 文件名
     * @return 解析出的交易记录列表
     */
    private List<Transaction> parseReturnTable(Table table, String fileName) {
        List<Transaction> transactions = new ArrayList<>();
        
        List<List<RectangularTextContainer>> rows = table.getRows();
        if (rows.isEmpty()) {
            return transactions;
        }
        
        // 解析表头
        List<String> headers = extractHeaders(rows.get(0));
        Map<HeaderMappingService.StandardField, Integer> headerMapping = 
                headerMappingService.createHeaderMapping(headers);
        
        // 解析数据行
        for (int i = 1; i < rows.size(); i++) {
            List<RectangularTextContainer> row = rows.get(i);
            Transaction transaction = parseTableRow(row, headerMapping, fileName);
            
            if (transaction != null) {
                transaction.setTransactionType("RETURN");
                transactions.add(transaction);
            }
        }
        
        return transactions;
    }
    
    /**
     * 从表格行中提取表头
     * @param headerRow 表头行
     * @return 表头列表
     */
    private List<String> extractHeaders(List<RectangularTextContainer> headerRow) {
        List<String> headers = new ArrayList<>();
        for (RectangularTextContainer cell : headerRow) {
            headers.add(dataCleaningService.cleanString(cell.getText()));
        }
        return headers;
    }
    
    /**
     * 解析表格行数据
     * @param row 表格行
     * @param headerMapping 表头映射
     * @param fileName 文件名
     * @return 解析出的交易记录
     */
    private Transaction parseTableRow(List<RectangularTextContainer> row, 
                                     Map<HeaderMappingService.StandardField, Integer> headerMapping,
                                     String fileName) {
        Transaction transaction = new Transaction();
        transaction.setSourceFile(fileName);
        transaction.setCreatedAt(LocalDate.now());
        
        // 解析各列数据
        for (Map.Entry<HeaderMappingService.StandardField, Integer> entry : headerMapping.entrySet()) {
            HeaderMappingService.StandardField field = entry.getKey();
            int columnIndex = entry.getValue();
            
            if (columnIndex >= row.size()) {
                continue;
            }
            
            String cellValue = dataCleaningService.cleanString(row.get(columnIndex).getText());
            
            switch (field) {
                case DATE:
                    transaction.setTransactionDate(dataCleaningService.parseDate(cellValue));
                    break;
                case PRODUCT_CODE:
                    transaction.setProductCode(cellValue);
                    break;
                case PRODUCT_NAME:
                    transaction.setProductName(cellValue);
                    break;
                case QUANTITY:
                    transaction.setQuantity(dataCleaningService.parseQuantity(cellValue));
                    break;
                case UNIT_PRICE:
                    transaction.setUnitPrice(dataCleaningService.parseUnitPrice(cellValue));
                    break;
                case TOTAL_AMOUNT:
                    transaction.setTotalAmount(dataCleaningService.parseAmount(cellValue));
                    break;
                case REMARKS:
                    transaction.setRemarks(cellValue);
                    break;
            }
        }
        
        // 如果所有关键字段都为空，则跳过此行
        if (transaction.getProductCode() == null && 
            transaction.getProductName() == null && 
            transaction.getQuantity() == null) {
            return null;
        }
        
        return transaction;
    }
}