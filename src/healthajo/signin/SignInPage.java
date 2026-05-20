package healthajo.signin;

import healthajo.AdminMainPanel;
import healthajo.UserMainPanel;
import healthajo.auth.AuthResult;
import healthajo.auth.AuthService;
import healthajo.component.HBadge;
import healthajo.component.HButton;
import healthajo.component.HCard;
import healthajo.component.HFormGroup;
import healthajo.component.HLabel;
import healthajo.component.HPasswordField;
import healthajo.component.HTextField;
import healthajo.component.theme.AppTheme;
import healthajo.page.BaseFrame;
import java.awt.BasicStroke;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/**
 * 로그인 화면.
 *
 * 데이터 흐름:
 *   입력(HTextField) → handleLogin() → AuthService.authenticate()
 *     → 성공: dispose() + 역할별 메인 화면 오픈 (TODO)
 *     → 실패: errLabel에 메시지 표시
 */
public class SignInPage extends BaseFrame {

    private final AuthService authService = new AuthService();

    private boolean adminMode = false;
    private float   slideX    = 0f;
    private Timer   anim;

    private JPanel          tabBg;
    private JLabel          memberTab;
    private JLabel          adminTab;
    private HFormGroup      formGroup;
    private HTextField      inputField;
    private HPasswordField  adminField;
    private JPanel          fieldWrapper;
    private CardLayout      fieldCards;
    private HLabel          errLabel;

    // ── BaseFrame ──────────────────────────────────────────────────────────────

    @Override
    protected String frameTitle() { return "건강하조"; }

    @Override
    protected JComponent buildContent() { return buildCard(); }

    // ── Card ───────────────────────────────────────────────────────────────────

    private HCard buildCard() {
        HCard card = new HCard(AppTheme.SP_8);

        card.add(buildBrandRow());
        card.add(Box.createVerticalStrut(AppTheme.SP_4));
        card.add(buildDivider());
        card.add(Box.createVerticalStrut(AppTheme.SP_4));
        card.add(buildTabSwitcher());
        card.add(Box.createVerticalStrut(AppTheme.SP_4));
        card.add(buildForm());
        card.add(Box.createVerticalStrut(AppTheme.SP_3));
        card.add(buildLoginButton());

        return card;
    }

    // ── Brand ──────────────────────────────────────────────────────────────────

