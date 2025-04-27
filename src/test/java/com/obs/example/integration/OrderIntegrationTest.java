package com.obs.example.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.obs.example.constant.InventoryType;
import com.obs.example.dto.OrderDto;
import com.obs.example.entity.Inventory;
import com.obs.example.entity.Item;
import com.obs.example.entity.Order;
import com.obs.example.repository.InventoryRepository;
import com.obs.example.repository.ItemRepository;
import com.obs.example.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Item testItem;
    private Inventory testInventory;
    private Order testOrder;
    private OrderDto testOrderDto;

    @BeforeEach
    void setUp() {
        // Clean up existing data
        orderRepository.deleteAll();
        inventoryRepository.deleteAll();
        itemRepository.deleteAll();

        // Create and save test item
        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Test Item");
        testItem.setPrice(100);
        testItem = itemRepository.save(testItem);

        // Create and save test inventory
        testInventory = new Inventory();
        testInventory.setId(1L);
        testInventory.setItem(testItem);
        testInventory.setQty(10);
        testInventory.setType(InventoryType.T);
        testInventory = inventoryRepository.save(testInventory);

        // Create test order
        testOrder = new Order();
        testOrder.setOrderNo("O1");
        testOrder.setItem(testItem);
        testOrder.setQty(5);
        testOrder.setPrice(100);

        // Create test order DTO
        testOrderDto = new OrderDto();
        testOrderDto.setOrderNo("O1");
        testOrderDto.setItemId(1L);  // Use the manually set ID
        testOrderDto.setQty(5);
        testOrderDto.setPrice(100);
    }

    @Test
    void createOrder_Success() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testOrderDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.responseCode", is("00")))
                .andExpect(jsonPath("$.responseMessage", is("Order added successfully")));
    }

    @Test
    void getOrder_Success() throws Exception {
        Order savedOrder = orderRepository.save(testOrder);

        mockMvc.perform(get("/api/orders/{id}", savedOrder.getOrderNo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode", is("00")))
                .andExpect(jsonPath("$.data.orderNo", is(savedOrder.getOrderNo())))
                .andExpect(jsonPath("$.data.qty", is(savedOrder.getQty())));
    }

    @Test
    void getAllOrders_Success() throws Exception {
        orderRepository.save(testOrder);

        mockMvc.perform(get("/api/orders")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode", is("00")))
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.content[0].orderNo", is("O1")));
    }

    @Test
    void updateOrder_Success() throws Exception {
        Order savedOrder = orderRepository.save(testOrder);
        testOrderDto.setQty(3);

        mockMvc.perform(post("/api/orders")
                        .param("orderNo", savedOrder.getOrderNo())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testOrderDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode", is("00")))
                .andExpect(jsonPath("$.responseMessage", is("Order updated successfully")));
    }

    @Test
    void deleteOrder_Success() throws Exception {
        Order savedOrder = orderRepository.save(testOrder);

        mockMvc.perform(delete("/api/orders/delete", savedOrder.getOrderNo())
                .param("orderNo", savedOrder.getOrderNo()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/orders/{orderNo}", savedOrder.getOrderNo()))
                .andExpect(status().isNotFound());
    }

}