// src/main/java/org/cg/stockportfoliomonitoringapp/holding/service/HoldingService.java
package org.cg.stockportfoliomonitoringapp.holding.service;

import lombok.RequiredArgsConstructor;
import org.cg.stockportfoliomonitoringapp.holding.dto.HoldingGainDetailsResponse;
import org.cg.stockportfoliomonitoringapp.holding.dto.HoldingResponseDTO;
import org.cg.stockportfoliomonitoringapp.holding.dto.HoldingStatusDTO;
import org.cg.stockportfoliomonitoringapp.holding.dto.XanoStockDto;
import org.cg.stockportfoliomonitoringapp.holding.model.Holding;
import org.cg.stockportfoliomonitoringapp.holding.repository.HoldingRepository;

import org.springframework.beans.factory.annotation.Value; // For Xano URL
import org.springframework.core.ParameterizedTypeReference; // For List<XanoStockDto>
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate; // For API calls

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Lombok: Generates constructor for final fields
public class HoldingService {

    private final HoldingRepository holdingRepository;
    private final RestTemplate restTemplate; // Injected by Spring (from AppConfig)

    @Value("${xano.mockdata.stocks.url}") // Injects Xano API URL
    private String xanoApiUrl;
    private Map<String, XanoStockDto> fetchAllXanoStocks() {
        try {
            ResponseEntity<List<XanoStockDto>> response = restTemplate.exchange(
                    xanoApiUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<XanoStockDto>>() {}
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().stream()
                        .collect(Collectors.toMap(XanoStockDto::getSymbol, stock -> stock));
            }
            throw new RuntimeException("Failed to fetch stocks from Xano: " + response.getStatusCode());
        } catch (Exception e) {
            System.out.println("Error fetching Xano stocks: " + e.getMessage());
            throw new RuntimeException("Could not connect to stock data API.", e);
        }
    }
    @Transactional
    public HoldingGainDetailsResponse createOrUpdateHolding(
            Long userId, String stockSymbol, Integer quantity, Double buyPrice) {
        if (stockSymbol == null || stockSymbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Stock symbol is required.");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }
        if (buyPrice == null || buyPrice <= 0) {
            throw new IllegalArgumentException("Buy price must be positive.");
        }
        Map<String, XanoStockDto> allXanoStocks = fetchAllXanoStocks();
        XanoStockDto xanoStock = allXanoStocks.get(stockSymbol.trim().toUpperCase());

        if (xanoStock == null) {
            throw new NoSuchElementException("Stock with symbol '" + stockSymbol + "' not found in external data.");
        }
        Holding holding = holdingRepository.findByUserIdAndStockSymbolIgnoreCase(userId, stockSymbol)
                .orElse(new Holding());
        holding.setUserId(userId);
        holding.setStockSymbol(xanoStock.getSymbol()); // Use Xano's symbol for consistency

        if (holding.getHoldingId() == null) { // New holding
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
        Holding savedHolding = holdingRepository.save(holding);
        HoldingGainDetailsResponse response = new HoldingGainDetailsResponse();
        response.setHoldingId(savedHolding.getHoldingId());
        response.setStockName(xanoStock.getName());
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
        response.setSector(xanoStock.getSector());
        response.setMessage("Holding created/updated successfully.");
        return response;
    }
    public List<XanoStockDto> getAllXanoStockDetails() {
        return new ArrayList<>(fetchAllXanoStocks().values());
    }
    public HoldingResponseDTO getHoldingsForUser(Long userId) {
        List<Holding> userHoldings = holdingRepository.findByUserId(userId);
        Map<String, XanoStockDto> stockDetailsMap = fetchAllXanoStocks();
        List<HoldingStatusDTO> statusList = new ArrayList<>();
        double totalPortfolioValue = 0.0;
        for (Holding h : userHoldings) {
            HoldingStatusDTO dto = new HoldingStatusDTO();
            dto.setSymbol(h.getStockSymbol());
            dto.setQuantity(h.getQuantity());
            dto.setBuyPrice(h.getBuyPrice());

            XanoStockDto stockFromXano = stockDetailsMap.get(h.getStockSymbol().toUpperCase());
            double currentPrice = 0.0;
            if (stockFromXano != null) {
                currentPrice = stockFromXano.getCurrentPrice();
                dto.setCompanyName(stockFromXano.getName());
                dto.setSector(stockFromXano.getSector());
            } else {
                dto.setCompanyName("N/A");
                dto.setSector("N/A");
                System.out.println("Warning: Stock details not found for symbol: " + h.getStockSymbol());
            }
            dto.setCurrentPrice(currentPrice);
            double profitOrLoss = (currentPrice - h.getBuyPrice()) * h.getQuantity();
            double gainPercentage = (h.getBuyPrice() == 0) ? 0.0 : (profitOrLoss / (h.getBuyPrice() * h.getQuantity())) * 100;
            dto.setProfitOrLoss(profitOrLoss);
            dto.setGainPercentage(gainPercentage);
            totalPortfolioValue += currentPrice * h.getQuantity();
            statusList.add(dto);
        }
        return new HoldingResponseDTO(statusList, totalPortfolioValue, "Holdings fetched successfully.");
    }
}