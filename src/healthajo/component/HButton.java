package healthajo.component;

import healthajo.component.theme.AppTheme;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;

/**
 * 버튼 컴포넌트. 팩토리 메서드로 variant 지정.
 *
 * HButton.primary("저장")
 * HButton.secondary("취소")
 * HButton.ghost("닫기")
 * HButton.danger("삭제")
 * HButton.primary("저장", HButton.Size.SM)
 */
public class HButton extends JButton {

    public enum Variant { PRIMARY, SECONDARY, GHOST, DANGER }
    public enum Size    { SM, MD, LG }

    // ── Factory
    public static HButton primary(String text)            { return new HButton(text, Variant.PRIMARY,   Size.MD); }
    public static HButton secondary(String text)          { return new HButton(text, Variant.SECONDARY, Size.MD); }
    public static HButton ghost(String text)              { return new HButton(text, Variant.GHOST,     Size.MD); }
    public static HButton danger(String text)             { return new HButton(text, Variant.DANGER,    Size.MD); }
    public static HButton primary(String text, Size sz)   { return new HButton(text, Variant.PRIMARY,   sz); }
    public static HButton secondary(String text, Size sz) { return new HButton(text, Variant.SECONDARY, sz); }
    public static HButton ghost(String text, Size sz)     { return new HButton(text, Variant.GHOST,     sz); }
    public static HButton danger(String text, Size sz)    { return new HButton(text, Variant.DANGER,    sz); }

    private final Variant variant;
    private final Size    size;

    private HButton(String text, Variant variant, Size size) {
        super(text);
        this.variant = variant;
        this.size    = size;
        setRolloverEnabled(true);
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        applySize(size);
    }

    private void applySize(Size size) {
        switch (size) {
            case SM -> setFont(AppTheme.SMALL);
            case LG -> setFont(AppTheme.BODY);
            default -> setFont(AppTheme.LABEL);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        int textW = (fm != null && getText() != null) ? fm.stringWidth(getText()) : 60;
        return switch (size) {
            case SM -> new Dimension(Math.max(60,  textW + AppTheme.SP_4 * 2), 32);
            case LG -> new Dimension(Math.max(140, textW + AppTheme.SP_6 * 2), 48);
            default -> new Dimension(Math.max(80,  textW + AppTheme.SP_5 * 2), 40);
        };
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        boolean hov = getModel().isRollover();
        boolean prs = getModel().isPressed();

        Color textColor;
        switch (variant) {
            case PRIMARY -> {
                Color base = prs ? AppTheme.PRIMARY_PRESS : hov ? AppTheme.PRIMARY_HOVER : AppTheme.PRIMARY;
                g2.setColor(base);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), AppTheme.R_MD, AppTheme.R_MD));
                textColor = Color.WHITE;
            }
            case SECONDARY -> {
                g2.setColor(hov ? AppTheme.SURFACE_RAISED : AppTheme.SURFACE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), AppTheme.R_MD, AppTheme.R_MD));
                g2.setColor(AppTheme.BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, AppTheme.R_MD, AppTheme.R_MD));
                textColor = AppTheme.TEXT;
            }
            case GHOST -> {
                if (hov || prs) {
                    g2.setColor(new Color(0, 0, 0, prs ? 10 : 6));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), AppTheme.R_MD, AppTheme.R_MD));
                }
                textColor = AppTheme.TEXT_SECONDARY;
            }
            case DANGER -> {
                Color base = prs ? new Color(200, 50, 50) : hov ? new Color(235, 65, 65) : AppTheme.DANGER;
                g2.setColor(base);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), AppTheme.R_MD, AppTheme.R_MD));
                textColor = Color.WHITE;
            }
            default -> textColor = AppTheme.TEXT;
        }

        g2.setFont(getFont());
        g2.setColor(textColor);
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth()  - fm.stringWidth(getText())) / 2;
        int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(getText(), x, y);
        g2.dispose();
    }
}
