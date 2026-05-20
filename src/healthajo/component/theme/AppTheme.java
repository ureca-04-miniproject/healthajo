package healthajo.component.theme;

import java.awt.*;

public final class AppTheme {
    private AppTheme() {}

    // ── Font family (fallback: Inter → Segoe UI → Korean → SansSerif)
    private static final String FONT_FAMILY;
    static {
        String[] candidates = {
            "Inter", "Segoe UI", "Apple SD Gothic Neo",
            "Noto Sans KR", "NanumBarunGothic", "NanumGothic", "맑은 고딕"
        };
        String chosen = "SansSerif";
        for (String name : candidates) {
            Font f = new Font(name, Font.PLAIN, 12);
            if (!f.getFamily().equals("Dialog")) { chosen = name; break; }
        }
        FONT_FAMILY = chosen;
    }

    public static Font font(int style, int size) {
        return new Font(FONT_FAMILY, style, size);
    }

    // ── Typography
    public static final Font H1      = font(Font.BOLD,  22);
    public static final Font H2      = font(Font.BOLD,  18);
    public static final Font H3      = font(Font.BOLD,  15);
    public static final Font BODY    = font(Font.PLAIN, 14);
    public static final Font BODY_SM = font(Font.PLAIN, 13);
    public static final Font SMALL   = font(Font.PLAIN, 12);
    public static final Font CAPTION = font(Font.PLAIN, 11);
    public static final Font LABEL   = font(Font.BOLD,  13);

    // ── Surface
    public static final Color BG             = new Color(248, 249, 252);
    public static final Color SURFACE        = new Color(255, 255, 255);
    public static final Color SURFACE_RAISED = new Color(241, 244, 250);
    public static final Color BORDER         = new Color(226, 232, 240);
    public static final Color BORDER_SUBTLE  = new Color(241, 245, 249);

    // ── Accent (indigo-500 계열)
    public static final Color PRIMARY        = new Color(99,  102, 241);
    public static final Color PRIMARY_HOVER  = new Color(79,  70,  229);
    public static final Color PRIMARY_PRESS  = new Color(67,  56,  202);
    public static final Color PRIMARY_TINT   = new Color(99,  102, 241, 28);

    public static final Color DANGER         = new Color(239, 68,  68);
    public static final Color DANGER_DIM     = new Color(254, 242, 242);
    public static final Color SUCCESS        = new Color(34,  197, 94);
    public static final Color SUCCESS_DIM    = new Color(240, 253, 244);
    public static final Color WARNING        = new Color(245, 158, 11);
    public static final Color WARNING_DIM    = new Color(255, 251, 235);

    // ── Text
    public static final Color TEXT           = new Color(15,  23,  42);
    public static final Color TEXT_SECONDARY = new Color(71,  85,  105);
    public static final Color TEXT_MUTED     = new Color(148, 163, 184);
    public static final Color TEXT_DISABLED  = new Color(203, 213, 225);

    // ── Input
    public static final Color INPUT_BG       = new Color(255, 255, 255);
    public static final Color INPUT_BORDER   = new Color(203, 213, 225);

    // ── Table rows
    public static final Color ROW_EVEN       = SURFACE;
    public static final Color ROW_ODD        = new Color(248, 249, 252);
    public static final Color ROW_HOVER      = new Color(238, 242, 255);
    public static final Color ROW_SELECTED   = new Color(224, 231, 255);

    // ── Spacing (4px grid)
    public static final int SP_1  = 4;
    public static final int SP_2  = 8;
    public static final int SP_3  = 12;
    public static final int SP_4  = 16;
    public static final int SP_5  = 20;
    public static final int SP_6  = 24;
    public static final int SP_8  = 32;
    public static final int SP_10 = 40;

    // ── Corner radius (arc diameter — pass directly to fillRoundRect / RoundRectangle2D)
    public static final int R_SM = 8;   // 4px radius — badges, chips
    public static final int R_MD = 16;  // 8px radius — buttons, inputs
    public static final int R_LG = 24;  // 12px radius — cards, panels
}
