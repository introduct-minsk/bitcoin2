package com.introduct.coindesk.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Map;

@RequiredArgsConstructor
@Getter
@Slf4j
@ToString
public class CurrencyRateStats {

  private final Currency currency;
  private final BigDecimal rate;
  private final BigDecimal min;
  private final BigDecimal max;

  public static CurrencyRateStats process(Currency currency, BigDecimal rate, Map<LocalDate, BigDecimal> history) {
    //  parse MIN
    BigDecimal min = history.values().stream()
            .min(BigDecimal::compareTo).orElse(null);
    // parse MAZ
    BigDecimal max = history.values().stream()
            .max(BigDecimal::compareTo).orElse(null);

    return new CurrencyRateStats(currency, rate, min, max);
  }
}
