package com.example.buzzer.service;

import com.example.buzzer.entity.ProductData;
import com.example.buzzer.repository.ProductDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
public class ProductDataService {

    @Autowired
    private PdfParserService pdfParserService;

    @Autowired
    private ProductDataRepository productDataRepository;

    // 上传并处理PDF文件
    @Transactional
    public List<ProductData> uploadAndProcessPdf(MultipartFile file) throws IOException {
        // 验证文件类型
        if (!file.getContentType().equals("application/pdf")) {
            throw new IllegalArgumentException("Only PDF files are supported");
        }

        // 解析PDF文件
        List<ProductData> productDataList = pdfParserService.parsePdfFile(file);

        // 保存到数据库
        return productDataRepository.saveAll(productDataList);
    }

    // 获取所有数据
    public List<ProductData> getAllProductData() {
        return productDataRepository.findAll();
    }

    // 根据是否退货获取数据
    public List<ProductData> getProductDataByReturnStatus(Boolean isReturn) {
        return productDataRepository.findByIsReturn(isReturn);
    }

    // 根据日期范围查询
    public List<ProductData> getProductDataByDateRange(LocalDate startDate, LocalDate endDate) {
        return productDataRepository.findByDateBetween(startDate, endDate);
    }

    // 根据款号查询
    public List<ProductData> getProductDataByProductCode(String productCode) {
        return productDataRepository.findByProductCode(productCode);
    }
}