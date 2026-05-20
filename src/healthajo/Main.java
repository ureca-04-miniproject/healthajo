package healthajo;

import healthajo.jdbc.factory.JdbcTableAutoInit;
import javax.swing.SwingUtilities;

public class Main {

  public static void main(String[] args) {
    JdbcTableAutoInit.initIfNotExist(); // ddl settup
    // 스윙 시작 지점
    SwingUtilities.invokeLater(SwingStarter::startSwing);
  }

}
