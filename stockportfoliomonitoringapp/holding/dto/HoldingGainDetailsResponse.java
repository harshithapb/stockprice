// src/main/java/org/cg/stockportfoliomonitoringapp/holding/dto/HoldingGainDetailsResponse.java
package org.cg.stockportfoliomonitoringapp.holding.dto;

import lombok.Data;

@Data
public class HoldingGainDetailsResponse {
    private Long holdingId;
    private String stockName;
    private String stockSymbol;
    private Integer quantity;
    private Double buyPrice;
    private Double currentPrice;
    private Double totalBuyValue;
    private Double currentMarketValue;
    private Double profitOrLoss;
    private Double gainPercent;
    private String message;
    private String sector;
}