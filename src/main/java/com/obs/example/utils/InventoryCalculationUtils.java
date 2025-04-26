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
        int currentQuantity = 0;

        currentQuantity = calculateInventoryAdjustments(currentQuantity, inventories);

        currentQuantity = subtractOrders(currentQuantity, orders);

        return currentQuantity;
    }

    private static int calculateInventoryAdjustments(int currentQuantity, List<Inventory> inventories) {
        for (Inventory inventory : inventories) {
            if (inventory.getType() == InventoryType.T) {
                currentQuantity += inventory.getQty();
            } else if (inventory.getType() == InventoryType.W) {
                currentQuantity -= inventory.getQty();
            }
        }
        return currentQuantity;
    }

    private static int subtractOrders(int currentQuantity, List<Order> orders) {
        for (Order order : orders) {
            currentQuantity -= order.getQuantity();
        }
        return currentQuantity;
    }
}
