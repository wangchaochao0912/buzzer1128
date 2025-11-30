package com.example.buzzer.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class HeaderMappingService {
    
    // 定义标准字段名称
    public enum StandardField {
        DATE("日期"),
        PRODUCT_CODE("款号"),
        PRODUCT_NAME("名称"),
        QUANTITY("数量"),
        UNIT_PRICE("单价"),
        TOTAL_AMOUNT("金额"),
        REMARKS("备注");
        
        private final String displayName;
        
        StandardField(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // 各种可能的表头变体映射
    private final Map<StandardField, List<String>> headerVariations = new HashMap<>();
    
    public HeaderMappingService() {
        // 日期的可能变体
        headerVariations.put(StandardField.DATE, Arrays.asList(
                "日期", "时间", "交易日期", "记录日期", "单据日期", "日"
        ));
        
        // 款号的可能变体
        headerVariations.put(StandardField.PRODUCT_CODE, Arrays.asList(
                "款号", "款式编号", "商品编号", "产品编号", "货号", "SKU", "编号", "代码"
        ));
        
        // 名称的可能变体
        headerVariations.put(StandardField.PRODUCT_NAME, Arrays.asList(
                "名称", "商品名称", "产品名称", "品名", "款式名称", "描述"
        ));
        
        // 数量的可能变体
        headerVariations.put(StandardField.QUANTITY, Arrays.asList(
                "数量", "件数", "个数", "库存", "数量(件)", "数量(个)", "数量/件"
        ));
        
        // 单价的可能变体
        headerVariations.put(StandardField.UNIT_PRICE, Arrays.asList(
                "单价", "价格", "单件价格", "单个价格", "单价(元)", "价格(元)"
        ));
        
        // 金额的可能变体
        headerVariations.put(StandardField.TOTAL_AMOUNT, Arrays.asList(
                "金额", "总金额", "总价", "总额", "合计", "小计", "金额(元)", "总价(元)"
        ));
        
        // 备注的可能变体
        headerVariations.put(StandardField.REMARKS, Arrays.asList(
                "备注", "说明", "描述", "注释", "备注信息", "其他"
        ));
    }
    
    /**
     * 根据实际表头名称映射到标准字段
     * @param actualHeader 实际表头名称
     * @return 标准字段，如果没有匹配则返回null
     */
    public StandardField mapToStandardField(String actualHeader) {
        if (actualHeader == null || actualHeader.trim().isEmpty()) {
            return null;
        }
        
        String cleanedHeader = actualHeader.trim();
        
        // 遍历所有标准字段，查找匹配
        for (Map.Entry<StandardField, List<String>> entry : headerVariations.entrySet()) {
            StandardField standardField = entry.getKey();
            List<String> variations = entry.getValue();
            
            // 检查是否完全匹配任何一个变体
            for (String variation : variations) {
                if (cleanedHeader.equals(variation)) {
                    return standardField;
                }
            }
            
            // 检查是否包含任何一个变体（模糊匹配）
            for (String variation : variations) {
                if (cleanedHeader.contains(variation) || variation.contains(cleanedHeader)) {
                    return standardField;
                }
            }
        }
        
        return null;
    }
    
    /**
     * 从表头列表中创建字段索引映射
     * @param headers 表头列表
     * @return 标准字段到列索引的映射
     */
    public Map<StandardField, Integer> createHeaderMapping(List<String> headers) {
        Map<StandardField, Integer> mapping = new HashMap<>();
        
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i);
            StandardField standardField = mapToStandardField(header);
            
            if (standardField != null) {
                mapping.put(standardField, i);
            }
        }
        
        return mapping;
    }
    
    /**
     * 添加新的表头变体
     * @param field 标准字段
     * @param variations 新的变体列表
     */
    public void addHeaderVariations(StandardField field, List<String> variations) {
        if (headerVariations.containsKey(field)) {
            headerVariations.get(field).addAll(variations);
        } else {
            headerVariations.put(field, variations);
        }
    }
}