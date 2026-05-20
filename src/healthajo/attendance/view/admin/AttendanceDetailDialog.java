package healthajo.attendance.view.admin;

import healthajo.attendance.application.AttendanceApplication;
import healthajo.attendance.domain.AttendanceSession;
import healthajo.attendance.domain.Attendee;
import healthajo.component.HButton;
import healthajo.component.HDialog;
import healthajo.component.HLabel;
import healthajo.component.HTable;
import healthajo.component.HToast;
import healthajo.component.theme.AppTheme;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;

/**
 * 세션 출석 현황 다이얼로그.
 *
 * 기능:
 *   - PENDING 선택 → 출석 처리 (ATTENDED)
 *   - PENDING 선택 → 결석 처리 (ABSENT)
 *   - ATTENDED/ABSENT 선택 → 되돌리기 (PENDING)
 *   - 출석 마감 (sessions.attendance_closed = 1)
 */
public class AttendanceDetailDialog extends JDialog {

    private static final AttendanceApplication APP = new AttendanceApplication();

    private final AttendanceSession   session;
    private final DefaultTableModel   model;
    private final HTable              attendTable;
    private final List<Attendee>      attendees = new ArrayList<>();
    private       boolean             closed;
    private       HButton             attendBtn;
    private       HButton             absentBtn;
    private       HButton             undoBtn;
    private       HButton             closeBtn;

