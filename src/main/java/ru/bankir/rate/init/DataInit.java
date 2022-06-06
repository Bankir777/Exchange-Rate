package ru.bankir.rate.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bankir.rate.service.ExchangeRateServiceImpl;
import javax.annotation.PostConstruct;

@Component
public class DataInit {

    private final ExchangeRateServiceImpl exchangeRateService;

    @Autowired
    public DataInit(ExchangeRateServiceImpl exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @PostConstruct
    public void firstDataInit() {
        exchangeRateService.refreshRates();
    }
}
