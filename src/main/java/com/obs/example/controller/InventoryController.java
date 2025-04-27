package com.obs.example.controller;

import com.obs.example.dto.BaseResponseDto;
import com.obs.example.dto.InventoryDto;
import com.obs.example.dto.InventoryResponseDto;
import com.obs.example.exception.BadRequestException;
import com.obs.example.exception.ResourceNotFoundException;
import com.obs.example.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<BaseResponseDto> getAllInventories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<InventoryResponseDto> inventories = inventoryService.getAllInventories(pageable);
            return ResponseEntity.status(HttpStatus.OK).body(
                    BaseResponseDto.builder()
                            .responseCode("00")
                            .responseMessage("success")
                            .data(inventories)
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
    public ResponseEntity<BaseResponseDto> getInventoryById(@PathVariable Long id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(
                    BaseResponseDto.builder()
                            .responseCode("00")
                            .responseMessage("Get inventory by id")
                            .data(inventoryService.getInventoryById(id))
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
    public ResponseEntity<BaseResponseDto> saveInventory(@RequestParam(required = false) Long id, @Valid @RequestBody InventoryDto inventoryDto) {
        try {

            inventoryService.saveInventory(id, inventoryDto);
            if (id != null) {
                return ResponseEntity.status(HttpStatus.OK).body(
                        BaseResponseDto.builder()
                                .responseCode("00")
                                .responseMessage("Inventory updated successfully")
                                .build()
                );
            } else {
                return ResponseEntity.status(HttpStatus.CREATED).body(
                        BaseResponseDto.builder()
                                .responseCode("00")
                                .responseMessage("Inventory added successfully")
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
    public ResponseEntity<BaseResponseDto> deleteInventory(@RequestParam Long id) {
        try {
            inventoryService.deleteInventory(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                    BaseResponseDto.builder()
                            .responseCode("00")
                            .responseMessage("Inventory deleted successfully")
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
