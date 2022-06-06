package ru.bankir.rate.service.serviceinterface;

import java.util.List;

public interface ExchangeRateService {
    List<String> getCharCodes();

    int getKeyForTag(String charCode);

    void refreshRates();

}
