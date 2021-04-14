package com.introduct.coindesk.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.introduct.coindesk.config.ConfigProvider.CoindeskProperties;
import com.introduct.coindesk.exception.CoindeskServiceException;
import com.introduct.coindesk.model.CurrentRate;
import com.introduct.coindesk.model.History;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.net.URIBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Map;

@Slf4j
public class CoindeskServiceImpl implements CoindeskService {

  private final ObjectMapper mapper;
  private final String coinBaseUrl;
  private final CloseableHttpClient httpClient;

  public CoindeskServiceImpl(
      CoindeskProperties props, ObjectMapper mapper, CloseableHttpClient httpClient) {
    this.coinBaseUrl = props.getUrl();
    this.mapper = mapper;
    this.httpClient = httpClient;
  }

  @Override
  public BigDecimal getCurrentRate(Currency currency) throws CoindeskServiceException {
    try {
      var uri = new URI(coinBaseUrl + "/currentprice/" + currency.getCurrencyCode() + ".json");

      log.debug("Sending 'current price' request to {}", uri);
      var currentRate = getApiResponse(uri, CurrentRate.class);

      log.debug("Received result for currency {}: {}", currency, currentRate);
      return currentRate.getBpi().get(currency.getCurrencyCode()).getRate();

    } catch (Exception e) {
      log.error("Unexpected error", e);
      throw new CoindeskServiceException("Unexpected error", e);
    }
  }

  @Override
  public Map<LocalDate, BigDecimal> getCurrencyHistory(
      Currency currency, LocalDate start, LocalDate end) throws CoindeskServiceException {
    // do validation
    if (start.isAfter(end)) throw new CoindeskServiceException("Start date is after end date");

    try {
      var uri =
          new URIBuilder(coinBaseUrl + "/historical/close.json")
              .addParameter("start", start.toString())
              .addParameter("end", end.toString())
              .addParameter("currency", currency.getCurrencyCode())
              .build();

      log.debug("Sending 'historical' request to {}", uri);
      var history = getApiResponse(uri, History.class);

      log.debug(
          "Received result for currency {}, start {}, end {}: {}", currency, start, end, history);
      return history.getBpi();

    } catch (Exception e) {
      log.error("Unexpected error", e);
      throw new CoindeskServiceException("Unexpected error", e);
    }
  }

  /**
   * Parses a response and handles exceptions.
   *
   * <p><b>Response - EUR historical rates:</b>
   *
   * <pre>
   *   "bpi": {
   *     "2021-03-14": 49372.3442,
   *     ...
   *     "2021-04-12": 50245.042
   *   },
   * </pre>
   *
   * <b>Response - USD currency rate:</b>
   *
   * <pre>
   * "bpi": {
   *   "USD": {
   *     "code": "USD",
   *     "rate": "62,787.2067",
   *     "description": "United States Dollar",
   *     "rate_float": 62787.2067
   *   }
   * }
   * </pre>
   */
  private <T> T getApiResponse(URI uri, Class<T> responseType) throws IOException {
    var request = new HttpGet(uri);

    try (var response = httpClient.execute(request)) {
      log.debug("Received response: {}", response);

      if (response.getCode() == 404)
        throw new CoindeskServiceException(response.getEntity().getContent().toString());

      if (response.getEntity() != null) {
        return mapper.readValue(response.getEntity().getContent(), responseType);
      }
      throw new CoindeskServiceException("Unexpected state");
    }
  }
}
