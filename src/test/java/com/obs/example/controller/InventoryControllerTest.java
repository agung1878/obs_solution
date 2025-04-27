package com.obs.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.obs.example.TestResultListener;
import com.obs.example.constant.InventoryType;
import com.obs.example.dto.BaseResponseDto;
import com.obs.example.dto.InventoryDto;
import com.obs.example.dto.InventoryResponseDto;
import com.obs.example.entity.Inventory;
import com.obs.example.exception.BadRequestException;
import com.obs.example.exception.ResourceNotFoundException;
import com.obs.example.service.InventoryService;
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
public class InventoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private InventoryService inventoryService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private InventoryDto inventoryDto;
    private InventoryResponseDto inventoryResponseDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new InventoryController(inventoryService)).build();

        inventoryDto = new InventoryDto();
        inventoryDto.setItemId(1L);
        inventoryDto.setQty(10);
        inventoryDto.setType(InventoryType.T);

        inventoryResponseDto = new InventoryResponseDto();
        inventoryResponseDto.setId(1L);
        inventoryResponseDto.setItemId(1L);
        inventoryResponseDto.setQty(10);
        inventoryResponseDto.setType(InventoryType.T);
    }

    @Test
    void getAllInventories_Success() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        List<InventoryResponseDto> content = Collections.singletonList(inventoryResponseDto);
        Page<InventoryResponseDto> page = new PageImpl<>(content, pageable, content.size());
        when(inventoryService.getAllInventories(pageable)).thenReturn(page);

        mockMvc.perform(get("/api/inventories?page=0&size=10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode").value("00"))
                .andExpect(jsonPath("$.responseMessage").value("success"))
                .andExpect(jsonPath("$.data.content[0].id").value(1L))
                .andExpect(jsonPath("$.data.content[0].itemId").value(1L))
                .andExpect(jsonPath("$.data.content[0].qty").value(10))
                .andExpect(jsonPath("$.data.content[0].type").value("T"));

        verify(inventoryService).getAllInventories(pageable);
    }

    @Test
    void getInventoryById_Success() throws Exception {
        Inventory inventory = new Inventory();
        inventory.setId(1L);
        when(inventoryService.getInventoryById(1L)).thenReturn(inventory);

        mockMvc.perform(get("/api/inventories/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode").value("00"))
                .andExpect(jsonPath("$.responseMessage").value("Get inventory by id"));

        verify(inventoryService).getInventoryById(1L);
    }

    @Test
    void getInventoryById_NotFound() throws Exception {
        when(inventoryService.getInventoryById(1L)).thenThrow(new ResourceNotFoundException("Inventory with id 1 not found"));

        mockMvc.perform(get("/api/inventories/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.responseCode").value("404"))
                .andExpect(jsonPath("$.responseMessage").value("Item with id: 1 not found"));

        verify(inventoryService).getInventoryById(1L);
    }

    @Test
    void saveInventory_Success() throws Exception {
        doNothing().when(inventoryService).saveInventory(any(), any());

        mockMvc.perform(post("/api/inventories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inventoryDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.responseCode").value("00"))
                .andExpect(jsonPath("$.responseMessage").value("Inventory added successfully"));

        verify(inventoryService).saveInventory(null, inventoryDto);
    }

    @Test
    void saveInventory_BadRequest() throws Exception {
        doThrow(new BadRequestException("Insufficient stock")).when(inventoryService).saveInventory(any(), any());

        mockMvc.perform(post("/api/inventories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inventoryDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.responseCode").value("400"))
                .andExpect(jsonPath("$.responseMessage").value("Insufficient stock"));

        verify(inventoryService).saveInventory(null, inventoryDto);
    }

    @Test
    void saveInventory_InvalidDto() throws Exception {
        InventoryDto invalidDto = new InventoryDto();
        invalidDto.setQty(-1);

        mockMvc.perform(post("/api/inventories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(inventoryService, never()).saveInventory(any(), any());
    }

    @Test
    void deleteInventory_Success() throws Exception {
        doNothing().when(inventoryService).deleteInventory(1L);

        mockMvc.perform(delete("/api/inventories/delete")
                        .param("id", Long.toString(1L))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.responseCode").value("00"))
                .andExpect(jsonPath("$.responseMessage").value("Inventory deleted successfully"));

        verify(inventoryService).deleteInventory(1L);
    }

    @Test
    void deleteInventory_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Inventory with id 1 not found")).when(inventoryService).deleteInventory(1L);

        mockMvc.perform(delete("/api/inventories/delete")
                        .param("id", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.responseCode").value("404"))
                .andExpect(jsonPath("$.responseMessage").value("Inventory with id 1 not found"));

        verify(inventoryService).deleteInventory(1L);
    }
}