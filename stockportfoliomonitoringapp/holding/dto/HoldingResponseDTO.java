package org.cg.stockdemo.HoldingManagement.stockportfoliomonitoringapp.holding.dto;

// src/main/java/org/cg/stockportfoliomonitoringapp/holding/dto/HoldingResponseDTO.java
package org.cg.stockportfoliomonitoringapp.holding.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HoldingResponseDTO {
    private List<HoldingStatusDTO> holdings;
    private double totalPortfolioValue;
    private String message;
}