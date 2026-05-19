package healthajo.jdbc.factory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class JdbcConnectionFactory {

  private final BlockingQueue<Connection> pool;
  private final int poolSize;
  private final int connectionTimeout;

  private JdbcConnectionFactory() {
    this.poolSize = JdbcConnectionConfig.getPoolSize();
    this.connectionTimeout = JdbcConnectionConfig.getConnectionTimeout();
    this.pool = new ArrayBlockingQueue<>(poolSize);
    initPool();
  }

  private void initPool() {
    try {
      Class.forName(JdbcConnectionConfig.getJdbcDriver());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("JDBC 드라이버를 찾을 수 없습니다.", e);
    }
    for (int i = 0; i < poolSize; i++) {
      pool.offer(createConnection());
    }
  }

  private Connection createConnection() {
    try {
      return DriverManager.getConnection(
          JdbcConnectionConfig.getJdbcUrl(),
          JdbcConnectionConfig.getUsername(),
          JdbcConnectionConfig.getPassword()
      );
    } catch (SQLException e) {
      throw new RuntimeException("DB 연결 생성에 실패했습니다.", e);
    }
  }

  // LazyHolder Singleton
  private static class Holder {
    private static final JdbcConnectionFactory INSTANCE = new JdbcConnectionFactory();
  }

  public static JdbcConnectionFactory getInstance() {
    return Holder.INSTANCE;
  }

  public JdbcConnection getConnection() {
    long deadline = System.currentTimeMillis() + connectionTimeout * 1000L;
    try {
      while (true) {
        long remaining = deadline - System.currentTimeMillis();
        if (remaining <= 0) {
          throw new RuntimeException("Connection Pool이 모두 사용 중입니다.");
        }
        Connection conn = pool.poll(remaining, TimeUnit.MILLISECONDS);
        if (conn == null) {
          throw new RuntimeException("Connection Pool이 모두 사용 중입니다.");
        }
        // stale connection은 버리고 새로 보충한 뒤 재시도
        if (!conn.isValid(1)) {
          pool.offer(createConnection());
          continue;
        }
        return new JdbcConnection(conn);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Connection 획득 중 인터럽트 발생", e);
    } catch (SQLException e) {
      throw new RuntimeException("Connection 유효성 확인 실패", e);
    }
  }

  private void returnConnection(Connection connection) {
    try {
      if (connection != null && connection.isValid(1)) {
        pool.offer(connection);
      } else {
        // stale/끊긴 커넥션은 버리고 새 커넥션으로 보충
        pool.offer(createConnection());
      }
    } catch (SQLException e) {
      // isValid() 자체 실패 시 새 커넥션으로 보충
      pool.offer(createConnection());
    }
  }

  public static class JdbcConnection implements AutoCloseable {

    private final Connection connection;

    private JdbcConnection(Connection connection) {
      this.connection = connection;
    }

    public Connection get() {
      return connection;
    }

    @Override
    public void close() {
      JdbcConnectionFactory.getInstance().returnConnection(connection);
    }
  }
}
