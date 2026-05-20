package healthajo.component;

import healthajo.component.theme.AppTheme;
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * 날짜 선택 컴포넌트.
 * 텍스트 직접 입력(YYYY-MM-DD) 또는 ▼ 버튼으로 달력 팝업 선택.
 *
 * HDatePicker picker = new HDatePicker("시작일 선택");
 * picker.setDate(LocalDate.now());
 * LocalDate date = picker.getDate();
 * String text    = picker.getText(); // "2025-05-20"
 */
public class HDatePicker extends JPanel {

    public static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String[] DOW = {"일", "월", "화", "수", "목", "금", "토"};

    private final HTextField  textField;
    private LocalDate         selectedDate  = null;
    private LocalDate         viewMonth;
    private JWindow           popup         = null;
    private JPanel            calContent;
    private JLabel            monthLabel;
    private JPanel            outerPanel;
    private AWTEventListener  globalListener;

    public HDatePicker() { this("YYYY-MM-DD"); }

    public HDatePicker(String placeholder) {
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);
        setMaximumSize(new Dimension(Short.MAX_VALUE, 44));
        setPreferredSize(new Dimension(200, 44));

        textField = new HTextField(placeholder);
        textField.setMaximumSize(new Dimension(Short.MAX_VALUE, 44));

        JButton calBtn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.INPUT_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setFont(AppTheme.font(Font.PLAIN, 12));
                g2.setColor(getModel().isRollover() ? AppTheme.PRIMARY : AppTheme.TEXT_MUTED);
                FontMetrics fm = g2.getFontMetrics();
                String t = "▼";
                g2.drawString(t, (getWidth() - fm.stringWidth(t)) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        calBtn.setPreferredSize(new Dimension(30, 44));
        calBtn.setContentAreaFilled(false);
        calBtn.setBorderPainted(false);
        calBtn.setFocusPainted(false);
        calBtn.setRolloverEnabled(true);
        calBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        calBtn.addActionListener(e -> togglePopup());

        add(textField, BorderLayout.CENTER);
        add(calBtn,    BorderLayout.EAST);

        textField.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) {
                try {
                    String t = textField.getText().trim();
                    if (!t.isEmpty()) selectedDate = LocalDate.parse(t, FMT);
                } catch (DateTimeParseException ignored) {}
            }
        });
    }

    // ── API ───────────────────────────────────────────────────────────────────

    public LocalDate getDate() {
        try {
            String t = textField.getText().trim();
            if (!t.isEmpty()) return LocalDate.parse(t, FMT);
        } catch (DateTimeParseException ignored) {}
        return selectedDate;
    }

    public String getText() { return textField.getText().trim(); }

    public void setDate(LocalDate date) {
        selectedDate = date;
        textField.setText(date != null ? date.format(FMT) : "");
    }

    // ── Popup ────────────────────────────────────────────────────────────────

    private void togglePopup() {
        if (popup != null && popup.isVisible()) closePopup();
        else {
            viewMonth = (selectedDate != null ? selectedDate : LocalDate.now()).withDayOfMonth(1);
            openPopup();
        }
    }

    private void openPopup() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        popup = new JWindow(owner);
        popup.setBackground(new Color(0, 0, 0, 0));

        outerPanel = new JPanel(new BorderLayout(0, AppTheme.SP_1)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppTheme.R_MD, AppTheme.R_MD);
                g2.setColor(AppTheme.BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, AppTheme.R_MD, AppTheme.R_MD);
                g2.dispose();
            }
        };
        outerPanel.setOpaque(false);
        outerPanel.setBorder(new EmptyBorder(AppTheme.SP_2, AppTheme.SP_3, AppTheme.SP_3, AppTheme.SP_3));
        outerPanel.setPreferredSize(new Dimension(280, 290));

        monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(AppTheme.LABEL);
        monthLabel.setForeground(AppTheme.TEXT);
        updateMonthLabel();

        JButton prevBtn = navBtn("◀");
        JButton nextBtn = navBtn("▶");
        prevBtn.addActionListener(e -> { viewMonth = viewMonth.minusMonths(1); rebuildGrid(); });
        nextBtn.addActionListener(e -> { viewMonth = viewMonth.plusMonths(1); rebuildGrid(); });

        JPanel nav = new JPanel(new BorderLayout());
        nav.setOpaque(false);
        nav.setBorder(new EmptyBorder(0, 0, AppTheme.SP_2, 0));
        nav.add(prevBtn,    BorderLayout.WEST);
        nav.add(monthLabel, BorderLayout.CENTER);
        nav.add(nextBtn,    BorderLayout.EAST);

        calContent = buildDayGrid();
        outerPanel.add(nav,        BorderLayout.NORTH);
        outerPanel.add(calContent, BorderLayout.CENTER);

        popup.add(outerPanel);
        popup.pack();

        try {
            Point loc = getLocationOnScreen();
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            int popH = popup.getPreferredSize().height;
            int y    = loc.y + getHeight() + 4;
            if (y + popH > screen.height - 40) y = loc.y - popH - 4;
            popup.setLocation(loc.x, y);
        } catch (Exception ignored) {}
        popup.setVisible(true);

        globalListener = event -> {
            if (event instanceof MouseEvent me && me.getID() == MouseEvent.MOUSE_PRESSED) {
                if (popup != null && !SwingUtilities.isDescendingFrom(me.getComponent(), popup)) {
                    SwingUtilities.invokeLater(this::closePopup);
                }
            }
        };
        Toolkit.getDefaultToolkit().addAWTEventListener(globalListener, AWTEvent.MOUSE_EVENT_MASK);
    }

    private void rebuildGrid() {
        updateMonthLabel();
        outerPanel.remove(calContent);
        calContent = buildDayGrid();
        outerPanel.add(calContent, BorderLayout.CENTER);
        outerPanel.revalidate();
        outerPanel.repaint();
        if (popup != null) popup.pack();
    }

    private void closePopup() {
        if (globalListener != null) {
            Toolkit.getDefaultToolkit().removeAWTEventListener(globalListener);
            globalListener = null;
        }
        if (popup != null) { popup.dispose(); popup = null; }
    }

    private void updateMonthLabel() {
        if (monthLabel != null)
            monthLabel.setText(viewMonth.getYear() + "년 " + viewMonth.getMonthValue() + "월");
    }

    private JPanel buildDayGrid() {
        JPanel grid = new JPanel(new GridLayout(7, 7, 2, 2));
        grid.setOpaque(false);

        // Day-of-week headers
        for (int i = 0; i < DOW.length; i++) {
            JLabel h = new JLabel(DOW[i], SwingConstants.CENTER);
            h.setFont(AppTheme.CAPTION);
            h.setForeground(i == 0 ? AppTheme.DANGER : AppTheme.TEXT_MUTED);
            grid.add(h);
        }

        // Empty cells before month start (Sunday-first grid)
        // Java DayOfWeek: MON=1..SUN=7 → Sun col = 7%7=0
        int startDow = viewMonth.getDayOfWeek().getValue() % 7;
        for (int i = 0; i < startDow; i++) grid.add(new JLabel());

        LocalDate today = LocalDate.now();
        for (int day = 1; day <= viewMonth.lengthOfMonth(); day++) {
            final LocalDate d = viewMonth.withDayOfMonth(day);
            final boolean isSun = d.getDayOfWeek() == DayOfWeek.SUNDAY;
            final boolean isTod = d.equals(today);
            final boolean isSel = d.equals(selectedDate);

            JButton btn = new JButton(String.valueOf(day)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = getWidth(), h = getHeight();
                    int r = Math.min(w, h) - 4;
                    int ox = (w - r) / 2, oy = (h - r) / 2;
                    if (isSel) {
                        g2.setColor(AppTheme.PRIMARY);
                        g2.fillOval(ox, oy, r, r);
                    } else if (getModel().isRollover()) {
                        g2.setColor(AppTheme.ROW_HOVER);
                        g2.fillOval(ox, oy, r, r);
                    }
                    g2.setFont(AppTheme.font(isSel || isTod ? Font.BOLD : Font.PLAIN, 12));
                    g2.setColor(isSel ? Color.WHITE
                               : isTod ? AppTheme.PRIMARY
                               : isSun ? AppTheme.DANGER
                               : AppTheme.TEXT);
                    FontMetrics fm = g2.getFontMetrics();
                    String t = getText();
                    g2.drawString(t, (w - fm.stringWidth(t)) / 2, (h + fm.getAscent() - fm.getDescent()) / 2);
                    if (isTod && !isSel) {
                        g2.setColor(AppTheme.PRIMARY);
                        g2.fillOval(w / 2 - 2, h - 5, 4, 4);
                    }
                    g2.dispose();
                }
                @Override protected void paintBorder(Graphics g) {}
            };
            btn.setOpaque(false);
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setRolloverEnabled(true);
            btn.setFont(AppTheme.SMALL);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> { setDate(d); closePopup(); });
            grid.add(btn);
        }
        return grid;
    }

    private static JButton navBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(AppTheme.CAPTION);
        btn.setForeground(AppTheme.TEXT_MUTED);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setRolloverEnabled(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(30, 26));
        return btn;
    }
}
