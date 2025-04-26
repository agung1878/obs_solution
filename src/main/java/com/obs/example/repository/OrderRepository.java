package com.obs.example.repository;

import com.obs.example.entity.Inventory;
import com.obs.example.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {

    Optional<Order> findByOrderNo(String orderId);

    List<Order> findByItemId(Long itemId);

    List<Order> findByItemIdIn(List<Long> itemIds);

    boolean existsByItemId(Long itemId);

}
