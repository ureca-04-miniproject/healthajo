package healthajo.component;

import healthajo.component.theme.AppTheme;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * 스타일이 적용된 텍스트 입력 필드.
 *
 * HTextField f = new HTextField("검색어 입력");
 * f.setPreferredSize(new Dimension(300, 44));  // 필요시 크기 조정
 */
public class HTextField extends JTextField {

    private String placeholder;

    public HTextField() {
        this("");
    }

    public HTextField(String placeholder) {
        this.placeholder = placeholder;
        init();
    }

    private void init() {
        setOpaque(false);
        setBorder(new EmptyBorder(AppTheme.SP_3, AppTheme.SP_4, AppTheme.SP_3, AppTheme.SP_4));
        setFont(AppTheme.BODY_SM);
        setForeground(AppTheme.TEXT);
        setCaretColor(AppTheme.PRIMARY);
        setPreferredSize(new Dimension(200, 44));
        setMaximumSize(new Dimension(Short.MAX_VALUE, 44));
        addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { repaint(); }
            @Override public void focusLost(FocusEvent e)   { repaint(); }
        });
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(AppTheme.INPUT_BG);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), AppTheme.R_MD, AppTheme.R_MD));
        g2.dispose();
        super.paintComponent(g);
        if (getText().isEmpty() && placeholder != null && !placeholder.isEmpty()) {
            Graphics2D ph = (Graphics2D) g.create();
            ph.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            ph.setColor(AppTheme.TEXT_MUTED);
            ph.setFont(getFont());
            FontMetrics fm = ph.getFontMetrics();
            ph.drawString(placeholder, getInsets().left,
                         (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            ph.dispose();
        }
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(isFocusOwner() ? 1.5f : 1f));
        g2.setColor(isFocusOwner() ? AppTheme.PRIMARY : AppTheme.INPUT_BORDER);
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, AppTheme.R_MD, AppTheme.R_MD));
        g2.dispose();
    }
}
