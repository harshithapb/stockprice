package org.cg.stockdemo.HoldingManagement.stockportfoliomonitoringapp.xano;

// src/main/java/org/cg/stockportfoliomonitoringapp/xano/StockPriceService.java
package org.cg.stockportfoliomonitoringapp.xano;

import org.cg.stockportfoliomonitoringapp.holding.dto.XanoStockDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service; // Changed to @Service as it's a Spring-managed component now
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; // For map conversion
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service // Mark as a Spring service
public class StockPriceService {

    private static final Logger log = LoggerFactory.getLogger(StockPriceService.class);

    @Value("${xano.mockdata.stocks.url}")
    private String xanoApiUrl; // Changed to instance variable to use @Value

    private final RestTemplate restTemplate; // Injected RestTemplate

    // Constructor injection for RestTemplate
    public StockPriceService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Fetches all stock data and returns it as a Map for easy lookup
    public Map<String, XanoStockDto> getAllStockDetailsMap() {
        log.info("Fetching all stock details from Xano API: {}", xanoApiUrl);
        try {
            ResponseEntity<List<XanoStockDto>> response = restTemplate.exchange(
                    xanoApiUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<XanoStockDto>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Successfully fetched {} stock details.", response.getBody().size());
                return response.getBody().stream()
                        .collect(Collectors.toMap(XanoStockDto::getSymbol, stock -> stock));
            } else {
                log.warn("Failed to fetch stock details. Status: {}", response.getStatusCode());
                return new HashMap<>();
            }
        } catch (RestClientException e) {
            log.error("Error fetching stock details from external API ({}): {}", xanoApiUrl, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch stock details from external API: " + e.getMessage());
        }
    }
}