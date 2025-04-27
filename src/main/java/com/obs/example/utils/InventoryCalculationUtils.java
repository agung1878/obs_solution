package com.obs.example.utils;

import com.obs.example.constant.InventoryType;
import com.obs.example.entity.Inventory;
import com.obs.example.entity.Order;

import java.util.List;

public final class InventoryCalculationUtils {

    private InventoryCalculationUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static int calculateStockQuantity(List<Inventory> inventories, List<Order> orders) {
        int incomingTotal = inventories.stream()
                .filter(inv -> inv.getType() == InventoryType.T)
                .mapToInt(Inventory::getQty)
                .sum();

        int outgoingTotal = inventories.stream()
                .filter(inv -> inv.getType() == InventoryType.W)
                .mapToInt(Inventory::getQty)
                .sum();

        int ordersTotal = orders.stream()
                .mapToInt(Order::getQty)
                .sum();

        return incomingTotal - outgoingTotal - ordersTotal;
    }
}
