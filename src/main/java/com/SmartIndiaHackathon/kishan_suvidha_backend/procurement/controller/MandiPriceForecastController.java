package com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.controller;

import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos.MandiPriceBacktestResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.dtos.MandiPriceForecastResponse;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.service.MandiPriceBacktestService;
import com.SmartIndiaHackathon.kishan_suvidha_backend.procurement.service.MandiPriceForecastService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/mandi-prices")
@PreAuthorize("hasAnyRole('FARMER', 'BUYER', 'OFFICER')")
public class MandiPriceForecastController {

    private final MandiPriceForecastService forecastService;
    private final MandiPriceBacktestService backtestService;

    public MandiPriceForecastController(
            MandiPriceForecastService forecastService,
            MandiPriceBacktestService backtestService
    ) {
        this.forecastService = forecastService;
        this.backtestService = backtestService;
    }

    @GetMapping("/forecast")
    public MandiPriceForecastResponse forecastNextDay(
            @RequestParam String cropName,
            @RequestParam String marketName,
            @RequestParam String state
    ) {
        return forecastService.forecastNextDay(
                cropName,
                marketName,
                state
        );
    }

    @GetMapping("/backtest")
    public MandiPriceBacktestResponse backtest(
            @RequestParam String cropName,
            @RequestParam String marketName,
            @RequestParam String state,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to
    ) {
        return backtestService.backtest(
                cropName,
                marketName,
                state,
                from,
                to
        );
    }
}