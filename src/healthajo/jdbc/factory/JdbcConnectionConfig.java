package healthajo.jdbc.factory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class JdbcConnectionConfig {

  private static final Properties DB_PROPERTIES = new Properties();

  static {
    Path path = Path.of("src/template", "database.properties");
    try (InputStream inputStream = Files.newInputStream(path)) {
      DB_PROPERTIES.load(inputStream);
    } catch (IOException e) {
      throw new RuntimeException("database.properties 로드 실패", e);
    }
  }

  private JdbcConnectionConfig() {}

  public static String getJdbcDriver() {
    return DB_PROPERTIES.getProperty("jdbc-driver");
  }

  public static String getJdbcUrl() {
    return DB_PROPERTIES.getProperty("jdbc-url");
  }

  public static String getUsername() {
    return DB_PROPERTIES.getProperty("user");
  }

  public static String getPassword() {
    return DB_PROPERTIES.getProperty("password");
  }

  public static int getPoolSize() {
    String val = DB_PROPERTIES.getProperty("pool-size", "10");
    try {
      return Integer.parseInt(val);
    } catch (NumberFormatException e) {
      throw new RuntimeException("pool-size 설정값이 유효하지 않습니다: " + val, e);
    }
  }

  public static int getConnectionTimeout() {
    String val = DB_PROPERTIES.getProperty("connection-timeout", "5");
    try {
      return Integer.parseInt(val);
    } catch (NumberFormatException e) {
      throw new RuntimeException("connection-timeout 설정값이 유효하지 않습니다: " + val, e);
    }
  }

}
