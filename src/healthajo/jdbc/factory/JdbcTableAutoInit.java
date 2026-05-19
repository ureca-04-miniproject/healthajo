package healthajo.jdbc.factory;

import healthajo.jdbc.factory.JdbcConnectionFactory.JdbcConnection;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Statement;

public class JdbcTableAutoInit {

  private static final JdbcConnectionFactory connectionFactory = JdbcConnectionFactory.getInstance();

  private JdbcTableAutoInit() {
  }

  public static void initIfNotExist() {
    Path path = Path.of("src/template", "ddl.sql");

    try (InputStream inputStream = Files.newInputStream(path)) {

      try (JdbcConnection jc = connectionFactory.getConnection()
          ; Statement statement = jc.get().createStatement()) {

        statement.execute(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
      } catch (Exception e) {
        throw new RuntimeException("DDL 실행 실패", e);
      }
    } catch (IOException e) {
      throw new RuntimeException("database.properties 로드 실패", e);
    }
  }

}
