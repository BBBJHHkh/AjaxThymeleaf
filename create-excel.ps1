$ErrorActionPreference = "Stop"

$excel = New-Object -ComObject Excel.Application
$excel.Visible = $false
$excel.DisplayAlerts = $false

$workbook = $excel.Workbooks.Add()

# ========== 电脑订单表 ==========
$sheet1 = $workbook.Worksheets.Item(1)
$sheet1.Name = "电脑订单"

# 表头
$sheet1.Cells.Item(1,1) = "品牌"
$sheet1.Cells.Item(1,2) = "价格"
$sheet1.Cells.Item(1,3) = "内存大小"
$sheet1.Cells.Item(1,4) = "出厂日期"
$sheet1.Cells.Item(1,5) = "销售日期"

# 数据
$sheet1.Cells.Item(2,1) = "联想"
$sheet1.Cells.Item(2,2) = 5999.00
$sheet1.Cells.Item(2,3) = "16GB"
$sheet1.Cells.Item(2,4) = "2024-01-15"
$sheet1.Cells.Item(2,5) = "2024-02-20"

$sheet1.Cells.Item(3,1) = "戴尔"
$sheet1.Cells.Item(3,2) = 7999.00
$sheet1.Cells.Item(3,3) = "32GB"
$sheet1.Cells.Item(3,4) = "2024-03-10"
$sheet1.Cells.Item(3,5) = "2024-04-15"

$sheet1.Cells.Item(4,1) = "惠普"
$sheet1.Cells.Item(4,2) = 6999.00
$sheet1.Cells.Item(4,3) = "16GB"
$sheet1.Cells.Item(4,4) = "2024-02-05"
$sheet1.Cells.Item(4,5) = "2024-03-01"

$sheet1.Cells.Item(5,1) = "苹果"
$sheet1.Cells.Item(5,2) = 12999.00
$sheet1.Cells.Item(5,3) = "16GB"
$sheet1.Cells.Item(5,4) = "2024-01-20"
$sheet1.Cells.Item(5,5) = "2024-02-28"

$sheet1.Cells.Item(6,1) = "华硕"
$sheet1.Cells.Item(6,2) = 5499.00
$sheet1.Cells.Item(6,3) = "8GB"
$sheet1.Cells.Item(6,4) = "2024-04-01"
$sheet1.Cells.Item(6,5) = "2024-04-20"

# 设置列宽
$sheet1.UsedRange.EntireColumn.AutoFit() | Out-Null

# ========== 客户表 ==========
$sheet2 = $workbook.Worksheets.Add()
$sheet2.Name = "客户表"

# 表头
$sheet2.Cells.Item(1,1) = "客户名称"
$sheet2.Cells.Item(1,2) = "客户电话"
$sheet2.Cells.Item(1,3) = "电脑型号"
$sheet2.Cells.Item(1,4) = "台数"
$sheet2.Cells.Item(1,5) = "邮箱"

# 数据
$sheet2.Cells.Item(2,1) = "张三"
$sheet2.Cells.Item(2,2) = "13800138001"
$sheet2.Cells.Item(2,3) = "联想ThinkPad"
$sheet2.Cells.Item(2,4) = 3
$sheet2.Cells.Item(2,5) = "zhangsan@example.com"

$sheet2.Cells.Item(3,1) = "李四"
$sheet2.Cells.Item(3,2) = "13900139002"
$sheet2.Cells.Item(3,3) = "戴尔 XPS"
$sheet2.Cells.Item(3,4) = 2
$sheet2.Cells.Item(3,5) = "lisi@example.com"

$sheet2.Cells.Item(4,1) = "王五"
$sheet2.Cells.Item(4,2) = "13700137003"
$sheet2.Cells.Item(4,3) = "苹果 MacBook Pro"
$sheet2.Cells.Item(4,4) = 1
$sheet2.Cells.Item(4,5) = "wangwu@example.com"

$sheet2.Cells.Item(5,1) = "赵六"
$sheet2.Cells.Item(5,2) = "13600136004"
$sheet2.Cells.Item(5,3) = "惠普战66"
$sheet2.Cells.Item(5,4) = 5
$sheet2.Cells.Item(5,5) = "zhaoliu@example.com"

$sheet2.Cells.Item(6,1) = "孙七"
$sheet2.Cells.Item(6,2) = "13500135005"
$sheet2.Cells.Item(6,3) = "华硕灵耀"
$sheet2.Cells.Item(6,4) = 2
$sheet2.Cells.Item(6,5) = "sunqi@example.com"

$sheet2.UsedRange.EntireColumn.AutoFit() | Out-Null

# 保存
$outputPath = "D:\cursorStudy\AjaxThymeleaf\AjaxThymeleaf\test-data.xlsx"
$workbook.SaveAs($outputPath, 51)  # 51 = xlOpenXMLWorkbook (.xlsx)
$workbook.Close($false)
$excel.Quit()

[System.Runtime.Interopservices.Marshal]::ReleaseComObject($excel) | Out-Null

Write-Host "Excel 文件已创建: $outputPath"
