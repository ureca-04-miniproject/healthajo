package healthajo;

import healthajo.jdbc.factory.JdbcTableAutoInit;

public class Main {

  public static void main(String[] args) {
    JdbcTableAutoInit.initIfNotExist();

  }

}
