package healthajo.membership.view.user;

import healthajo.component.HBadge;
import healthajo.component.HButton;
import healthajo.component.HLabel;
import healthajo.component.theme.AppTheme;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/**
 * 사용자 — 나의 회원권 상세 다이얼로그.
 *
 * data: [프로그램명, 회원권명, 총횟수, 잔여횟수, 상태, 발급일]
 */
public class MembershipDetailDialog extends JDialog {

    public MembershipDetailDialog(JFrame parent, Object[] data) {
        super(parent, "회원권 상세", true);
        setResizable(false);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(AppTheme.SURFACE);
        setLayout(new BorderLayout());

        String  program   = data[0].toString();
        String  name      = data[1].toString();
        int     total     = parseInt(data[2]);
        int     remaining = parseInt(data[3]);
        String  status    = data[4].toString();
        String  issuedAt  = data[5].toString();
        boolean expired   = "EXPIRED".equals(status);

        add(buildHeader(program, name, status), BorderLayout.NORTH);
        add(buildBody(total, remaining, issuedAt, expired),  BorderLayout.CENTER);
        add(buildFooter(),                       BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(460, 0));
        setLocationRelativeTo(parent);
    }

    // ── 헤더 ──────────────────────────────────────────────────────────────────

    private JPanel buildHeader(String program, String name, String status) {
        boolean expired = "EXPIRED".equals(status);

        JPanel header = new JPanel(new BorderLayout(AppTheme.SP_3, 0));
        header.setBackground(AppTheme.SURFACE);
        header.setBorder(new EmptyBorder(AppTheme.SP_6, AppTheme.SP_6, AppTheme.SP_5, AppTheme.SP_6));

        JPanel textCol = new JPanel();
        textCol.setOpaque(false);
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));

        JLabel programLbl = HLabel.h2(program);
        programLbl.setAlignmentX(LEFT_ALIGNMENT);
        if (expired) programLbl.setForeground(AppTheme.TEXT_MUTED);

        JLabel nameLbl = HLabel.body(name);
        nameLbl.setForeground(AppTheme.TEXT_SECONDARY);
        nameLbl.setAlignmentX(LEFT_ALIGNMENT);

        textCol.add(programLbl);
        textCol.add(Box.createVerticalStrut(AppTheme.SP_1));
        textCol.add(nameLbl);

        HBadge badge = expired
            ? HBadge.of("만료", AppTheme.DANGER_DIM, AppTheme.DANGER)
            : HBadge.of("활성", AppTheme.SUCCESS_DIM, AppTheme.SUCCESS);

        JPanel badgeWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        badgeWrap.setOpaque(false);
        badgeWrap.add(badge);

        header.add(textCol,   BorderLayout.CENTER);
        header.add(badgeWrap, BorderLayout.EAST);

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(AppTheme.SURFACE);
        outer.setBorder(new MatteBorder(0, 0, 1, 0, AppTheme.BORDER));
        outer.add(header);
        return outer;
    }

    // ── 바디 ──────────────────────────────────────────────────────────────────

    private JPanel buildBody(int total, int remaining, String issuedAt, boolean expired) {
        JPanel body = new JPanel();
        body.setBackground(AppTheme.SURFACE);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(AppTheme.SP_6, AppTheme.SP_6, AppTheme.SP_5, AppTheme.SP_6));

        body.add(buildStatCard(total, remaining, expired));
        body.add(Box.createVerticalStrut(AppTheme.SP_5));
        body.add(buildInfoGrid(total, issuedAt));
        if (expired) {
            body.add(Box.createVerticalStrut(AppTheme.SP_4));
            body.add(buildExpiredNote());
        }
        return body;
    }

    // ── 잔여 횟수 카드 ────────────────────────────────────────────────────────

    private JPanel buildStatCard(int total, int remaining, boolean expired) {
        float ratio = total > 0 ? (float) remaining / total : 0f;
        Color accentColor = expired       ? AppTheme.TEXT_DISABLED
                          : ratio <= 0.2f ? AppTheme.DANGER
                          : ratio <= 0.5f ? AppTheme.WARNING
                          :                 AppTheme.PRIMARY;
        Color accentDim   = expired       ? AppTheme.SURFACE_RAISED
                          : ratio <= 0.2f ? AppTheme.DANGER_DIM
                          : ratio <= 0.5f ? AppTheme.WARNING_DIM
                          :                 AppTheme.PRIMARY_TINT;

        // 카드 배경
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accentDim);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(),
                        AppTheme.R_LG, AppTheme.R_LG));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(AppTheme.SP_6, AppTheme.SP_5, AppTheme.SP_5, AppTheme.SP_5));
        card.setAlignmentX(LEFT_ALIGNMENT);

        // 섹션 레이블
        JLabel sectionLbl = HLabel.label("잔여 횟수");
        sectionLbl.setForeground(expired ? AppTheme.TEXT_MUTED : accentColor);
        sectionLbl.setAlignmentX(LEFT_ALIGNMENT);

        // 큰 숫자
        JLabel bigCount = new JLabel(String.valueOf(remaining));
        bigCount.setFont(AppTheme.font(Font.BOLD, 40));
        bigCount.setForeground(expired ? AppTheme.TEXT_MUTED : accentColor);
        bigCount.setAlignmentX(LEFT_ALIGNMENT);

        // 총 대비 잔여 표시
        JLabel subLbl = new JLabel("/ " + total + "회  ·  " + Math.round(ratio * 100) + "% 남음");
        subLbl.setFont(AppTheme.BODY_SM);
        subLbl.setForeground(AppTheme.TEXT_SECONDARY);
        subLbl.setAlignmentX(LEFT_ALIGNMENT);

        // 프로그레스 바
        JPanel progressBar = buildProgressBar(ratio, accentColor, expired);
        progressBar.setAlignmentX(LEFT_ALIGNMENT);

        card.add(sectionLbl);
        card.add(Box.createVerticalStrut(AppTheme.SP_2));
        card.add(bigCount);
        card.add(Box.createVerticalStrut(AppTheme.SP_1));
        card.add(subLbl);
        card.add(Box.createVerticalStrut(AppTheme.SP_3));
        card.add(progressBar);
        return card;
    }

    private JPanel buildProgressBar(float ratio, Color fill, boolean expired) {
        Color trackColor = expired ? AppTheme.BORDER : new Color(
            fill.getRed(), fill.getGreen(), fill.getBlue(), 40);

        JPanel bar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(trackColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));
                int filled = Math.max(0, (int)(w * ratio));
                if (filled > 0 && !expired) {
                    g2.setColor(fill);
                    g2.fill(new RoundRectangle2D.Float(0, 0, filled, h, h, h));
                }
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(300, 8));
        bar.setMaximumSize(new Dimension(Short.MAX_VALUE, 8));
        bar.setMinimumSize(new Dimension(100, 8));
        return bar;
    }

    // ── 상세 정보 그리드 ──────────────────────────────────────────────────────

    private JPanel buildInfoGrid(int total, String issuedAt) {
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        grid.setAlignmentX(LEFT_ALIGNMENT);

        addGridRow(grid, 0, "총 횟수",  total + "회");
        addGridRow(grid, 1, "발급일",   issuedAt);

        return grid;
    }

    private void addGridRow(JPanel grid, int gridY, String label, String value) {
        int topInset = gridY == 0 ? 0 : AppTheme.SP_4;

        GridBagConstraints lc = new GridBagConstraints();
        lc.gridx = 0; lc.gridy = gridY;
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(topInset, 0, 0, AppTheme.SP_8);

        GridBagConstraints vc = new GridBagConstraints();
        vc.gridx = 1; vc.gridy = gridY;
        vc.anchor = GridBagConstraints.EAST;
        vc.weightx = 1.0;
        vc.fill = GridBagConstraints.HORIZONTAL;
        vc.insets = new Insets(topInset, 0, 0, 0);

        JLabel lbl = HLabel.body(label);
        lbl.setForeground(AppTheme.TEXT_MUTED);

        JLabel val = HLabel.body(value);
        val.setHorizontalAlignment(SwingConstants.RIGHT);

        grid.add(lbl, lc);
        grid.add(val, vc);
    }

    // ── 만료 안내 ─────────────────────────────────────────────────────────────

    private JPanel buildExpiredNote() {
        JPanel note = new JPanel(new BorderLayout(AppTheme.SP_3, 0));
        note.setOpaque(false);
        note.setAlignmentX(LEFT_ALIGNMENT);

        JPanel accent = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.DANGER_DIM);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(),
                        AppTheme.R_SM, AppTheme.R_SM));
                g2.dispose();
            }
        };
        accent.setOpaque(false);
        accent.setLayout(new FlowLayout(FlowLayout.LEFT, AppTheme.SP_4, AppTheme.SP_4));

        JLabel noteLbl = HLabel.body("이 회원권은 만료되었습니다. 새 회원권 발급을 문의하세요.");
        noteLbl.setForeground(AppTheme.DANGER);
        accent.add(noteLbl);

        note.add(accent, BorderLayout.CENTER);
        return note;
    }

    // ── 푸터 ──────────────────────────────────────────────────────────────────

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SP_2, AppTheme.SP_3));
        footer.setBackground(AppTheme.SURFACE);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, AppTheme.BORDER));
        HButton closeBtn = HButton.secondary("닫기", HButton.Size.SM);
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);
        return footer;
    }

    // ── 유틸 ──────────────────────────────────────────────────────────────────

    private static int parseInt(Object v) {
        try { return Integer.parseInt(v.toString()); }
        catch (Exception e) { return 0; }
    }
}
