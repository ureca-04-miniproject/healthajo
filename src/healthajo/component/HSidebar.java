package healthajo.component;

import healthajo.component.theme.AppTheme;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/**
 * 좌측 네비게이션 사이드바.
 *
 * HSidebar sidebar = new HSidebar("관리자");
 * sidebar.addMenu("사용자 관리", () -> cards.show(content, "users"));
 * sidebar.setActive("사용자 관리");
 */
public class HSidebar extends JPanel {

    private final List<SidebarItem> items = new ArrayList<>();

    public HSidebar(String role) {
        setPreferredSize(new Dimension(200, 0));
        setBackground(AppTheme.SURFACE);
        setBorder(new MatteBorder(0, 0, 0, 1, AppTheme.BORDER));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(buildHeader(role));
        add(buildDivider());
        add(Box.createVerticalStrut(AppTheme.SP_2));
    }

    public void addMenu(String label, Runnable action) {
        SidebarItem item = new SidebarItem(label, action);
        items.add(item);
        add(item);
    }

    public void setActive(String label) {
        items.forEach(item -> item.setActive(item.label.equals(label)));
    }

    // ── Header ─────────────────────────────────────────────────────────────────

    private JPanel buildHeader(String role) {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(AppTheme.SP_5, AppTheme.SP_5, AppTheme.SP_4, AppTheme.SP_5));
        header.setMaximumSize(new Dimension(200, 76));
        header.setAlignmentX(LEFT_ALIGNMENT);

        JLabel logo = HLabel.h3("건강하조");
        logo.setForeground(AppTheme.PRIMARY);
        logo.setAlignmentX(LEFT_ALIGNMENT);

        JLabel roleLabel = HLabel.muted(role);
        roleLabel.setAlignmentX(LEFT_ALIGNMENT);

        header.add(logo);
        header.add(Box.createVerticalStrut(2));
        header.add(roleLabel);
        return header;
    }

    private JPanel buildDivider() {
        JPanel div = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(AppTheme.BORDER);
                g.fillRect(0, 0, getWidth(), 1);
            }
        };
        div.setOpaque(false);
        div.setMaximumSize(new Dimension(200, 1));
        div.setPreferredSize(new Dimension(200, 1));
        div.setAlignmentX(LEFT_ALIGNMENT);
        return div;
    }

    // ── Menu item ──────────────────────────────────────────────────────────────

    private class SidebarItem extends JPanel {
        final String label;
        private final JLabel textLabel;
        private boolean active  = false;
        private boolean hovered = false;

        SidebarItem(String label, Runnable action) {
            this.label = label;
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(0, AppTheme.SP_3, 0, AppTheme.SP_3));
            setMaximumSize(new Dimension(200, 40));
            setPreferredSize(new Dimension(200, 40));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(LEFT_ALIGNMENT);

            textLabel = new JLabel(label);
            textLabel.setFont(AppTheme.BODY_SM);
            textLabel.setForeground(AppTheme.TEXT);
            textLabel.setBorder(new EmptyBorder(0, AppTheme.SP_2, 0, 0));
            add(textLabel, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    items.forEach(i -> i.setActive(false));
                    setActive(true);
                    action.run();
                }
                @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
            });
        }

        void setActive(boolean v) {
            active = v;
            textLabel.setFont(active ? AppTheme.LABEL : AppTheme.BODY_SM);
            textLabel.setForeground(active ? AppTheme.PRIMARY : AppTheme.TEXT);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (active || hovered) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(active ? AppTheme.PRIMARY_TINT : new Color(0, 0, 0, 6));
                g2.fill(new RoundRectangle2D.Float(
                    AppTheme.SP_1, 2, getWidth() - AppTheme.SP_2, getHeight() - 4,
                    AppTheme.R_SM, AppTheme.R_SM));
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }
}
