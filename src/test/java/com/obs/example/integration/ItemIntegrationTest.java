package com.obs.example.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.obs.example.dto.ItemDto;
import com.obs.example.entity.Item;
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
class ItemIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ItemRepository itemRepository;

    private Item testItem;
    private ItemDto testItemDto;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();

        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Test Item");
        testItem.setPrice(100);

        testItemDto = new ItemDto();
        testItemDto.setId(1L);
        testItemDto.setName("Test Item");
        testItemDto.setPrice(100);
    }

    @Test
    void createItem_Success() throws Exception {
        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testItemDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.responseCode", is("00")))
                .andExpect(jsonPath("$.responseMessage", is("Item added successfully")));
    }

    @Test
    void getItem_Success() throws Exception {
        Item savedItem = itemRepository.save(testItem);

        mockMvc.perform(get("/api/items/{id}", savedItem.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode", is("00")))
                .andExpect(jsonPath("$.data.id", is(savedItem.getId().intValue())))
                .andExpect(jsonPath("$.data.name", is(savedItem.getName())));
    }

    @Test
    void getAllItems_Success() throws Exception {
        itemRepository.save(testItem);

        mockMvc.perform(get("/api/items")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode", is("00")))
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.content[0].name", is("Test Item")));
    }

    @Test
    void updateItem_Success() throws Exception {
        Item savedItem = itemRepository.save(testItem);
        testItemDto.setName("Updated Item");

        mockMvc.perform(post("/api/items")
                        .param("id", savedItem.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testItemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode", is("00")))
                .andExpect(jsonPath("$.responseMessage", is("Item updated successfully")));
    }

    @Test
    void deleteItem_Success() throws Exception {
        Item savedItem = itemRepository.save(testItem);

        mockMvc.perform(delete("/api/items/delete")
                        .param("id", savedItem.getId().toString()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/items/{id}", savedItem.getId()))
                .andExpect(status().isNotFound());
    }
}