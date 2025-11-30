package com.example.buzzer.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DataCleaningService {
    
    // 日期格式模式
    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{4})[-/年](\\d{1,2})[-/月](\\d{1,2})[日]?");
    private static final Pattern DATETIME_PATTERN = Pattern.compile("(\\d{4})[-/年](\\d{1,2})[-/月](\\d{1,2})[日]?[\\sT](\\d{1,2})[:：时](\\d{1,2})[:：分]?(\\d{1,2})?[秒]?");
    
    // 数量模式（数字+单位）
    private static final Pattern QUANTITY_PATTERN = Pattern.compile("(-?\\d+(?:\\.\\d+)?)\\s*([件个台箱包只把支瓶套]?)");
    
    // 金额模式（数字+单位）
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("(-?\\d+(?:\\.\\d+)?)\\s*([元万千百十]?)");
    
    /**
     * 清理字符串，去除前后空格和特殊字符
     * @param input 原始字符串
     * @return 清理后的字符串
     */
    public String cleanString(String input) {
        if (input == null) {
            return "";
        }
        
        // 去除前后空格
        String cleaned = input.trim();
        
        // 去除中间多余的空格
        cleaned = cleaned.replaceAll("\\s+", " ");
        
        // 去除常见的不可见字符
        cleaned = cleaned.replaceAll("[\\u00A0\\u2007\\u202F]", " ");
        
        return cleaned;
    }
    
    /**
     * 解析日期字符串
     * @param dateStr 日期字符串
     * @return 解析后的日期
     */
    public LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        
        String cleaned = cleanString(dateStr);
        
        // 尝试匹配年月日格式
        Matcher dateMatcher = DATE_PATTERN.matcher(cleaned);
        if (dateMatcher.find()) {
            try {
                int year = Integer.parseInt(dateMatcher.group(1));
                int month = Integer.parseInt(dateMatcher.group(2));
                int day = Integer.parseInt(dateMatcher.group(3));
                
                // 验证日期有效性
                if (month >= 1 && month <= 12 && day >= 1 && day <= 31) {
                    return LocalDate.of(year, month, day);
                }
            } catch (NumberFormatException e) {
                // 忽略解析错误，继续尝试其他格式
            }
        }
        
        // 尝试匹配年月日时分秒格式
        Matcher datetimeMatcher = DATETIME_PATTERN.matcher(cleaned);
        if (datetimeMatcher.find()) {
            try {
                int year = Integer.parseInt(datetimeMatcher.group(1));
                int month = Integer.parseInt(datetimeMatcher.group(2));
                int day = Integer.parseInt(datetimeMatcher.group(3));
                
                // 验证日期有效性
                if (month >= 1 && month <= 12 && day >= 1 && day <= 31) {
                    return LocalDate.of(year, month, day);
                }
            } catch (NumberFormatException e) {
                // 忽略解析错误
            }
        }
        
        // 尝试使用标准日期格式解析
        try {
            return LocalDate.parse(cleaned, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e1) {
            try {
                return LocalDate.parse(cleaned, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            } catch (DateTimeParseException e2) {
                try {
                    return LocalDate.parse(cleaned, DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
                } catch (DateTimeParseException e3) {
                    // 所有格式都失败，返回null
                    return null;
                }
            }
        }
    }
    
    /**
     * 解析数量字符串，提取数字部分
     * @param quantityStr 数量字符串
     * @return 解析后的数量
     */
    public Integer parseQuantity(String quantityStr) {
        if (quantityStr == null || quantityStr.trim().isEmpty()) {
            return null;
        }
        
        String cleaned = cleanString(quantityStr);
        
        // 尝试匹配数量模式（数字+单位）
        Matcher matcher = QUANTITY_PATTERN.matcher(cleaned);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                // 忽略解析错误
            }
        }
        
        // 如果没有匹配到模式，尝试直接解析数字
        try {
            // 移除非数字字符（保留负号和小数点）
            String numericStr = cleaned.replaceAll("[^-\\d.]", "");
            if (!numericStr.isEmpty()) {
                BigDecimal decimal = new BigDecimal(numericStr);
                return decimal.intValue();
            }
        } catch (NumberFormatException e) {
            // 忽略解析错误
        }
        
        return null;
    }
    
    /**
     * 解析金额字符串，提取数字部分
     * @param amountStr 金额字符串
     * @return 解析后的金额
     */
    public BigDecimal parseAmount(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            return null;
        }
        
        String cleaned = cleanString(amountStr);
        
        // 尝试匹配金额模式（数字+单位）
        Matcher matcher = AMOUNT_PATTERN.matcher(cleaned);
        if (matcher.find()) {
            try {
                return new BigDecimal(matcher.group(1));
            } catch (NumberFormatException e) {
                // 忽略解析错误
            }
        }
        
        // 如果没有匹配到模式，尝试直接解析数字
        try {
            // 移除非数字字符（保留负号和小数点）
            String numericStr = cleaned.replaceAll("[^-\\d.]", "");
            if (!numericStr.isEmpty()) {
                return new BigDecimal(numericStr);
            }
        } catch (NumberFormatException e) {
            // 忽略解析错误
        }
        
        return null;
    }
    
    /**
     * 解析单价字符串，提取数字部分
     * @param priceStr 单价字符串
     * @return 解析后的单价
     */
    public BigDecimal parseUnitPrice(String priceStr) {
        return parseAmount(priceStr); // 单价和金额的解析逻辑相同
    }
}