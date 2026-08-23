package net.hka.examples.thymeleaf.business.dto;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {
    private boolean valid = true;
    private String message;
    private List<ExcelComputerOrder> computerOrders = new ArrayList<>();
    private List<ExcelCustomer> customers = new ArrayList<>();
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

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public void addError(String error) {
        this.errors.add(error);
        this.valid = false;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean hasDataErrors() {
        return computerOrders.stream().anyMatch(o -> !o.isValid()) ||
               customers.stream().anyMatch(c -> !c.isValid());
    }

    public int getTotalErrorCount() {
        int count = errors.size();
        count += computerOrders.stream().filter(o -> !o.isValid()).count();
        count += customers.stream().filter(c -> !c.isValid()).count();
        return count;
    }
}
