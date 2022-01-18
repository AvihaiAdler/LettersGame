package application.util;

import java.util.Arrays;

public class ConfigValues {
  private int numOfGames;
  private String host;
  private int port;
  private String[] columns;
  private String[] strings;
  
  public ConfigValues() {
    
  }
  
  public ConfigValues(int numOfGames, String host, int port, String[] columns,
          String[] strings) {
    super();
    this.numOfGames = numOfGames;
    this.host = host;
    this.port = port;
    this.columns = columns;
    this.strings = strings;
  }
  
  public int getNumOfGames() {
    return numOfGames;
  }
  
  public void setNumOfGames(int numOfGames) {
    this.numOfGames = numOfGames;
  }
  
  public String getHost() {
    return host;
  }
  
  public void setHost(String host) {
    this.host = host;
  }
  
  public int getPort() {
    return port;
  }
  
  public void setPort(int port) {
    this.port = port;
  }
  
  public String[] getColumns() {
    return columns;
  }
  
  public void setColumns(String[] columns) {
    this.columns = columns;
  }
  
  public String[] getStrings() {
    return strings;
  }

  public void setStrings(String[] strings) {
    this.strings = strings;
  }

  @Override
  public String toString() {
    return "ConfigValues [numOfGames=" + numOfGames + ", host=" + host + ", port=" + port + ", columns="
            + Arrays.toString(columns) + ", strings=" + Arrays.toString(strings) + "]";
  }
}
