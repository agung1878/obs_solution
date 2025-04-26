package com.obs.example.dto;

import com.obs.example.constant.InventoryType;
import lombok.Data;

@Data
public class InventoryResponseDto {

    private Long id;

    private int qty;

    private InventoryType type;

    private Long itemId;

}
