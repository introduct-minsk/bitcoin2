package com.introduct.coindesk.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
public class History {

  private Map<LocalDate, BigDecimal> bpi;
}
