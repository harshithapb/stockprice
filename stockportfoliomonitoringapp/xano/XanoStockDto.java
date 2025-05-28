package org.cg.stockdemo.HoldingManagement.stockportfoliomonitoringapp.xano;

// src/main/java/org/cg/stockportfoliomonitoringapp/holding/dto/XanoStockDto.java
package org.cg.stockportfoliomonitoringapp.holding.dto;

import lombok.Data;

@Data
public class XanoStockDto {
    private Long id;
    private String name; // maps to companyName in new DTOs
    private String symbol;
    private Double currentPrice;
    private String sector;
}