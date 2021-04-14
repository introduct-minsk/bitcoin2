package com.introduct.coindesk.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.introduct.coindesk.config.ConfigProvider.CoindeskProperties;
import com.introduct.coindesk.exception.CoindeskServiceException;
import com.introduct.coindesk.model.CurrentRate;
import com.introduct.coindesk.model.History;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.net.URIBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
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
  public BigDecimal getCurrentRate(String currencyCode) {
    try {
      var uri = new URI(coinBaseUrl + "/currentprice/" + currencyCode + ".json");

      log.debug("Sending 'current price' request to {}", uri);
      var currentRate = getApiResponse(uri, CurrentRate.class);

      log.debug("Received result for currency {}: {}", currencyCode, currentRate);
      return currentRate.getBpi().get(currencyCode).getRate();
    } catch (URISyntaxException e) {
      log.error("Unexpected error during processing request.", e);
      throw new CoindeskServiceException("Unexpected error during processing request.", e);
    }
  }

  @Override
  public Map<LocalDate, BigDecimal> getCurrencyHistory(
          String currencyCode, LocalDate start, LocalDate end) {
    try {
      // do validation
      if (start.isAfter(end)) throw new CoindeskServiceException("Start date is after end date");

      var uri =
          new URIBuilder(coinBaseUrl + "/historical/close.json")
              .addParameter("start", start.toString())
              .addParameter("end", end.toString())
              .addParameter("currency", currencyCode)
              .build();

      log.debug("Sending 'historical' request to {}", uri);
      var history = getApiResponse(uri, History.class);

      log.debug(
          "Received result for currency {}, start {}, end {}: {}", currencyCode, start, end, history);
      return history.getBpi();
    } catch (URISyntaxException e) {
      log.error("Unexpected error during processing request.", e);
      throw new CoindeskServiceException("Unexpected error during processing request.", e);
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
  private <T> T getApiResponse(URI uri, Class<T> responseType){
    var request = new HttpGet(uri);

    try (final CloseableHttpResponse response = httpClient.execute(request)) {
      log.info("Received response: {} with http code: {}", response, response.getCode());

      if (response.getCode() == HttpStatus.SC_NOT_FOUND) {
        throw new CoindeskServiceException(getMessageFromResponse(response));
      }

      if (response.getEntity() != null) {
         return mapper.readValue(response.getEntity().getContent(), responseType);
      } else {
        throw new CoindeskServiceException("Invalid response from the server.");
      }
    } catch (IOException e) {
      log.error("Unexpected error during processing request.", e);
      throw new CoindeskServiceException("Unexpected error during processing request.", e);
    }
  }

  private String getMessageFromResponse(ClassicHttpResponse response) {
    try (InputStreamReader inputStreamReader = new InputStreamReader(response.getEntity().getContent());
         BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
      StringBuilder sb = new StringBuilder();

      String str;

      while((str = bufferedReader.readLine())!= null){
        sb.append(str);
      }

      return sb.toString();
    } catch (IOException e) {
      log.error("Unexpected error during reading response.", e);
      throw new CoindeskServiceException("Unexpected error during reading response.", e);
    }
  }
}
