package com.introduct.coindesk.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class CurrentRate {

  private Map<String, Rate> bpi;

  @Data
  public static class Rate {

    private String code;
    private String description;

    @JsonProperty("rate_float")
    private BigDecimal rate;
  }
}
