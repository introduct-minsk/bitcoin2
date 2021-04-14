package com.introduct.coindesk.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigProvider {

  public static CoindeskProperties getCoindeskProperties() {
    Config conf = ConfigFactory.load();

    var props =  new CoindeskProperties(conf.getString("coindesk.url"));
    log.info("Configured properties: {}", props);

    return props;
  }

  @RequiredArgsConstructor
  @ToString
  public static class CoindeskProperties {
    @Getter
    private final String url;
  }
}
