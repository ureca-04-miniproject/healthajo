package healthajo.attendance.view.admin;

import healthajo.component.HButton;
import healthajo.component.HDialog;
import healthajo.component.HLabel;
import healthajo.component.HTable;
import healthajo.component.HToast;
import healthajo.component.theme.AppTheme;
import java.awt.*;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;

/**
 * 세션 출석 현황 다이얼로그.
 *
 * 기능:
 *   - PENDING 선택 → 출석 처리 (횟수 차감)
 *   - PENDING 선택 → 결석 처리 (횟수 차감 없음)
 *   - ATTENDED 선택 → 출석 취소 (당일만, 횟수 복구)
 *   - 출석 마감
 *
 * TODO: 실제 세션 ID로 DB 조회
 */
public class AttendanceDetailDialog extends JDialog {

    private final Object[]      sessionData;
    private final DefaultTableModel model;
    private final HTable        attendTable;
    private       boolean       closed = false;
    private       HButton       attendBtn;
    private       HButton       absentBtn;
    private       HButton       undoBtn;
    private       HButton       closeBtn;

    public AttendanceDetailDialog(JFrame parent, Object[] sessionData) {
        super(parent, "출석 현황 — " + sessionData[0] + " " + sessionData[1], true);
        this.sessionData = sessionData;

        setLayout(new BorderLayout());
        setSize(720, 540);
        setLocationRelativeTo(parent);
        setResizable(true);
        getContentPane().setBackground(AppTheme.SURFACE);

        // ── 헤더 ─────────────────────────────────────────────────────────────────
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, AppTheme.SP_4, AppTheme.SP_4));
        header.setBackground(AppTheme.SURFACE);
        header.setBorder(new MatteBorder(0, 0, 1, 0, AppTheme.BORDER));

        header.add(HLabel.h3(sessionData[0] + " — " + sessionData[1]));
        header.add(HLabel.muted(sessionData[2] + " ~ " + sessionData[3]));
        header.add(HLabel.small("총: " + sessionData[4] + "  출석: " + sessionData[5] + "  미처리: " + sessionData[6]));

        // ── 테이블 ────────────────────────────────────────────────────────────────
        model = new DefaultTableModel(
            new String[]{"", "이름", "전화번호", "회원권명", "잔여 횟수", "출석 상태", "처리 일시"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 0; }
            @Override public Class<?> getColumnClass(int c) { return c == 0 ? Boolean.class : String.class; }
        };
        loadAttendees();

        attendTable = new HTable(model);
        attendTable.setCheckboxColumn(0);
        attendTable.setBadgeRenderer(5);
        attendTable.getColumnModel().getColumn(1).setPreferredWidth(90);
        attendTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        attendTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        attendTable.getColumnModel().getColumn(4).setPreferredWidth(70);
        attendTable.getColumnModel().getColumn(5).setPreferredWidth(90);
        attendTable.getColumnModel().getColumn(6).setPreferredWidth(130);

        // ── 툴바 ─────────────────────────────────────────────────────────────────
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SP_2, AppTheme.SP_2));
        toolbar.setBackground(AppTheme.BG);

        attendBtn = HButton.primary("출석 처리", HButton.Size.SM);
        absentBtn = HButton.secondary("결석 처리", HButton.Size.SM);
        undoBtn   = HButton.ghost("출석 취소", HButton.Size.SM);
        closeBtn  = HButton.danger("출석 마감", HButton.Size.SM);

        attendBtn.addActionListener(e -> onAttend());
        absentBtn.addActionListener(e -> onAbsent());
        undoBtn.addActionListener(e -> onUndoAttend());
        closeBtn.addActionListener(e -> onClosedAttendance());

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
    }

    private void loadAttendees() {
        // MOCK: 세션 수강자 목록
        // TODO: SELECT r.id, u.name, u.phone, m.name, m.remaining_count,
        //              r.attendance_status, r.attended_at
        //       FROM reservations r
        //       JOIN users u ON u.id = r.user_id
        //       LEFT JOIN memberships m ON m.id = r.membership_id
        //       WHERE r.session_id = ?
        //         AND r.status IN ('CONFIRMED','MEMBERSHIP_ISSUED')
        model.addRow(new Object[]{false, "홍길동", "010-1234-5678", "스피닝 A반 수강권", "15", "PENDING",  ""});
        model.addRow(new Object[]{false, "김영희", "010-2345-6789", "스피닝 A반 수강권", "20", "ATTENDED", "2025-05-20 07:05"});
        model.addRow(new Object[]{false, "이철수", "010-3456-7890", "스피닝 A반 수강권",  "5", "PENDING",  ""});
        model.addRow(new Object[]{false, "박민준", "010-4567-8901", "스피닝 A반 수강권", "16", "PENDING",  ""});
        model.addRow(new Object[]{false, "최서연", "010-5678-9012", "(미발급)",           "-", "PENDING",  ""});
    }

    private int[] getChecked() {
        java.util.List<Integer> list = new java.util.ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (Boolean.TRUE.equals(model.getValueAt(i, 0))) list.add(i);
        }
        return list.stream().mapToInt(Integer::intValue).toArray();
    }

    private void onAttend() {
        if (closed) { HDialog.alert(parentFrame(), "안내", "출석이 마감된 세션입니다.", HDialog.Type.WARNING); return; }
        int[] rows = getChecked();
        if (rows.length == 0) { HDialog.info(parentFrame(), "출석 처리할 수강자를 선택하세요."); return; }

        // PENDING 여부 확인
        for (int r : rows) {
            String status = (String) model.getValueAt(r, 5);
            if (!"PENDING".equals(status)) {
                HDialog.alert(parentFrame(), "안내", "PENDING 상태의 수강자만 선택하세요.", HDialog.Type.WARNING);
                return;
            }
        }

        boolean isPast = false; // TODO: 세션 end_time 비교
        String msg = isPast
            ? "지난 수업에 대해 " + rows.length + "명을 출석 처리하시겠습니까?\n잔여 횟수가 차감됩니다."
            : "선택한 " + rows.length + "명을 출석 처리하시겠습니까?\n잔여 횟수가 차감됩니다.";

        if (HDialog.confirm(parentFrame(), "출석 처리", msg)) {
            String now = java.time.LocalDateTime.now().toString().substring(0, 16).replace("T", " ");
            for (int r : rows) {
                // TODO: UPDATE reservations SET attendance_status='ATTENDED', attended_at=NOW() WHERE id=?
                //       membership.remaining_count -1
                //       remaining_count=0 → EXPIRED
                //       membership_histories INSERT (change_type='USE')
                String remaining = (String) model.getValueAt(r, 4);
                int rem = remaining.equals("-") ? 0 : Integer.parseInt(remaining);
                model.setValueAt(false, r, 0);
                model.setValueAt("ATTENDED", r, 5);
                model.setValueAt(now, r, 6);
                if (rem > 0) model.setValueAt(String.valueOf(rem - 1), r, 4);
            }
            HToast.success(parentFrame(), rows.length + "명이 출석 처리되었습니다.");
        }
    }

    private void onAbsent() {
        if (closed) { HDialog.alert(parentFrame(), "안내", "출석이 마감된 세션입니다.", HDialog.Type.WARNING); return; }
        int[] rows = getChecked();
        if (rows.length == 0) { HDialog.info(parentFrame(), "결석 처리할 수강자를 선택하세요."); return; }

        if (HDialog.confirm(parentFrame(), "결석 처리",
                "선택한 " + rows.length + "명을 결석 처리하시겠습니까?\n횟수는 차감되지 않습니다.")) {
            for (int r : rows) {
                // TODO: UPDATE reservations SET attendance_status='ABSENT' WHERE id=?
                model.setValueAt(false, r, 0);
                model.setValueAt("ABSENT", r, 5);
            }
            HToast.success(parentFrame(), rows.length + "명이 결석 처리되었습니다.");
        }
    }

    private void onUndoAttend() {
        if (closed) { HDialog.alert(parentFrame(), "안내", "출석이 마감된 세션입니다.", HDialog.Type.WARNING); return; }
        int[] rows = getChecked();
        if (rows.length == 0) { HDialog.info(parentFrame(), "출석 취소할 수강자를 선택하세요."); return; }

        // 처리 당일 여부 체크 (MOCK: 항상 가능)
        // TODO: attended_at.date = 오늘 여부 확인

        if (HDialog.confirm(parentFrame(), "출석 취소",
                "선택한 " + rows.length + "명의 출석을 취소하시겠습니까?\n잔여 횟수가 복구됩니다.")) {
            for (int r : rows) {
                String status = (String) model.getValueAt(r, 5);
                if (!"ATTENDED".equals(status)) continue;
                // TODO: UPDATE reservations SET attendance_status='PENDING', attended_at=NULL WHERE id=?
                //       membership.remaining_count +1 (EXPIRED → ACTIVE)
                //       membership_histories INSERT (change_type='CANCEL_RESTORE')
                String remaining = (String) model.getValueAt(r, 4);
                int rem = remaining.equals("-") ? 0 : Integer.parseInt(remaining);
                model.setValueAt(false, r, 0);
                model.setValueAt("PENDING", r, 5);
                model.setValueAt("", r, 6);
                if (!remaining.equals("-")) model.setValueAt(String.valueOf(rem + 1), r, 4);
            }
            HToast.success(parentFrame(), "출석이 취소되었습니다.");
        }
    }

    private void onClosedAttendance() {
        long pending = java.util.stream.IntStream.range(0, model.getRowCount())
            .filter(i -> "PENDING".equals(model.getValueAt(i, 5))).count();

        String extra = pending > 0
            ? "\n\n주의: 미처리 인원이 " + pending + "명 있습니다."
            : "";

        if (HDialog.confirmDanger(parentFrame(), "출석 마감",
                "출석을 마감하시겠습니까?\n마감 후에는 수정이 불가합니다." + extra)) {
            // TODO: UPDATE sessions SET attendance_closed=1, attendance_closed_at=NOW() WHERE id=?
            closed = true;
            attendBtn.setEnabled(false);
            absentBtn.setEnabled(false);
            undoBtn.setEnabled(false);
            closeBtn.setEnabled(false);
            HToast.success(parentFrame(), "출석이 마감되었습니다.");
        }
    }

    private JFrame parentFrame() {
        return (JFrame) getOwner();
    }
}
