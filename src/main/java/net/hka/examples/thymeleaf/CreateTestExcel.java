package net.hka.examples.thymeleaf;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;

public class CreateTestExcel {

    public static void main(String[] args) {
        String outputPath = "test-data.xlsx";

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet computerSheet = workbook.createSheet("Computer Orders");

            Row header1 = computerSheet.createRow(0);
            header1.createCell(0).setCellValue("Brand");
            header1.createCell(1).setCellValue("Price");
            header1.createCell(2).setCellValue("Memory Size");
            header1.createCell(3).setCellValue("Manufacture Date");
            header1.createCell(4).setCellValue("Sale Date");

            Object[][] computerData = {
                {"Lenovo", 5999.00, "16GB", "2024-01-15", "2024-02-20"},
                {"Dell", 7999.00, "32GB", "2024-03-10", "2024-04-15"},
                {"HP", 6999.00, "16GB", "2024-02-05", "2024-03-01"},
                {"Apple", 12999.00, "16GB", "2024-01-20", "2024-02-28"},
                {"ASUS", 5499.00, "8GB", "2024-04-01", "2024-04-20"}
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

            Sheet customerSheet = workbook.createSheet("Customers");

            Row header2 = customerSheet.createRow(0);
            header2.createCell(0).setCellValue("Customer Name");
            header2.createCell(1).setCellValue("Phone");
            header2.createCell(2).setCellValue("Computer Model");
            header2.createCell(3).setCellValue("Quantity");
            header2.createCell(4).setCellValue("Email");

            Object[][] customerData = {
                {"Zhang San", "13800138001", "Lenovo ThinkPad", 3, "zhangsan@example.com"},
                {"Li Si", "13900139002", "Dell XPS", 2, "lisi@example.com"},
                {"Wang Wu", "13700137003", "Apple MacBook Pro", 1, "wangwu@example.com"},
                {"Zhao Liu", "13600136004", "HP Zhan 66", 5, "zhaoliu@example.com"},
                {"Sun Qi", "13500135005", "ASUS ZenBook", 2, "sunqi@example.com"}
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

            System.out.println("Excel file created: " + outputPath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
