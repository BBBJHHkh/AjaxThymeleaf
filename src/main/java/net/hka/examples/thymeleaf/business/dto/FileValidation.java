package net.hka.examples.thymeleaf.business.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件级验证结果
 */
public class FileValidation {
    
    // 文件状态
    private boolean exists = true;              // 文件是否存在
    private boolean validFormat = true;         // 格式是否正确 (.xlsx/.xls)
    private boolean canBeOpened = true;         // 是否可以打开（文件未损坏）
    
    // Excel 元信息
    private List<String> allSheetNames = new ArrayList<>();  // Excel 中所有工作表的名称
    private int sheetCount = 0;                             // 工作表数量
    
    // 文件级错误信息
    private List<String> errors = new ArrayList<>();        // 文件级错误列表

    public boolean isExists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }

    public boolean isValidFormat() {
        return validFormat;
    }

    public void setValidFormat(boolean validFormat) {
        this.validFormat = validFormat;
    }

    public boolean isCanBeOpened() {
        return canBeOpened;
    }

    public void setCanBeOpened(boolean canBeOpened) {
        this.canBeOpened = canBeOpened;
    }

    public List<String> getAllSheetNames() {
        return allSheetNames;
    }

    public void setAllSheetNames(List<String> allSheetNames) {
        this.allSheetNames = allSheetNames;
        this.sheetCount = allSheetNames != null ? allSheetNames.size() : 0;
    }

    public int getSheetCount() {
        return sheetCount;
    }

    public void setSheetCount(int sheetCount) {
        this.sheetCount = sheetCount;
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
     * 文件级验证是否通过
     */
    public boolean isValid() {
        return exists && validFormat && canBeOpened && !hasErrors();
    }

    /**
     * 获取错误摘要
     */
    public String getErrorSummary() {
        if (!hasErrors()) {
            return null;
        }
        return String.join("; ", errors);
    }
}
