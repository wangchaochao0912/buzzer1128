package com.example.buzzer.service;

import com.example.buzzer.entity.Inventory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PdfParserService {

    // 支持的日期格式
    private static final List<SimpleDateFormat> DATE_FORMATS;
    static {
        DATE_FORMATS = new ArrayList<>();
        DATE_FORMATS.add(new SimpleDateFormat("yyyy-MM-dd"));
        DATE_FORMATS.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

    // 表头映射：支持多种表头名称
    private static final List<String> DATE_HEADERS;
    private static final List<String> STYLE_NUMBER_HEADERS;
    private static final List<String> NAME_HEADERS;
    private static final List<String> QUANTITY_HEADERS;
    private static final List<String> UNIT_PRICE_HEADERS;
    private static final List<String> AMOUNT_HEADERS;
    private static final List<String> REMARK_HEADERS;

    static {
        DATE_HEADERS = new ArrayList<>();
        DATE_HEADERS.add("日期");
        DATE_HEADERS.add("交易日期");
        DATE_HEADERS.add("发生日期");

        STYLE_NUMBER_HEADERS = new ArrayList<>();
        STYLE_NUMBER_HEADERS.add("款号");
        STYLE_NUMBER_HEADERS.add("款式编号");
        STYLE_NUMBER_HEADERS.add("商品编号");

        NAME_HEADERS = new ArrayList<>();
        NAME_HEADERS.add("名称");
        NAME_HEADERS.add("商品名称");
        NAME_HEADERS.add("货品名称");

        QUANTITY_HEADERS = new ArrayList<>();
        QUANTITY_HEADERS.add("数量");
        QUANTITY_HEADERS.add("件数");
        QUANTITY_HEADERS.add("个数");

        UNIT_PRICE_HEADERS = new ArrayList<>();
        UNIT_PRICE_HEADERS.add("单价");
        UNIT_PRICE_HEADERS.add("单件价格");
        UNIT_PRICE_HEADERS.add("单位价格");

        AMOUNT_HEADERS = new ArrayList<>();
        AMOUNT_HEADERS.add("金额");
        AMOUNT_HEADERS.add("总金额");
        AMOUNT_HEADERS.add("总价");

        REMARK_HEADERS = new ArrayList<>();
        REMARK_HEADERS.add("备注");
        REMARK_HEADERS.add("说明");
        REMARK_HEADERS.add("备注信息");
    }

    /**
     * 解析PDF文件并提取库存数据
     */
    public List<Inventory> parsePdf(InputStream inputStream) throws Exception {
        List<Inventory> inventoryList = new ArrayList<>();

        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            // 这里简化处理，实际应该根据PDF结构和表格位置进行更复杂的解析
            // 目前假设PDF文本已经按行组织，并且包含表格数据
            String[] lines = text.split("\\n");

            // 找到所有表头所在的行
            List<Integer> headerRowIndices = findAllHeaderRows(lines);
            if (headerRowIndices.isEmpty()) {
                throw new Exception("未找到表格表头");
            }

            // 解析每个表格
            for (int headerIndex = 0; headerIndex < headerRowIndices.size(); headerIndex++) {
                int headerRowIndex = headerRowIndices.get(headerIndex);
                int nextHeaderIndex = headerIndex < headerRowIndices.size() - 1 ? 
                    headerRowIndices.get(headerIndex + 1) : lines.length;

                // 解析表头，确定各字段的列索引
                String[] headers = lines[headerRowIndex].split("\\s+");
                int dateIndex = findColumnIndex(headers, DATE_HEADERS);
                int styleNumberIndex = findColumnIndex(headers, STYLE_NUMBER_HEADERS);
                int nameIndex = findColumnIndex(headers, NAME_HEADERS);
                int quantityIndex = findColumnIndex(headers, QUANTITY_HEADERS);
                int unitPriceIndex = findColumnIndex(headers, UNIT_PRICE_HEADERS);
                int amountIndex = findColumnIndex(headers, AMOUNT_HEADERS);
                int remarkIndex = findColumnIndex(headers, REMARK_HEADERS);

                // 用于处理单元格合并的前一行数据
                Inventory previousInventory = null;

                // 解析数据行
                for (int i = headerRowIndex + 1; i < nextHeaderIndex; i++) {
                    String line = lines[i].trim();
                    if (line.isEmpty()) {
                        continue;
                    }

                    String[] data = line.split("\\s+");
                    Inventory inventory = new Inventory();

                    // 解析日期
                    if (dateIndex != -1) {
                        // 日期列是最前面的列，所以不需要考虑前面列的合并
                        if (dateIndex < data.length) {
                            String dateValue = data[dateIndex].trim();
                            if (!dateValue.isEmpty()) {
                                // 有值，直接解析
                                inventory.setDate(parseDate(dateValue));
                            } else {
                                // 为空，检查是否是当前列有单元格合并
                                if (previousInventory != null && previousInventory.getDate() != null) {
                                    inventory.setDate(previousInventory.getDate());
                                } else {
                                    // 不是合并单元格，不继承
                                    inventory.setDate(null);
                                }
                            }
                        } else {
                            // 索引超出范围，可能是当前列有单元格合并
                            if (previousInventory != null && previousInventory.getDate() != null) {
                                inventory.setDate(previousInventory.getDate());
                            } else {
                                // 前一行也没有值，设置为null
                                inventory.setDate(null);
                            }
                        }
                    }

                    // 解析款号
                    if (styleNumberIndex != -1) {
                        // 款号列的偏移量只受其前面列（日期列）合并的影响
                        int adjustedIndex = styleNumberIndex;
                        // 如果日期列有合并（数据中没有日期值），则款号列的索引需要减1
                        if (dateIndex != -1 && (dateIndex >= data.length || (dateIndex < data.length && data[dateIndex].trim().isEmpty()))) {
                            adjustedIndex = Math.max(0, adjustedIndex - 1);
                        }
                        
                        if (adjustedIndex >= 0 && adjustedIndex < data.length) {
                            String styleNumberValue = data[adjustedIndex].trim();
                            if (!styleNumberValue.isEmpty()) {
                                // 有值，直接解析
                                inventory.setStyleNumber(cleanString(styleNumberValue));
                            } else {
                                // 为空，检查是否是当前列有单元格合并
                                if (previousInventory != null && previousInventory.getStyleNumber() != null) {
                                    inventory.setStyleNumber(previousInventory.getStyleNumber());
                                } else {
                                    // 不是合并单元格，不继承
                                    inventory.setStyleNumber(null);
                                }
                            }
                        } else {
                            // 调整后的索引超出范围，可能是当前列有单元格合并
                            if (previousInventory != null && previousInventory.getStyleNumber() != null) {
                                inventory.setStyleNumber(previousInventory.getStyleNumber());
                            } else {
                                // 前一行也没有值，设置为null
                                inventory.setStyleNumber(null);
                            }
                        }
                    }

                    // 解析名称
                    if (nameIndex != -1) {
                        // 名称列的偏移量受其前面列（日期列、款号列）合并的影响
                        int adjustedIndex = nameIndex;
                        int offset = 0;
                        
                        // 检查日期列是否有合并
                        if (dateIndex != -1 && (dateIndex >= data.length || (dateIndex < data.length && data[dateIndex].trim().isEmpty()))) {
                            offset++;
                        }
                        
                        // 检查款号列是否有合并
                        if (styleNumberIndex != -1) {
                            int styleAdjustedIndex = styleNumberIndex - (offset > 0 ? 1 : 0);
                            if (styleAdjustedIndex >= 0 && styleAdjustedIndex < data.length) {
                                if (data[styleAdjustedIndex].trim().isEmpty()) {
                                    offset++;
                                }
                            } else {
                                offset++;
                            }
                        }
                        
                        adjustedIndex = Math.max(0, adjustedIndex - offset);
                        
                        if (adjustedIndex >= 0 && adjustedIndex < data.length) {
                            String nameValue = data[adjustedIndex].trim();
                            if (!nameValue.isEmpty()) {
                                // 有值，直接解析
                                inventory.setName(cleanString(nameValue));
                            } else {
                                // 为空，检查是否是当前列有单元格合并
                                if (previousInventory != null && previousInventory.getName() != null) {
                                    inventory.setName(previousInventory.getName());
                                } else {
                                    // 不是合并单元格，不继承
                                    inventory.setName(null);
                                }
                            }
                        } else {
                            // 调整后的索引超出范围，可能是当前列有单元格合并
                            if (previousInventory != null && previousInventory.getName() != null) {
                                inventory.setName(previousInventory.getName());
                            } else {
                                // 前一行也没有值，设置为null
                                inventory.setName(null);
                            }
                        }
                    }



                    // 解析数量
                    if (quantityIndex != -1) {
                        int offset = calculateOffset(quantityIndex, data, 
                                                      dateIndex, styleNumberIndex, nameIndex, 
                                                      quantityIndex, unitPriceIndex, amountIndex, remarkIndex);
                        int adjustedIndex = Math.max(0, quantityIndex - offset);
                        
                        if (adjustedIndex >= 0 && adjustedIndex < data.length) {
                            String quantityValue = data[adjustedIndex].trim();
                            if (!quantityValue.isEmpty()) {
                                // 有值，直接解析
                                inventory.setQuantity(parseInteger(quantityValue));
                            } else {
                                // 为空，检查是否是当前列有单元格合并
                                if (previousInventory != null && previousInventory.getQuantity() != null) {
                                    inventory.setQuantity(previousInventory.getQuantity());
                                } else {
                                    // 不是合并单元格，不继承
                                    inventory.setQuantity(null);
                                }
                            }
                        } else {
                            // 调整后的索引超出范围，可能是当前列有单元格合并
                            if (previousInventory != null && previousInventory.getQuantity() != null) {
                                inventory.setQuantity(previousInventory.getQuantity());
                            } else {
                                // 前一行也没有值，设置为null
                                inventory.setQuantity(null);
                            }
                        }
                    }

                    // 解析单价
                    if (unitPriceIndex != -1) {
                        int offset = calculateOffset(unitPriceIndex, data, 
                                                      dateIndex, styleNumberIndex, nameIndex, 
                                                      quantityIndex, unitPriceIndex, amountIndex, remarkIndex);
                        int adjustedIndex = Math.max(0, unitPriceIndex - offset);
                        
                        if (adjustedIndex >= 0 && adjustedIndex < data.length) {
                            String unitPriceValue = data[adjustedIndex].trim();
                            if (!unitPriceValue.isEmpty()) {
                                // 有值，直接解析
                                inventory.setUnitPrice(parseDouble(unitPriceValue));
                            } else {
                                // 为空，检查是否是当前列有单元格合并
                                if (previousInventory != null && previousInventory.getUnitPrice() != null) {
                                    inventory.setUnitPrice(previousInventory.getUnitPrice());
                                } else {
                                    // 不是合并单元格，不继承
                                    inventory.setUnitPrice(null);
                                }
                            }
                        } else {
                            // 调整后的索引超出范围，可能是当前列有单元格合并
                            if (previousInventory != null && previousInventory.getUnitPrice() != null) {
                                inventory.setUnitPrice(previousInventory.getUnitPrice());
                            } else {
                                // 前一行也没有值，设置为null
                                inventory.setUnitPrice(null);
                            }
                        }
                    }

                    // 解析金额
                    if (amountIndex != -1) {
                        int offset = calculateOffset(amountIndex, data, 
                                                      dateIndex, styleNumberIndex, nameIndex, 
                                                      quantityIndex, unitPriceIndex, amountIndex, remarkIndex);
                        int adjustedIndex = Math.max(0, amountIndex - offset);
                        
                        if (adjustedIndex >= 0 && adjustedIndex < data.length) {
                            String amountValue = data[adjustedIndex].trim();
                            if (!amountValue.isEmpty()) {
                                // 有值，直接解析
                                inventory.setAmount(parseDouble(amountValue));
                            } else {
                                // 为空，检查是否是当前列有单元格合并
                                if (previousInventory != null && previousInventory.getAmount() != null) {
                                    inventory.setAmount(previousInventory.getAmount());
                                } else {
                                    // 不是合并单元格，不继承
                                    inventory.setAmount(null);
                                }
                            }
                        } else {
                            // 调整后的索引超出范围，可能是当前列有单元格合并
                            if (previousInventory != null && previousInventory.getAmount() != null) {
                                inventory.setAmount(previousInventory.getAmount());
                            } else {
                                // 前一行也没有值，设置为null
                                inventory.setAmount(null);
                            }
                        }
                    }

                    // 解析备注
                    if (remarkIndex != -1) {
                        int offset = calculateOffset(remarkIndex, data, 
                                                      dateIndex, styleNumberIndex, nameIndex, 
                                                      quantityIndex, unitPriceIndex, amountIndex, remarkIndex);
                        int adjustedIndex = Math.max(0, remarkIndex - offset);
                        
                        if (adjustedIndex >= 0 && adjustedIndex < data.length) {
                            String remarkValue = data[adjustedIndex].trim();
                            if (!remarkValue.isEmpty()) {
                                // 有值，直接解析
                                inventory.setRemark(cleanString(remarkValue));
                            } else {
                                // 为空，检查是否是当前列有单元格合并
                                if (previousInventory != null && previousInventory.getRemark() != null) {
                                    inventory.setRemark(previousInventory.getRemark());
                                } else {
                                    // 不是合并单元格，不继承
                                    inventory.setRemark(null);
                                }
                            }
                        } else {
                            // 调整后的索引超出范围，可能是当前列有单元格合并
                            if (previousInventory != null && previousInventory.getRemark() != null) {
                                inventory.setRemark(previousInventory.getRemark());
                            } else {
                                // 前一行也没有值，设置为null
                                inventory.setRemark(null);
                            }
                        }
                    }

                    // 确定是进货还是退货
                    if (headerRowIndices.size() == 1) {
                        // 只有一个表格，根据数量或金额的正负区分进退货
                        determineInventoryType(inventory);
                    } else {
                        // 有多个表格，第一个表格为进货，第二个表格为退货
                        if (headerIndex == 0) {
                            // 第一个表格，设置为进货
                            inventory.setReturned(false);
                        } else if (headerIndex == 1) {
                            // 第二个表格，设置为退货
                            inventory.setReturned(true);
                        }
                        // 超过两个表格的情况，暂时按默认处理
                    }

                    inventoryList.add(inventory);
                    previousInventory = inventory;
                }
            }
        }

        return inventoryList;
    }

    /**
     * 找到所有包含表头的行
     */
    private List<Integer> findAllHeaderRows(String[] lines) {
        List<Integer> headerRows = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].toLowerCase();
            // 检查是否包含至少3个关键表头字段，以识别表头行
            int matchedHeaders = 0;
            if (DATE_HEADERS.stream().anyMatch(h -> line.contains(h.toLowerCase()))) matchedHeaders++;
            if (STYLE_NUMBER_HEADERS.stream().anyMatch(h -> line.contains(h.toLowerCase()))) matchedHeaders++;
            if (NAME_HEADERS.stream().anyMatch(h -> line.contains(h.toLowerCase()))) matchedHeaders++;
            if (QUANTITY_HEADERS.stream().anyMatch(h -> line.contains(h.toLowerCase()))) matchedHeaders++;
            if (UNIT_PRICE_HEADERS.stream().anyMatch(h -> line.contains(h.toLowerCase()))) matchedHeaders++;
            if (AMOUNT_HEADERS.stream().anyMatch(h -> line.contains(h.toLowerCase()))) matchedHeaders++;
            
            // 如果匹配到至少3个关键表头字段，则认为是表头行
            if (matchedHeaders >= 3) {
                headerRows.add(i);
            }
        }
        return headerRows;
    }

    /**
     * 找到指定字段的列索引
     */
    private int findColumnIndex(String[] headers, List<String> possibleHeaders) {
        for (int i = 0; i < headers.length; i++) {
            String header = headers[i].toLowerCase();
            if (possibleHeaders.stream().anyMatch(h -> header.contains(h.toLowerCase()))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 解析日期字符串
     */
    private Date parseDate(String dateStr) {
        for (SimpleDateFormat format : DATE_FORMATS) {
            try {
                return format.parse(dateStr);
            } catch (ParseException e) {
                // 尝试下一种格式
            }
        }
        return null;
    }

    /**
     * 解析整数（去除单位和空格）
     */
    private Integer parseInteger(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        // 去除所有空格，然后去除单位（件、个等）
        String numericStr = str.replaceAll("\\s+", "").replaceAll("[^\\d-]", "");
        try {
            return Integer.parseInt(numericStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 解析浮点数（去除单位和空格）
     */
    private Double parseDouble(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        // 去除所有空格，然后去除单位（元等）
        String numericStr = str.replaceAll("\\s+", "").replaceAll("[^\\d\\.-]", "");
        try {
            return Double.parseDouble(numericStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int calculateOffset(int columnIndex, String[] data, 
                                 int dateIndex, int styleNumberIndex, int nameIndex, 
                                 int quantityIndex, int unitPriceIndex, int amountIndex, int remarkIndex) {
        int offset = 0;
        
        // 定义所有列的顺序，确保按正确的顺序检查前面的列
        int[] columnOrder = {dateIndex, styleNumberIndex, nameIndex, 
                              quantityIndex, unitPriceIndex, amountIndex, remarkIndex};
        
        // 遍历所有列，找到当前列之前的列
        for (int currentColumn : columnOrder) {
            // 如果当前列是我们要计算偏移量的列，停止遍历
            if (currentColumn == columnIndex) {
                break;
            }
            
            // 如果当前列在表头中存在
            if (currentColumn != -1) {
                // 计算当前列的实际数据索引（考虑前面的偏移量）
                int actualIndex = currentColumn - offset;
                
                // 检查实际索引是否有效，并且数据是否为空
                if (actualIndex < 0 || actualIndex >= data.length || 
                    data[actualIndex].trim().isEmpty()) {
                    // 如果是空值或索引无效，说明有单元格合并，增加偏移量
                    offset++;
                }
            }
        }
        
        return offset;
    }

    /**
     * 清理字符串（去除前后空格和中间多余空格）
     */
    private String cleanString(String str) {
        if (str == null) {
            return null;
        }
        // 去除前后空格，然后将中间的多个空格替换为单个空格
        return str.trim().replaceAll("\\s+", " ");
    }

    /**
     * 确定是进货还是退货（适用于单个表格的情况）
     */
    private void determineInventoryType(Inventory inventory) {
        // 这里假设如果数量或金额为负数，则为退货
        if ((inventory.getQuantity() != null && inventory.getQuantity() < 0) ||
                (inventory.getAmount() != null && inventory.getAmount() < 0)) {
            inventory.setReturned(true);
            // 将负数转换为正数存储
            if (inventory.getQuantity() != null) {
                inventory.setQuantity(Math.abs(inventory.getQuantity()));
            }
            if (inventory.getAmount() != null) {
                inventory.setAmount(Math.abs(inventory.getAmount()));
            }
        } else {
            inventory.setReturned(false);
        }
    }
}
