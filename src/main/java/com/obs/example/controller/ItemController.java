package com.obs.example.controller;


import com.obs.example.dto.BaseResponseDto;
import com.obs.example.dto.ItemDto;
import com.obs.example.dto.ItemResponseDto;
import com.obs.example.entity.Item;
import com.obs.example.exception.BadRequestException;
import com.obs.example.exception.ResourceNotFoundException;
import com.obs.example.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<BaseResponseDto> getAllItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ItemResponseDto> items = itemService.getAllItems(pageable);
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
    public ResponseEntity<BaseResponseDto> getItemById(@PathVariable Long id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(
                    BaseResponseDto.builder()
                            .responseCode("00")
                            .responseMessage("Get item by id")
                            .data(itemService.getItemById(id))
                            .build()
            );
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    BaseResponseDto.builder()
                            .responseCode("404")
                            .responseMessage("Item with id: " + id + " not found")
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
    public ResponseEntity<BaseResponseDto> saveItem(@RequestParam(required = false) Long id, @Valid @RequestBody ItemDto itemDto) {
        try {
            itemService.saveItem(id, itemDto);
            if (id != null) {
                return ResponseEntity.status(HttpStatus.OK).body(
                        BaseResponseDto.builder()
                                .responseCode("00")
                                .responseMessage("Item updated successfully")
                                .build()
                );
            } else {
                return ResponseEntity.status(HttpStatus.CREATED).body(
                        BaseResponseDto.builder()
                                .responseCode("00")
                                .responseMessage("Item added successfully")
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
    public ResponseEntity<BaseResponseDto> deleteItem(@RequestParam Long id) {
        try {
            itemService.deleteItem(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                    BaseResponseDto.builder()
                            .responseCode("00")
                            .responseMessage("Item deleted successfully")
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