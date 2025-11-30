package com.example.buzzer.service;

import com.example.buzzer.model.ProductTransaction;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PdfParserService {

    // 日期格式器，支持多种格式
    private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss")
    );

    // 列头映射，支持扩展更新
    private static final List<String> DATE_HEADERS = Arrays.asList("日期", "交易日期", "发生日期");
    private static final List<String> STYLE_NUMBER_HEADERS = Arrays.asList("款号", "款式编号", "商品编号", "货号");
    private static final List<String> PRODUCT_NAME_HEADERS = Arrays.asList("名称", "商品名称", "品名");
    private static final List<String> QUANTITY_HEADERS = Arrays.asList("数量", "件数", "个数");
    private static final List<String> UNIT_PRICE_HEADERS = Arrays.asList("单价", "单件价格", " unit price ");
    private static final List<String> AMOUNT_HEADERS = Arrays.asList("金额", "总金额", "总价", "合计");
    private static final List<String> REMARKS_HEADERS = Arrays.asList("备注", "说明", "备注信息");

    // 正则表达式，用于提取数字
    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");

    public List<ProductTransaction> parsePdf(File pdfFile, boolean isTwoTables) throws IOException {
        List<ProductTransaction> transactions = new ArrayList<>();

        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            // 简单的表格解析逻辑，实际需要更复杂的算法
            // 这里假设PDF文本中的表格是按行和列组织的，每行数据用换行符分隔，列之间用制表符或空格分隔
            String[] lines = text.split("\\n");
            if (lines.length == 0) {
                return transactions;
            }

            // 找到表头所在行
            int headerRowIndex = findHeaderRowIndex(lines);
            if (headerRowIndex == -1) {
                throw new IllegalArgumentException("无法识别表格表头");
            }

            // 解析表头，确定各列的索引
            String[] headers = parseHeaders(lines[headerRowIndex]);
            int dateIndex = findColumnIndex(headers, DATE_HEADERS);
            int styleNumberIndex = findColumnIndex(headers, STYLE_NUMBER_HEADERS);
            int productNameIndex = findColumnIndex(headers, PRODUCT_NAME_HEADERS);
            int quantityIndex = findColumnIndex(headers, QUANTITY_HEADERS);
            int unitPriceIndex = findColumnIndex(headers, UNIT_PRICE_HEADERS);
            int amountIndex = findColumnIndex(headers, AMOUNT_HEADERS);
            int remarksIndex = findColumnIndex(headers, REMARKS_HEADERS);

            // 验证必要的列是否存在
            if (dateIndex == -1 || styleNumberIndex == -1 || productNameIndex == -1 ||
                    quantityIndex == -1 || unitPriceIndex == -1 || amountIndex == -1) {
                throw new IllegalArgumentException("表格缺少必要的列");
            }

            // 解析数据行
            for (int i = headerRowIndex + 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) {
                    continue;
                }

                String[] cells = parseCells(line);
                if (cells.length <= Math.max(Math.max(dateIndex, styleNumberIndex), Math.max(productNameIndex, quantityIndex)) ||
                        cells.length <= Math.max(Math.max(unitPriceIndex, amountIndex), remarksIndex)) {
                    continue; // 数据不完整，跳过
                }

                try {
                    ProductTransaction transaction = new ProductTransaction();

                    // 解析日期
                    String dateStr = cells[dateIndex].trim();
                    LocalDate date = parseDate(dateStr);
                    transaction.setDate(date);

                    // 解析款号
                    String styleNumber = cells[styleNumberIndex].trim();
                    transaction.setStyleNumber(styleNumber);

                    // 解析名称
                    String productName = cells[productNameIndex].trim();
                    transaction.setProductName(productName);

                    // 解析数量
                    String quantityStr = cells[quantityIndex].trim();
                    Integer quantity = parseQuantity(quantityStr);
                    transaction.setQuantity(quantity);

                    // 解析单价
                    String unitPriceStr = cells[unitPriceIndex].trim();
                    BigDecimal unitPrice = parseAmount(unitPriceStr);
                    transaction.setUnitPrice(unitPrice);

                    // 解析金额
                    String amountStr = cells[amountIndex].trim();
                    BigDecimal amount = parseAmount(amountStr);
                    transaction.setAmount(amount);

                    // 解析备注
                    if (remarksIndex != -1 && cells.length > remarksIndex) {
                        String remarks = cells[remarksIndex].trim();
                        transaction.setRemarks(remarks);
                    }

                    // 判断是进货还是退货
                    boolean isReturn;
                    if (isTwoTables) {
                        // 如果是两个表格，第一个表格为进货，第二个表格为退货
                        // 这里需要更复杂的逻辑来判断当前行属于哪个表格
                        // 暂时简单处理，假设前半部分是进货，后半部分是退货
                        isReturn = i > (headerRowIndex + 1 + (lines.length - headerRowIndex - 1) / 2);
                    } else {
                        // 如果是一个表格，根据金额正负判断
                        isReturn = amount.compareTo(BigDecimal.ZERO) < 0;
                    }
                    transaction.setIsReturn(isReturn);

                    transactions.add(transaction);
                } catch (Exception e) {
                    // 跳过解析失败的数据行
                    continue;
                }
            }
        }

        return transactions;
    }

    private int findHeaderRowIndex(String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }

            String[] headers = parseHeaders(line);
            int matchCount = 0;
            for (String header : headers) {
                if (DATE_HEADERS.contains(header) || STYLE_NUMBER_HEADERS.contains(header) ||
                        PRODUCT_NAME_HEADERS.contains(header) || QUANTITY_HEADERS.contains(header) ||
                        UNIT_PRICE_HEADERS.contains(header) || AMOUNT_HEADERS.contains(header)) {
                    matchCount++;
                }
            }

            if (matchCount >= 3) { // 至少匹配3个必要列
                return i;
            }
        }
        return -1;
    }

    private String[] parseHeaders(String line) {
        // 简单的表头解析，实际需要更复杂的算法
        // 假设表头列之间用多个空格或制表符分隔
        return line.split("\\s{2,}|\\t");
    }

    private String[] parseCells(String line) {
        // 简单的单元格解析，实际需要更复杂的算法
        // 假设单元格之间用多个空格或制表符分隔
        return line.split("\\s{2,}|\\t");
    }

    private int findColumnIndex(String[] headers, List<String> possibleHeaders) {
        for (int i = 0; i < headers.length; i++) {
            String header = headers[i].trim();
            if (possibleHeaders.contains(header)) {
                return i;
            }
        }
        return -1;
    }

    private LocalDate parseDate(String dateStr) {
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (Exception e) {
                // 尝试下一个格式
            }
        }
        throw new IllegalArgumentException("无法解析日期格式: " + dateStr);
    }

    private Integer parseQuantity(String quantityStr) {
        // 提取数字部分
        Matcher matcher = NUMBER_PATTERN.matcher(quantityStr);
        if (matcher.find()) {
            String numberStr = matcher.group();
            return Integer.parseInt(numberStr);
        }
        throw new IllegalArgumentException("无法解析数量: " + quantityStr);
    }

    private BigDecimal parseAmount(String amountStr) {
        // 提取数字部分
        Matcher matcher = NUMBER_PATTERN.matcher(amountStr);
        if (matcher.find()) {
            String numberStr = matcher.group();
            return new BigDecimal(numberStr);
        }
        throw new IllegalArgumentException("无法解析金额: " + amountStr);
    }
}