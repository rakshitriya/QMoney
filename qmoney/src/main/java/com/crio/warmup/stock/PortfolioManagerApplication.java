
package com.crio.warmup.stock;


import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {


  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
     String file = args[0];
     String contents = readFileAsString(file);
     ObjectMapper objectMapper = getObjectMapper();
     PortfolioTrade[] portfolioTrades = objectMapper.readValue(contents, PortfolioTrade[].class);
     List<String> result = new ArrayList<>();
     for(int i=0;i<portfolioTrades.length;i++)
      result.add(portfolioTrades[i].getSymbol());
     return result;
  }
  private static String readFileAsString(String filename) throws URISyntaxException, IOException {
    return new String(Files.readAllBytes(resolveFileFromResources(filename).toPath()));
  }

  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.
  public static List<TotalReturnsDto> mainReadQuotesHelper(String[] args, List<PortfolioTrade> trades) throws IOException, URISyntaxException {
    RestTemplate restTemplate = new RestTemplate();
    List<TotalReturnsDto> tests  = new ArrayList<TotalReturnsDto>();
    for( PortfolioTrade t:trades){
      String uri = "https://api.tiingo.com/tiingo/daily/" + t.getSymbol()+"/prices?startDate="+t.getPurchaseDate().toString()+ "&endDate="+ args[1] + "&token=8c54f71a4595146aec4c5cd4e8da04c4e6b6b022";
      TiingoCandle [] results = restTemplate.getForObject(uri, TiingoCandle[].class);
      if( results!=null){
        tests.add(new TotalReturnsDto(t.getSymbol(), results[results.length-1].getClose()));
      }
    }
    return tests;
  }
  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    ObjectMapper objectMapper = getObjectMapper();
    List<PortfolioTrade> trades = Arrays.asList(objectMapper.readValue(resolveFileFromResources(args[0]), PortfolioTrade[].class));
    List<TotalReturnsDto> sortedByValue = mainReadQuotesHelper(args, trades);
    Collections.sort(sortedByValue, TotalReturnsDto.closingComparator);
    List<String> stocks = new ArrayList<String>();
    for(TotalReturnsDto trd:sortedByValue){
      stocks.add(trd.getSymbol());
    }
    return stocks;
  }
  







  // Note:
  // 1. You may have to register on Tiingo to get the api_token.
  // 2. Look at args parameter and the module instructions carefully.
  // 2. You can copy relevant code from #mainReadFile to parse the Json.
  // 3. Use RestTemplate#getForObject in order to call the API,
  //    and deserialize the results in List<Candle>



  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(
        Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }


  public static List<String> debugOutputs() {

     String valueOfArgument0 = "trades.json";
     String resultOfResolveFilePathArgs0 = "File@55 \"/home/crio-user/workspace/rbr-rakshit14-ME_QMONEY_V2/qmoney/bin/main/trades.json\"";
     String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@6150c3ec";
     String functionNameFromTestFileInStackTrace = "mainReadFile(new String[]{filename})";
     String lineNumberFromTestFileInStackTrace = "29";


    return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
        toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
        lineNumberFromTestFileInStackTrace});
  }

  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
        List<PortfolioTrade> trades = new ArrayList<>();
        // Since the JSON file is in the resources folder
        Path path = Paths.get(ClassLoader.getSystemResource(filename).toURI());
        byte[] jsonData = Files.readAllBytes(path);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        PortfolioTrade[] portfolioTrades = objectMapper.readValue(jsonData, PortfolioTrade[].class);
        for (PortfolioTrade trade : portfolioTrades) {
            trades.add(trade);
        }
        return trades;
 }
 
 public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  // Construct the Tiingo URL using the provided trade information
  String tiingoUrl = "https://api.tiingo.com/tiingo/daily/";
  tiingoUrl += trade.getSymbol() + "/prices";
  tiingoUrl += "?startDate=" + trade.getPurchaseDate().format(formatter);
  tiingoUrl += "&endDate=" + endDate.format(formatter);
  tiingoUrl += "&token=" + token;

  return tiingoUrl;
}

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.
 public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate, PortfolioTrade trade, Double buyPrice, Double sellPrice){
    Double absReturn = (sellPrice - buyPrice)/ buyPrice;
    String symbol = trade.getSymbol();
    LocalDate purchaseDate = trade.getPurchaseDate();

    Double numYears = (double) ChronoUnit.DAYS.between(purchaseDate, endDate) / 365;
    Double annualizedReturns = Math.pow((1+absReturn), (1/numYears)) -1;
    return new AnnualizedReturn(symbol, annualizedReturns, absReturn);
 }

 public static List<AnnualizedReturn> mainCalculateSingleReturn(String [] args) throws IOException, URISyntaxException, DateTimeParseException{
    List<AnnualizedReturn> annualizedReturns = new ArrayList<>();
    LocalDate endLocalDate = LocalDate.parse(args[1]);
    File trades = resolveFileFromResources(args[0]);
    ObjectMapper objectMapper = getObjectMapper();
    PortfolioTrade[] tradeJsons = objectMapper.readValue(trades, PortfolioTrade[].class);
    for(int i=0;i<tradeJsons.length;i++){
      annualizedReturns.add(getAnnualizedReturn(tradeJsons[i],endLocalDate));
    }
    Comparator<AnnualizedReturn> SortByAnnReturn = Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
    Collections.sort(annualizedReturns, SortByAnnReturn);
    return annualizedReturns;
 }
 public static String TOKEN = "8c54f71a4595146aec4c5cd4e8da04c4e6b6b022";
 public static String getToken(){
  return TOKEN;
 }

 public static AnnualizedReturn getAnnualizedReturn(PortfolioTrade trade, LocalDate endLocalDate){
    String ticker = trade.getSymbol();
    LocalDate startLocalDate = trade.getPurchaseDate();
    if(startLocalDate.compareTo(endLocalDate) >= 0){
      throw new RuntimeException();
    }
    String url = String.format("https://api.tiingo.com/tiingo/daily/%s/prices?" + "startDate=%s&endDate=%s&token=%s", ticker, startLocalDate.toString(), endLocalDate.toString(), TOKEN);
    RestTemplate restTemplate = new RestTemplate();

    TiingoCandle[]  stockStartToEndDate = restTemplate.getForObject(url, TiingoCandle[].class);

    if(stockStartToEndDate !=  null){
      TiingoCandle stockStartDate = stockStartToEndDate[0];
      TiingoCandle stockLatest = stockStartToEndDate[stockStartToEndDate.length - 1];
      Double buyPrice = stockStartDate.getOpen();
      Double sellPrice = stockLatest.getClose();
      AnnualizedReturn annualizedReturn = calculateAnnualizedReturns(endLocalDate, trade, buyPrice, sellPrice);
      return annualizedReturn;
    }else {
      return new AnnualizedReturn(ticker, Double.NaN, Double.NaN);
    }
 }


