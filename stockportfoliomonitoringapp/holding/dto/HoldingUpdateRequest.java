package org.cg.stockdemo.HoldingManagement.stockportfoliomonitoringapp.holding.dto;
// src/main/java/org/cg/stockportfoliomonitoringapp/holding/dto/HoldingUpdateRequest.java
//   package org.cg.stockportfoliomonitoringapp.holding.dto;

import lombok.Data;

@Data
public class HoldingUpdateRequest {
    private String stockSymbol; // The symbol of the stock to add/update
    private Integer quantity;   // Quantity to buy/add
    private Double buyPrice;    // Price per share for this transaction
}