    public AttendanceDetailDialog(JFrame parent, AttendanceSession session) {
        super(parent, "출석 현황 — " + session.programName() + " " + session.sessionDate(), true);
        this.session = session;
        this.closed  = session.attendanceClosed();

        setLayout(new BorderLayout());
        setSize(640, 540);
        setLocationRelativeTo(parent);
        setResizable(true);
        getContentPane().setBackground(AppTheme.SURFACE);

        // ── 헤더 ─────────────────────────────────────────────────────────────────
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, AppTheme.SP_4, AppTheme.SP_4));
        header.setBackground(AppTheme.SURFACE);
        header.setBorder(new MatteBorder(0, 0, 1, 0, AppTheme.BORDER));

        header.add(HLabel.h3(session.programName() + " — " + session.sessionDate()));
        header.add(HLabel.muted(session.startTime() + " ~ " + session.endTime()));
        header.add(HLabel.small("예약: " + session.bookedCount() + " / 정원: " + session.capacity()));

        // ── 테이블 ────────────────────────────────────────────────────────────────
        model = new DefaultTableModel(
            new String[]{"", "이름", "전화번호", "출석 상태", "처리 일시"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 0; }
            @Override public Class<?> getColumnClass(int c) { return c == 0 ? Boolean.class : String.class; }
        };
        loadAttendees();

        attendTable = new HTable(model);
        attendTable.setCheckboxColumn(0);
        attendTable.setBadgeRenderer(3);
        attendTable.getColumnModel().getColumn(1).setPreferredWidth(110);
        attendTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        attendTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        attendTable.getColumnModel().getColumn(4).setPreferredWidth(150);

        // ── 툴바 ─────────────────────────────────────────────────────────────────
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SP_2, AppTheme.SP_2));
        toolbar.setBackground(AppTheme.BG);

        attendBtn = HButton.primary("출석 처리", HButton.Size.SM);
        absentBtn = HButton.secondary("결석 처리", HButton.Size.SM);
        undoBtn   = HButton.ghost("되돌리기", HButton.Size.SM);
        closeBtn  = HButton.danger("출석 마감", HButton.Size.SM);

        attendBtn.addActionListener(e -> onAttend());
        absentBtn.addActionListener(e -> onAbsent());
        undoBtn.addActionListener(e -> onUndo());
        closeBtn.addActionListener(e -> onClose());

        toolbar.add(undoBtn);
        toolbar.add(absentBtn);
        toolbar.add(attendBtn);
        toolbar.add(closeBtn);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(AppTheme.BG);
        tablePanel.add(toolbar, BorderLayout.NORTH);
        tablePanel.add(attendTable.inScrollPane(), BorderLayout.CENTER);

        // ── 푸터 ─────────────────────────────────────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SP_2, AppTheme.SP_3));
        footer.setBackground(AppTheme.SURFACE);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, AppTheme.BORDER));
        HButton dismissBtn = HButton.secondary("닫기", HButton.Size.SM);
        dismissBtn.addActionListener(e -> dispose());
        footer.add(dismissBtn);

        add(header,     BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(footer,     BorderLayout.SOUTH);

        if (closed) reflectClosed();
    }

    private void loadAttendees() {
        model.setRowCount(0);
        attendees.clear();
        try {
            List<Attendee> rows = APP.findAttendees(session.id());
            for (Attendee a : rows) {
                attendees.add(a);
                model.addRow(new Object[]{
                    false, a.userName(), a.userPhone(), a.attendanceStatus(), a.attendedAt()
                });
            }
        } catch (RuntimeException ex) {
            HDialog.error(parentFrame(), "수강자 목록을 불러오지 못했습니다.\n" + ex.getMessage());
        }
    }

    private int[] getChecked() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (Boolean.TRUE.equals(model.getValueAt(i, 0))) list.add(i);
        }
        return list.stream().mapToInt(Integer::intValue).toArray();
    }

    private Long reservationIdAt(int row) {
        return row >= 0 && row < attendees.size() ? attendees.get(row).reservationId() : null;
    }

    private void onAttend() {
        if (closed) { HDialog.alert(parentFrame(), "안내", "출석이 마감된 세션입니다.", HDialog.Type.WARNING); return; }
        int[] rows = getChecked();
        if (rows.length == 0) { HDialog.info(parentFrame(), "출석 처리할 수강자를 선택하세요."); return; }

        for (int r : rows) {
            if (!"PENDING".equals(model.getValueAt(r, 3))) {
                HDialog.alert(parentFrame(), "안내", "PENDING 상태의 수강자만 선택하세요.", HDialog.Type.WARNING);
                return;
            }
        }

        if (HDialog.confirm(parentFrame(), "출석 처리",
                "선택한 " + rows.length + "명을 출석 처리하시겠습니까?")) {
            apply(rows, "출석", id -> APP.markAttended(id));
        }
    }

    private void onAbsent() {
        if (closed) { HDialog.alert(parentFrame(), "안내", "출석이 마감된 세션입니다.", HDialog.Type.WARNING); return; }
        int[] rows = getChecked();
        if (rows.length == 0) { HDialog.info(parentFrame(), "결석 처리할 수강자를 선택하세요."); return; }

        if (HDialog.confirm(parentFrame(), "결석 처리",
                "선택한 " + rows.length + "명을 결석 처리하시겠습니까?")) {
            apply(rows, "결석", id -> APP.markAbsent(id));
        }
    }

    private void onUndo() {
        if (closed) { HDialog.alert(parentFrame(), "안내", "출석이 마감된 세션입니다.", HDialog.Type.WARNING); return; }
        int[] rows = getChecked();
        if (rows.length == 0) { HDialog.info(parentFrame(), "되돌릴 수강자를 선택하세요."); return; }

        for (int r : rows) {
            if ("PENDING".equals(model.getValueAt(r, 3))) {
                HDialog.alert(parentFrame(), "안내", "이미 미처리(PENDING) 상태인 수강자가 포함되어 있습니다.", HDialog.Type.WARNING);
                return;
            }
        }

        if (HDialog.confirm(parentFrame(), "되돌리기",
                "선택한 " + rows.length + "명을 미처리 상태로 되돌리시겠습니까?")) {
            apply(rows, "되돌리기", id -> APP.revertToPending(id));
        }
    }

    /** 선택 행에 대해 DB 처리를 수행하고, 성공 시 목록을 다시 로드해 동기화한다. */
    private void apply(int[] rows, String label, java.util.function.Consumer<Long> action) {
        int failed = 0;
        for (int r : rows) {
            Long id = reservationIdAt(r);
            if (id == null) { failed++; continue; }
            try {
                action.accept(id);
            } catch (RuntimeException ex) {
                failed++;
            }
        }
        loadAttendees();
        if (failed == 0) {
            HToast.success(parentFrame(), rows.length + "명이 " + label + " 처리되었습니다.");
        } else {
            HDialog.error(parentFrame(), label + " 처리 중 " + failed + "건이 실패했습니다.");
        }
    }

    private void onClose() {
        if (closed) { HDialog.alert(parentFrame(), "안내", "이미 마감된 세션입니다.", HDialog.Type.WARNING); return; }

        long pending = java.util.stream.IntStream.range(0, model.getRowCount())
            .filter(i -> "PENDING".equals(model.getValueAt(i, 3))).count();

        String extra = pending > 0 ? "\n\n주의: 미처리 인원이 " + pending + "명 있습니다." : "";

        if (HDialog.confirmDanger(parentFrame(), "출석 마감",
                "출석을 마감하시겠습니까?\n마감 후에는 수정이 불가합니다." + extra)) {
            try {
                APP.closeAttendance(session.id());
            } catch (RuntimeException ex) {
                HDialog.error(parentFrame(), "출석 마감에 실패했습니다.\n" + ex.getMessage());
                return;
            }
            reflectClosed();
            HToast.success(parentFrame(), "출석이 마감되었습니다.");
        }
    }

    private void reflectClosed() {
        closed = true;
        attendBtn.setEnabled(false);
        absentBtn.setEnabled(false);
        undoBtn.setEnabled(false);
        closeBtn.setEnabled(false);
    }

    private JFrame parentFrame() {
        return (JFrame) getOwner();
    }
}
