package com.testassignment.bank.exception;

import lombok.Data;

import java.util.List;

@Data
public class ValidationErrorResponse {

    private final String message;
    private List<String> details;
    private int statusCode;

    public ValidationErrorResponse(String message, List<String> details, int value) {
        this.message = message;
        this.details = details;
        this.statusCode = value;
    }
}
