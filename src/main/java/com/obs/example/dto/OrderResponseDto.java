package com.obs.example.dto;

import com.obs.example.entity.Item;
import lombok.Data;

@Data
public class OrderResponseDto {
    private String orderNo;
    private int qty;
    private double price;
    private Long itemId;

}