public static Double getOpeningPriceOnStartDate(List<Candle> candles) {
  if (candles.isEmpty()) {
      return null; // or throw an exception indicating empty list
  }
  return candles.get(0).getOpen();
}

public static Double getClosingPriceOnEndDate(List<Candle> candles) {
  if (candles.isEmpty()) {
      return null; // or throw an exception indicating empty list
  }
  return candles.get(candles.size() - 1).getClose();
}
public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
  List<Candle> candleList = new ArrayList<>();
  String ticker = trade.getSymbol();
  LocalDate startLocalDate = trade.getPurchaseDate();
  if(startLocalDate.compareTo(endDate) >= 0){
    throw new RuntimeException();
  }
  String url = String.format("https://api.tiingo.com/tiingo/daily/%s/prices?" + "startDate=%s&endDate=%s&token=%s", ticker, startLocalDate.toString(), endDate.toString(), token);
  RestTemplate restTemplate = new RestTemplate();

  TiingoCandle[]  tiingoCandles = restTemplate.getForObject(url, TiingoCandle[].class);
  if (tiingoCandles != null) {
    candleList.addAll(Arrays.asList(tiingoCandles));
  }
  return candleList;
}
  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Once you are done with the implementation inside PortfolioManagerImpl and
  //  PortfolioManagerFactory, create PortfolioManager using PortfolioManagerFactory.
  //  Refer to the code from previous modules to get the List<PortfolioTrades> and endDate, and
  //  call the newly implemented method in PortfolioManager to calculate the annualized returns.

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.

  public static RestTemplate restTemplate = new RestTemplate();
  private static String provider;
  public static PortfolioManager portfolioManager = PortfolioManagerFactory.getPortfolioManager(provider, restTemplate);

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
       String file = args[0];
       LocalDate endDate = LocalDate.parse(args[1]);
       String contents = readFileAsString(file);
       ObjectMapper objectMapper = getObjectMapper();
       PortfolioTrade[] portfolioTrades = objectMapper.readValue(contents, PortfolioTrade[].class);
       return portfolioManager.calculateAnnualizedReturn(Arrays.asList(portfolioTrades), endDate);
  }









  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());
    printJsonObject(mainReadFile(args));
    //printJsonObject(mainCalculateReturnsAfterRefactor(args));
}
}

