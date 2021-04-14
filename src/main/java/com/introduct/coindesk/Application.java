package com.introduct.coindesk;

import com.introduct.coindesk.exception.CoindeskServiceException;
import com.introduct.coindesk.model.CurrencyRateStats;
import com.introduct.coindesk.service.CoindeskServiceFacade;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;

@Slf4j
public class Application {

  public static void main(String[] args) {
    System.out.print("Please enter currency code: ");
    Scanner in = new Scanner(System.in);

    // parse input
    String currency = in.nextLine().toUpperCase();
    log.info("Parsed currency code: {}", currency);

    // process output
    try {
      CurrencyRateStats stats = new CoindeskServiceFacade().getStats(currency);
      log.info("Processed currency stats: " + stats);

      System.out.println("The current Bitcoin rate is " + stats.getRate());
      System.out.println("The lowest Bitcoin rate in the last 30 days is " + stats.getMin());
      System.out.println("The highest Bitcoin rate in the last 30 days is " + stats.getMax());
    } catch (CoindeskServiceException e) {
      System.out.println(e.getMessage());
      log.error(e.getMessage(), e);
    }
  }
}
