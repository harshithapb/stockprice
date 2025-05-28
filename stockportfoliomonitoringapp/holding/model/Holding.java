package org.cg.stockdemo.HoldingManagement.stockportfoliomonitoringapp.holding.model;

// src/main/java/org/cg/stockportfoliomonitoringapp/holding/model/Holding.java
//package org.cg.stockportfoliomonitoringapp.holding.model;

import jakarta.persistence.*;
        import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "holdings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long holdingId; // Primary key for the holding record itself

    private Long userId; // The ID of the user owning this holding
    private String stockSymbol; // The symbol of the stock (e.g., "AAPL")
    private Integer quantity; // Number of shares held
    private Double buyPrice; // Average buy price of the shares
    // No need for stockName or sector here, we can fetch that from Xano dynamically
}