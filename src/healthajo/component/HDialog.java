package healthajo.component;

import healthajo.component.theme.AppTheme;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/**
 * 앱 스타일 모달 다이얼로그.
 * 네이티브 타이틀바 없이 라운드 패널로 직접 그린다.
 *
 * HDialog.confirm(frame, "삭제 확인", "선택한 3명을 삭제하시겠습니까?");
 * HDialog.error(frame, "중복된 전화번호입니다.");
 * HDialog.alert(frame, "경고", "내용", HDialog.Type.WARNING);
 */
public class HDialog extends JDialog {

    public enum Type { INFO, SUCCESS, WARNING, DANGER }

    private boolean confirmed = false;

    private HDialog(JFrame parent, String title, String message, Type type, boolean hasCancel) {
        super(parent, true);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        getRootPane().setOpaque(false);

        Color typeColor = switch (type) {
            case SUCCESS -> AppTheme.SUCCESS;
            case WARNING -> AppTheme.WARNING;
            case DANGER  -> AppTheme.DANGER;
            default      -> AppTheme.PRIMARY;
        };
        Color typeDim = switch (type) {
            case SUCCESS -> AppTheme.SUCCESS_DIM;
            case WARNING -> AppTheme.WARNING_DIM;
            case DANGER  -> AppTheme.DANGER_DIM;
            default      -> AppTheme.PRIMARY_TINT;
        };
        String typeChar = switch (type) {
            case SUCCESS -> "✓";
            case WARNING -> "!";
            case DANGER  -> "✕";
            default      -> "i";
        };

        // ── 루트 패널: 라운드 배경 + 자식 클리핑 ──────────────────────────────
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppTheme.R_LG, AppTheme.R_LG);
                g2.dispose();
            }
            @Override protected void paintChildren(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), AppTheme.R_LG, AppTheme.R_LG));
                super.paintChildren(g2);
                g2.dispose();
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, AppTheme.R_LG, AppTheme.R_LG);
                g2.dispose();
            }
        };
        root.setOpaque(false);

        // ── 아이콘 원형 뱃지 ───────────────────────────────────────────────────
        JPanel iconCircle = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int d = Math.min(getWidth(), getHeight());
                g2.setColor(typeDim);
                g2.fillOval(0, 0, d, d);
                g2.setFont(AppTheme.font(Font.BOLD, 13));
                g2.setColor(typeColor);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(typeChar,
                    (d - fm.stringWidth(typeChar)) / 2,
                    (d + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        iconCircle.setPreferredSize(new Dimension(32, 32));
        iconCircle.setOpaque(false);

        // ── 헤더: 아이콘 + 타이틀 ─────────────────────────────────────────────
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(AppTheme.H3);
        titleLbl.setForeground(AppTheme.TEXT);

        JPanel header = new JPanel(new BorderLayout(AppTheme.SP_3, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(AppTheme.SP_5, AppTheme.SP_6, AppTheme.SP_3, AppTheme.SP_6));
        header.add(iconCircle, BorderLayout.WEST);
        header.add(titleLbl,   BorderLayout.CENTER);

        // ── 본문: 아이콘 너비만큼 들여써서 타이틀 텍스트와 정렬 ─────────────
        // SP_6(left padding) + 32(icon) + SP_3(gap) = 68px
        String html = "<html><body style='width:290px;font-size:11pt'>"
                    + message.replace("\n", "<br>")
                    + "</body></html>";
        JLabel msgLbl = new JLabel(html);
        msgLbl.setFont(AppTheme.BODY_SM);
        msgLbl.setForeground(AppTheme.TEXT_SECONDARY);

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(0, AppTheme.SP_6 + 32 + AppTheme.SP_3, AppTheme.SP_5, AppTheme.SP_6));
        body.add(msgLbl, BorderLayout.CENTER);

        // ── 푸터: 버튼 ────────────────────────────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SP_2, AppTheme.SP_3));
        footer.setOpaque(false);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, AppTheme.BORDER));

        HButton confirm = type == Type.DANGER
            ? HButton.danger("확인", HButton.Size.SM)
            : HButton.primary("확인", HButton.Size.SM);
        confirm.addActionListener(e -> { confirmed = true; dispose(); });

        if (hasCancel) {
            HButton cancel = HButton.ghost("취소", HButton.Size.SM);
            cancel.addActionListener(e -> dispose());
            footer.add(cancel);
        }
        footer.add(confirm);

        root.add(header, BorderLayout.NORTH);
        root.add(body,   BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
        setSize(420, 210);
        setLocationRelativeTo(parent);
        getRootPane().setDefaultButton(confirm);

        // ESC → 닫기
        getRootPane().registerKeyboardAction(
            e -> dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);

        // 헤더 드래그로 창 이동
        final int[] drag = {0, 0};
        MouseAdapter dragger = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e)  { drag[0] = e.getX(); drag[1] = e.getY(); }
            @Override public void mouseDragged(MouseEvent e)  {
                setLocation(getX() + e.getX() - drag[0], getY() + e.getY() - drag[1]);
            }
        };
        header.addMouseListener(dragger);
        header.addMouseMotionListener(dragger);
    }

    // ── static factory (기존 API 그대로) ─────────────────────────────────────

    public static boolean confirm(JFrame parent, String title, String message) {
        HDialog d = new HDialog(parent, title, message, Type.WARNING, true);
        d.setVisible(true);
        return d.confirmed;
    }

    public static boolean confirmDanger(JFrame parent, String title, String message) {
        HDialog d = new HDialog(parent, title, message, Type.DANGER, true);
        d.setVisible(true);
        return d.confirmed;
    }

    public static void alert(JFrame parent, String title, String message, Type type) {
        new HDialog(parent, title, message, type, false).setVisible(true);
    }

    public static void info(JFrame parent, String message) {
        alert(parent, "안내", message, Type.INFO);
    }

    public static void error(JFrame parent, String message) {
        alert(parent, "오류", message, Type.DANGER);
    }
}
