package net.hka.examples.thymeleaf.business.service;

import net.hka.examples.thymeleaf.business.dto.ExcelComputerOrder;
import net.hka.examples.thymeleaf.business.dto.ExcelCustomer;
import net.hka.examples.thymeleaf.business.dto.ValidationResult;
import net.hka.examples.thymeleaf.business.repository.ComputerOrderRepository;
import net.hka.examples.thymeleaf.business.repository.CustomerRepository;
import net.hka.examples.thymeleaf.domain.ComputerOrder;
import net.hka.examples.thymeleaf.domain.Customer;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelService {

    private static final String[] COMPUTER_SHEET_NAMES = {"电脑订单", "Computer Orders", "Orders"};
    private static final String[] CUSTOMER_SHEET_NAMES = {"客户表", "Customers", "客户"};
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ComputerOrderRepository computerOrderRepository;
    private final CustomerRepository customerRepository;

    public ExcelService(ComputerOrderRepository computerOrderRepository,
                         CustomerRepository customerRepository) {
        this.computerOrderRepository = computerOrderRepository;
        this.customerRepository = customerRepository;
    }

    public ValidationResult parseAndValidateExcel(MultipartFile file) {
        ValidationResult result = new ValidationResult();

        if (file == null || file.isEmpty()) {
            result.addError("文件不能为空");
            return result;
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            result.addError("只支持 .xlsx 或 .xls 格式的 Excel 文件");
            return result;
        }

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet computerSheet = findSheet(workbook, COMPUTER_SHEET_NAMES);
            Sheet customerSheet = findSheet(workbook, CUSTOMER_SHEET_NAMES);

            if (computerSheet == null) {
                result.addError("未找到电脑订单工作表，请确保包含以下名称之一: " + String.join(", ", COMPUTER_SHEET_NAMES));
            }
            if (customerSheet == null) {
                result.addError("未找到客户工作表，请确保包含以下名称之一: " + String.join(", ", CUSTOMER_SHEET_NAMES));
            }

            if (!result.isValid()) {
                return result;
            }

            List<ExcelComputerOrder> computerOrders = parseComputerSheet(computerSheet);
            List<ExcelCustomer> customers = parseCustomerSheet(customerSheet);

            result.setComputerOrders(computerOrders);
            result.setCustomers(customers);

            if (computerOrders.isEmpty()) {
                result.addError("电脑订单工作表中没有数据");
            }
            if (customers.isEmpty()) {
                result.addError("客户工作表中没有数据");
            }

        } catch (IOException e) {
            result.addError("文件读取失败，文件可能已损坏");
        } catch (Exception e) {
            result.addError("Excel 解析错误: " + e.getMessage());
        }

        return result;
    }

    private Sheet findSheet(Workbook workbook, String[] sheetNames) {
        for (String name : sheetNames) {
            Sheet sheet = workbook.getSheet(name);
            if (sheet != null) {
                return sheet;
            }
        }
        return null;
    }

    private List<ExcelComputerOrder> parseComputerSheet(Sheet sheet) {
        List<ExcelComputerOrder> orders = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        int lastRow = sheet.getLastRowNum();
        if (lastRow < 2) {
            return orders;
        }

        for (int i = 2; i <= lastRow; i++) {
            Row row = sheet.getRow(i);
            if (row == null || isEmptyRow(row)) {
                continue;
            }

            ExcelComputerOrder order = new ExcelComputerOrder();
            order.setRowNumber(i + 1);
            StringBuilder errors = new StringBuilder();

            try {
                String brand = getCellValueAsString(row.getCell(0), formatter);
                if (brand == null || brand.trim().isEmpty()) {
                    errors.append("品牌不能为空; ");
                } else {
                    order.setBrand(brand.trim());
                }

                BigDecimal price = getCellValueAsBigDecimal(row.getCell(1), formatter);
                if (price == null) {
                    errors.append("价格格式错误或为空; ");
                } else if (price.compareTo(BigDecimal.ZERO) < 0) {
                    errors.append("价格不能为负数; ");
                } else {
                    order.setPrice(price);
                }

                String memorySize = getCellValueAsString(row.getCell(2), formatter);
                if (memorySize == null || memorySize.trim().isEmpty()) {
                    errors.append("内存大小不能为空; ");
                } else {
                    order.setMemorySize(memorySize.trim());
                }

                LocalDate manufactureDate = getCellValueAsLocalDate(row.getCell(3), formatter);
                if (manufactureDate == null) {
                    errors.append("出厂日期格式错误或为空(应为yyyy-MM-dd格式); ");
                } else {
                    order.setManufactureDate(manufactureDate);
                }

                LocalDate saleDate = getCellValueAsLocalDate(row.getCell(4), formatter);
                order.setSaleDate(saleDate);

            } catch (Exception e) {
                errors.append("解析错误: ").append(e.getMessage()).append("; ");
            }

            if (errors.length() > 0) {
                order.setErrorMessage(errors.toString());
            }

            orders.add(order);
        }

        return orders;
    }

    private List<ExcelCustomer> parseCustomerSheet(Sheet sheet) {
        List<ExcelCustomer> customers = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        int lastRow = sheet.getLastRowNum();
        if (lastRow < 2) {
            return customers;
        }

        for (int i = 2; i <= lastRow; i++) {
            Row row = sheet.getRow(i);
            if (row == null || isEmptyRow(row)) {
                continue;
            }

            ExcelCustomer customer = new ExcelCustomer();
            customer.setRowNumber(i + 1);
            StringBuilder errors = new StringBuilder();

            try {
                String name = getCellValueAsString(row.getCell(0), formatter);
                if (name == null || name.trim().isEmpty()) {
                    errors.append("客户名称不能为空; ");
                } else {
                    customer.setName(name.trim());
                }

                String phone = getCellValueAsString(row.getCell(1), formatter);
                if (phone == null || phone.trim().isEmpty()) {
                    errors.append("客户电话不能为空; ");
                } else {
                    customer.setPhone(phone.trim());
                }

                String computers = getCellValueAsString(row.getCell(2), formatter);
                customer.setComputers(computers != null ? computers.trim() : null);

                Integer quantity = getCellValueAsInteger(row.getCell(3), formatter);
                if (quantity != null && quantity < 0) {
                    errors.append("台数不能为负数; ");
                } else {
                    customer.setQuantity(quantity);
                }

                String email = getCellValueAsString(row.getCell(4), formatter);
                if (email != null && !email.trim().isEmpty()) {
                    if (!isValidEmail(email.trim())) {
                        errors.append("邮箱格式不正确; ");
                    }
                    customer.setEmail(email.trim());
                }

            } catch (Exception e) {
                errors.append("解析错误: ").append(e.getMessage()).append("; ");
            }

            if (errors.length() > 0) {
                customer.setErrorMessage(errors.toString());
            }

            customers.add(customer);
        }

        return customers;
    }

    private boolean isEmptyRow(Row row) {
        for (int i = 0; i < 5; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValueAsString(cell, new DataFormatter());
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private String getCellValueAsString(Cell cell, DataFormatter formatter) {
        if (cell == null) {
            return null;
        }
        return formatter.formatCellValue(cell).trim();
    }

    private BigDecimal getCellValueAsBigDecimal(Cell cell, DataFormatter formatter) {
        if (cell == null) {
            return null;
        }
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return BigDecimal.valueOf(cell.getNumericCellValue());
            } else if (cell.getCellType() == CellType.STRING) {
                String value = formatter.formatCellValue(cell).trim();
                if (value.isEmpty()) {
                    return null;
                }
                return new BigDecimal(value.replace(",", ""));
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }

    private LocalDate getCellValueAsLocalDate(Cell cell, DataFormatter formatter) {
        if (cell == null) {
            return null;
        }
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            } else if (cell.getCellType() == CellType.STRING) {
                String value = formatter.formatCellValue(cell).trim();
                if (value.isEmpty()) {
                    return null;
                }
                return LocalDate.parse(value, DATE_FORMATTER);
            }
        } catch (DateTimeParseException e) {
            return null;
        }
        return null;
    }

    private Integer getCellValueAsInteger(Cell cell, DataFormatter formatter) {
        if (cell == null) {
            return null;
        }
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (int) cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                String value = formatter.formatCellValue(cell).trim();
                if (value.isEmpty()) {
                    return null;
                }
                return Integer.parseInt(value.replace(",", ""));
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    @Transactional
    public ValidationResult uploadData(List<ExcelComputerOrder> computerOrders, List<ExcelCustomer> customers) {
        ValidationResult result = new ValidationResult();

        try {
            List<ComputerOrder> entities = new ArrayList<>();
            for (ExcelComputerOrder order : computerOrders) {
                if (order.isValid()) {
                    ComputerOrder entity = new ComputerOrder();
                    entity.setBrand(order.getBrand());
                    entity.setPrice(order.getPrice());
                    entity.setMemorySize(order.getMemorySize());
                    entity.setManufactureDate(order.getManufactureDate());
                    entity.setSaleDate(order.getSaleDate());
                    entities.add(entity);
                }
            }
            computerOrderRepository.saveAll(entities);

            List<Customer> customerEntities = new ArrayList<>();
            for (ExcelCustomer customer : customers) {
                if (customer.isValid()) {
                    Customer entity = new Customer();
                    entity.setName(customer.getName());
                    entity.setPhone(customer.getPhone());
                    entity.setComputersJson(customer.getComputers());
                    entity.setQuantity(customer.getQuantity());
                    entity.setEmail(customer.getEmail());
                    customerEntities.add(entity);
                }
            }
            customerRepository.saveAll(customerEntities);

            result.setMessage("上传成功！共上传 " + entities.size() + " 条电脑订单，"
                            + customerEntities.size() + " 条客户数据");

        } catch (Exception e) {
            result.setValid(false);
            result.setMessage("数据库保存失败: " + e.getMessage());
        }

        return result;
    }
}
