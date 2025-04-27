package com.obs.example.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.obs.example.TestResultListener;
import com.obs.example.dto.BaseResponseDto;
import com.obs.example.dto.OrderDto;
import com.obs.example.dto.OrderResponseDto;
import com.obs.example.entity.Order;
import com.obs.example.exception.BadRequestException;
import com.obs.example.exception.ResourceNotFoundException;
import com.obs.example.service.OrderService;
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
public class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private OrderDto orderDto;
    private OrderResponseDto orderResponseDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new OrderController(orderService)).build();

        orderDto = new OrderDto();
        orderDto.setOrderNo("ORD001");
        orderDto.setItemId(1L);
        orderDto.setQty(5);
        orderDto.setPrice(100);

        orderResponseDto = new OrderResponseDto();
        orderResponseDto.setOrderNo("ORD001");
        orderResponseDto.setItemId(1L);
        orderResponseDto.setQty(5);
        orderResponseDto.setPrice(100.0);
    }

    @Test
    void getAllOrders_Success() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        List<OrderResponseDto> content = Collections.singletonList(orderResponseDto);
        Page<OrderResponseDto> page = new PageImpl<>(content, pageable, content.size());
        when(orderService.getAllOrders(pageable)).thenReturn(page);

        mockMvc.perform(get("/api/orders?page=0&size=10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode").value("00"))
                .andExpect(jsonPath("$.responseMessage").value("success"))
                .andExpect(jsonPath("$.data.content[0].orderNo").value("ORD001"))
                .andExpect(jsonPath("$.data.content[0].itemId").value(1L))
                .andExpect(jsonPath("$.data.content[0].qty").value(5))
                .andExpect(jsonPath("$.data.content[0].price").value(100.0));

        verify(orderService).getAllOrders(pageable);
    }

    @Test
    void getOrderById_Success() throws Exception {
        Order order = new Order();
        order.setOrderNo("ORD001");
        when(orderService.getOrderById("ORD001")).thenReturn(order);

        mockMvc.perform(get("/api/orders/ORD001")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode").value("00"))
                .andExpect(jsonPath("$.responseMessage").value("Get order by id"));

        verify(orderService).getOrderById("ORD001");
    }

    @Test
    void getOrderById_NotFound() throws Exception {
        when(orderService.getOrderById("ORD001")).thenThrow(new ResourceNotFoundException("Order with order id: ORD001 not found"));

        mockMvc.perform(get("/api/orders/ORD001")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.responseCode").value("404"))
                .andExpect(jsonPath("$.responseMessage").value("Order with order id: ORD001 not found"));

        verify(orderService).getOrderById("ORD001");
    }

    @Test
    void saveOrder_Success() throws Exception {
        Order order = new Order();
        order.setOrderNo("ORD001");
        when(orderService.saveOrder(any(), any())).thenReturn(order);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.responseCode").value("00"))
                .andExpect(jsonPath("$.responseMessage").value("Order added successfully"));

        verify(orderService).saveOrder(null, orderDto);
    }

    @Test
    void saveOrder_BadRequest() throws Exception {
        when(orderService.saveOrder(any(), any())).thenThrow(new BadRequestException("Insufficient stock"));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.responseCode").value("400"))
                .andExpect(jsonPath("$.responseMessage").value("Insufficient stock"));

        verify(orderService).saveOrder(null, orderDto);
    }

    @Test
    void saveOrder_InvalidDto() throws Exception {
        OrderDto invalidDto = new OrderDto();
        invalidDto.setQty(-1);
        invalidDto.setPrice(-1);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).saveOrder(any(), any());
    }

    @Test
    void deleteOrder_Success() throws Exception {
        doNothing().when(orderService).deleteOrder("ORD001");

        mockMvc.perform(delete("/api/orders/delete")
                        .param("orderNo", "ORD001")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.responseCode").value("00"))
                .andExpect(jsonPath("$.responseMessage").value("Order deleted successfully"));

        verify(orderService).deleteOrder("ORD001");
    }

    @Test
    void deleteOrder_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Order with id ORD001 not found")).when(orderService).deleteOrder("ORD001");

        mockMvc.perform(delete("/api/orders/delete")
                        .param("orderNo", "ORD001")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.responseCode").value("404"))
                .andExpect(jsonPath("$.responseMessage").value("Order with id ORD001 not found"));

        verify(orderService).deleteOrder("ORD001");
    }
}