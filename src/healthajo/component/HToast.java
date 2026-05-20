package healthajo.component;

import healthajo.component.theme.AppTheme;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * 화면 우하단 2.5초 표시 후 자동 소멸 토스트.
 *
 * HToast.success(frame, "저장되었습니다.");
 * HToast.error(frame, "삭제에 실패했습니다.");
 */
public class HToast {

    public static void success(JFrame parent, String message) {
        show(parent, message, AppTheme.SUCCESS_DIM, AppTheme.SUCCESS);
    }

    public static void error(JFrame parent, String message) {
        show(parent, message, AppTheme.DANGER_DIM, AppTheme.DANGER);
    }

    public static void info(JFrame parent, String message) {
        show(parent, message, AppTheme.PRIMARY_TINT, AppTheme.PRIMARY);
    }

    private static void show(JFrame parent, String message, Color bg, Color accent) {
        JWindow toast = new JWindow(parent);
        toast.setBackground(new Color(0, 0, 0, 0));

        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppTheme.R_MD, AppTheme.R_MD);
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, AppTheme.R_MD, AppTheme.R_MD);
                g2.dispose();
            }
        };
        panel.setOpaque(false);

        JLabel label = new JLabel(message, SwingConstants.CENTER);
        label.setFont(AppTheme.BODY_SM);
        label.setForeground(accent);
        label.setBorder(new EmptyBorder(0, AppTheme.SP_4, 0, AppTheme.SP_4));
        panel.add(label, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(310, 48));

        toast.add(panel);
        toast.pack();

        Point loc   = parent.getLocationOnScreen();
        Dimension ps = parent.getSize();
        toast.setLocation(loc.x + ps.width - 330, loc.y + ps.height - 90);
        toast.setVisible(true);

        new Timer(2500, e -> toast.dispose()).start();
    }
}
