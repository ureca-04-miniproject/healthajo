package healthajo.component;

import healthajo.component.theme.AppTheme;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;

/**
 * 상태 배지. HBadge.active(), HBadge.confirmed() 등 팩토리 메서드로 생성.
 * 커스텀: HBadge.of("TEXT", bgColor, fgColor)
 */
public class HBadge extends JComponent {

    public static HBadge active()            { return new HBadge("ACTIVE",            AppTheme.SUCCESS_DIM,         AppTheme.SUCCESS); }
    public static HBadge expired()           { return new HBadge("EXPIRED",           AppTheme.DANGER_DIM,          AppTheme.DANGER); }
    public static HBadge confirmed()         { return new HBadge("CONFIRMED",         AppTheme.PRIMARY_TINT,        AppTheme.PRIMARY); }
    public static HBadge membershipIssued()  { return new HBadge("MEMBERSHIP_ISSUED", new Color(204, 251, 241),     new Color(13, 148, 136)); }
    public static HBadge cancelled()         { return new HBadge("CANCELLED",         AppTheme.WARNING_DIM,         AppTheme.WARNING); }
    public static HBadge open()              { return new HBadge("OPEN",              AppTheme.SUCCESS_DIM,         AppTheme.SUCCESS); }
    public static HBadge pending()           { return new HBadge("PENDING",           AppTheme.WARNING_DIM,         AppTheme.WARNING); }
    public static HBadge attended()          { return new HBadge("ATTENDED",          AppTheme.SUCCESS_DIM,         AppTheme.SUCCESS); }
    public static HBadge absent()            { return new HBadge("ABSENT",            AppTheme.DANGER_DIM,          AppTheme.DANGER); }
    public static HBadge of(String text, Color bg, Color fg) { return new HBadge(text, bg, fg); }

    private final String text;
    private final Color  bg;
    private final Color  fg;

    private HBadge(String text, Color bg, Color fg) {
        this.text = text;
        this.bg   = bg;
        this.fg   = fg;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(bg);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), AppTheme.R_SM, AppTheme.R_SM));
        g2.setFont(AppTheme.CAPTION);
        g2.setColor(fg);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text,
            (getWidth()  - fm.stringWidth(text)) / 2,
            (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
        g2.dispose();
    }

    @Override public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(AppTheme.CAPTION);
        return new Dimension(fm.stringWidth(text) + 16, fm.getHeight() + 8);
    }
    @Override public Dimension getMinimumSize() { return getPreferredSize(); }
    @Override public Dimension getMaximumSize() { return getPreferredSize(); }
}
