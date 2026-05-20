package healthajo.component;

import healthajo.component.theme.AppTheme;
import java.awt.*;
import javax.swing.*;

/** 타이포그래피 팩토리. HLabel.h2("제목"), HLabel.muted("설명") 형태로 사용. */
public class HLabel extends JLabel {

    public static HLabel h1(String text)    { return new HLabel(text, AppTheme.H1,      AppTheme.TEXT); }
    public static HLabel h2(String text)    { return new HLabel(text, AppTheme.H2,      AppTheme.TEXT); }
    public static HLabel h3(String text)    { return new HLabel(text, AppTheme.H3,      AppTheme.TEXT); }
    public static HLabel body(String text)  { return new HLabel(text, AppTheme.BODY,    AppTheme.TEXT); }
    public static HLabel small(String text) { return new HLabel(text, AppTheme.SMALL,   AppTheme.TEXT_SECONDARY); }
    public static HLabel muted(String text) { return new HLabel(text, AppTheme.CAPTION, AppTheme.TEXT_MUTED); }
    public static HLabel label(String text) { return new HLabel(text, AppTheme.LABEL,   AppTheme.TEXT_SECONDARY); }
    public static HLabel of(String text, Font font, Color color) { return new HLabel(text, font, color); }

    private HLabel(String text, Font font, Color color) {
        super(text);
        setFont(font);
        setForeground(color);
        setOpaque(false);
    }
}
