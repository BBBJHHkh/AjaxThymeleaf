package net.hka.examples.thymeleaf;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;

public class CreateTestExcel {

    public static void main(String[] args) {
        String outputPath = "test-data.xlsx";

        try (Workbook workbook = new XSSFWorkbook()) {

            // ========== 电脑订单表 ==========
            Sheet computerSheet = workbook.createSheet("电脑订单");

            Row header1 = computerSheet.createRow(0);
            header1.createCell(0).setCellValue("品牌");
            header1.createCell(1).setCellValue("价格");
            header1.createCell(2).setCellValue("内存大小");
            header1.createCell(3).setCellValue("出厂日期");
            header1.createCell(4).setCellValue("销售日期");

            Object[][] computerData = {
                {"联想", 5999.00, "16GB", "2024-01-15", "2024-02-20"},
                {"戴尔", 7999.00, "32GB", "2024-03-10", "2024-04-15"},
                {"惠普", 6999.00, "16GB", "2024-02-05", "2024-03-01"},
                {"苹果", 12999.00, "16GB", "2024-01-20", "2024-02-28"},
                {"华硕", 5499.00, "8GB", "2024-04-01", "2024-04-20"}
            };

            for (int i = 0; i < computerData.length; i++) {
                Row row = computerSheet.createRow(i + 1);
                Object[] data = computerData[i];
                for (int j = 0; j < data.length; j++) {
                    if (data[j] instanceof Double) {
                        row.createCell(j).setCellValue((Double) data[j]);
                    } else {
                        row.createCell(j).setCellValue((String) data[j]);
                    }
                }
            }

            // ========== 客户表 ==========
            Sheet customerSheet = workbook.createSheet("客户表");

            Row header2 = customerSheet.createRow(0);
            header2.createCell(0).setCellValue("客户名称");
            header2.createCell(1).setCellValue("客户电话");
            header2.createCell(2).setCellValue("电脑型号");
            header2.createCell(3).setCellValue("台数");
            header2.createCell(4).setCellValue("邮箱");

            Object[][] customerData = {
                {"张三", "13800138001", "联想ThinkPad", 3, "zhangsan@example.com"},
                {"李四", "13900139002", "戴尔 XPS", 2, "lisi@example.com"},
                {"王五", "13700137003", "苹果 MacBook Pro", 1, "wangwu@example.com"},
                {"赵六", "13600136004", "惠普战66", 5, "zhaoliu@example.com"},
                {"孙七", "13500135005", "华硕灵耀", 2, "sunqi@example.com"}
            };

            for (int i = 0; i < customerData.length; i++) {
                Row row = customerSheet.createRow(i + 1);
                Object[] data = customerData[i];
                for (int j = 0; j < data.length; j++) {
                    if (data[j] instanceof Double) {
                        row.createCell(j).setCellValue((Double) data[j]);
                    } else if (data[j] instanceof Integer) {
                        row.createCell(j).setCellValue((Integer) data[j]);
                    } else {
                        row.createCell(j).setCellValue((String) data[j]);
                    }
                }
            }

            try (FileOutputStream outputStream = new FileOutputStream(outputPath)) {
                workbook.write(outputStream);
            }

            System.out.println("Excel 文件已创建: " + outputPath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
