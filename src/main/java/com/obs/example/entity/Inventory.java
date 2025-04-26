package com.obs.example.entity;

import com.obs.example.constant.InventoryType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Entity
@Data
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @PositiveOrZero(message = "Quantity must be zero or positive")
    private int qty;

    @NotNull(message = "Type is mandatory")
    private InventoryType type;

    @ManyToOne
    @JoinColumn(name = "item_id")
    @NotNull(message = "Item is mandatory")
    private Item item;
}