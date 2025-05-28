// src/main/java/org/cg/stockportfoliomonitoringapp/holding/service/HoldingService.java
package org.cg.stockportfoliomonitoringapp.holding.service;

import lombok.RequiredArgsConstructor;
import org.cg.stockportfoliomonitoringapp.holding.dto.*;
import org.cg.stockportfoliomonitoringapp.holding.model.Holding;
import org.cg.stockportfoliomonitoringapp.holding.repository.HoldingRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HoldingService {

    private final HoldingRepository holdingRepository;
    private final RestTemplate restTemplate;

    @Value("${xano.mockdata.stocks.url}")
    private String xanoApiUrl;
    @Transactional
    public HoldingGainDetailsResponse createOrUpdateHolding(
            Long userId, String stockSymbol, Integer quantity, Double buyPrice) {

        if (stockSymbol == null || stockSymbol.trim().isEmpty() || quantity == null || quantity <= 0 || buyPrice == null || buyPrice <= 0) {
            throw new IllegalArgumentException("Invalid input.");
        }
        Map<String, XanoStockDto> allXanoStocks;
        try {
            ResponseEntity<List<XanoStockDto>> response = restTemplate.exchange(xanoApiUrl, HttpMethod.GET, null, new ParameterizedTypeReference<List<XanoStockDto>>() {});
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                allXanoStocks = response.getBody().stream().collect(Collectors.toMap(XanoStockDto::getSymbol, stock -> stock));
            } else {
                throw new RuntimeException("Xano API error: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("API connection failed.", e);
        }
        XanoStockDto xanoStock = allXanoStocks.get(stockSymbol.trim().toUpperCase());
        if (xanoStock == null) {
            throw new NoSuchElementException("Stock not found.");
        }
        Holding holding = holdingRepository.findByUserIdAndStockSymbolIgnoreCase(userId, stockSymbol).orElse(new Holding());
        holding.setUserId(userId);
        holding.setStockSymbol(xanoStock.getSymbol());

        if (holding.getHoldingId() == null) { // New holding
            holding.setQuantity(quantity);
            holding.setBuyPrice(buyPrice);
        } else { // Existing holding
            int totalQty = holding.getQuantity() + quantity;
            double newAvgPrice = ((holding.getBuyPrice() * holding.getQuantity()) + (buyPrice * quantity)) / totalQty;
            holding.setQuantity(totalQty);
            holding.setBuyPrice(newAvgPrice);
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
        response.setMessage("Success.");
        return response;
    }
    public HoldingResponseDTO getHoldingsForUser(Long userId) {
        List<Holding> userHoldings = holdingRepository.findByUserId(userId);

        // --- INLINED Xano API Call ---
        Map<String, XanoStockDto> stockDetailsMap;
        try {
            ResponseEntity<List<XanoStockDto>> response = restTemplate.exchange(xanoApiUrl, HttpMethod.GET, null, new ParameterizedTypeReference<List<XanoStockDto>>() {});
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                stockDetailsMap = response.getBody().stream().collect(Collectors.toMap(XanoStockDto::getSymbol, stock -> stock));
            } else {
                throw new RuntimeException("Xano API error: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("API connection failed.", e);
        }
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
                dto.setCompanyName("N/A"); // Default if stock not found
                dto.setSector("N/A");
            }
            dto.setCurrentPrice(currentPrice);

            double profitOrLoss = (currentPrice - h.getBuyPrice()) * h.getQuantity();
            double gainPercentage = (h.getBuyPrice() == 0) ? 0.0 : (profitOrLoss / (h.getBuyPrice() * h.getQuantity())) * 100;

            dto.setProfitOrLoss(profitOrLoss);
            dto.setGainPercentage(gainPercentage);

            totalPortfolioValue += currentPrice * h.getQuantity();
            statusList.add(dto);
        }
        return new HoldingResponseDTO(statusList, totalPortfolioValue, "Success.");
    }
}
