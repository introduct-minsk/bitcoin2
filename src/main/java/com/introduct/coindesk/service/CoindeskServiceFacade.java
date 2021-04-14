package com.introduct.coindesk.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.introduct.coindesk.config.ConfigProvider;
import com.introduct.coindesk.config.ConfigProvider.CoindeskProperties;
import com.introduct.coindesk.model.CurrencyRateStats;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.util.Timeout;

import java.time.LocalDate;
import java.util.Currency;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CoindeskServiceFacade {
  private final CoindeskService service;

  public CoindeskServiceFacade(CoindeskProperties props) {
    var mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.registerModule(new JavaTimeModule());

    var timeout = Timeout.of(5 * 1000, TimeUnit.MILLISECONDS);
    var config =
        RequestConfig.custom()
            .setConnectTimeout(timeout)
            .setConnectionRequestTimeout(timeout)
            .build();

    // TODO add hook to close this client
    var httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

    service = new CoindeskServiceImpl(props, mapper, httpClient);
  }

  public CoindeskServiceFacade() {
    this(ConfigProvider.getCoindeskProperties());
  }

  /**
   * Returns a accumulated result.
   *
   * <p><b>Example:</b>
   *
   * <pre>
   *  CurrencyRateStats:
   *   - currency=USD
   *   - rate=62787.2067
   *   - min=51344.275
   *   - max=63564.8333
   * </pre>
   */
  public CurrencyRateStats getStats(String currency) {
    var parsedCurrency = Currency.getInstance(currency);

    // 1.) request current rate
    var currentPrice = service.getCurrentRate(parsedCurrency);

    // 2.) request historical data
    var history =
        service.getCurrencyHistory(parsedCurrency, LocalDate.now().minusDays(30), LocalDate.now());

    // accumulate results
    return CurrencyRateStats.process(parsedCurrency, currentPrice, history);
  }
}
