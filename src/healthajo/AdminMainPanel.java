package healthajo;

import healthajo.attendance.view.admin.AttendancePanel;
import healthajo.component.HSidebar;
import healthajo.component.theme.AppTheme;
import healthajo.membership.view.MembershipAdminPanel;
import healthajo.program.view.admin.ProgramListPanel;
import healthajo.reservation.view.admin.ReservationPanel;
import healthajo.session.view.admin.SessionListPanel;
import healthajo.user.view.admin.UserListPanel;
import java.awt.*;
import javax.swing.*;

/**
 * 관리자 메인 프레임.
 *
 * 로그인(AuthResult.Role.ADMIN) 이후 열리는 최상위 창.
 * 좌측 HSidebar + 우측 CardLayout 컨텐츠 구조.
 */
public class AdminMainPanel extends JFrame {

    public AdminMainPanel() {
        setTitle("건강하조 — 관리자");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1024, 680));
        setSize(1280, 800);
        setExtendedState(MAXIMIZED_BOTH);
        setResizable(true);
        getContentPane().setBackground(AppTheme.BG);
        setLayout(new BorderLayout());

        // ── 컨텐츠 패널 (CardLayout) ─────────────────────────────────────────────
        CardLayout cards   = new CardLayout();
        JPanel     content = new JPanel(cards);
        content.setBackground(AppTheme.BG);

        content.add(new UserListPanel(),        "users");
        content.add(new ProgramListPanel(),     "programs");
        content.add(new SessionListPanel(),     "sessions");
        content.add(new AttendancePanel(),      "attendance");
        content.add(new ReservationPanel(),     "reservations");
        content.add(new MembershipAdminPanel(), "memberships");

        // ── 사이드바 ─────────────────────────────────────────────────────────────
        HSidebar sidebar = new HSidebar("관리자");
        sidebar.addMenu("사용자 관리",  () -> cards.show(content, "users"));
        sidebar.addMenu("프로그램 관리",() -> cards.show(content, "programs"));
        sidebar.addMenu("세션 관리",    () -> cards.show(content, "sessions"));
        sidebar.addMenu("출석 관리",    () -> cards.show(content, "attendance"));
        sidebar.addMenu("예약 관리",    () -> cards.show(content, "reservations"));
        sidebar.addMenu("회원권 관리",  () -> cards.show(content, "memberships"));

        add(sidebar, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);

        cards.show(content, "users");
        sidebar.setActive("사용자 관리");

        setLocationRelativeTo(null);
        setVisible(true);
    }
}
