package healthajo.reservation.view.user;

import healthajo.component.HBadge;
import healthajo.component.HButton;
import healthajo.component.HLabel;
import healthajo.component.theme.AppTheme;
import healthajo.jdbc.core.Record;
import healthajo.memberships.dao.MembershipDAO;
import healthajo.reservation.domain.Reservation;

import static healthajo.jdbc.table.TMembership.MEMBERSHIP;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/**
 * 사용자 — 나의 예약 상세 다이얼로그.
 */
public class ReservationDetailDialog extends JDialog {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DT_FMT   = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final MembershipDAO MEMBERSHIP_DAO = new MembershipDAO();

    public ReservationDetailDialog(JFrame parent, Reservation reservation) {
        super(parent, "예약 상세", true);
        setResizable(false);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(AppTheme.SURFACE);
        setLayout(new BorderLayout());

        String program    = reservation.programName() != null ? reservation.programName() : "";
        String date       = reservation.sessionDate() != null ? reservation.sessionDate().format(DATE_FMT) : "";
        String start      = reservation.startTime()   != null ? reservation.startTime()  : "";
        String end        = reservation.endTime()     != null ? reservation.endTime()    : "";
        String status     = reservation.status()      != null ? reservation.status()     : "";
        String reservedAt = reservation.reservedAt()  != null ? reservation.reservedAt().format(DT_FMT) : "";

        add(buildHeader(program, date, start, end, status), BorderLayout.NORTH);
        add(buildBody(reservation, program, status, date, start, end, reservedAt), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(480, 0));
        setLocationRelativeTo(parent);
    }

    // ── 헤더 ──────────────────────────────────────────────────────────────────

