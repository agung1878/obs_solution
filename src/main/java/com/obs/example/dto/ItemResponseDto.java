package com.obs.example.dto;

import lombok.Data;

@Data
public class ItemResponseDto {
    private Long id;
    private String name;
    private Integer price;
    private Integer stock;
}
