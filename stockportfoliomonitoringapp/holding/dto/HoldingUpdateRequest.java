// src/main/java/org/cg/stockportfoliomonitoringapp/holding/dto/HoldingUpdateRequest.java
package org.cg.stockportfoliomonitoringapp.holding.dto;

import lombok.Data;

@Data
public class HoldingUpdateRequest {
    private String stockSymbol; // e.g., "AAPL"
    private Integer quantity;   // e.g., 10
    private Double buyPrice;    // e.g., 150.00
}