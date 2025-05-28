package org.cg.stockdemo.HoldingManagement.stockportfoliomonitoringapp.holding.repository;

// src/main/java/org/cg/stockportfoliomonitoringapp/holding/repository/HoldingRepository.java
package org.cg.stockportfoliomonitoringapp.holding.repository;
import org.cg.stockportfoliomonitoringapp.holding.model.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, Long> {
    // Finds a specific holding by user and stock symbol (case-insensitive)
    Optional<Holding> findByUserIdAndStockSymbolIgnoreCase(Long userId, String stockSymbol);

    // Finds all holdings for a specific user
    List<Holding> findByUserId(Long userId);
}