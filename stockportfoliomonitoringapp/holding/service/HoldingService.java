// src/main/java/org/cg/stockportfoliomonitoringapp/holding/service/HoldingService.java
package org.cg.stockportfoliomonitoringapp.holding.service;

import lombok.RequiredArgsConstructor; // For constructor injection
import org.cg.stockportfoliomonitoringapp.holding.dto.HoldingGainDetailsResponse;
import org.cg.stockportfoliomonitoringapp.holding.dto.HoldingResponseDTO;
import org.cg.stockportfoliomonitoringapp.holding.dto.HoldingStatusDTO;
import org.cg.stockportfoliomonitoringapp.holding.dto.XanoStockDto;
import org.cg.stockportfoliomonitoringapp.holding.model.Holding;
import org.cg.stockportfoliomonitoringapp.holding.repository.HoldingRepository;
import org.cg.stockportfoliomonitoringapp.xano.StockPriceService; // For fetching stock data

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // For database transactions

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException; // For specific error handling

@Service // Marks this class as a Spring service component
@RequiredArgsConstructor // Lombok: Generates constructor for final fields (holdingRepository, stockPriceService)
public class HoldingService {

    private final HoldingRepository holdingRepository; // Dependency for database operations
    private final StockPriceService stockPriceService; // Dependency for fetching stock details from Xano

    @Transactional // Ensures database operations are atomic
    public HoldingGainDetailsResponse createOrUpdateHolding(
            Long userId, String stockSymbol, Integer quantity, Double buyPrice) {

        // 1. Basic input validation
        if (stockSymbol == null || stockSymbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Stock symbol is required.");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }
        if (buyPrice == null || buyPrice <= 0) {
            throw new IllegalArgumentException("Buy price must be positive.");
        }

        // 2. Fetch stock details from Xano by symbol
        Map<String, XanoStockDto> allXanoStocks = stockPriceService.getAllStockDetailsMap();
        XanoStockDto xanoStock = allXanoStocks.get(stockSymbol.trim().toUpperCase()); // Use uppercase for robust lookup

        if (xanoStock == null) {
            throw new NoSuchElementException("Stock with symbol '" + stockSymbol + "' not found in external data.");
        }

        // 3. Find existing holding or create a new one
        Holding holding = holdingRepository.findByUserIdAndStockSymbolIgnoreCase(userId, stockSymbol)
                .orElse(new Holding()); // If not found, create new Holding object

        // 4. Update holding details based on new transaction
        holding.setUserId(userId);
        holding.setStockSymbol(xanoStock.getSymbol()); // Ensure symbol is consistent with Xano

        if (holding.getHoldingId() == null) { // This is a new holding
            holding.setQuantity(quantity);
            holding.setBuyPrice(buyPrice);
        } else { // Existing holding: calculate new average price and total quantity
            int oldQuantity = holding.getQuantity();
            double oldBuyPrice = holding.getBuyPrice();

            int newQuantity = oldQuantity + quantity;
            double newTotalCost = (oldQuantity * oldBuyPrice) + (quantity * buyPrice);
            double newAverageBuyPrice = newTotalCost / newQuantity;

            holding.setQuantity(newQuantity);
            holding.setBuyPrice(newAverageBuyPrice);
        }

        // 5. Save updated/new holding to database
        Holding savedHolding = holdingRepository.save(holding);

        // 6. Prepare and return response DTO (inlined mapping)
        HoldingGainDetailsResponse response = new HoldingGainDetailsResponse();
        response.setHoldingId(savedHolding.getHoldingId());
        response.setStockName(xanoStock.getName()); // Use Xano's name
        response.setStockSymbol(savedHolding.getStockSymbol());
        response.setQuantity(savedHolding.getQuantity());
        response.setBuyPrice(savedHolding.getBuyPrice());
        response.setCurrentPrice(xanoStock.getCurrentPrice());

        double totalBuyValue = savedHolding.getQuantity() * savedHolding.getBuyPrice();
        double currentMarketValue = savedHolding.getQuantity() * xanoStock.getCurrentPrice();
        double profitOrLoss = currentMarketValue - totalBuyValue;
        double gainPercent = (totalBuyValue == 0) ? 0.0 : (profitOrLoss / totalBuyValue) * 100;

        response.setTotalBuyValue(totalBuyValue);
        response.setCurrentMarketValue(currentMarketValue);
        response.setProfitOrLoss(profitOrLoss);
        response.setGainPercent(gainPercent);
        response.setSector(xanoStock.getSector()); // Use Xano's sector
        response.setMessage("Holding created/updated successfully.");
        return response;
    }

    public List<XanoStockDto> getAllXanoStockDetails() {
        // Directly get values from the map returned by StockPriceService
        return new ArrayList<>(stockPriceService.getAllStockDetailsMap().values());
    }

    public HoldingResponseDTO getHoldingsForUser(Long userId) {
        // 1. Fetch user's holdings from the database
        List<Holding> userHoldings = holdingRepository.findByUserId(userId);

        // 2. Fetch all stock details from Xano once for efficient lookup
        Map<String, XanoStockDto> stockDetailsMap = stockPriceService.getAllStockDetailsMap();

        List<HoldingStatusDTO> statusList = new ArrayList<>();
        double totalPortfolioValue = 0.0;

        // 3. Process each holding, enrich with Xano data, and calculate
        for (Holding h : userHoldings) {
            HoldingStatusDTO dto = new HoldingStatusDTO();
            dto.setSymbol(h.getStockSymbol());
            dto.setQuantity(h.getQuantity());
            dto.setBuyPrice(h.getBuyPrice());

            XanoStockDto stockFromXano = stockDetailsMap.get(h.getStockSymbol().toUpperCase()); // Case-insensitive match
            double currentPrice = 0.0;
            if (stockFromXano != null) {
                currentPrice = stockFromXano.getCurrentPrice();
                dto.setCompanyName(stockFromXano.getName());
                dto.setSector(stockFromXano.getSector());
            } else {
                dto.setCompanyName("N/A"); // Default if stock not found in Xano
                dto.setSector("N/A");
                // In a real app, you might log a warning here.
            }
            dto.setCurrentPrice(currentPrice);

            double profitOrLoss = (currentPrice - h.getBuyPrice()) * h.getQuantity();
            double gainPercentage = (h.getBuyPrice() == 0) ? 0.0 : (profitOrLoss / (h.getBuyPrice() * h.getQuantity())) * 100;

            dto.setProfitOrLoss(profitOrLoss);
            dto.setGainPercentage(gainPercentage);

            totalPortfolioValue += currentPrice * h.getQuantity();
            statusList.add(dto);
        }

        // 4. Return consolidated response
        return new HoldingResponseDTO(statusList, totalPortfolioValue, "Holdings fetched successfully.");
    }
}