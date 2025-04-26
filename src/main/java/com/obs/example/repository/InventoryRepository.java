package com.obs.example.repository;

import com.obs.example.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    List<Inventory> findByItemId(Long itemId);
    boolean existsByItemId(Long itemId);
    List<Inventory> findByItemIdIn(List<Long> itemIds);

}
