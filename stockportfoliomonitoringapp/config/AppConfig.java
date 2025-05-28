package org.cg.stockdemo.HoldingManagement.stockportfoliomonitoringapp.config;

// src/main/java/org/cg/stockportfoliomonitoringapp/config/AppConfig.java
package org.cg.stockportfoliomonitoringapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}