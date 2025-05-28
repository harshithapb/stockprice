package org.cg.stockdemo.HoldingManagement.stockportfoliomonitoringapp.holding.dto;

// src/main/java/org/cg/stockportfoliomonitoringapp/holding/dto/XanoStockDto.java
package org.cg.stockportfoliomonitoringapp.holding.dto;

import lombok.Data;

@Data
public class XanoStockDto {
    private Long id;
    private String name;        // Stock company name
    private String symbol;      // Stock symbol
    private Double currentPrice; // Current market price
    private String sector;      // Stock sector
}