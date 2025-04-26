package com.obs.example.service;

import com.obs.example.constant.InventoryType;
import com.obs.example.dto.ItemDto;
import com.obs.example.dto.ItemResponseDto;
import com.obs.example.entity.Inventory;
import com.obs.example.entity.Item;
import com.obs.example.entity.Order;
import com.obs.example.exception.BadRequestException;
import com.obs.example.exception.ReferentialIntegrityViolationException;
import com.obs.example.exception.ResourceNotFoundException;
import com.obs.example.repository.InventoryRepository;
import com.obs.example.repository.ItemRepository;
import com.obs.example.repository.OrderRepository;
import com.obs.example.utils.InventoryCalculationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;

    public Page<ItemResponseDto> getAllItems(Pageable pageable) {
        return itemRepository.findAll(pageable)
                .map(this::toItemResponse);
    }

    public Item getItemById(Long id) {

        Optional<Item> item = itemRepository.findById(id);

        if (item.isEmpty()) {
            throw new ResourceNotFoundException("Item with id: " +id +" not found");
        }

        return item.get();
    }

    @Transactional
    public void saveItem(Long id, ItemDto itemDto) {

        Item item;

        if (id == null) {
            item = new Item();
            item.setId(itemDto.getId());
        } else {
            item = itemRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Item not found with ID: " + id));
            item.setId(id);
        }


        item.setName(itemDto.getName());
        item.setPrice(itemDto.getPrice());

        itemRepository.save(item);
    }


    public void deleteItem(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with ID: " + id));

        if (inventoryRepository.existsByItemId(id)) {
            throw new ReferentialIntegrityViolationException(
                    "Item has associated inventory records");
        }

        if (orderRepository.existsByItemId(id)) {
            throw new ReferentialIntegrityViolationException(
                    "Item has associated inventory records");
        }

        itemRepository.delete(item);
    }


    private ItemResponseDto toItemResponse(Item item) {
        ItemResponseDto response = new ItemResponseDto();
        response.setId(item.getId());
        response.setName(item.getName());
        response.setPrice(item.getPrice());

        response.setStock(getItemStock(item.getId()));


        return response;
    }

    public int getItemStock(Long itemId) {
        List<Inventory> inventories = inventoryRepository.findByItemId(itemId);
        List<Order> orders = orderRepository.findByItemId(itemId);
        return InventoryCalculationUtils.calculateStockQuantity(inventories, orders);
    }

}
