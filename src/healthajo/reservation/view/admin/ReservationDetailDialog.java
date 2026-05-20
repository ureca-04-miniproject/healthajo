package healthajo.reservation.view.admin;

import healthajo.component.HBadge;
import healthajo.component.HButton;
import healthajo.component.HLabel;
import healthajo.component.theme.AppTheme;
import healthajo.reservation.domain.Reservation;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/**
 * 관리자 — 예약 상세 다이얼로그.
 *
 * 회원·예약·출석·취소·회원권 정보를 섹션별로 보여준다.
 */
public class ReservationDetailDialog extends JDialog {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DT_FMT   = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ReservationDetailDialog(JFrame parent, Reservation r) {
        super(parent, "예약 상세", true);
        setResizable(false);
        getContentPane().setBackground(AppTheme.SURFACE);
        setLayout(new BorderLayout());

        add(buildHeader(r), BorderLayout.NORTH);
        add(buildBody(r),   BorderLayout.CENTER);
        add(buildFooter(),  BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(460, getHeight()));
        setLocationRelativeTo(parent);
    }

    // ── 헤더 ──────────────────────────────────────────────────────────────────

    private JPanel buildHeader(Reservation r) {
        JPanel header = new JPanel(new BorderLayout(AppTheme.SP_3, 0));
        header.setBackground(AppTheme.SURFACE);
        header.setBorder(new EmptyBorder(AppTheme.SP_6, AppTheme.SP_6, AppTheme.SP_5, AppTheme.SP_6));

        JPanel textCol = new JPanel();
        textCol.setOpaque(false);
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));

        JLabel program = HLabel.h2(r.programName() != null ? r.programName() : "-");
        program.setAlignmentX(LEFT_ALIGNMENT);

        String dateLine = (r.sessionDate() != null ? r.sessionDate().format(DATE_FMT) : "-")
                + "  " + nz(r.startTime()) + " ~ " + nz(r.endTime());
        JLabel session = HLabel.small(dateLine);
        session.setAlignmentX(LEFT_ALIGNMENT);

        textCol.add(program);
        textCol.add(Box.createVerticalStrut(AppTheme.SP_1));
        textCol.add(session);

        JPanel badges = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SP_1, 0));
        badges.setOpaque(false);
        badges.add(statusBadge(r.status()));

        header.add(textCol, BorderLayout.CENTER);
        header.add(badges,  BorderLayout.EAST);

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(AppTheme.SURFACE);
        outer.setBorder(new MatteBorder(0, 0, 1, 0, AppTheme.BORDER));
        outer.add(header);
        return outer;
    }

    // ── 바디 ──────────────────────────────────────────────────────────────────

    private JPanel buildBody(Reservation r) {
        JPanel body = new JPanel();
        body.setBackground(AppTheme.SURFACE);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(AppTheme.SP_5, AppTheme.SP_6, AppTheme.SP_6, AppTheme.SP_6));

        body.add(section("회원 정보",
                row("이름", nz(r.userName())),
                row("전화번호", nz(r.userPhone()))));
        body.add(Box.createVerticalStrut(AppTheme.SP_4));

        body.add(section("예약 정보",
                row("세션 날짜", r.sessionDate() != null ? r.sessionDate().format(DATE_FMT) : "-"),
                row("시간", nz(r.startTime()) + " ~ " + nz(r.endTime())),
                row("예약 상태", statusText(r.status())),
                row("예약일", fmt(r.reservedAt(), DT_FMT)),
                row("회원권 사용", r.membershipId() != null ? "사용 (회원권 #" + r.membershipId() + ")" : "미사용")));
        body.add(Box.createVerticalStrut(AppTheme.SP_4));

        body.add(section("출석 정보",
                row("출석 상태", attendanceText(r.attendanceStatus())),
                row("출석 처리일", fmt(r.attendedAt(), DT_FMT))));

        if ("CANCELLED".equals(r.status())) {
            body.add(Box.createVerticalStrut(AppTheme.SP_4));
            body.add(section("취소 정보",
                    row("취소 주체", cancelledByText(r.cancelledBy())),
                    row("취소일", fmt(r.cancelledAt(), DT_FMT))));
        }

        return body;
    }

    // ── 섹션 / 행 ──────────────────────────────────────────────────────────────

    private JPanel section(String title, JComponent... rows) {
        JPanel sec = new JPanel();
        sec.setOpaque(false);
        sec.setLayout(new BoxLayout(sec, BoxLayout.Y_AXIS));
        sec.setAlignmentX(LEFT_ALIGNMENT);

        JLabel head = HLabel.label(title);
        head.setForeground(AppTheme.PRIMARY);
        head.setAlignmentX(LEFT_ALIGNMENT);
        sec.add(head);
        sec.add(Box.createVerticalStrut(AppTheme.SP_2));
        for (JComponent rowComp : rows) {
            rowComp.setAlignmentX(LEFT_ALIGNMENT);
            sec.add(rowComp);
            sec.add(Box.createVerticalStrut(AppTheme.SP_1));
        }
        return sec;
    }

    private JComponent row(String label, String value) {
        JPanel p = new JPanel(new BorderLayout(AppTheme.SP_3, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Short.MAX_VALUE, 24));

        JLabel l = HLabel.muted(label);
        l.setPreferredSize(new Dimension(100, 20));
        JLabel v = HLabel.body(value == null || value.isBlank() ? "-" : value);

        p.add(l, BorderLayout.WEST);
        p.add(v, BorderLayout.CENTER);
        return p;
    }

    // ── 푸터 ──────────────────────────────────────────────────────────────────

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SP_2, AppTheme.SP_3));
        footer.setBackground(AppTheme.SURFACE);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, AppTheme.BORDER));
        HButton close = HButton.secondary("닫기", HButton.Size.SM);
        close.addActionListener(e -> dispose());
        footer.add(close);
        return footer;
    }

    // ── 배지 / 라벨 변환 ─────────────────────────────────────────────────────────

    private static HBadge statusBadge(String status) {
        if (status == null) return HBadge.of("-", AppTheme.SURFACE_RAISED, AppTheme.TEXT_MUTED);
        return switch (status) {
            case "CONFIRMED"         -> HBadge.of("예약 확정",   AppTheme.PRIMARY_TINT,    AppTheme.PRIMARY);
            case "MEMBERSHIP_ISSUED" -> HBadge.of("회원권 사용", new Color(204, 251, 241), new Color(13, 148, 136));
            case "CANCELLED"         -> HBadge.of("취소",        AppTheme.WARNING_DIM,     AppTheme.WARNING);
            default                  -> HBadge.of(status,        AppTheme.SURFACE_RAISED,  AppTheme.TEXT_MUTED);
        };
    }

    private static String statusText(String status) {
        if (status == null) return "-";
        return switch (status) {
            case "CONFIRMED"         -> "예약 확정";
            case "MEMBERSHIP_ISSUED" -> "회원권 사용";
            case "CANCELLED"         -> "취소";
            default                  -> status;
        };
    }

    private static String attendanceText(String s) {
        if (s == null) return "-";
        return switch (s) {
            case "PENDING"  -> "미처리";
            case "ATTENDED" -> "출석";
            case "ABSENT"   -> "결석";
            default         -> s;
        };
    }

    private static String cancelledByText(String by) {
        if (by == null) return "-";
        return switch (by) {
            case "ADMIN" -> "관리자";
            case "USER"  -> "회원";
            default      -> by;
        };
    }

    private static String fmt(java.time.LocalDateTime dt, DateTimeFormatter f) {
        return dt != null ? dt.format(f) : "-";
    }

    private static String nz(String s) {
        return s == null || s.isBlank() ? "-" : s;
    }
}
