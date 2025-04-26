package com.obs.example.service;

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
import com.obs.example.utils.InventoryCalculationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;

    public Page<InventoryResponseDto> getAllInventories(Pageable pageable) {
        return inventoryRepository.findAll(pageable)
                .map(this::toInventoryResponse);
    }

    public Inventory getInventoryById(Long id) {

        Optional<Inventory> inventory = inventoryRepository.findById(id);
        if (inventory.isEmpty()) {
            throw new ResourceNotFoundException("Inventory with id " + id + " not found");
        }

        return inventory.get();
    }

    public void saveInventory(Long id, InventoryDto inventoryDto) {

        Inventory inventory;

        if (id == null) {
            inventory = new Inventory();

            if (inventoryDto.getType().equals(InventoryType.W) && getItemStock(inventoryDto.getItemId()) < inventoryDto.getQuantity()) {
                throw new BadRequestException("Insufficient stock for withdrawal item with id " + inventoryDto.getItemId() + " stock = " + getItemStock(inventoryDto.getItemId()));
            }
        } else {
            inventory = inventoryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with ID: " + id));
        }

        Item item = itemRepository.findById(inventoryDto.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with ID: " + inventoryDto.getItemId()));


        inventory.setItem(item);
        inventory.setType(inventoryDto.getType());
        inventory.setQty(inventoryDto.getQuantity());

        inventoryRepository.save(inventory);
    }

    public void deleteInventory(Long id) {
       Optional<Inventory> inventory = inventoryRepository.findById(id);
       if (inventory.isEmpty()) {
           throw new ResourceNotFoundException("Inventory with id " + id + " not found");
       }

       if (inventory.get().getType().equals(InventoryType.T)) {
           if (getItemStock(inventory.get().getItem().getId()) < inventory.get().getQty()) {
               throw new BadRequestException("Cannot delete inventory with id " + id + " because the stock will be minus!");
           }
       }

       inventoryRepository.delete(inventory.get());
    }

    private InventoryResponseDto toInventoryResponse(Inventory inventory) {
        InventoryResponseDto response = new InventoryResponseDto();
        response.setId(inventory.getId());
        response.setQty(inventory.getQty());
        response.setType(inventory.getType());
        response.setItemId(inventory.getItem().getId());
        return response;
    }

    public int getItemStock(Long itemId) {
        List<Inventory> inventories = inventoryRepository.findByItemId(itemId);
        List<Order> orders = orderRepository.findByItemId(itemId);
        return InventoryCalculationUtils.calculateStockQuantity(inventories, orders);
    }
}
