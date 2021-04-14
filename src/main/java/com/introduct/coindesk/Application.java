package com.introduct.coindesk;

import com.introduct.coindesk.model.CurrencyRateStats;
import com.introduct.coindesk.service.CoindeskServiceFacade;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;

@Slf4j
public class Application {

  public static void main(String[] args) {
    log.info("Please enter currency code: ");
    Scanner in = new Scanner(System.in);

    // parse input
    String currency = in.nextLine().toUpperCase();
    log.info("Parsed currency code: {}", currency);

    // process output
    CurrencyRateStats stats = new CoindeskServiceFacade().getStats(currency);
    log.info("Processed currency stats: {}", stats);
  }
}
