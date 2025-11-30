package com.example.buzzer.config;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class HeaderMappingConfig {
    // 表头映射配置
    private final Map<String, List<String>> headerMappings = new HashMap<>();

    public HeaderMappingConfig() {
        // 初始化默认映射
        headerMappings.put("date", Arrays.asList("日期", "日期时间", "交易日期", "发生日期"));
        headerMappings.put("productCode", Arrays.asList("款号", "款式编号", "产品编号", "货品编号", "货号"));
        headerMappings.put("productName", Arrays.asList("名称", "产品名称", "货品名称", "商品名称"));
        headerMappings.put("quantity", Arrays.asList("数量", "件数", "数量"));
        headerMappings.put("unitPrice", Arrays.asList("单价", "单件价格", "单位价格", "价格"));
        headerMappings.put("amount", Arrays.asList("金额", "总金额", "总价", "合计"));
        headerMappings.put("remark", Arrays.asList("备注", "说明", "备注信息"));
    }

    // 根据表头文本获取字段名
    public String getFieldNameByHeaderText(String headerText) {
        if (headerText == null || headerText.trim().isEmpty()) {
            return null;
        }
        
        String trimmedText = headerText.trim();
        
        for (Map.Entry<String, List<String>> entry : headerMappings.entrySet()) {
            for (String alias : entry.getValue()) {
                if (trimmedText.contains(alias) || alias.contains(trimmedText)) {
                    return entry.getKey();
                }
            }
        }
        
        return null;
    }

    // 添加新的表头映射
    public void addHeaderMapping(String fieldName, String headerText) {
        headerMappings.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(headerText);
    }

    // 更新表头映射
    public void updateHeaderMapping(String fieldName, List<String> headerTexts) {
        headerMappings.put(fieldName, headerTexts);
    }

    // 获取所有表头映射
    public Map<String, List<String>> getAllHeaderMappings() {
        return new HashMap<>(headerMappings);
    }
}