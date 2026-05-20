package healthajo.component;

import healthajo.component.theme.AppTheme;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

/**
 * 커스텀 탭 패널.
 * 기본 JTabbedPane 대신 사용. 플랫 디자인, 언더라인 인디케이터.
 *
 * HTabPanel tabs = new HTabPanel();
 * tabs.addTab("기본 정보", infoPanel);
 * tabs.addTab("스케줄",    schedulePanel);
 */
public class HTabPanel extends JPanel {

    private final CardLayout   cards     = new CardLayout();
    private final JPanel       content   = new JPanel(cards);
    private final JPanel       tabBar    = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    private final List<JLabel> tabLabels = new ArrayList<>();
    private int activeIndex = 0;

    public HTabPanel() {
        setLayout(new BorderLayout());
        setBackground(AppTheme.SURFACE);
        setOpaque(true);

        tabBar.setBackground(AppTheme.SURFACE);
        tabBar.setOpaque(true);
        tabBar.setBorder(new MatteBorder(0, 0, 1, 0, AppTheme.BORDER));

        content.setBackground(AppTheme.SURFACE);
        content.setOpaque(true);

        add(tabBar,  BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
    }

    public void addTab(String title, Component panel) {
        int    idx = tabLabels.size();
        String key = String.valueOf(idx);

        JLabel lbl = new JLabel(title, SwingConstants.CENTER);
        lbl.setFont(AppTheme.BODY_SM);
        lbl.setOpaque(true);
        lbl.setBackground(AppTheme.SURFACE);
        lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        applyTabStyle(lbl, idx == 0);

        lbl.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { switchTo(idx); }
            @Override public void mouseEntered(MouseEvent e) { if (idx != activeIndex) lbl.setForeground(AppTheme.TEXT); }
            @Override public void mouseExited(MouseEvent e)  { if (idx != activeIndex) lbl.setForeground(AppTheme.TEXT_MUTED); }
        });

        tabLabels.add(lbl);
        tabBar.add(lbl);
        content.add(panel, key);
        if (idx == 0) cards.show(content, key);
    }

    public void switchTo(int idx) {
        if (idx < 0 || idx >= tabLabels.size() || idx == activeIndex) return;
        applyTabStyle(tabLabels.get(activeIndex), false);
        activeIndex = idx;
        applyTabStyle(tabLabels.get(activeIndex), true);
        cards.show(content, String.valueOf(idx));
    }

    private static void applyTabStyle(JLabel lbl, boolean active) {
        lbl.setForeground(active ? AppTheme.PRIMARY : AppTheme.TEXT_MUTED);
        if (active) {
            lbl.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 2, 0, AppTheme.PRIMARY),
                new EmptyBorder(AppTheme.SP_3, AppTheme.SP_5, AppTheme.SP_3 - 2, AppTheme.SP_5)));
        } else {
            lbl.setBorder(new EmptyBorder(AppTheme.SP_3, AppTheme.SP_5, AppTheme.SP_3, AppTheme.SP_5));
        }
    }
}
