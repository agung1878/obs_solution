package com.obs.example.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Entity
@Data
@Table(name = "Orders")
public class Order {

    @Id
    private String orderNo;

    @Positive(message = "Quantity must be positive")
    private int quantity;

    @Positive(message = "Price must be positive")
    private Integer price;

    @ManyToOne
    @JoinColumn(name = "item_id")
    @NotNull(message = "Item is mandatory")
    private Item item;
}