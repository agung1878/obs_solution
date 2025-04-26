package com.obs.example.service;

import com.obs.example.constant.InventoryType;
import com.obs.example.dto.ItemResponseDto;
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
import com.obs.example.utils.InventoryCalculationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final InventoryRepository inventoryRepository;

    public Page<OrderResponseDto> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(this::toOrderResponse);
    }

    public Order getOrderById(String id) {

        Optional<Order> order = orderRepository.findByOrderNo(id);

        if (order.isEmpty()) {
            throw new ResourceNotFoundException("Item with order id: " +id +" not found");
        }

        return order.get();
    }

    public Order saveOrder(String orderNo, OrderDto orderDto) {

        Order order;

        if (orderNo == null || orderNo.isEmpty()) {
            order = new Order();
        } else {
            order = orderRepository.findByOrderNo(orderNo)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderNo));
        }

        Item item = itemRepository.findById(orderDto.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with ID: " + orderDto.getItemId()));

        if (!item.getPrice().equals(orderDto.getPrice())) {
            throw new BadRequestException("Invalid price for order for item: " + orderDto.getItemId());
        }



        if (getItemStock(orderDto.getItemId()) < orderDto.getQuantity()) {
            throw new BadRequestException("Insufficient stock for item ID: " + orderDto.getItemId() + ". Available: " + getItemStock(orderDto.getItemId()));
        }

        order.setOrderNo(orderDto.getOrderNo());
        order.setItem(item);
        order.setQuantity(orderDto.getQuantity());
        order.setPrice(orderDto.getPrice());

        return orderRepository.save(order);
    }

    public void deleteOrder(String id) {
        Optional<Order> order = orderRepository.findById(id);
        if (order.isEmpty()) {
            throw new ResourceNotFoundException("Order with id " + id + " not found");
        }
        orderRepository.delete(order.get());
    }

    private OrderResponseDto toOrderResponse(Order order) {
        OrderResponseDto response = new OrderResponseDto();
        response.setOrderNo(order.getOrderNo());
        response.setItemId(order.getItem().getId());
        response.setPrice(order.getPrice());
        response.setQuantity(order.getQuantity());
        return response;
    }

    public int getItemStock(Long itemId) {
        List<Inventory> inventories = inventoryRepository.findByItemId(itemId);
        List<Order> orders = orderRepository.findByItemId(itemId);
        return InventoryCalculationUtils.calculateStockQuantity(inventories, orders);
    }
}
