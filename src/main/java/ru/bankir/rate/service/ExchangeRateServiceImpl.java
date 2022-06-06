package ru.bankir.rate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.bankir.rate.feign_client.FeignClientExchangeRate;
import ru.bankir.rate.model.ExchangeRate;
import ru.bankir.rate.service.serviceinterface.ExchangeRateService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

@Service
public class ExchangeRateServiceImpl implements ExchangeRateService {

    private ExchangeRate prevRate;
    private ExchangeRate currentRate;
    private FeignClientExchangeRate feignClientExchangeRate;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;
    @Value("7027fadbe1074c7296cb1e31c3044676")
    private String appId;
    @Value("${openexchangerates.base}")
    private String base;

    @Autowired
    public ExchangeRateServiceImpl(
            FeignClientExchangeRate feignClientExchangeRate,
            @Qualifier("date_bean") SimpleDateFormat dateFormat,
            @Qualifier("time_bean") SimpleDateFormat timeFormat
    ) {
        this.feignClientExchangeRate = feignClientExchangeRate;
        this.dateFormat = dateFormat;
        this.timeFormat = timeFormat;
    }

    @Override
    public List<String> getCharCodes() {
        List<String> result = null;
        if (this.currentRate.getRates() != null) {
            result = new ArrayList<>(this.currentRate.getRates().keySet());
        }
        return result;
    }

    @Override
    public int getKeyForTag(String charCode) {
        this.refreshRates();
        Double prevCoefficient = this.getCoefficient(this.prevRate, charCode);
        Double currentCoefficient = this.getCoefficient(this.currentRate, charCode);
        return prevCoefficient != null && currentCoefficient != null
                ? Double.compare(currentCoefficient, prevCoefficient)
                : -101;
    }

    @Override
    public void refreshRates() {
        long currentTime = System.currentTimeMillis();
        this.refreshCurrentRates(currentTime);
        this.refreshPrevRates(currentTime);
    }

    private void refreshCurrentRates(long time) {
        if (
                this.currentRate == null ||
                        !timeFormat.format(Long.valueOf(this.currentRate.getTimestamp()) * 1000)
                                .equals(timeFormat.format(time))
        ) {
            this.currentRate = feignClientExchangeRate.getLatestRates(this.appId);
        }
    }

    private void refreshPrevRates(long time) {
        Calendar prevCalendar = Calendar.getInstance();
        prevCalendar.setTimeInMillis(time);
        String currentDate = dateFormat.format(prevCalendar.getTime());
        prevCalendar.add(Calendar.DAY_OF_YEAR, -1);
        String newPrevDate = dateFormat.format(prevCalendar.getTime());
        if (
                this.prevRate == null
                        || (
                        !dateFormat.format(Long.valueOf(this.prevRate.getTimestamp()) * 1000)
                                .equals(newPrevDate)
                                && !dateFormat.format(Long.valueOf(this.prevRate.getTimestamp()) * 1000)
                                .equals(currentDate)
                )
        ) {
            this.prevRate = feignClientExchangeRate.getHistoricalRates(newPrevDate, appId);
        }
    }

    private Double getCoefficient(ExchangeRate rate, String charCode) {
        Double result = null;
        Double targetRate = null;
        Double appBaseRate = null;
        Double defaultBaseRate = null;
        Map<String, Double> map = null;
        if (rate != null && rate.getRates() != null) {
            map = rate.getRates();
            targetRate = map.get(charCode);
            appBaseRate = map.get(this.base);
            defaultBaseRate = map.get(rate.getBase());
        }
        if (targetRate != null && appBaseRate != null && defaultBaseRate != null) {
            result = new BigDecimal(
                    (defaultBaseRate / appBaseRate) * targetRate
            )
                    .setScale(4, RoundingMode.UP)
                    .doubleValue();
        }
        return result;
    }
}
