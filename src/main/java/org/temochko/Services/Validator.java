package org.temochko.Services;

import org.temochko.DTOs.ProductCreateDto;
import org.temochko.DTOs.ProductUpdateDto;

import java.util.ArrayList;
import java.util.List;

public class Validator {
    public record ValidationError(String errorMessage, String memberName) {}

    public static List<ValidationError> validate(ProductCreateDto product) {
        var errors = new ArrayList<ValidationError>();
        errors = validateProduct(product.getName(), product.getPrice(), product.getQuantity());
        return errors;
    }

    public static List<ValidationError> validate(ProductUpdateDto product) {
        var errors = new ArrayList<ValidationError>();
        errors = validateProduct(product.getName(), product.getPrice(), product.getQuantity());
        return errors;
    }

    public static ArrayList<ValidationError> validateProduct(String name, double price, int quantity) {
        var errors = new ArrayList<ValidationError>();
        if (name == null || name.isEmpty() || name.length() < 2 || name.length() > 100) {
            errors.add(new ValidationError("Product name cannot be empty / less than 2 / bigger than 100 chars", "productName"));
        }

        if (price < 0) {
            errors.add(new ValidationError("Product price cannot be negative", "price"));
        }

        if (quantity < 0) {
            errors.add(new ValidationError("Product quantity cannot be negative", "quantity"));
        }
        return errors;
    }

}