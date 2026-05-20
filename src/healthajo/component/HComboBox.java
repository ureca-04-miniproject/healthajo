package healthajo.component;

import healthajo.component.theme.AppTheme;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;

/**
 * 스타일이 적용된 드롭다운.
 *
 * HComboBox<String> box = new HComboBox<>(new String[]{"회원", "관리자"});
 */
public class HComboBox<T> extends JComboBox<T> {

    public HComboBox() { init(); }

    public HComboBox(T[] items) { super(items); init(); }

    public HComboBox(DefaultComboBoxModel<T> model) { super(model); init(); }

    private void init() {
        setFont(AppTheme.BODY_SM);
        setForeground(AppTheme.TEXT);
        setBackground(AppTheme.INPUT_BG);
        setMaximumRowCount(8);
        setPreferredSize(new Dimension(200, 44));
        setMaximumSize(new Dimension(Short.MAX_VALUE, 44));

        setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = new JButton() {
                    // 고정 너비(26px)로 텍스트 표시 영역 침범 방지
                    @Override public Dimension getPreferredSize() { return new Dimension(26, 0); }
                    @Override public Dimension getMinimumSize()   { return getPreferredSize(); }
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                            RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(AppTheme.INPUT_BG);
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        g2.setFont(AppTheme.font(Font.PLAIN, 10));
                        g2.setColor(AppTheme.TEXT_MUTED);
                        FontMetrics fm = g2.getFontMetrics();
                        String arrow = "▾";
                        g2.drawString(arrow,
                            (getWidth()  - fm.stringWidth(arrow)) / 2,
                            (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                        g2.dispose();
                    }
                    @Override protected void paintBorder(Graphics g) {}
                };
                btn.setBorder(BorderFactory.createEmptyBorder());
                btn.setOpaque(false);
                btn.setContentAreaFilled(false);
                btn.setBorderPainted(false);
                btn.setFocusPainted(false);
                return btn;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle b, boolean hasFocus) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.INPUT_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, comboBox.getWidth(), comboBox.getHeight(),
                                                   AppTheme.R_MD, AppTheme.R_MD));
                g2.setColor(hasFocus ? AppTheme.PRIMARY : AppTheme.INPUT_BORDER);
                g2.setStroke(new BasicStroke(hasFocus ? 1.5f : 1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f,
                        comboBox.getWidth() - 1, comboBox.getHeight() - 1, AppTheme.R_MD, AppTheme.R_MD));
                g2.dispose();
            }
        });

        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                l.setFont(AppTheme.BODY_SM);
                l.setForeground(isSelected ? AppTheme.TEXT : AppTheme.TEXT_SECONDARY);
                l.setBackground(isSelected ? AppTheme.ROW_SELECTED : AppTheme.SURFACE);
                l.setBorder(new EmptyBorder(AppTheme.SP_2, AppTheme.SP_4, AppTheme.SP_2, AppTheme.SP_4));
                l.setOpaque(true);
                return l;
            }
        });
    }
}
