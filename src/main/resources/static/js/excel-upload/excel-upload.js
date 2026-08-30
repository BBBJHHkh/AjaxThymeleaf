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

                if (result.valid && !result.hasErrors) {
                    validatedData = result;
                    $('#btnUpload').prop('disabled', false);
                    showMessage(result.message || MSG.CHECK_PASSED, 'success');
                    displayData(result);
                } else {
                    validatedData = null;
                    $('#btnUpload').prop('disabled', true);
                    var errorMsg = MSG.CHECK_FAILED;
                    if (result.errors && result.errors.length > 0) {
                        errorMsg += '<br>' + result.errors.join('<br>');
                    }
                    showMessage(errorMsg, 'danger');
                    $('#dataSection').hide();
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

        // Display computer orders
        var computerBody = $('#computerTableBody');
        computerBody.empty();
        if (result.computerOrders && result.computerOrders.length > 0) {
            $.each(result.computerOrders, function(i, order) {
                var rowClass = order.valid ? '' : 'error-row';
                var statusBadge = order.valid
                    ? '<span class="label label-success">' + MSG.STATUS_VALID + '</span>'
                    : '<span class="label label-danger">' + MSG.STATUS_INVALID + '</span>';
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
                    ? '<span class="label label-success">' + MSG.STATUS_VALID + '</span>'
                    : '<span class="label label-danger">' + MSG.STATUS_INVALID + '</span>';
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
