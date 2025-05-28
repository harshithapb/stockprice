package org.cg.stockdemo.HoldingManagement.stockportfoliomonitoringapp.holding.controller;
// src/main/java/org/cg/stockportfoliomonitoringapp/holding/controller/HoldingController.java
package org.cg.stockportfoliomonitoringapp.holding.controller;

import lombok.RequiredArgsConstructor; // Lombok for constructor injection
import org.cg.stockportfoliomonitoringapp.holding.dto.HoldingGainDetailsResponse; // For POST response
import org.cg.stockportfoliomonitoringapp.holding.dto.HoldingResponseDTO;     // For GET /holdings/{userId} response
import org.cg.stockportfoliomonitoringapp.holding.dto.HoldingUpdateRequest;
import org.cg.stockportfoliomonitoringapp.holding.dto.SimulatedHoldingRequest;
import org.cg.stockportfoliomonitoringapp.holding.dto.SimulatedHoldingResponse;
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

    private final HoldingService holdingService; // Injected via @RequiredArgsConstructor

    /**
     * POST endpoint to create or update a user's stock holding.
     * URL: POST http://localhost:8080/holdings/{userId}
     * Request Body: { "stockSymbol": "AAPL", "quantity": 10, "buyPrice": 150.0 }
     * (stockName is no longer needed in request body, as it's fetched from Xano)
     */
    @PostMapping("/{userId}")
    public ResponseEntity<?> createOrUpdateHolding(
            @PathVariable Long userId,
            @RequestBody HoldingUpdateRequest request) {
        try {
            // Call service with simplified parameters
            HoldingGainDetailsResponse response = holdingService.createOrUpdateHolding(
                    userId,
                    request.getStockSymbol(),
                    request.getQuantity(),
                    request.getBuyPrice()
            );
            return ResponseEntity.ok(response); // Return 200 OK with detailed response
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404 Not Found
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // 400 Bad Request
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage()); // 500 Internal Server Error
        }
    }

    /**
     * GET endpoint to retrieve ALL available stock details from Xano API.
     * URL: GET http://localhost:8080/holdings/stocks/all
     */
    @GetMapping("/stocks/all")
    public ResponseEntity<List<XanoStockDto>> getAllXanoStockDetails() {
        List<XanoStockDto> allStocks = holdingService.getAllXanoStockDetails();
        return ResponseEntity.ok(allStocks);
    }

    /**
     * GET endpoint to retrieve stock holdings for a specific user.
     * URL: GET http://localhost:8080/holdings/{userId}
     * Returns a consolidated response with total portfolio value.
     */
    @GetMapping("{userId}")
    public ResponseEntity<HoldingResponseDTO> getHoldingsForUser(@PathVariable Long userId) {
        HoldingResponseDTO userHoldings = holdingService.getHoldingsForUser(userId);
        // If holdings list is empty, the DTO will reflect that, returning 200 OK with empty list
        return ResponseEntity.ok(userHoldings);
    }

    /**
     * POST endpoint for simulating a stock holding (kept for context).
     * URL: POST http://localhost:8080/holdings/simulateHolding
     */
    @PostMapping("/simulateHolding")
    public ResponseEntity<SimulatedHoldingResponse> simulateHolding(@RequestBody SimulatedHoldingRequest request) {
        SimulatedHoldingResponse response = holdingService.simulateHolding(request);
        return ResponseEntity.ok(response);
    }
}