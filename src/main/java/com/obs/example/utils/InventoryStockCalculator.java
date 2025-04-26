package com.obs.example.utils;

import com.obs.example.entity.Inventory;
import com.obs.example.entity.Order;
import com.obs.example.repository.InventoryRepository;
import com.obs.example.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InventoryStockCalculator {

    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;

    public int calculateCurrentStock(Long itemId) {
        List<Inventory> inventories = inventoryRepository.findByItemId(itemId);
        List<Order> orders = orderRepository.findByItemId(itemId);

        return InventoryCalculationUtils.calculateStockQuantity(inventories, orders);
    }

    public Map<Long, Integer> calculateStockForItems(List<Long> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // Safely group inventories by item ID
        Map<Long, List<Inventory>> inventoriesByItem = inventoryRepository.findByItemIdIn(itemIds)
                .stream()
                .filter(inv -> inv.getItem() != null) // Filter out null items
                .collect(Collectors.groupingBy(
                        inv -> inv.getItem().getId(),
                        Collectors.toList()
                ));

        // Safely group orders by item ID
        Map<Long, List<Order>> ordersByItem = orderRepository.findByItemIdIn(itemIds)
                .stream()
                .filter(order -> order.getItem() != null) // Filter out null items
                .collect(Collectors.groupingBy(
                        order -> order.getItem().getId(),
                        Collectors.toList()
                ));

        // Calculate stock for each item
        return itemIds.stream()
                .collect(Collectors.toMap(
                        itemId -> itemId,
                        itemId -> InventoryCalculationUtils.calculateStockQuantity(
                                inventoriesByItem.getOrDefault(itemId, Collections.emptyList()),
                                ordersByItem.getOrDefault(itemId, Collections.emptyList())
                        )
                ));
    }
}
