package com.obs.example.service;

import com.obs.example.TestResultListener;
import com.obs.example.constant.InventoryType;
import com.obs.example.dto.OrderDto;
import com.obs.example.dto.OrderResponseDto;
import com.obs.example.entity.Inventory;
import com.obs.example.entity.Item;
import com.obs.example.entity.Order;
import com.obs.example.exception.BadRequestException;
import com.obs.example.exception.ResourceNotFoundException;
import com.obs.example.repository.InventoryRepository;
import com.obs.example.repository.ItemRepository;
import com.obs.example.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, TestResultListener.class})
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private OrderService orderService;

    private Item testItem;
    private Order testOrder;
    private OrderDto testOrderDto;
    private Inventory testInventory;

    @BeforeEach
    void setUp() {
        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Test Item");
        testItem.setPrice(100);

        testOrder = new Order();
        testOrder.setOrderNo("O1");
        testOrder.setItem(testItem);
        testOrder.setQty(5);
        testOrder.setPrice(100);

        testOrderDto = new OrderDto();
        testOrderDto.setOrderNo("O1");
        testOrderDto.setItemId(1L);
        testOrderDto.setQty(5);
        testOrderDto.setPrice(100);

        testInventory = new Inventory();
        testInventory.setId(1L);
        testInventory.setItem(testItem);
        testInventory.setQty(10);
        testInventory.setType(InventoryType.T);
    }

    @Test
    void getAllOrders_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> page = new PageImpl<>(Collections.singletonList(testOrder));
        when(orderRepository.findAll(pageable)).thenReturn(page);

        Page<OrderResponseDto> result = orderService.getAllOrders(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testOrder.getOrderNo(), result.getContent().getFirst().getOrderNo());
        verify(orderRepository).findAll(pageable);
    }

    @Test
    void getOrderById_Success() {
        when(orderRepository.findByOrderNo("O1")).thenReturn(Optional.of(testOrder));

        Order result = orderService.getOrderById("O1");

        assertNotNull(result);
        assertEquals(testOrder.getOrderNo(), result.getOrderNo());
        verify(orderRepository).findByOrderNo("O1");
    }

    @Test
    void getOrderById_NotFound() {
        when(orderRepository.findByOrderNo("INVALID")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                orderService.getOrderById("INVALID")
        );
        verify(orderRepository).findByOrderNo("INVALID");
    }

    @Test
    void saveOrder_NewOrder_Success() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(inventoryRepository.findByItemId(1L)).thenReturn(Collections.singletonList(testInventory));
        when(orderRepository.findByItemId(1L)).thenReturn(Collections.emptyList());
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.saveOrder(null, testOrderDto);

        assertNotNull(result);
        assertEquals(testOrderDto.getOrderNo(), result.getOrderNo());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void saveOrder_UpdateExisting_Success() {
        when(orderRepository.findByOrderNo("O1")).thenReturn(Optional.of(testOrder));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(inventoryRepository.findByItemId(1L)).thenReturn(Collections.singletonList(testInventory));
        when(orderRepository.findByItemId(1L)).thenReturn(Collections.singletonList(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.saveOrder("O1", testOrderDto);

        assertNotNull(result);
        assertEquals(testOrderDto.getOrderNo(), result.getOrderNo());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void saveOrder_InvalidPrice() {
        testOrderDto.setPrice(200); // Different price than item
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        assertThrows(BadRequestException.class, () ->
                orderService.saveOrder(null, testOrderDto)
        );
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void saveOrder_InsufficientStock() {
        testOrderDto.setQty(15); // More than available stock
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(inventoryRepository.findByItemId(1L)).thenReturn(Collections.singletonList(testInventory));
        when(orderRepository.findByItemId(1L)).thenReturn(Collections.emptyList());

        assertThrows(BadRequestException.class, () ->
                orderService.saveOrder(null, testOrderDto)
        );
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void deleteOrder_Success() {
        when(orderRepository.findById("O1")).thenReturn(Optional.of(testOrder));

        assertDoesNotThrow(() ->
                orderService.deleteOrder("O1")
        );

        verify(orderRepository).delete(testOrder);
    }

    @Test
    void deleteOrder_NotFound() {
        when(orderRepository.findById("INVALID")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                orderService.deleteOrder("INVALID")
        );
        verify(orderRepository, never()).delete(any());
    }

    @Test
    void getItemStock_Success() {
        when(inventoryRepository.findByItemId(1L)).thenReturn(Collections.singletonList(testInventory));
        when(orderRepository.findByItemId(1L)).thenReturn(Collections.singletonList(testOrder));

        int result = orderService.getItemStock(1L);

        assertEquals(5, result); // 10 (inventory) - 5 (order)
        verify(inventoryRepository).findByItemId(1L);
        verify(orderRepository).findByItemId(1L);
    }
}