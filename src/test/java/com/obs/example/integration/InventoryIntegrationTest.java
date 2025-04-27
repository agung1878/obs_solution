package com.obs.example.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.obs.example.constant.InventoryType;
import com.obs.example.dto.InventoryDto;
import com.obs.example.entity.Inventory;
import com.obs.example.entity.Item;
import com.obs.example.repository.InventoryRepository;
import com.obs.example.repository.ItemRepository;
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
class InventoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ItemRepository itemRepository;

    private Item testItem;
    private Inventory testInventory;
    private InventoryDto testInventoryDto;

    @BeforeEach
    void setUp() {
        // Clean up existing data
        inventoryRepository.deleteAll();
        itemRepository.deleteAll();

        // Set up test item
        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Test Item");
        testItem.setPrice(100);
        testItem = itemRepository.save(testItem);

        // Set up test inventory DTO for POST/PUT tests
        testInventoryDto = new InventoryDto();
        testInventoryDto.setId(1L);
        testInventoryDto.setItemId(testItem.getId());
        testInventoryDto.setQty(10);
        testInventoryDto.setType(InventoryType.T);

        // Set up test inventory for GET/UPDATE/DELETE tests
        testInventory = new Inventory();
        testInventory.setId(1L);
        testInventory.setItem(testItem);
        testInventory.setQty(10);
        testInventory.setType(InventoryType.T);
    }

    @Test
    void createInventory_Success() throws Exception {
        mockMvc.perform(post("/api/inventories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testInventoryDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.responseCode", is("00")))
                .andExpect(jsonPath("$.responseMessage", is("Inventory added successfully")));
    }

    @Test
    void getInventory_Success() throws Exception {
        Inventory savedInventory = inventoryRepository.save(testInventory);

        mockMvc.perform(get("/api/inventories/{id}", savedInventory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode", is("00")))
                .andExpect(jsonPath("$.data.id", is(savedInventory.getId().intValue())))
                .andExpect(jsonPath("$.data.qty", is(savedInventory.getQty())));
    }

    @Test
    void getAllInventories_Success() throws Exception {
        inventoryRepository.save(testInventory);

        mockMvc.perform(get("/api/inventories")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode", is("00")))
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.content[0].qty", is(10)));
    }

    @Test
    void updateInventory_Success() throws Exception {
        Inventory savedInventory = inventoryRepository.save(testInventory);
        testInventoryDto.setQty(20);

        mockMvc.perform(post("/api/inventories")
                        .param("id", savedInventory.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testInventoryDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode", is("00")))
                .andExpect(jsonPath("$.responseMessage", is("Inventory updated successfully")));
    }

    @Test
    void deleteInventory_Success() throws Exception {
        Inventory savedInventory = inventoryRepository.save(testInventory);

        mockMvc.perform(delete("/api/inventories/detele")
                        .param("id", savedInventory.getId().toString()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/inventories/{id}", savedInventory.getId()))
                .andExpect(status().isNotFound());
    }
}
