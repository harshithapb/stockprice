// src/main/java/org/cg/stockportfoliomonitoringapp/holding/dto/HoldingStatusDTO.java
package org.cg.stockportfoliomonitoringapp.holding.dto;

import lombok.Data;

@Data
public class HoldingStatusDTO {
    private String symbol;
    private String companyName;
    private String sector;
    private int quantity;
    private double buyPrice;
    private double currentPrice;
    private double profitOrLoss;
    private double gainPercentage;
}