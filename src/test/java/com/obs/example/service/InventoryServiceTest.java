package com.obs.example.service;

import com.obs.example.TestResultListener;
import com.obs.example.constant.InventoryType;
import com.obs.example.dto.InventoryDto;
import com.obs.example.dto.InventoryResponseDto;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, TestResultListener.class})
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Item testItem;
    private Inventory testInventory;
    private InventoryDto testInventoryDto;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Test Item");
        testItem.setPrice(100);

        testInventory = new Inventory();
        testInventory.setId(1L);
        testInventory.setItem(testItem);
        testInventory.setQty(10);
        testInventory.setType(InventoryType.T);

        testInventoryDto = new InventoryDto();
        testInventoryDto.setItemId(1L);
        testInventoryDto.setQty(10);
        testInventoryDto.setType(InventoryType.T);

        testOrder = new Order();
        testOrder.setOrderNo("O1");
        testOrder.setItem(testItem);
        testOrder.setQty(5);
    }

    @Test
    void getAllInventories_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Inventory> page = new PageImpl<>(Collections.singletonList(testInventory));
        when(inventoryRepository.findAll(pageable)).thenReturn(page);

        Page<InventoryResponseDto> result = inventoryService.getAllInventories(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testInventory.getId(), result.getContent().get(0).getId());
        assertEquals(testInventory.getQty(), result.getContent().get(0).getQty());
        verify(inventoryRepository).findAll(pageable);
    }

    @Test
    void getInventoryById_Success() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));

        Inventory result = inventoryService.getInventoryById(1L);

        assertNotNull(result);
        assertEquals(testInventory.getId(), result.getId());
        verify(inventoryRepository).findById(1L);
    }

    @Test
    void getInventoryById_NotFound() {
        when(inventoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                inventoryService.getInventoryById(999L)
        );
        verify(inventoryRepository).findById(999L);
    }

    @Test
    void saveInventory_NewInventory_Success() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        assertDoesNotThrow(() ->
                inventoryService.saveInventory(null, testInventoryDto)
        );

        verify(itemRepository).findById(1L);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void saveInventory_WithdrawalWithInsufficientStock() {
        testInventoryDto.setType(InventoryType.W);
        testInventoryDto.setQty(15);

        when(inventoryRepository.findByItemId(1L)).thenReturn(Collections.singletonList(testInventory));
        when(orderRepository.findByItemId(1L)).thenReturn(Collections.singletonList(testOrder));

        assertThrows(BadRequestException.class, () ->
                inventoryService.saveInventory(null, testInventoryDto)
        );

        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void saveInventory_UpdateExisting_Success() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        assertDoesNotThrow(() ->
                inventoryService.saveInventory(1L, testInventoryDto)
        );

        verify(inventoryRepository).findById(1L);
        verify(itemRepository).findById(1L);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void deleteInventory_Success() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.findByItemId(1L)).thenReturn(Arrays.asList(testInventory));
        when(orderRepository.findByItemId(1L)).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() ->
                inventoryService.deleteInventory(1L)
        );

        verify(inventoryRepository).findById(1L);
        verify(inventoryRepository).delete(testInventory);
    }

    @Test
    void deleteInventory_NotFound() {
        when(inventoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                inventoryService.deleteInventory(999L)
        );

        verify(inventoryRepository).findById(999L);
        verify(inventoryRepository, never()).delete(any());
    }

    @Test
    void deleteInventory_InsufficientStock() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.findByItemId(1L)).thenReturn(Collections.singletonList(testInventory));
        when(orderRepository.findByItemId(1L)).thenReturn(Collections.singletonList(testOrder));

        assertThrows(BadRequestException.class, () ->
                inventoryService.deleteInventory(1L)
        );

        verify(inventoryRepository).findById(1L);
        verify(inventoryRepository, never()).delete(any());
    }

    @Test
    void getItemStock_Success() {
        when(inventoryRepository.findByItemId(1L)).thenReturn(Collections.singletonList(testInventory));
        when(orderRepository.findByItemId(1L)).thenReturn(Collections.singletonList(testOrder));

        int result = inventoryService.getItemStock(1L);

        assertEquals(5, result); // 10 (inventory) - 5 (order) = 5
        verify(inventoryRepository).findByItemId(1L);
        verify(orderRepository).findByItemId(1L);
    }
}