package com.example.buzzer.service;

import com.example.buzzer.config.HeaderMappingConfig;
import com.example.buzzer.entity.ProductData;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PdfParserService {

    @Autowired
    private HeaderMappingConfig headerMappingConfig;

    // 解析PDF文件并提取数据
    public List<ProductData> parsePdfFile(MultipartFile file) throws IOException {
        List<ProductData> productDataList = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             PDDocument document = PDDocument.load(inputStream)) {

            for (int pageNum = 0; pageNum < document.getNumberOfPages(); pageNum++) {
                // 使用自定义的文本提取器获取按位置组织的文本
                PositionAwareTextStripper stripper = new PositionAwareTextStripper();
                stripper.setStartPage(pageNum + 1);
                stripper.setEndPage(pageNum + 1);
                stripper.getText(document);

                // 获取提取的文本行
                List<List<PositionedText>> textRows = stripper.getTextRows();
                
                // 尝试识别表格并处理
                List<TableData> tables = identifyTables(textRows);
                
                // 处理识别到的表格
                for (int tableIndex = 0; tableIndex < tables.size(); tableIndex++) {
                    TableData table = tables.get(tableIndex);
                    List<List<String>> rows = table.getRows();

                    if (rows.isEmpty()) continue;

                    // 第一行为表头
                    List<String> headers = rows.get(0);
                    Map<Integer, String> columnFieldMap = mapColumnsToFields(headers);

                    // 处理数据行
                    for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {
                        List<String> row = rows.get(rowIndex);
                        ProductData productData = createProductData(row, columnFieldMap, tables.size() > 1, tableIndex == 1);
                        if (productData != null) {
                            productDataList.add(productData);
                        }
                    }
                }
            }
        }

        return productDataList;
    }

    // 识别表格
    private List<TableData> identifyTables(List<List<PositionedText>> textRows) {
        List<TableData> tables = new ArrayList<>();
        if (textRows.isEmpty()) return tables;

        // 简化的表格识别：尝试根据列对齐和内容模式识别表格
        // 创建一个表格（实际应用中可能需要更复杂的逻辑来识别多个表格）
        TableData table = new TableData();
        tables.add(table);

        // 尝试确定列数和列位置
        List<Float> columnPositions = determineColumnPositions(textRows);
        if (columnPositions.isEmpty()) {
            // 如果无法确定列位置，使用简单的空格分隔
            for (List<PositionedText> row : textRows) {
                List<String> rowData = new ArrayList<>();
                StringBuilder cell = new StringBuilder();
                for (PositionedText text : row) {
                    String content = text.getText();
                    // 简单的启发式：如果包含关键字，可能是新列的开始
                    if (content.matches(".*[日期款号名称数量单价金额备注].*")) {
                        if (cell.length() > 0) {
                            rowData.add(cell.toString());
                            cell = new StringBuilder();
                        }
                    }
                    cell.append(content).append(" ");
                }
                if (cell.length() > 0) {
                    rowData.add(cell.toString().trim());
                }
                if (!rowData.isEmpty()) {
                    table.addRow(rowData);
                }
            }
        } else {
            // 根据列位置分割数据
            for (List<PositionedText> row : textRows) {
                List<String> rowData = new ArrayList<>(Collections.nCopies(columnPositions.size(), ""));
                
                for (PositionedText text : row) {
                    int columnIndex = findColumnIndex(text.getX(), columnPositions);
                    if (columnIndex >= 0 && columnIndex < rowData.size()) {
                        if (!rowData.get(columnIndex).isEmpty()) {
                            rowData.set(columnIndex, rowData.get(columnIndex) + " " + text.getText());
                        } else {
                            rowData.set(columnIndex, text.getText());
                        }
                    }
                }
                
                // 移除空行
                boolean hasData = false;
                for (int i = 0; i < rowData.size(); i++) {
                    rowData.set(i, rowData.get(i).trim());
                    if (!rowData.get(i).isEmpty()) {
                        hasData = true;
                    }
                }
                
                if (hasData) {
                    table.addRow(rowData);
                }
            }
        }

        // 尝试分割可能的多个表格（基于空行或特定模式）
        // 这里使用简化逻辑，实际应用中可能需要更复杂的处理
        if (table.getRows().size() > 20) { // 如果表格太大，可能包含多个表格
            List<List<String>> rows = table.getRows();
            TableData table1 = new TableData();
            TableData table2 = new TableData();
            tables.clear();
            tables.add(table1);
            tables.add(table2);
            
            // 简单地将表格分成两部分
            int midIndex = rows.size() / 2;
            for (int i = 0; i < rows.size(); i++) {
                if (i < midIndex) {
                    table1.addRow(rows.get(i));
                } else {
                    table2.addRow(rows.get(i));
                }
            }
            
            // 确保每个表格都有表头
            if (table2.getRows().size() > 0 && isHeaderRow(table2.getRows().get(0))) {
                // 看起来table2有自己的表头，可能是第二个表格
            } else if (table1.getRows().size() > 0) {
                // 复制表头到table2
                table2.addRowAt(0, new ArrayList<>(table1.getRows().get(0)));
            }
        }

        return tables;
    }

    // 确定列位置
    private List<Float> determineColumnPositions(List<List<PositionedText>> textRows) {
        Set<Float> positions = new HashSet<>();
        
        // 收集所有文本元素的X坐标
        for (List<PositionedText> row : textRows) {
            for (PositionedText text : row) {
                positions.add(text.getX());
            }
        }
        
        // 转换为列表并排序
        List<Float> sortedPositions = new ArrayList<>(positions);
        Collections.sort(sortedPositions);
        
        // 过滤掉过于接近的位置（可能是同一列的不同行）
        List<Float> filteredPositions = new ArrayList<>();
        float threshold = 10.0f; // 10像素的阈值
        
        for (float pos : sortedPositions) {
            boolean isNewColumn = true;
            for (float existingPos : filteredPositions) {
                if (Math.abs(pos - existingPos) < threshold) {
                    isNewColumn = false;
                    break;
                }
            }
            if (isNewColumn) {
                filteredPositions.add(pos);
            }
        }
        
        return filteredPositions;
    }

    // 查找文本对应的列索引
    private int findColumnIndex(float x, List<Float> columnPositions) {
        for (int i = 0; i < columnPositions.size(); i++) {
            if (i == columnPositions.size() - 1) {
                return i;
            }
            if (x >= columnPositions.get(i) && x < columnPositions.get(i + 1)) {
                return i;
            }
        }
        return -1;
    }

    // 判断是否为表头行
    private boolean isHeaderRow(List<String> row) {
        int headerKeywordCount = 0;
        List<String> headerKeywords = Arrays.asList("日期", "款号", "名称", "数量", "单价", "金额", "备注");
        
        for (String cell : row) {
            for (String keyword : headerKeywords) {
                if (cell.contains(keyword)) {
                    headerKeywordCount++;
                    break;
                }
            }
        }
        
        return headerKeywordCount >= 3; // 至少包含3个关键字则认为是表头
    }

    // 表格数据类
    private static class TableData {
        private List<List<String>> rows = new ArrayList<>();
        
        public void addRow(List<String> row) {
            rows.add(new ArrayList<>(row));
        }
        
        public void addRowAt(int index, List<String> row) {
            rows.add(index, new ArrayList<>(row));
        }
        
        public List<List<String>> getRows() {
            return rows;
        }
    }

    // 位置感知的文本
    private static class PositionedText {
        private float x;
        private float y;
        private String text;
        
        public PositionedText(float x, float y, String text) {
            this.x = x;
            this.y = y;
            this.text = text;
        }
        
        public float getX() {
            return x;
        }
        
        public float getY() {
            return y;
        }
        
        public String getText() {
            return text;
        }
    }

    // 位置感知的文本提取器
    private static class PositionAwareTextStripper extends PDFTextStripper {
        private List<List<PositionedText>> textRows = new ArrayList<>();
        private Map<Float, List<PositionedText>> yToTextMap = new HashMap<>();
        private float currentPageHeight;
        
        public PositionAwareTextStripper() throws IOException {
            super();
        }
        
        @Override
        protected void startPage(PDPage page) throws IOException {
            super.startPage(page);
            currentPageHeight = page.getMediaBox().getHeight();
        }
        
        @Override
        protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
            // 收集文本位置信息
            for (TextPosition textPos : textPositions) {
                if (!textPos.getUnicode().trim().isEmpty()) {
                    // 计算相对于页面底部的Y坐标（使Y坐标从上到下递增）
                    float y = currentPageHeight - textPos.getY();
                    float x = textPos.getX();
                    
                    // 按照Y坐标分组，接近的Y坐标视为同一行
                    float roundedY = Math.round(y * 100) / 100f; // 四舍五入到小数点后两位
                    yToTextMap.computeIfAbsent(roundedY, k -> new ArrayList<>())
                              .add(new PositionedText(x, y, textPos.getUnicode()));
                }
            }
        }
        
        @Override
        public String getText(PDDocument document) throws IOException {
            super.getText(document);
            
            // 将收集的文本按行组织
            List<Float> sortedYs = new ArrayList<>(yToTextMap.keySet());
            Collections.sort(sortedYs);
            
            for (float y : sortedYs) {
                List<PositionedText> texts = yToTextMap.get(y);
                // 按X坐标排序
                texts.sort(Comparator.comparing(PositionedText::getX));
                textRows.add(texts);
            }
            
            return super.getText(document); // 返回原始文本（但我们主要使用textRows）
        }
        
        public List<List<PositionedText>> getTextRows() {
            return textRows;
        }
    }

    // 映射列到字段名
    private Map<Integer, String> mapColumnsToFields(List<String> headers) {
        Map<Integer, String> columnFieldMap = new HashMap<>();

        for (int i = 0; i < headers.size(); i++) {
            String headerText = headers.get(i);
            String fieldName = headerMappingConfig.getFieldNameByHeaderText(headerText);
            if (fieldName != null) {
                columnFieldMap.put(i, fieldName);
            }
        }

        return columnFieldMap;
    }

    // 创建ProductData对象
    private ProductData createProductData(List<String> row, Map<Integer, String> columnFieldMap, 
                                         boolean multipleTables, boolean isReturnTable) {
        ProductData data = new ProductData();
        boolean hasData = false;

        for (Map.Entry<Integer, String> entry : columnFieldMap.entrySet()) {
            int columnIndex = entry.getKey();
            String fieldName = entry.getValue();

            if (columnIndex < row.size()) {
                String cellValue = row.get(columnIndex);
                if (cellValue != null && !cellValue.trim().isEmpty()) {
                    cellValue = cellValue.trim();
                    hasData = true;

                    switch (fieldName) {
                        case "date":
                            data.setDate(parseDate(cellValue));
                            break;
                        case "productCode":
                            data.setProductCode(cellValue);
                            break;
                        case "productName":
                            data.setProductName(cellValue);
                            break;
                        case "quantity":
                            data.setQuantity(parseQuantity(cellValue));
                            break;
                        case "unitPrice":
                            data.setUnitPrice(parseAmount(cellValue));
                            break;
                        case "amount":
                            BigDecimal amount = parseAmount(cellValue);
                            data.setAmount(amount);
                            // 如果是单个表格，根据金额正负判断是否为退货
                            if (!multipleTables && amount != null && amount.compareTo(BigDecimal.ZERO) < 0) {
                                data.setReturn(true);
                            }
                            break;
                        case "remark":
                            data.setRemark(cellValue);
                            break;
                    }
                }
            }
        }

        // 设置退货标志
        if (multipleTables) {
            data.setReturn(isReturnTable);
        } else if (data.getReturn() == null) {
            data.setReturn(false);
        }

        return hasData ? data : null;
    }

    // 解析日期
    private LocalDate parseDate(String dateStr) {
        List<DateTimeFormatter> formatters = Arrays.asList(
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd"),
                DateTimeFormatter.ofPattern("yyyy年MM月dd日"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss")
        );

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {
                // 尝试下一个格式
            }
        }

        // 尝试从字符串中提取日期部分
        Pattern pattern = Pattern.compile("\\d{4}[-/]\\d{1,2}[-/]\\d{1,2}");
        Matcher matcher = pattern.matcher(dateStr);
        if (matcher.find()) {
            String extractedDate = matcher.group();
            for (DateTimeFormatter formatter : formatters) {
                try {
                    return LocalDate.parse(extractedDate, formatter);
                } catch (DateTimeParseException e) {
                    // 尝试下一个格式
                }
            }
        }

        return null;
    }

    // 解析数量
    private Integer parseQuantity(String quantityStr) {
        // 移除非数字字符，保留负号
        String numStr = quantityStr.replaceAll("[^\\d-]", "");
        if (numStr.isEmpty() || numStr.equals("-")) {
            return null;
        }
        try {
            return Integer.parseInt(numStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // 解析金额
    private BigDecimal parseAmount(String amountStr) {
        // 移除非数字和小数点、负号的字符
        String numStr = amountStr.replaceAll("[^\\d.-]", "");
        if (numStr.isEmpty() || numStr.equals(".") || numStr.equals("-.")) {
            return null;
        }
        try {
            return new BigDecimal(numStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}