package org.cg.stockdemo.HoldingManagement.stockportfoliomonitoringapp.holding.dto;

// src/main/java/org/cg/stockportfoliomonitoringapp/holding/dto/HoldingStatusDTO.java
package org.cg.stockportfoliomonitoringapp.holding.dto;

import lombok.Data;

@Data
public class HoldingStatusDTO {
    private String symbol;
    private String companyName; // From Xano
    private String sector;      // From Xano
    private int quantity;       // Current quantity held
    private double buyPrice;    // Average buy price
    private double currentPrice; // Current market price from Xano
    private double profitOrLoss; // Calculated gain/loss
    private double gainPercentage; // Calculated gain/loss percentage
}