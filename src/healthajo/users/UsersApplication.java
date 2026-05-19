package healthajo.users;

import javax.swing.*;

public class UsersApplication {

    public static void launch() {
        JFrame frame = new JFrame("회원 관리");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(750, 500);
        frame.setLocationRelativeTo(null);

        UsersDAO dao = new UsersDAO();
        UsersView view = new UsersView(dao);
        frame.add(view);

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(UsersApplication::launch);
    }
}