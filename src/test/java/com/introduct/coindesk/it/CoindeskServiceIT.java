package com.introduct.coindesk.it;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.introduct.coindesk.config.ConfigProvider;
import com.introduct.coindesk.exception.CoindeskServiceException;
import com.introduct.coindesk.service.CoindeskService;
import com.introduct.coindesk.service.CoindeskServiceImpl;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

public class CoindeskServiceIT {
  // a subject to test
  private CoindeskService service;

  @BeforeEach
  public void setUp() {
    var mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.registerModule(new JavaTimeModule());

    service = new CoindeskServiceImpl(ConfigProvider.getCoindeskProperties(), mapper, HttpClients.createDefault());
  }

  @ParameterizedTest
  @ValueSource(strings = {"USD", "EUR"})
  void getCurrentRate(String currency) {
    // - act -
    var price = service.getCurrentRate(Currency.getInstance(currency));

    // - assert -
    assertNotNull(price);
    assertTrue(price.compareTo(BigDecimal.ZERO) > 0);
  }

  @ParameterizedTest
  @ValueSource(strings = {"USD", "EUR"})
  void getHistory(String currency) {
    // - arrange -
    var start = LocalDate.now().minusMonths(1);
    var end = LocalDate.now();

    // - act -
    var history = service.getCurrencyHistory(Currency.getInstance(currency), start, end);

    // - assert -
    assertNotNull(history);
    assertTrue(history.size() > 0);

    history.forEach(
        (key, value) -> {
          assertTrue(key.isAfter(start) || key.isEqual(start));
          assertTrue(key.isBefore(end) || key.isEqual(end));
          assertTrue(value.compareTo(BigDecimal.ZERO) > 0);
        });
  }

  @Test
  void getHistory_InTheFuture() {
    assertThrows(
        CoindeskServiceException.class,
        () ->
            service.getCurrencyHistory(
                Currency.getInstance("USD"),
                LocalDate.now().plusYears(1),
                LocalDate.now().plusYears(2)));
  }

  @Test
  void getHistory_TooFarInThePast() {
    assertThrows(
        CoindeskServiceException.class,
        () ->
            service.getCurrencyHistory(
                Currency.getInstance("USD"),
                LocalDate.now().minusYears(101),
                LocalDate.now().minusYears(100)));
  }

  @Test
  void getHistory_InvalidRange() {
    assertThrows(
        CoindeskServiceException.class,
        () ->
            service.getCurrencyHistory(
                Currency.getInstance("USD"),
                LocalDate.now().plusDays(1),
                LocalDate.now().minusDays(1)));
  }
}
