package com.obs.example.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class OrderDto {
    private String orderNo;

    @NotNull(message = "Item ID is mandatory")
    private Long itemId;

    @Positive(message = "Quantity must be positive")
    private int quantity;

    @Positive(message = "Price must be positive")
    private Integer price;
}
