package net.hka.examples.thymeleaf.business.dto;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {
    
    // ===== 整体状态 =====
    private boolean valid = true;
    private String message;
    
    // ===== 文件级验证 =====
    private FileValidation fileValidation;
    
    // ===== 工作表级验证（包含统计信息）=====
    private SheetValidation computerOrdersSheet;
    private SheetValidation customersSheet;
    
    // ===== 行级数据（用于前端表格展示）=====
    private List<ExcelComputerOrder> computerOrders = new ArrayList<>();
    private List<ExcelCustomer> customers = new ArrayList<>();
    
    // ===== 兼容旧代码：errors 字段（已废弃，使用 fileValidation.errors）=====
    @Deprecated
    private List<String> errors = new ArrayList<>();

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public FileValidation getFileValidation() {
        return fileValidation;
    }

    public void setFileValidation(FileValidation fileValidation) {
        this.fileValidation = fileValidation;
    }

    public SheetValidation getComputerOrdersSheet() {
        return computerOrdersSheet;
    }

    public void setComputerOrdersSheet(SheetValidation computerOrdersSheet) {
        this.computerOrdersSheet = computerOrdersSheet;
    }

    public SheetValidation getCustomersSheet() {
        return customersSheet;
    }

    public void setCustomersSheet(SheetValidation customersSheet) {
        this.customersSheet = customersSheet;
    }

    public List<ExcelComputerOrder> getComputerOrders() {
        return computerOrders;
    }

    public void setComputerOrders(List<ExcelComputerOrder> computerOrders) {
        this.computerOrders = computerOrders;
        // 自动更新工作表统计信息
        if (computerOrdersSheet != null) {
            computerOrdersSheet.updateStatistics(computerOrders);
        }
    }

    public List<ExcelCustomer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<ExcelCustomer> customers) {
        this.customers = customers;
        // 自动更新工作表统计信息
        if (customersSheet != null) {
            customersSheet.updateStatistics(customers);
        }
    }

    @Deprecated
    public List<String> getErrors() {
        return errors;
    }

    @Deprecated
    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    /**
     * @deprecated 使用 fileValidation.addError(error) 代替
     */
    @Deprecated
    public void addError(String error) {
        this.errors.add(error);
        this.valid = false;
        // 同步到 fileValidation
        if (fileValidation != null) {
            fileValidation.addError(error);
        }
    }

    /**
     * 是否有任何错误（文件级 + 工作表级 + 行级）
     */
    public boolean hasErrors() {
        return (fileValidation != null && fileValidation.hasErrors()) ||
               (computerOrdersSheet != null && computerOrdersSheet.hasErrors()) ||
               (customersSheet != null && customersSheet.hasErrors()) ||
               hasDataErrors();
    }

    /**
     * 是否有行级数据错误
     */
    public boolean hasDataErrors() {
        return computerOrders.stream().anyMatch(o -> !o.isValid()) ||
               customers.stream().anyMatch(c -> !c.isValid());
    }

    /**
     * 获取总错误数（文件级 + 工作表级 + 行级）
     */
    public int getTotalErrorCount() {
        int count = 0;
        
        // 文件级错误
        if (fileValidation != null && fileValidation.hasErrors()) {
            count += fileValidation.getErrors().size();
        }
        
        // 工作表级错误
        if (computerOrdersSheet != null && computerOrdersSheet.hasErrors()) {
            count += computerOrdersSheet.getErrors().size();
        }
        if (customersSheet != null && customersSheet.hasErrors()) {
            count += customersSheet.getErrors().size();
        }
        
        // 行级错误
        count += computerOrders.stream().filter(o -> !o.isValid()).count();
        count += customers.stream().filter(c -> !c.isValid()).count();
        
        return count;
    }

    /**
     * 更新整体状态（根据各层级验证结果）
     */
    public void updateOverallStatus() {
        // 如果文件级验证失败，整体失败
        if (fileValidation != null && !fileValidation.isValid()) {
            this.valid = false;
            return;
        }
        
        // 如果工作表级验证失败，整体失败
        if (computerOrdersSheet != null && !computerOrdersSheet.isParseable()) {
            this.valid = false;
        }
        if (customersSheet != null && !customersSheet.isParseable()) {
            this.valid = false;
        }
        
        // 如果有行级错误，整体也标记为失败（但可以部分上传）
        if (hasDataErrors()) {
            this.valid = false;
        }
    }
}
