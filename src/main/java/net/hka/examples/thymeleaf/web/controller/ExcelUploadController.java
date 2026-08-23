package net.hka.examples.thymeleaf.web.controller;

import net.hka.examples.thymeleaf.business.dto.ExcelComputerOrder;
import net.hka.examples.thymeleaf.business.dto.ExcelCustomer;
import net.hka.examples.thymeleaf.business.dto.ValidationResult;
import net.hka.examples.thymeleaf.business.service.ExcelService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
// 将基础路径修改为与前端 JS 对齐
@RequestMapping("/excel-upload")
public class ExcelUploadController {

    private final ExcelService excelService;

    // 校验通过后，落盘的目标文件夹（来自 application.properties）
    @Value("${excel.upload.success-folder}")
    private String successFolder;

    public ExcelUploadController(ExcelService excelService) {
        this.excelService = excelService;
    }

    // 访问路径变为: /thymeleaf/excel-upload
    @GetMapping
    public String showExcelUploadPage(Model model) {
        model.addAttribute("module", "excel-upload");
        return "excel-upload/excel-upload";
    }

    // 访问路径变为: /thymeleaf/excel-upload/check
    @PostMapping("/check")
    @ResponseBody
    public ValidationResult checkExcelFile(@RequestParam("file") MultipartFile file) {
        ValidationResult result = excelService.parseAndValidateExcel(file);

        if (result.isValid() && !result.hasDataErrors()) {
            // 校验通过 → 把文件保存到 successFolder
            try {
                String savedPath = saveValidatedFile(file);
                result.setMessage("校验通过！文件已保存到: " + savedPath);
            } catch (IOException e) {
                result.setValid(false);
                result.addError("文件保存失败: " + e.getMessage());
            }
        }

        return result;
    }

    /**
     * 将校验通过的文件保存到配置的目标目录。
     * 文件名格式: 原名_时间戳.扩展名，避免重复上传时覆盖同名文件。
     */
    private String saveValidatedFile(MultipartFile file) throws IOException {
        Path targetDir = Paths.get(successFolder);
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        String original = file.getOriginalFilename();
        String baseName = original;
        String ext = "";
        if (original != null && original.contains(".")) {
            int dotIdx = original.lastIndexOf('.');
            baseName = original.substring(0, dotIdx);
            ext = original.substring(dotIdx);
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String newFileName = baseName + "_" + timestamp + ext;

        File dest = targetDir.resolve(newFileName).toFile();
        file.transferTo(dest);

        return dest.getAbsolutePath();
    }

    // 访问路径变为: /thymeleaf/excel-upload/upload
    @PostMapping(value = "/upload", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ValidationResult uploadExcelData(@RequestBody ExcelUploadData uploadData) {
        if (uploadData.getComputerOrders() == null || uploadData.getComputerOrders().isEmpty() ||
            uploadData.getCustomers() == null || uploadData.getCustomers().isEmpty()) {
            ValidationResult result = new ValidationResult();
            result.setValid(false);
            result.setMessage("没有可上传的数据，请先进行 Check 操作");
            return result;
        }

        List<ExcelComputerOrder> validComputers = new ArrayList<>();
        for (ExcelComputerOrder order : uploadData.getComputerOrders()) {
            if (order.isValid()) {
                validComputers.add(order);
            }
        }

        List<ExcelCustomer> validCustomers = new ArrayList<>();
        for (ExcelCustomer customer : uploadData.getCustomers()) {
            if (customer.isValid()) {
                validCustomers.add(customer);
            }
        }

        return excelService.uploadData(validComputers, validCustomers);
    }

    // 这里是之前被省略的内部类，必须要保留才能被正常解析
    public static class ExcelUploadData {
        private List<ExcelComputerOrder> computerOrders;
        private List<ExcelCustomer> customers;

        public List<ExcelComputerOrder> getComputerOrders() {
            return computerOrders;
        }

        public void setComputerOrders(List<ExcelComputerOrder> computerOrders) {
            this.computerOrders = computerOrders;
        }

        public List<ExcelCustomer> getCustomers() {
            return customers;
        }

        public void setCustomers(List<ExcelCustomer> customers) {
            this.customers = customers;
        }
    }
}