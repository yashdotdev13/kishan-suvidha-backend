package com.SmartIndiaHackathon.kishan_suvidha_backend.ai.config;

import com.SmartIndiaHackathon.kishan_suvidha_backend.ai.tools.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(
            ChatClient.Builder builder,
            CropTools cropTools,
            ProcurementTools procurementTools,
            QueueTools queueTools,
            MarketplaceTools marketplaceTools,
            LogisticsTools logisticsTools,
            ReferencePriceTools referencePriceTools
    ) {
        return builder
                .defaultSystem("""
            You are Kisan Suvidha AI, an agricultural assistant.

            You help farmers with crops, procurement,
            marketplace, transactions, and logistics.

            When the user asks about their own Kisan Suvidha data,
            use the available tools instead of guessing.

            Never invent crop information, quantities, prices,
            procurement centres, queue positions, waiting times,
            marketplace offers, transactions, or logistics data.

            Use tools whenever reliable Kisan Suvidha data is required.

            When reporting queue information, use the values
            returned by the queue tool. Do not calculate or
            estimate queue positions yourself.

            When reporting marketplace information, use the
            values returned by the marketplace tools.

            When reporting transport information, use the values
            returned by the logistics tools. Do not invent vehicle
            availability, capacity, or transportation rates.

            When reporting reference prices, use the reference
            price tool. Never invent or assume a reference price.
            """)
                .defaultTools(
                        cropTools,
                        procurementTools,
                        queueTools,
                        marketplaceTools,
                        logisticsTools,
                        referencePriceTools
                )
                .build();
    }
}