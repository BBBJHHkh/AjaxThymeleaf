package net.hka.examples.thymeleaf.business.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 工作表级验证结果
 */
public class SheetValidation {
    
    // 工作表身份
    private String sheetType;                   // 工作表类型: "COMPUTER" 或 "CUSTOMER"
    private List<String> expectedNames = new ArrayList<>(); // 期望的工作表名称列表
    private String foundName;                   // 实际找到的工作表名称（如果找到）
    
    // 工作表状态
    private SheetStatus status = SheetStatus.VALID;  // 状态枚举
    
    // 统计信息
    private int totalRows = 0;                  // 总行数（不含标题行）
    private int validRows = 0;                  // 有效行数
    private int invalidRows = 0;                // 无效行数
    
    // 工作表级错误（不包括行级错误）
    private List<String> errors = new ArrayList<>();  // 如：工作表为空、名称不匹配等

    public String getSheetType() {
        return sheetType;
    }

    public void setSheetType(String sheetType) {
        this.sheetType = sheetType;
    }

    public List<String> getExpectedNames() {
        return expectedNames;
    }

    public void setExpectedNames(List<String> expectedNames) {
        this.expectedNames = expectedNames;
    }

    public String getFoundName() {
        return foundName;
    }

    public void setFoundName(String foundName) {
        this.foundName = foundName;
    }

    public SheetStatus getStatus() {
        return status;
    }

    public void setStatus(SheetStatus status) {
        this.status = status;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getValidRows() {
        return validRows;
    }

    public void setValidRows(int validRows) {
        this.validRows = validRows;
    }

    public int getInvalidRows() {
        return invalidRows;
    }

    public void setInvalidRows(int invalidRows) {
        this.invalidRows = invalidRows;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public void addError(String error) {
        this.errors.add(error);
    }

    /**
     * 是否有错误
     */
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    /**
     * 工作表是否可以解析
     */
    public boolean isParseable() {
        return status == SheetStatus.VALID && foundName != null;
    }

    /**
     * 获取状态消息
     */
    public String getStatusMessage() {
        switch (status) {
            case VALID:
                return "正常";
            case NOT_FOUND:
                return "未找到工作表";
            case NAME_MISMATCH:
                return "工作表名称不匹配";
            case EMPTY:
                return "工作表为空";
            case PARSE_ERROR:
                return "解析错误";
            default:
                return "未知状态";
        }
    }

    /**
     * 更新统计信息（从数据列表中计算）
     */
    public void updateStatistics(List<?> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            this.totalRows = 0;
            this.validRows = 0;
            this.invalidRows = 0;
            return;
        }

        this.totalRows = dataList.size();
        this.validRows = 0;
        this.invalidRows = 0;

        for (Object item : dataList) {
            if (item instanceof ExcelComputerOrder) {
                if (((ExcelComputerOrder) item).isValid()) {
                    this.validRows++;
                } else {
                    this.invalidRows++;
                }
            } else if (item instanceof ExcelCustomer) {
                if (((ExcelCustomer) item).isValid()) {
                    this.validRows++;
                } else {
                    this.invalidRows++;
                }
            }
        }
    }

    /**
     * 工作表状态枚举
     */
    public enum SheetStatus {
        VALID,              // 工作表有效，可以解析
        NOT_FOUND,          // 工作表未找到
        NAME_MISMATCH,      // 工作表名称不匹配
        EMPTY,              // 工作表为空（只有标题）
        PARSE_ERROR         // 解析错误
    }
}
