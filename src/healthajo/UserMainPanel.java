package healthajo;

import healthajo.attendance.view.user.MyAttendancePanel;
import healthajo.component.HSidebar;
import healthajo.component.theme.AppTheme;
import healthajo.membership.view.user.MyMembershipPanel;
import healthajo.program.view.user.ProgramListPanel;
import healthajo.reservation.view.user.MyReservationPanel;
import java.awt.*;
import javax.swing.*;

/**
 * 회원 메인 프레임.
 *
 * 로그인(AuthResult.Role.USER) 이후 열리는 최상위 창.
 * 좌측 HSidebar + 우측 CardLayout 컨텐츠 구조.
 */
public class UserMainPanel extends JFrame {

    public UserMainPanel(Long userId) {
        setTitle("건강하조");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1024, 680));
        setSize(1280, 800);
        setExtendedState(MAXIMIZED_BOTH);
        setResizable(true);
        getContentPane().setBackground(AppTheme.BG);
        setLayout(new BorderLayout());

        CardLayout cards   = new CardLayout();
        JPanel     content = new JPanel(cards);
        content.setBackground(AppTheme.BG);

        content.add(new ProgramListPanel(userId),    "programs");
        content.add(new MyReservationPanel(userId),  "reservations");
        content.add(new MyMembershipPanel(userId),   "memberships");
        content.add(new MyAttendancePanel(userId),   "attendance");

        HSidebar sidebar = new HSidebar("회원");
        sidebar.addMenu("프로그램 예약", () -> cards.show(content, "programs"));
        sidebar.addMenu("나의 예약",     () -> cards.show(content, "reservations"));
        sidebar.addMenu("나의 회원권",   () -> cards.show(content, "memberships"));
        sidebar.addMenu("출석 내역",     () -> cards.show(content, "attendance"));

        add(sidebar, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);

        cards.show(content, "programs");
        sidebar.setActive("프로그램 예약");

        setLocationRelativeTo(null);
        setVisible(true);
    }
}
