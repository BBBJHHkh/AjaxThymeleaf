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
            return '请先选择一个 Excel 文件';
        }
        if (file.size > MAX_FILE_SIZE) {
            var sizeMB = (file.size / 1024 / 1024).toFixed(2);
            return '文件过大（' + sizeMB + ' MB），最大允许 20 MB';
        }
        var name = (file.name || '').toLowerCase();
        var extOk = ALLOWED_EXTENSIONS.some(function(ext) {
            return name.endsWith(ext);
        });
        if (!extOk) {
            return '只支持 .xlsx 或 .xls 格式的 Excel 文件';
        }
        return null;
    }

    // Check button click
    $('#btnCheck').click(function() {
        var fileInput = document.getElementById('excelFile');
        var file = fileInput.files[0];

        if (!file) {
            showMessage('请先选择一个 Excel 文件', 'warning');
            return;
        }

        var preCheckError = validateFile(file);
        if (preCheckError) {
            showMessage(preCheckError, 'warning');
            return;
        }

        var formData = new FormData();
        formData.append('file', file);

        $('#btnCheck').prop('disabled', true).html('<span class="glyphicon glyphicon-refresh"></span> Checking...');

        $.ajax({
            url: '/thymeleaf/excel-upload/check',
            type: 'POST',
            data: formData,
            processData: false,
            contentType: false,
            success: function(result) {
                $('#btnCheck').prop('disabled', false).html('<span class="glyphicon glyphicon-check"></span> Check');

                if (result.valid && !result.hasErrors) {
                    validatedData = result;
                    $('#btnUpload').prop('disabled', false);
                    showMessage(result.message || '校验通过！', 'success');
                    displayData(result);
                } else {
                    validatedData = null;
                    $('#btnUpload').prop('disabled', true);
                    var errorMsg = '校验失败！';
                    if (result.errors && result.errors.length > 0) {
                        errorMsg += '<br>' + result.errors.join('<br>');
                    }
                    showMessage(errorMsg, 'danger');
                    $('#dataSection').hide();
                }
            },
            error: function(xhr, status, error) {
                $('#btnCheck').prop('disabled', false).html('<span class="glyphicon glyphicon-check"></span> Check');
                var errorMsg = '系统错误：';
                if (xhr.status === 404) {
                    errorMsg = '接口不存在 (404)，请联系管理员检查服务器配置';
                } else if (xhr.status === 500) {
                    errorMsg = '服务器内部错误 (500)，请查看服务器日志';
                } else if (xhr.status === 403) {
                    errorMsg = '没有权限访问此接口 (403)';
                } else if (status === 'timeout') {
                    errorMsg = '请求超时，请检查网络连接';
                } else {
                    errorMsg = '请求失败 (' + xhr.status + '): ' + (xhr.responseJSON ? xhr.responseJSON.message : error);
                }
                showMessage(errorMsg, 'danger');
            }
        });
    });

    // Upload button click
    $('#btnUpload').click(function() {
        if (!validatedData) {
            showMessage('请先进行 Check 操作', 'warning');
            return;
        }

        $('#btnUpload').prop('disabled', true).html('<span class="glyphicon glyphicon-refresh"></span> Uploading...');

        $.ajax({
            url: '/thymeleaf/excel-upload/upload',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({
                computerOrders: validatedData.computerOrders,
                customers: validatedData.customers
            }),
            success: function(result) {
                $('#btnUpload').prop('disabled', false).html('<span class="glyphicon glyphicon-upload"></span> Upload');
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
                $('#btnUpload').prop('disabled', false).html('<span class="glyphicon glyphicon-upload"></span> Upload');
                showMessage('上传失败: ' + error, 'danger');
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

        // Display computer orders
        var computerBody = $('#computerTableBody');
        computerBody.empty();
        if (result.computerOrders && result.computerOrders.length > 0) {
            $.each(result.computerOrders, function(i, order) {
                var rowClass = order.valid ? '' : 'error-row';
                var statusBadge = order.valid
                    ? '<span class="label label-success">有效</span>'
                    : '<span class="label label-danger">无效</span>';
                var errorMsg = order.errorMessage || '';

                computerBody.append(
                    '<tr class="' + rowClass + '">' +
                    '<td>' + order.rowNumber + '</td>' +
                    '<td>' + (order.brand || '') + '</td>' +
                    '<td>' + (order.price || '') + '</td>' +
                    '<td>' + (order.memorySize || '') + '</td>' +
                    '<td>' + (order.manufactureDate || '') + '</td>' +
                    '<td>' + (order.saleDate || '') + '</td>' +
                    '<td>' + statusBadge + (errorMsg ? '<br><small class="error-message">' + errorMsg + '</small>' : '') + '</td>' +
                    '</tr>'
                );
            });
        }

        // Display customers
        var customerBody = $('#customerTableBody');
        customerBody.empty();
        if (result.customers && result.customers.length > 0) {
            $.each(result.customers, function(i, customer) {
                var rowClass = customer.valid ? '' : 'error-row';
                var statusBadge = customer.valid
                    ? '<span class="label label-success">有效</span>'
                    : '<span class="label label-danger">无效</span>';
                var errorMsg = customer.errorMessage || '';

                customerBody.append(
                    '<tr class="' + rowClass + '">' +
                    '<td>' + customer.rowNumber + '</td>' +
                    '<td>' + (customer.name || '') + '</td>' +
                    '<td>' + (customer.phone || '') + '</td>' +
                    '<td>' + (customer.computers || '') + '</td>' +
                    '<td>' + (customer.quantity || '') + '</td>' +
                    '<td>' + (customer.email || '') + '</td>' +
                    '<td>' + statusBadge + (errorMsg ? '<br><small class="error-message">' + errorMsg + '</small>' : '') + '</td>' +
                    '</tr>'
                );
            });
        }
    }
});