    private JPanel buildHeader(String program, String date, String start, String end, String status) {
        boolean cancelled = "CANCELLED".equals(status);

        JPanel header = new JPanel(new BorderLayout(AppTheme.SP_3, 0));
        header.setBackground(AppTheme.SURFACE);
        header.setBorder(new EmptyBorder(AppTheme.SP_6, AppTheme.SP_6, AppTheme.SP_5, AppTheme.SP_6));

        JPanel textCol = new JPanel();
        textCol.setOpaque(false);
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));

        JLabel programLbl = HLabel.h2(program);
        programLbl.setAlignmentX(LEFT_ALIGNMENT);
        if (cancelled) programLbl.setForeground(AppTheme.TEXT_MUTED);

        JLabel sessionLbl = HLabel.small(date + "  " + start + " ~ " + end);
        sessionLbl.setForeground(AppTheme.TEXT_MUTED);
        sessionLbl.setAlignmentX(LEFT_ALIGNMENT);

        textCol.add(programLbl);
        textCol.add(Box.createVerticalStrut(AppTheme.SP_1));
        textCol.add(sessionLbl);

        JPanel badgeWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        badgeWrap.setOpaque(false);
        badgeWrap.add(statusBadge(status));

        header.add(textCol,   BorderLayout.CENTER);
        header.add(badgeWrap, BorderLayout.EAST);

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(AppTheme.SURFACE);
        outer.setBorder(new MatteBorder(0, 0, 1, 0, AppTheme.BORDER));
        outer.add(header);
        return outer;
    }

    private static HBadge statusBadge(String status) {
        return switch (status) {
            case "CONFIRMED"         -> HBadge.of("예약 확정",   AppTheme.PRIMARY_TINT,      AppTheme.PRIMARY);
            case "MEMBERSHIP_ISSUED" -> HBadge.of("회원권 사용", new Color(204, 251, 241),   new Color(13, 148, 136));
            case "CANCELLED"         -> HBadge.of("취소",        AppTheme.WARNING_DIM,       AppTheme.WARNING);
            default                  -> HBadge.of(status,        AppTheme.SURFACE_RAISED,    AppTheme.TEXT_MUTED);
        };
    }

    // ── 바디 ──────────────────────────────────────────────────────────────────

    private JPanel buildBody(Reservation reservation, String program, String status,
                             String date, String start, String end, String reservedAt) {
        JPanel body = new JPanel();
        body.setBackground(AppTheme.SURFACE);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(AppTheme.SP_6, AppTheme.SP_6, AppTheme.SP_5, AppTheme.SP_6));

        body.add(buildSessionCard(date, start, end, "CANCELLED".equals(status)));
        body.add(Box.createVerticalStrut(AppTheme.SP_5));

        JPanel section = switch (status) {
            case "CONFIRMED"         -> buildConfirmedSection(reservedAt);
            case "MEMBERSHIP_ISSUED" -> buildMembershipSection(reservation, program, date);
            case "CANCELLED"         -> buildCancelledSection(reservation, reservedAt);
            default                  -> new JPanel();
        };
        body.add(section);
        return body;
    }

    // ── 세션 카드 ─────────────────────────────────────────────────────────────

    private JPanel buildSessionCard(String date, String start, String end, boolean cancelled) {
        Color accent    = cancelled ? AppTheme.TEXT_DISABLED : AppTheme.PRIMARY;
        Color accentDim = cancelled ? AppTheme.SURFACE_RAISED : AppTheme.PRIMARY_TINT;
        int   duration  = parseDuration(start, end);

        JPanel card = roundedPanel(accentDim, AppTheme.R_LG);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(AppTheme.SP_5, AppTheme.SP_5, AppTheme.SP_4, AppTheme.SP_5));
        card.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sectionLbl = HLabel.label("세션 정보");
        sectionLbl.setForeground(accent);
        sectionLbl.setAlignmentX(LEFT_ALIGNMENT);

        JLabel dateLbl = new JLabel(date);
        dateLbl.setFont(AppTheme.font(Font.BOLD, 26));
        dateLbl.setForeground(cancelled ? AppTheme.TEXT_MUTED : AppTheme.TEXT);
        dateLbl.setAlignmentX(LEFT_ALIGNMENT);

        String timeText = start + " ~ " + end + (duration > 0 ? "  (" + duration + "분)" : "");
        JLabel timeLbl = HLabel.body(timeText);
        timeLbl.setForeground(cancelled ? AppTheme.TEXT_MUTED : AppTheme.TEXT_SECONDARY);
        timeLbl.setAlignmentX(LEFT_ALIGNMENT);

        card.add(sectionLbl);
        card.add(Box.createVerticalStrut(AppTheme.SP_2));
        card.add(dateLbl);
        card.add(Box.createVerticalStrut(AppTheme.SP_1));
        card.add(timeLbl);
        return card;
    }

    // ── CONFIRMED 섹션 ────────────────────────────────────────────────────────

    private JPanel buildConfirmedSection(String reservedAt) {
        JPanel section = new JPanel();
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setAlignmentX(LEFT_ALIGNMENT);

        section.add(buildNotice(
            "세션이 예약 확정되었습니다.",
            "시작 시간 전까지 센터에 도착해 주세요.",
            AppTheme.SUCCESS_DIM, AppTheme.SUCCESS));
        section.add(Box.createVerticalStrut(AppTheme.SP_4));

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        grid.setAlignmentX(LEFT_ALIGNMENT);
        addGridRow(grid, 0, "예약일", reservedAt);
        section.add(grid);
        return section;
    }

    // ── MEMBERSHIP_ISSUED 섹션 ────────────────────────────────────────────────

    private JPanel buildMembershipSection(Reservation reservation, String program, String sessionDate) {
        String  memberName = program + " 수강권";
        int     total = 0, remaining = 0;
        String  issuedAt = "";
        boolean expired = false;

        Long membershipId = reservation.membershipId();
        if (membershipId != null) {
            try {
                Record m = MEMBERSHIP_DAO.findById(membershipId);
                if (m != null) {
                    String nm = m.get(MEMBERSHIP.NAME);
                    if (nm != null && !nm.isBlank()) memberName = nm;
                    Integer t = m.get(MEMBERSHIP.TOTAL_COUNT);
                    Integer r = m.get(MEMBERSHIP.REMAINING_COUNT);
                    total     = t != null ? t : 0;
                    remaining = r != null ? r : 0;
                    expired   = "EXPIRED".equals(m.get(MEMBERSHIP.STATUS));
                    java.sql.Timestamp ts = m.get(MEMBERSHIP.ISSUED_AT);
                    issuedAt  = ts != null ? ts.toLocalDateTime().format(DATE_FMT) : "";
                }
            } catch (RuntimeException ignore) {
                // 회원권 조회 실패 시 기본값 유지
            }
        }

        float ratio     = total > 0 ? (float) remaining / total : 0f;
        Color accent    = expired       ? AppTheme.TEXT_DISABLED
                        : ratio <= 0.2f ? AppTheme.DANGER
                        : ratio <= 0.5f ? AppTheme.WARNING
                        :                 AppTheme.PRIMARY;
        Color accentDim = expired       ? AppTheme.SURFACE_RAISED
                        : ratio <= 0.2f ? AppTheme.DANGER_DIM
                        : ratio <= 0.5f ? AppTheme.WARNING_DIM
                        :                 AppTheme.PRIMARY_TINT;

        JPanel section = new JPanel();
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setAlignmentX(LEFT_ALIGNMENT);

        section.add(buildMembershipCard(total, remaining, accent, accentDim, ratio, expired));
        section.add(Box.createVerticalStrut(AppTheme.SP_5));
        section.add(buildMembershipGrid(memberName, total, sessionDate, issuedAt));
        return section;
    }

    private JPanel buildMembershipCard(int total, int remaining,
                                       Color accent, Color accentDim, float ratio, boolean expired) {
        JPanel card = roundedPanel(accentDim, AppTheme.R_LG);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(AppTheme.SP_6, AppTheme.SP_5, AppTheme.SP_5, AppTheme.SP_5));
        card.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sectionLbl = HLabel.label("회원권 사용 현황");
        sectionLbl.setForeground(expired ? AppTheme.TEXT_MUTED : accent);
        sectionLbl.setAlignmentX(LEFT_ALIGNMENT);

        JPanel countRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        countRow.setOpaque(false);
        countRow.setAlignmentX(LEFT_ALIGNMENT);

        JLabel bigCount = new JLabel(String.valueOf(remaining));
        bigCount.setFont(AppTheme.font(Font.BOLD, 36));
        bigCount.setForeground(accent);

        JLabel subCount = new JLabel("  / " + total + "회 남음");
        subCount.setFont(AppTheme.BODY_SM);
        subCount.setForeground(AppTheme.TEXT_SECONDARY);

        countRow.add(bigCount);
        countRow.add(subCount);

        JPanel bar = buildProgressBar(ratio, accent, expired);
        bar.setAlignmentX(LEFT_ALIGNMENT);

        card.add(sectionLbl);
        card.add(Box.createVerticalStrut(AppTheme.SP_2));
        card.add(countRow);
        card.add(Box.createVerticalStrut(AppTheme.SP_3));
        card.add(bar);

        if (expired) {
            card.add(Box.createVerticalStrut(AppTheme.SP_2));
            JLabel expiredLbl = HLabel.small("이 세션 사용 후 회원권이 만료되었습니다.");
            expiredLbl.setForeground(AppTheme.DANGER);
            expiredLbl.setAlignmentX(LEFT_ALIGNMENT);
            card.add(expiredLbl);
        }
        return card;
    }

    private JPanel buildMembershipGrid(String memberName, int total, String lastUsedDate, String issuedAt) {
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        grid.setAlignmentX(LEFT_ALIGNMENT);
        addGridRow(grid, 0, "회원권명",      memberName);
        addGridRow(grid, 1, "총 횟수",       total + "회");
        addGridRow(grid, 2, "이 세션 사용일", lastUsedDate);
        addGridRow(grid, 3, "발급일",        issuedAt);
        return grid;
    }

    // ── CANCELLED 섹션 ────────────────────────────────────────────────────────

    private JPanel buildCancelledSection(Reservation reservation, String reservedAt) {
        String cancelledAt = reservation.cancelledAt() != null
            ? reservation.cancelledAt().format(DT_FMT) : "";
        String cancelledBy = switch (reservation.cancelledBy() != null ? reservation.cancelledBy() : "") {
            case "ADMIN" -> "관리자";
            case "USER"  -> "회원 직접 취소";
            default      -> "";
        };

        JPanel section = new JPanel();
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setAlignmentX(LEFT_ALIGNMENT);

        section.add(buildNotice(
            "취소된 예약입니다.",
            "이 예약은 취소 처리되어 세션에 참여할 수 없습니다.",
            AppTheme.WARNING_DIM, AppTheme.WARNING));
        section.add(Box.createVerticalStrut(AppTheme.SP_4));

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        grid.setAlignmentX(LEFT_ALIGNMENT);
        addGridRow(grid, 0, "원래 예약일", reservedAt);
        if (!cancelledAt.isEmpty()) addGridRow(grid, 1, "취소일",    cancelledAt);
        if (!cancelledBy.isEmpty()) addGridRow(grid, 2, "취소 처리", cancelledBy);
        section.add(grid);
        return section;
    }

    // ── 공통 안내 패널 ────────────────────────────────────────────────────────

    private JPanel buildNotice(String title, String desc, Color bg, Color fg) {
        JPanel outer = roundedPanel(bg, AppTheme.R_SM);
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setBorder(new EmptyBorder(AppTheme.SP_5, AppTheme.SP_4, AppTheme.SP_4, AppTheme.SP_4));
        outer.setAlignmentX(LEFT_ALIGNMENT);

        JLabel titleLbl = HLabel.label(title);
        titleLbl.setForeground(fg);
        titleLbl.setAlignmentX(LEFT_ALIGNMENT);

        JLabel descLbl = HLabel.small(desc);
        descLbl.setForeground(AppTheme.TEXT_SECONDARY);
        descLbl.setAlignmentX(LEFT_ALIGNMENT);

        outer.add(titleLbl);
        outer.add(Box.createVerticalStrut(AppTheme.SP_1));
        outer.add(descLbl);
        return outer;
    }

    // ── 프로그레스 바 ─────────────────────────────────────────────────────────

    private JPanel buildProgressBar(float ratio, Color fill, boolean expired) {
        Color trackColor = expired ? AppTheme.BORDER
                : new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), 40);
        JPanel bar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(trackColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));
                int filled = (int) (w * ratio);
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

    // ── 정보 그리드 ───────────────────────────────────────────────────────────

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

    private static JPanel roundedPanel(Color bg, int radius) {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius));
                g2.dispose();
            }
        };
    }

    private static int parseDuration(String start, String end) {
        try {
            String[] s = start.split(":");
            String[] e = end.split(":");
            return Math.max(0,
                (Integer.parseInt(e[0]) * 60 + Integer.parseInt(e[1])) -
                (Integer.parseInt(s[0]) * 60 + Integer.parseInt(s[1])));
        } catch (Exception ex) { return 0; }
    }
}
