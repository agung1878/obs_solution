package com.obs.example.utils;

import com.obs.example.TestResultListener;
import com.obs.example.constant.InventoryType;
import com.obs.example.entity.Inventory;
import com.obs.example.entity.Item;
import com.obs.example.entity.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(TestResultListener.class)
class InventoryCalculationUtilsTest {

    private Item testItem;
    private Inventory incomingInventory;
    private Inventory outgoingInventory;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Test Item");
        testItem.setPrice(100);

        incomingInventory = new Inventory();
        incomingInventory.setId(1L);
        incomingInventory.setItem(testItem);
        incomingInventory.setQty(10);
        incomingInventory.setType(InventoryType.T);

        outgoingInventory = new Inventory();
        outgoingInventory.setId(2L);
        outgoingInventory.setItem(testItem);
        outgoingInventory.setQty(5);
        outgoingInventory.setType(InventoryType.W);

        testOrder = new Order();
        testOrder.setOrderNo("O1");
        testOrder.setItem(testItem);
        testOrder.setQty(3);
    }

    @Test
    void calculateStockQuantity_EmptyLists() {
        int result = InventoryCalculationUtils.calculateStockQuantity(
                Collections.emptyList(),
                Collections.emptyList()
        );

        assertEquals(0, result);
    }

    @Test
    void calculateStockQuantity_OnlyIncomingInventory() {
        int result = InventoryCalculationUtils.calculateStockQuantity(
                Collections.singletonList(incomingInventory),
                Collections.emptyList()
        );

        assertEquals(10, result);
    }

    @Test
    void calculateStockQuantity_OnlyOutgoingInventory() {
        int result = InventoryCalculationUtils.calculateStockQuantity(
                Collections.singletonList(outgoingInventory),
                Collections.emptyList()
        );

        assertEquals(-5, result);
    }

    @Test
    void calculateStockQuantity_OnlyOrders() {
        int result = InventoryCalculationUtils.calculateStockQuantity(
                Collections.emptyList(),
                Collections.singletonList(testOrder)
        );

        assertEquals(-3, result);
    }

    @Test
    void calculateStockQuantity_MixedInventories() {
        int result = InventoryCalculationUtils.calculateStockQuantity(
                Arrays.asList(incomingInventory, outgoingInventory),
                Collections.emptyList()
        );

        assertEquals(5, result); // 10 (incoming) - 5 (outgoing)
    }

    @Test
    void calculateStockQuantity_MixedInventoriesAndOrders() {
        int result = InventoryCalculationUtils.calculateStockQuantity(
                Arrays.asList(incomingInventory, outgoingInventory),
                Collections.singletonList(testOrder)
        );

        assertEquals(2, result); // 10 (incoming) - 5 (outgoing) - 3 (order)
    }

    @Test
    void calculateStockQuantity_MultipleOrders() {
        Order secondOrder = new Order();
        secondOrder.setOrderNo("O2");
        secondOrder.setItem(testItem);
        secondOrder.setQty(2);

        int result = InventoryCalculationUtils.calculateStockQuantity(
                Collections.singletonList(incomingInventory),
                Arrays.asList(testOrder, secondOrder)
        );

        assertEquals(5, result); // 10 (incoming) - 3 (first order) - 2 (second order)
    }

    @Test
    void calculateStockQuantity_MultipleInventoriesOfSameType() {
        Inventory anotherIncoming = new Inventory();
        anotherIncoming.setId(3L);
        anotherIncoming.setItem(testItem);
        anotherIncoming.setQty(5);
        anotherIncoming.setType(InventoryType.T);

        int result = InventoryCalculationUtils.calculateStockQuantity(
                Arrays.asList(incomingInventory, anotherIncoming),
                Collections.emptyList()
        );

        assertEquals(15, result); // 10 + 5
    }
}