package com.obs.example.dto;

import com.obs.example.entity.Item;
import lombok.Data;

@Data
public class OrderResponseDto {
    private String orderNo;
    private int quantity;
    private double price;
    private Long itemId;

}
