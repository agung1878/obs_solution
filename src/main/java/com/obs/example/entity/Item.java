package com.obs.example.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;


@Entity
@Data
public class Item {

    @Id
    private Long id;

    @NotBlank(message = "Name is mandatory")
    private String name;

    @Positive(message = "Price must be positive")
    private Integer price;
}