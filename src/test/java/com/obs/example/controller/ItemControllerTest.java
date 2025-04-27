package com.obs.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.obs.example.TestResultListener;
import com.obs.example.dto.BaseResponseDto;
import com.obs.example.dto.ItemDto;
import com.obs.example.dto.ItemResponseDto;
import com.obs.example.entity.Item;
import com.obs.example.exception.BadRequestException;
import com.obs.example.exception.ResourceNotFoundException;
import com.obs.example.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith({MockitoExtension.class, TestResultListener.class})
public class ItemControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ItemService itemService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ItemDto itemDto;
    private ItemResponseDto itemResponseDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ItemController(itemService)).build();

        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Test Item");
        itemDto.setPrice(100);

        itemResponseDto = new ItemResponseDto();
        itemResponseDto.setId(1L);
        itemResponseDto.setName("Test Item");
        itemResponseDto.setPrice(100);
        itemResponseDto.setStock(10);
    }

    @Test
    void getAllItems_Success() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        List<ItemResponseDto> content = Collections.singletonList(itemResponseDto);
        Page<ItemResponseDto> page = new PageImpl<>(content, pageable, content.size());
        when(itemService.getAllItems(pageable)).thenReturn(page);

        mockMvc.perform(get("/api/items?page=0&size=10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode").value("00"))
                .andExpect(jsonPath("$.responseMessage").value("success"))
                .andExpect(jsonPath("$.data.content[0].id").value(1L))
                .andExpect(jsonPath("$.data.content[0].name").value("Test Item"))
                .andExpect(jsonPath("$.data.content[0].price").value(100))
                .andExpect(jsonPath("$.data.content[0].stock").value(10));

        verify(itemService).getAllItems(pageable);
    }

    @Test
    void getItemById_Success() throws Exception {
        Item item = new Item();
        item.setId(1L);
        when(itemService.getItemById(1L)).thenReturn(item);

        mockMvc.perform(get("/api/items/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode").value("00"))
                .andExpect(jsonPath("$.responseMessage").value("Get item by id"));

        verify(itemService).getItemById(1L);
    }

    @Test
    void getItemById_NotFound() throws Exception {
        when(itemService.getItemById(1L)).thenThrow(new ResourceNotFoundException("Item with id: 1 not found"));

        mockMvc.perform(get("/api/items/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.responseCode").value("404"))
                .andExpect(jsonPath("$.responseMessage").value("Item with id: 1 not found"));

        verify(itemService).getItemById(1L);
    }

    @Test
    void saveItem_Success() throws Exception {
        doNothing().when(itemService).saveItem(any(), any());

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.responseCode").value("00"))
                .andExpect(jsonPath("$.responseMessage").value("Item added successfully"));

        verify(itemService).saveItem(null, itemDto);
    }

    @Test
    void saveItem_BadRequest() throws Exception {
        doThrow(new BadRequestException("Invalid item data")).when(itemService).saveItem(any(), any());

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.responseCode").value("400"))
                .andExpect(jsonPath("$.responseMessage").value("Invalid item data"));

        verify(itemService).saveItem(null, itemDto);
    }

    @Test
    void saveItem_InvalidDto() throws Exception {
        ItemDto invalidDto = new ItemDto();
        invalidDto.setPrice(-1);

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).saveItem(any(), any());
    }

    @Test
    void deleteItem_Success() throws Exception {
        doNothing().when(itemService).deleteItem(1L);

        mockMvc.perform(delete("/api/items/delete?id=1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.responseCode").value("00"))
                .andExpect(jsonPath("$.responseMessage").value("Item deleted successfully"));

        verify(itemService).deleteItem(1L);
    }

    @Test
    void deleteItem_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Item not found with ID: 1")).when(itemService).deleteItem(1L);

        mockMvc.perform(delete("/api/items/delete?id=1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.responseCode").value("404"))
                .andExpect(jsonPath("$.responseMessage").value("Item not found with ID: 1"));

        verify(itemService).deleteItem(1L);
    }
}