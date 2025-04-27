package com.obs.example.service;

import com.obs.example.TestResultListener;
import com.obs.example.constant.InventoryType;
import com.obs.example.dto.ItemDto;
import com.obs.example.dto.ItemResponseDto;
import com.obs.example.entity.Inventory;
import com.obs.example.entity.Item;
import com.obs.example.entity.Order;
import com.obs.example.exception.ReferentialIntegrityViolationException;
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
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ItemService itemService;

    private Item testItem;
    private ItemDto testItemDto;
    private Inventory testInventory;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Test Item");
        testItem.setPrice(100);

        testItemDto = new ItemDto();
        testItemDto.setId(1L);
        testItemDto.setName("Test Item");
        testItemDto.setPrice(100);

        testInventory = new Inventory();
        testInventory.setId(1L);
        testInventory.setItem(testItem);
        testInventory.setQty(10);
        testInventory.setType(InventoryType.T);

        testOrder = new Order();
        testOrder.setOrderNo("O1");
        testOrder.setItem(testItem);
        testOrder.setQty(5);
    }

    @Test
    void getAllItems_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> page = new PageImpl<>(Collections.singletonList(testItem));
        when(itemRepository.findAll(pageable)).thenReturn(page);
        when(inventoryRepository.findByItemId(1L)).thenReturn(Collections.singletonList(testInventory));
        when(orderRepository.findByItemId(1L)).thenReturn(Collections.singletonList(testOrder));

        Page<ItemResponseDto> result = itemService.getAllItems(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testItem.getId(), result.getContent().getFirst().getId());
        assertEquals(testItem.getName(), result.getContent().getFirst().getName());
        assertEquals(5, result.getContent().getFirst().getStock());
        verify(itemRepository).findAll(pageable);
    }

    @Test
    void getItemStock_Success() {
        when(inventoryRepository.findByItemId(1L)).thenReturn(Collections.singletonList(testInventory));
        when(orderRepository.findByItemId(1L)).thenReturn(Collections.singletonList(testOrder));

        int result = itemService.getItemStock(1L);

        assertEquals(5, result);

        verify(inventoryRepository).findByItemId(1L);
        verify(orderRepository).findByItemId(1L);
    }

    @Test
    void getItemById_Success() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        Item result = itemService.getItemById(1L);

        assertNotNull(result);
        assertEquals(testItem.getId(), result.getId());
        assertEquals(testItem.getName(), result.getName());
        verify(itemRepository).findById(1L);
    }

    @Test
    void getItemById_NotFound() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                itemService.getItemById(999L)
        );
        verify(itemRepository).findById(999L);
    }

    @Test
    void saveItem_NewItem_Success() {
        when(itemRepository.save(any(Item.class))).thenReturn(testItem);

        assertDoesNotThrow(() ->
                itemService.saveItem(null, testItemDto)
        );

        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void saveItem_UpdateExisting_Success() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemRepository.save(any(Item.class))).thenReturn(testItem);

        assertDoesNotThrow(() ->
                itemService.saveItem(1L, testItemDto)
        );

        verify(itemRepository).findById(1L);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void saveItem_UpdateNonExisting() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                itemService.saveItem(999L, testItemDto)
        );

        verify(itemRepository).findById(999L);
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void deleteItem_Success() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(inventoryRepository.existsByItemId(1L)).thenReturn(false);
        when(orderRepository.existsByItemId(1L)).thenReturn(false);

        assertDoesNotThrow(() ->
                itemService.deleteItem(1L)
        );

        verify(itemRepository).findById(1L);
        verify(itemRepository).delete(testItem);
    }

    @Test
    void deleteItem_NotFound() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                itemService.deleteItem(999L)
        );

        verify(itemRepository).findById(999L);
        verify(itemRepository, never()).delete(any());
    }

    @Test
    void deleteItem_WithInventoryRecords() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(inventoryRepository.existsByItemId(1L)).thenReturn(true);

        assertThrows(ReferentialIntegrityViolationException.class, () ->
                itemService.deleteItem(1L)
        );

        verify(itemRepository).findById(1L);
        verify(itemRepository, never()).delete(any());
    }

    @Test
    void deleteItem_WithOrderRecords() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(inventoryRepository.existsByItemId(1L)).thenReturn(false);
        when(orderRepository.existsByItemId(1L)).thenReturn(true);

        assertThrows(ReferentialIntegrityViolationException.class, () ->
                itemService.deleteItem(1L)
        );

        verify(itemRepository).findById(1L);
        verify(itemRepository, never()).delete(any());
    }
}