package com.obs.example.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.obs.example.dto.BaseResponseDto;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.lang.module.ResolutionException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<BaseResponseDto> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        if (ex.getCause() instanceof ConstraintViolationException) {
            ConstraintViolationException cve = (ConstraintViolationException) ex.getCause();

            if ("23503".equals(cve.getSQLState())) {
                String constraintName = cve.getConstraintName();
                String message = getFriendlyMessage(constraintName);

                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(new BaseResponseDto(
                                "REFERENTIAL_INTEGRITY_VIOLATION",
                                message
                        ));
            }
        }

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new BaseResponseDto(
                        "DATA_INTEGRITY_VIOLATION",
                        "Operation could not be completed due to data integrity constraints"
                ));
    }

    private String getFriendlyMessage(String constraintName) {
        if (constraintName == null) {
            return "Operation would violate database referential integrity rules";
        }

        // Map constraint names to friendly messages
        if (constraintName.contains("INVENTORY_ITEM_FK")) {
            return "Cannot complete operation because this item has inventory records. " +
                    "Please delete or reassign all inventory entries first.";
        } else if (constraintName.contains("ORDER_ITEM_FK")) {
            return "Cannot delete item because it appears in existing orders. " +
                    "Please cancel or modify the orders first.";
        }

        return String.format(
                "Operation violates database constraint: %s. " +
                        "Please ensure all related records are properly handled.",
                constraintName
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BaseResponseDto> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex) {

        String parameterName = ex.getName();
        Class<?> requiredType = ex.getRequiredType();
        String inputValue = ex.getValue() != null ? ex.getValue().toString() : "null";

        String errorMessage = getString(requiredType, inputValue, parameterName);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new BaseResponseDto("INVALID_PARAMETER", errorMessage));
    }

    private static String getString(Class<?> requiredType, String inputValue, String parameterName) {
        String errorMessage;

        if (requiredType != null) {
            errorMessage = String.format(
                    "Invalid value '%s' for parameter '%s'. Expected type: %s",
                    inputValue,
                    parameterName,
                    requiredType.getSimpleName()
            );
        } else {
            errorMessage = String.format(
                    "Invalid value '%s' for parameter '%s'",
                    inputValue,
                    parameterName
            );
        }
        return errorMessage;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponseDto> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex) {

        String errorMessage = "Malformed JSON request";
        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) cause;
            String fieldPath = ife.getPath().stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .collect(Collectors.joining("."));

            String actualValue = ife.getValue() != null ? ife.getValue().toString() : "null";
            String targetType = ife.getTargetType() != null ?
                    ife.getTargetType().getSimpleName() : "unknown type";

            errorMessage = String.format(
                    "Invalid value '%s' for field '%s'. Expected type: %s",
                    actualValue,
                    fieldPath,
                    targetType
            );
        }
        else if (cause != null && cause.getMessage() != null) {
            errorMessage = cause.getMessage();

            if (errorMessage.startsWith("JSON parse error: ")) {
                errorMessage = errorMessage.substring("JSON parse error: ".length());
            }
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new BaseResponseDto("INVALID_REQUEST_BODY", errorMessage));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<BaseResponseDto> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.badRequest()
                .body(new BaseResponseDto("BAD_REQUEST", ex.getMessage()));
    }

    @ExceptionHandler(ResolutionException.class)
    public ResponseEntity<BaseResponseDto> handleResourceNotFound(ResolutionException ex) {
        return ResponseEntity.badRequest()
                .body(new BaseResponseDto("NOT_FOUND", ex.getMessage()));
    }
}

