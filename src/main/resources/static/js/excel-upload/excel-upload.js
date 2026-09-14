$(function() {
    var validatedData = null;

    // 20MB，字节数（与后端 spring.servlet.multipart.max-file-size 保持一致）
    var MAX_FILE_SIZE = 20 * 1024 * 1024;
    var ALLOWED_EXTENSIONS = ['.xlsx', '.xls'];

    // 选完文件后立刻校验大小和后缀，给用户即时反馈
    $('#excelFile').on('change', function() {
        var file = this.files && this.files[0];
        if (!file) {
            return;
        }
        var error = validateFile(file);
        if (error) {
            showMessage(error, 'warning');
            // 清掉选择，避免用户继续点 Check 把非法文件传上去
            $(this).val('');
        }
    });

    function validateFile(file) {
        if (!file) {
            return MSG.FILE_REQUIRED;
        }
        if (file.size > MAX_FILE_SIZE) {
            var sizeMB = (file.size / 1024 / 1024).toFixed(2);
            var maxMB = (MAX_FILE_SIZE / 1024 / 1024).toFixed(0);
            return formatMsg(MSG.FILE_TOO_LARGE, sizeMB, maxMB);
        }
        var name = (file.name || '').toLowerCase();
        var extOk = ALLOWED_EXTENSIONS.some(function(ext) {
            return name.endsWith(ext);
        });
        if (!extOk) {
            return MSG.FILE_INVALID_FORMAT;
        }
        return null;
    }

    // Check button click
    $('#btnCheck').click(function() {
        var fileInput = document.getElementById('excelFile');
        var file = fileInput.files[0];

        if (!file) {
            showMessage(MSG.FILE_REQUIRED, 'warning');
            return;
        }

        var preCheckError = validateFile(file);
        if (preCheckError) {
            showMessage(preCheckError, 'warning');
            return;
        }

        var formData = new FormData();
        formData.append('file', file);

        $('#btnCheck').prop('disabled', true).html('<span class="glyphicon glyphicon-refresh"></span> ' + MSG.BTN_CHECKING);

        $.ajax({
            url: '/thymeleaf/excel-upload/check',
            type: 'POST',
            data: formData,
            processData: false,
            contentType: false,
            success: function(result) {
                $('#btnCheck').prop('disabled', false).html('<span class="glyphicon glyphicon-check"></span> ' + MSG.BTN_CHECK);

                if (result.valid && !result.hasDataErrors) {
                    validatedData = result;
                    $('#btnUpload').prop('disabled', false);
                    showMessage(result.message || MSG.CHECK_PASSED, 'success');
                    displayData(result);
                } else {
                    validatedData = null;
                    $('#btnUpload').prop('disabled', true);
                    
                    // 构建错误消息
                    var errorMsg = result.message || MSG.CHECK_FAILED;
                    
                    // 文件级错误
                    if (result.fileValidation && result.fileValidation.errors && result.fileValidation.errors.length > 0) {
                        errorMsg += '<hr>' + result.fileValidation.errors.join('<br>');
                    }
                    
                    // 兼容旧版 errors 字段
                    if (result.errors && result.errors.length > 0) {
                        errorMsg += '<hr>' + result.errors.join('<br>');
                    }
                    
                    showMessage(errorMsg, 'danger');
                    
                    // 如果有工作表数据，仍然显示（即使有错误）
                    if (result.computerOrders || result.customers) {
                        displayData(result);
                    } else {
                        $('#dataSection').hide();
                    }
                }
            },
            error: function(xhr, status, error) {
                $('#btnCheck').prop('disabled', false).html('<span class="glyphicon glyphicon-check"></span> ' + MSG.BTN_CHECK);
                var errorMsg = MSG.ERROR_SYSTEM;
                if (xhr.status === 404) {
                    errorMsg = MSG.ERROR_NOT_FOUND;
                } else if (xhr.status === 500) {
                    errorMsg = MSG.ERROR_SERVER_ERROR;
                } else if (xhr.status === 403) {
                    errorMsg = MSG.ERROR_FORBIDDEN;
                } else if (status === 'timeout') {
                    errorMsg = MSG.ERROR_TIMEOUT;
                } else {
                    errorMsg = formatMsg(MSG.ERROR_REQUEST_FAILED, xhr.status, (xhr.responseJSON ? xhr.responseJSON.message : error));
                }
                showMessage(errorMsg, 'danger');
            }
        });
    });

    // Upload button click
    $('#btnUpload').click(function() {
        if (!validatedData) {
            showMessage(MSG.CHECK_REQUIRE_FIRST, 'warning');
            return;
        }

        $('#btnUpload').prop('disabled', true).html('<span class="glyphicon glyphicon-refresh"></span> ' + MSG.BTN_UPLOADING);

        $.ajax({
            url: '/thymeleaf/excel-upload/upload',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({
                computerOrders: validatedData.computerOrders,
                customers: validatedData.customers
            }),
            success: function(result) {
                $('#btnUpload').prop('disabled', false).html('<span class="glyphicon glyphicon-upload"></span> ' + MSG.BTN_UPLOAD);
                if (result.valid) {
                    showMessage(result.message, 'success');
                    validatedData = null;
                    $('#dataSection').hide();
                    $('#excelFile').val('');
                } else {
                    showMessage(result.message, 'danger');
                }
            },
            error: function(xhr, status, error) {
                $('#btnUpload').prop('disabled', false).html('<span class="glyphicon glyphicon-upload"></span> ' + MSG.BTN_UPLOAD);
                showMessage(formatMsg(MSG.UPLOAD_FAILED, error), 'danger');
            }
        });
    });

    // Reset button click
    $('#btnReset').click(function() {
        $('#excelFile').val('');
        $('#resultSection').hide();
        $('#dataSection').hide();
        validatedData = null;
        $('#btnUpload').prop('disabled', true);
        $('#computerTableBody').empty();
        $('#customerTableBody').empty();
    });

    function showMessage(message, type) {
        $('#resultSection').show();
        $('#messageBox')
            .removeClass('alert-success alert-danger alert-warning alert-info')
            .addClass('alert-' + type)
            .html(message)
            .show();
    }

    function displayData(result) {
        $('#dataSection').show();
        $('#tabContent').show();

        // ===== 显示文件级信息 =====
        displayFileInfo(result.fileValidation);

        // ===== 显示电脑订单工作表 =====
        displaySheetData(
            result.computerOrdersSheet,
            result.computerOrders,
            'computer',
            '#computerSheetStatus',
            '#computerTableBody',
            function(order) {
                return '<tr class="' + (order.valid ? '' : 'error-row') + '">' +
                    '<td>' + order.rowNumber + '</td>' +
                    '<td>' + (order.brand || '') + '</td>' +
                    '<td>' + (order.price || '') + '</td>' +
                    '<td>' + (order.memorySize || '') + '</td>' +
                    '<td>' + (order.manufactureDate || '') + '</td>' +
                    '<td>' + (order.saleDate || '') + '</td>' +
                    '<td>' + buildStatusCell(order) + '</td>' +
                    '</tr>';
            }
        );

        // ===== 显示客户信息工作表 =====
        displaySheetData(
            result.customersSheet,
            result.customers,
            'customer',
            '#customerSheetStatus',
            '#customerTableBody',
            function(customer) {
                return '<tr class="' + (customer.valid ? '' : 'error-row') + '">' +
                    '<td>' + customer.rowNumber + '</td>' +
                    '<td>' + (customer.name || '') + '</td>' +
                    '<td>' + (customer.phone || '') + '</td>' +
                    '<td>' + (customer.computers || '') + '</td>' +
                    '<td>' + (customer.quantity || '') + '</td>' +
                    '<td>' + (customer.email || '') + '</td>' +
                    '<td>' + buildStatusCell(customer) + '</td>' +
                    '</tr>';
            }
        );
    }

    /**
     * 显示文件级信息
     */
    function displayFileInfo(fileValidation) {
        if (!fileValidation) return;

        var html = '<div class="alert alert-info" style="margin-top: 15px;">';
        html += '<strong><span class="glyphicon glyphicon-file"></span> 文件信息</strong><br>';
        html += '<small>';
        html += '工作表数量: <strong>' + (fileValidation.sheetCount || 0) + '</strong>';
        if (fileValidation.allSheetNames && fileValidation.allSheetNames.length > 0) {
            html += ' (' + fileValidation.allSheetNames.join(', ') + ')';
        }
        html += '</small>';
        html += '</div>';

        // 在 dataSection 开头插入
        $('#dataSection').prepend(html);
    }

    /**
     * 显示工作表数据（包含工作表状态和统计）
     */
    function displaySheetData(sheetValidation, dataList, sheetType, statusSelector, tableBodySelector, rowBuilder) {
        if (!sheetValidation) return;

        // 构建工作表状态 HTML
        var statusHtml = buildSheetStatusHtml(sheetValidation, dataList);
        $(statusSelector).html(statusHtml).show();

        // 显示表格数据
        var tableBody = $(tableBodySelector);
        tableBody.empty();

        if (dataList && dataList.length > 0) {
            $.each(dataList, function(i, item) {
                tableBody.append(rowBuilder(item));
            });
        } else {
            // 显示"无数据"提示
            var colCount = tableBody.closest('table').find('thead th').length;
            tableBody.append(
                '<tr><td colspan="' + colCount + '" class="text-center text-muted">' +
                '<em>无数据</em></td></tr>'
            );
        }
    }

    /**
     * 构建工作表状态 HTML（包含错误和统计）
     */
    function buildSheetStatusHtml(sheetValidation, dataList) {
        var html = '';

        // 工作表状态
        var statusClass = 'info';
        var statusIcon = 'info-sign';
        var statusText = '正常';

        if (sheetValidation.status === 'NOT_FOUND') {
            statusClass = 'danger';
            statusIcon = 'remove-circle';
            statusText = '未找到工作表';
        } else if (sheetValidation.status === 'EMPTY') {
            statusClass = 'warning';
            statusIcon = 'warning-sign';
            statusText = '工作表为空';
        } else if (sheetValidation.status === 'PARSE_ERROR') {
            statusClass = 'danger';
            statusIcon = 'exclamation-sign';
            statusText = '解析错误';
        } else if (sheetValidation.invalidRows > 0) {
            statusClass = 'warning';
            statusIcon = 'warning-sign';
            statusText = '部分数据有错误';
        } else if (sheetValidation.validRows > 0) {
            statusClass = 'success';
            statusIcon = 'ok-circle';
            statusText = '全部有效';
        }

        html += '<div class="alert alert-' + statusClass + '" style="margin-bottom: 10px;">';
        html += '<span class="glyphicon glyphicon-' + statusIcon + '"></span> ';
        html += '<strong>' + statusText + '</strong>';

        // 工作表错误信息
        if (sheetValidation.errors && sheetValidation.errors.length > 0) {
            html += '<br><small>';
            html += sheetValidation.errors.join('<br>');
            html += '</small>';
        }

        // 统计信息
        if (sheetValidation.totalRows > 0) {
            html += '<hr style="margin: 10px 0;">';
            html += '<div class="sheet-statistics">';
            html += '<span class="label label-default">共 ' + sheetValidation.totalRows + ' 行</span> ';
            html += '<span class="label label-success">有效 ' + sheetValidation.validRows + ' 行</span> ';
            html += '<span class="label label-danger">错误 ' + sheetValidation.invalidRows + ' 行</span>';
            html += '</div>';
        }

        html += '</div>';

        return html;
    }

    /**
     * 构建状态单元格（有效/无效标记 + 错误信息）
     */
    function buildStatusCell(item) {
        var statusBadge = item.valid
            ? '<span class="label label-success">' + MSG.STATUS_VALID + '</span>'
            : '<span class="label label-danger">' + MSG.STATUS_INVALID + '</span>';

        var errorMsg = item.errorMessage || '';
        if (errorMsg) {
            statusBadge += '<br><small class="error-message">' + errorMsg + '</small>';
        }

        return statusBadge;
    }
});
