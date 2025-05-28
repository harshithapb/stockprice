// src/main/java/org/cg/stockportfoliomonitoringapp/holding/controller/HoldingController.java
package org.cg.stockportfoliomonitoringapp.holding.controller;

import lombok.RequiredArgsConstructor;
import org.cg.stockportfoliomonitoringapp.holding.dto.HoldingGainDetailsResponse;
import org.cg.stockportfoliomonitoringapp.holding.dto.HoldingResponseDTO;
import org.cg.stockportfoliomonitoringapp.holding.dto.HoldingUpdateRequest;
import org.cg.stockportfoliomonitoringapp.holding.dto.XanoStockDto;
import org.cg.stockportfoliomonitoringapp.holding.service.HoldingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/holdings") // Base path for all endpoints in this controller
@RequiredArgsConstructor // Lombok: Generates constructor for final HoldingService
public class HoldingController {

    private final HoldingService holdingService; // Injected by Spring

    @PostMapping("/{userId}")
    public ResponseEntity<?> createOrUpdateHolding(
            @PathVariable Long userId,
            @RequestBody HoldingUpdateRequest request) {
        try {
            HoldingGainDetailsResponse response = holdingService.createOrUpdateHolding(
                    userId,
                    request.getStockSymbol(),
                    request.getQuantity(),
                    request.getBuyPrice()
            );
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }
    @GetMapping("/stocks/all")
    public ResponseEntity<List<XanoStockDto>> getAllXanoStockDetails() {
        List<XanoStockDto> allStocks = holdingService.getAllXanoStockDetails();
        return ResponseEntity.ok(allStocks);
    }

    @GetMapping("{userId}")
    public ResponseEntity<HoldingResponseDTO> getHoldingsForUser(@PathVariable Long userId) {
        HoldingResponseDTO userHoldings = holdingService.getHoldingsForUser(userId);
        return ResponseEntity.ok(userHoldings);
    }
}