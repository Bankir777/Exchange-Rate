package ru.bankir.rate.feign_client;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.bankir.rate.model.ExchangeRate;

@org.springframework.cloud.openfeign.FeignClient(name = "OERClient", url = "${openexchangerates.url.general}")
public interface FeignClientExchangeRate {

    @GetMapping("/latest.json")
    ExchangeRate getLatestRates(
            @RequestParam("app_id") String appId);

    @GetMapping("/historical/{date}.json")
    ExchangeRate getHistoricalRates(
            @PathVariable String date,
            @RequestParam("app_id") String appId);
}
