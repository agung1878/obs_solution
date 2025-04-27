package com.obs.example.controller;

import com.obs.example.dto.*;
import com.obs.example.exception.BadRequestException;
import com.obs.example.exception.ResourceNotFoundException;
import com.obs.example.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<BaseResponseDto> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<OrderResponseDto> items = orderService.getAllOrders(pageable);
            return ResponseEntity.status(HttpStatus.OK).body(
                    BaseResponseDto.builder()
                            .responseCode("00")
                            .responseMessage("success")
                            .data(items)
                            .build()
            );
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    BaseResponseDto.builder()
                            .responseCode("500")
                            .responseMessage(e.getLocalizedMessage())
                            .build()
            );
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponseDto> getOrderId(@PathVariable String id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(
                    BaseResponseDto.builder()
                            .responseCode("00")
                            .responseMessage("Get order by id")
                            .data(orderService.getOrderById(id))
                            .build()
            );
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    BaseResponseDto.builder()
                            .responseCode("404")
                            .responseMessage("Order with order id: " + id + " not found")
                            .build()
            );
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    BaseResponseDto.builder()
                            .responseCode("500")
                            .responseMessage(e.getLocalizedMessage())
                            .build()
            );
        }
    }

    @PostMapping
    public ResponseEntity<BaseResponseDto> saveOrder(@RequestParam(required = false) String orderId, @Valid @RequestBody OrderDto orderDto) {
        try {
            orderService.saveOrder(orderId, orderDto);

            if (StringUtils.hasText(orderId)) {
                return ResponseEntity.status(HttpStatus.OK).body(
                    BaseResponseDto.builder()
                            .responseCode("00")
                            .responseMessage("Order updated successfully")
                            .build()
            );
            } else {
                return ResponseEntity.status(HttpStatus.CREATED).body(
                        BaseResponseDto.builder()
                                .responseCode("00")
                                .responseMessage("Order added successfully")
                                .build()
                );
            }
        } catch (BadRequestException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    BaseResponseDto.builder()
                            .responseCode("400")
                            .responseMessage(e.getLocalizedMessage())
                            .build()
            );
        } catch (ResourceNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    BaseResponseDto.builder()
                            .responseCode("404")
                            .responseMessage(e.getLocalizedMessage())
                            .build()
            );
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    BaseResponseDto.builder()
                            .responseCode("500")
                            .responseMessage(e.getLocalizedMessage())
                            .build()
            );
        }

    }


    @DeleteMapping("/delete")
    public ResponseEntity<BaseResponseDto> deleteItem(@RequestParam String orderNo) {
        try {
            orderService.deleteOrder(orderNo);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                    BaseResponseDto.builder()
                            .responseCode("00")
                            .responseMessage("Order deleted successfully")
                            .build()
            );

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    BaseResponseDto.builder()
                            .responseCode("404")
                            .responseMessage(e.getLocalizedMessage())
                            .build()
            );
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    BaseResponseDto.builder()
                            .responseCode("500")
                            .responseMessage(e.getLocalizedMessage())
                            .build()
            );

        }
    }
}