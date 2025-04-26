package com.obs.example.dto;

import com.obs.example.constant.InventoryType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class InventoryDto {

    private Long id;

    @NotNull(message = "Item ID is mandatory")
    private Long itemId;

    @PositiveOrZero(message = "Quantity must be zero or positive")
    private int quantity;

    @NotNull(message = "Type is mandatory")
    private InventoryType type;
}
