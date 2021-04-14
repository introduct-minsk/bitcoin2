package com.introduct.coindesk.test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.introduct.coindesk.config.ConfigProvider.CoindeskProperties;
import com.introduct.coindesk.service.CoindeskService;
import com.introduct.coindesk.service.CoindeskServiceFacade;
import com.introduct.coindesk.service.CoindeskServiceImpl;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URLEncodedUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CoindeskServiceTest {
  private static MockWebServer mockWebServer;

  private static CoindeskService service;
  private static CoindeskServiceFacade facade;

  @BeforeAll
  public static void setup() throws IOException {
    var dispatcher =
        new Dispatcher() {

          public String getJson(String filename) throws URISyntaxException, IOException {
            URL resource = this.getClass().getClassLoader().getResource(filename);
            byte[] bytes = Files.readAllBytes(Paths.get(resource.toURI()));
            return new String(bytes);
          }

          @SneakyThrows
          @Override
          public MockResponse dispatch(RecordedRequest request) {
            // Example: /currentprice/USD.json
            if (request.getPath().contains("currentprice")) {

              return new MockResponse()
                  .setResponseCode(200)
                  .setHeader("Content-Type", "application/json")
                  .setBody(getJson("mock/response/" + request.getPath()));

            } else if (request.getPath().contains("historical")) {
              var params =
                  URLEncodedUtils.parse(
                          new URI(request.getRequestUrl().toString()), StandardCharsets.UTF_8)
                      .stream()
                      .collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));

              return new MockResponse()
                  .setResponseCode(200)
                  .setHeader("Content-Type", "application/json")
                  .setBody(getJson("mock/response/historical/" + params.get("currency") + ".json"));
            } else
              throw new IllegalArgumentException("Unexpected request path: " + request.getPath());
          }
        };

    // start mock server
    mockWebServer = new MockWebServer();
    mockWebServer.setDispatcher(dispatcher);
    mockWebServer.start();

    var mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.registerModule(new JavaTimeModule());

    String address = "http://" + mockWebServer.getHostName() + ":" + mockWebServer.getPort();
    var testProps = new CoindeskProperties(address);

    service = new CoindeskServiceImpl(testProps, mapper, HttpClients.createDefault());
    facade = new CoindeskServiceFacade(testProps);
  }

  @AfterAll
  public static void cleanup() throws IOException {
    mockWebServer.shutdown();
  }

  // TODO persisted historical data has a limited lifetime.
  //  generate data on the fly to be able to handle LocalDateTime.now() in tests

  @ParameterizedTest
  @CsvSource({"USD,62787.2067,51344.275,63564.8333", "EUR,52661.6101,43609.7734,50425.9885"})
  public void testProcessingLogic(String currency, String rate, String min, String max) {
    // - act -
    var stats = facade.getStats(currency);

    // - assert -
    assertEquals(currency, stats.getCurrency().toString());
    assertEquals(new BigDecimal(rate), stats.getRate());
    assertEquals(new BigDecimal(min), stats.getMin());
    assertEquals(new BigDecimal(max), stats.getMax());
  }
}
