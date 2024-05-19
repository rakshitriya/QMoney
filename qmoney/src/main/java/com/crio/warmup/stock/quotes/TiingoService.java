
package com.crio.warmup.stock.quotes;

import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.net.URISyntaxException;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.client.RestTemplate;


public class TiingoService implements StockQuotesService {

  private RestTemplate restTemplate;
  public static  String TOKEN = "8c54f71a4595146aec4c5cd4e8da04c4e6b6b022";
  protected TiingoService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public ObjectMapper getObjectMapper() {
    // Implement the method logic here
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
}
  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Implement getStockQuote method below that was also declared in the interface.
  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to) throws JsonProcessingException, StockQuoteServiceException {
    List<Candle> stocksStartToEndDate;
    if(from.compareTo(to)>=0){
      throw new RuntimeException();
    }
     String url = buildUri(symbol, from, to);
     try {
      String stocks = restTemplate.getForObject(url, String.class);
     ObjectMapper objectMapper = getObjectMapper();
     TiingoCandle[] stocksStartToEndDateArray = objectMapper.readValue(stocks, TiingoCandle[].class);
     if(stocksStartToEndDateArray != null)
     stocksStartToEndDate = Arrays.asList(stocksStartToEndDateArray);
     else
     stocksStartToEndDate = Arrays.asList(new TiingoCandle[0]);

     }catch(NullPointerException e){
      throw new StockQuoteServiceException("Error occurred when requesting response from Tiingo API", e.getCause());
     }
     
     return stocksStartToEndDate;
  }
  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String token = "8c54f71a4595146aec4c5cd4e8da04c4e6b6b022";
    String uriTemplate = "https:api.tiingo.com/tiingo/daily/$SYMBOL/prices?" + "startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";
    String url = uriTemplate.replace("$APIKEY", token).replace("$STARTDATE", startDate.toString()).replace("$ENDDATE", endDate.toString());
    return url;
}

  // Note:
  // 1. You can move the code from PortfolioManagerImpl#getStockQuote inside newly created method.
  // 2. Run the tests using command below and make sure it passes.
  //    ./gradlew test --tests TiingoServiceTest


  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Write a method to create appropriate url to call the Tiingo API.

}