    private JPanel buildBrandRow() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = HLabel.h2("건강하조");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        HBadge badge = HBadge.of("예약 관리 시스템", AppTheme.SURFACE_RAISED, AppTheme.TEXT_MUTED);
        badge.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(title);
        p.add(Box.createVerticalStrut(AppTheme.SP_2));
        p.add(badge);
        return p;
    }

    // ── Divider ────────────────────────────────────────────────────────────────

    private JSeparator buildDivider() {
        JSeparator sep = new JSeparator() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(AppTheme.BORDER);
                g.fillRect(0, getHeight() / 2, getWidth(), 1);
            }
        };
        sep.setOpaque(false);
        sep.setMaximumSize(new Dimension(Short.MAX_VALUE, 1));
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);
        return sep;
    }

    // ── Tab switcher ───────────────────────────────────────────────────────────

    private JPanel buildTabSwitcher() {
        tabBg = new JPanel(new GridLayout(1, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(AppTheme.SURFACE_RAISED);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));
                float hw = w / 2f, px = slideX * hw, ph = h - 6f;
                g2.setColor(AppTheme.SURFACE);
                g2.fill(new RoundRectangle2D.Float(px + 3, 3, hw - 6, ph, ph, ph));
                g2.setColor(AppTheme.BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(px + 3.5f, 3.5f, hw - 7, ph - 1, ph - 1, ph - 1));
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(312, 42); }
            @Override public Dimension getMaximumSize()   { return getPreferredSize(); }
        };
        tabBg.setOpaque(false);
        tabBg.setAlignmentX(Component.CENTER_ALIGNMENT);

        memberTab = tabLabel("회원",   AppTheme.PRIMARY);
        adminTab  = tabLabel("관리자", AppTheme.TEXT_MUTED);
        tabBg.add(memberTab);
        tabBg.add(adminTab);

        memberTab.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (adminMode)  switchMode(false); }
        });
        adminTab.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (!adminMode) switchMode(true); }
        });
        return tabBg;
    }

    private JLabel tabLabel(String text, Color fg) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(AppTheme.LABEL);
        l.setForeground(fg);
        l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return l;
    }

    private void switchMode(boolean toAdmin) {
        adminMode = toAdmin;
        clearError();

        float target = toAdmin ? 1f : 0f;
        if (anim != null) anim.stop();
        anim = new Timer(10, null);
        anim.addActionListener(e -> {
            float d = target - slideX;
            if (Math.abs(d) < 0.012f) { slideX = target; ((Timer) e.getSource()).stop(); }
            else                        slideX += d * 0.14f;
            tabBg.repaint();
            memberTab.setForeground(lerp(AppTheme.PRIMARY,    AppTheme.TEXT_MUTED, slideX));
            adminTab.setForeground( lerp(AppTheme.TEXT_MUTED, AppTheme.PRIMARY,    slideX));
        });
        anim.start();

        formGroup.setLabel(toAdmin ? "관리자 코드" : "전화번호");
        if (toAdmin) {
            fieldCards.show(fieldWrapper, "admin");
            adminField.setText("");
            adminField.requestFocusInWindow();
        } else {
            fieldCards.show(fieldWrapper, "phone");
            inputField.setText("");
            inputField.requestFocusInWindow();
        }
    }

    // ── Form ───────────────────────────────────────────────────────────────────

    private JPanel buildForm() {
        inputField = new HTextField("전화번호를 입력하세요");
        inputField.setPreferredSize(new Dimension(312, 48));
        inputField.setMaximumSize(new Dimension(312, 48));
        inputField.addActionListener(e -> handleLogin());

        adminField = new HPasswordField("관리자 코드를 입력하세요");
        adminField.setPreferredSize(new Dimension(312, 48));
        adminField.setMaximumSize(new Dimension(312, 48));
        adminField.addActionListener(e -> handleLogin());

        fieldCards   = new CardLayout();
        fieldWrapper = new JPanel(fieldCards);
        fieldWrapper.setOpaque(false);
        fieldWrapper.setMaximumSize(new Dimension(312, 48));
        fieldWrapper.add(inputField, "phone");
        fieldWrapper.add(adminField, "admin");

        formGroup = new HFormGroup("전화번호", fieldWrapper);
        formGroup.setAlignmentX(Component.CENTER_ALIGNMENT);
        formGroup.setMaximumSize(new Dimension(312, 80));

        errLabel = HLabel.of("", AppTheme.SMALL, AppTheme.DANGER);
        errLabel.setHorizontalAlignment(SwingConstants.CENTER);
        errLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        errLabel.setMaximumSize(new Dimension(312, 20));
        errLabel.setVisible(false);

        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        wrapper.setMaximumSize(new Dimension(312, 104));
        wrapper.add(formGroup);
        wrapper.add(Box.createVerticalStrut(AppTheme.SP_1));
        wrapper.add(errLabel);

        return wrapper;
    }

    // ── Login button ───────────────────────────────────────────────────────────

    private HButton buildLoginButton() {
        HButton btn = HButton.primary("로그인", HButton.Size.LG);
        btn.setPreferredSize(new Dimension(312, 48));
        btn.setMaximumSize(new Dimension(312, 48));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.addActionListener(e -> handleLogin());
        return btn;
    }

    // ── 인증 흐름 ──────────────────────────────────────────────────────────────

    private void handleLogin() {
        String input = adminMode
            ? new String(adminField.getPassword()).trim()
            : inputField.getText().trim();
        AuthResult result = authService.authenticate(input, adminMode);
        if (result.isSuccess()) {
            onLoginSuccess(result);
        } else {
            showError(result.message());
        }
    }

    private void onLoginSuccess(AuthResult result) {
        dispose();
        if (result.role() == AuthResult.Role.ADMIN) {
            new AdminMainPanel();
        } else {
            new UserMainPanel(result.userId());
        }
    }

    private void showError(String message) {
        errLabel.setText(message);
        errLabel.setVisible(true);
        errLabel.revalidate();
        inputField.requestFocusInWindow();
    }

    private void clearError() {
        errLabel.setVisible(false);
        errLabel.setText("");
        errLabel.revalidate();
    }

    // ── 유틸 ───────────────────────────────────────────────────────────────────

    private Color lerp(Color a, Color b, float t) {
        t = Math.clamp(t, 0f, 1f);
        return new Color(
            (int)(a.getRed()   + t * (b.getRed()   - a.getRed())),
            (int)(a.getGreen() + t * (b.getGreen() - a.getGreen())),
            (int)(a.getBlue()  + t * (b.getBlue()  - a.getBlue()))
        );
    }
}
