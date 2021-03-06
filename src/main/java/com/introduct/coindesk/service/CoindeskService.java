package com.introduct.coindesk.service;

import com.introduct.coindesk.exception.CoindeskServiceException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Provides generic methods to access Coindesk API
 */
public interface CoindeskService {

  BigDecimal getCurrentRate(String currencyCode) throws CoindeskServiceException;

  Map<LocalDate, BigDecimal> getCurrencyHistory(String currencyCode, LocalDate start, LocalDate end)
      throws CoindeskServiceException;
}
