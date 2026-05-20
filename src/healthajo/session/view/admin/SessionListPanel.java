package healthajo.session.view.admin;

import healthajo.attendance.view.admin.AttendanceDetailDialog;
import healthajo.component.HButton;
import healthajo.component.HDialog;
import healthajo.component.HTable;
import healthajo.component.HToast;
import healthajo.component.theme.AppTheme;
import healthajo.template.BaseListPanel;
import java.util.List;
import javax.swing.*;
import javax.swing.table.TableColumnModel;

/**
 * 관리자 — 세션 관리 화면.
 *
 * TODO: SELECT s.id, p.name, s.session_date,
 *              TIME_FORMAT(s.start_time,'%H:%i'), TIME_FORMAT(s.end_time,'%H:%i'),
 *              s.capacity, s.booked_count, IFNULL(i.name, '-'), s.status
 *       FROM sessions s
 *       JOIN programs p ON p.id = s.program_id
 *       LEFT JOIN instructors i ON i.id = s.instructor_id
 *       WHERE s.session_date >= CURDATE() - INTERVAL 30 DAY
 *       ORDER BY s.session_date, s.start_time
 */
public class SessionListPanel extends BaseListPanel {

    public SessionListPanel() { super(); }

    @Override protected String   pageTitle()         { return "세션 관리"; }
    @Override protected boolean  hasCheckbox()       { return false; }
    @Override protected String   searchPlaceholder() { return "프로그램명 또는 날짜 검색"; }

    @Override
    protected String[] columnNames() {
        return new String[]{"프로그램명", "날짜", "시작", "종료", "정원", "예약 수", "강사", "상태"};
    }

    @Override
    protected List<? extends JComponent> toolbarButtons() {
        HButton assignBtn = HButton.secondary("강사 배정", HButton.Size.SM);
        assignBtn.addActionListener(e -> onAssignInstructor());

        HButton cancelBtn = HButton.danger("세션 취소", HButton.Size.SM);
        cancelBtn.addActionListener(e -> onCancelSession());

        return List.of(assignBtn, cancelBtn);
    }

    @Override
    protected void configureColumns(TableColumnModel cm) {
        int[] widths = {150, 100, 60, 60, 60, 60, 90, 80};
        for (int i = 0; i < widths.length; i++) {
            cm.getColumn(i).setPreferredWidth(widths[i]);
        }
        table.setBadgeRenderer(7);
    }

    @Override
    protected void loadData() {
        // MOCK
        // TODO: DB 조회 후 교체
        model.addRow(new Object[]{"스피닝 A반",    "2025-05-20", "07:00", "08:00", "20", "15", "김강사", "OPEN"});
        model.addRow(new Object[]{"요가 기초반",   "2025-05-20", "10:00", "11:00", "15", "12", "이강사", "OPEN"});
        model.addRow(new Object[]{"필라테스 중급", "2025-05-20", "14:00", "15:00", "12", "10", "-",     "OPEN"});
        model.addRow(new Object[]{"스피닝 A반",    "2025-05-22", "07:00", "08:00", "20", "14", "김강사", "OPEN"});
        model.addRow(new Object[]{"요가 기초반",   "2025-05-22", "10:00", "11:00", "15", "11", "이강사", "OPEN"});
        model.addRow(new Object[]{"골프 입문반",   "2025-05-21", "09:00", "10:30", "10",  "4", "-",     "OPEN"});
        setTotalCount(model.getRowCount());
    }

    @Override
    protected void onRowDoubleClick(int modelRow) {
        Object[] row = getRowData(modelRow);
        // row: [프로그램명, 날짜, 시작, 종료, 정원, 예약수, 강사, 상태]
        // AttendanceDetailDialog expects: [program, date, start, end, capacity, booked, pending]
        Object[] data = new Object[]{row[0], row[1], row[2], row[3], row[4], row[5], "—"};
        new AttendanceDetailDialog(parentFrame(), data).setVisible(true);
    }

    private void onAssignInstructor() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            HDialog.info(parentFrame(), "강사를 배정할 세션을 선택하세요.");
            return;
        }
        int mr = table.convertRowIndexToModel(viewRow);
        String program = (String) model.getValueAt(mr, 0);
        String date    = (String) model.getValueAt(mr, 1);

        // TODO: SELECT id, name FROM instructors WHERE status = 'ACTIVE'
        // MOCK: 강사 목록
        String[] instructors = {"김강사", "이강사", "박강사", "최강사"};
        String selected = (String) JOptionPane.showInputDialog(
            parentFrame(), "[" + program + " " + date + "]\n배정할 강사를 선택하세요:",
            "강사 배정", JOptionPane.PLAIN_MESSAGE, null, instructors, instructors[0]);

        if (selected != null) {
            // TODO: UPDATE sessions SET instructor_id = ? WHERE id = ?
            model.setValueAt(selected, mr, 6);
            HToast.success(parentFrame(), "강사가 배정되었습니다: " + selected);
        }
    }

    private void onCancelSession() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            HDialog.info(parentFrame(), "취소할 세션을 선택하세요.");
            return;
        }
        int mr     = table.convertRowIndexToModel(viewRow);
        String status = (String) model.getValueAt(mr, 7);
        if ("CANCELLED".equals(status)) {
            HDialog.alert(parentFrame(), "안내", "이미 취소된 세션입니다.", HDialog.Type.WARNING);
            return;
        }
        String program = (String) model.getValueAt(mr, 0);
        String date    = (String) model.getValueAt(mr, 1);
        String booked  = (String) model.getValueAt(mr, 5);

        if (HDialog.confirmDanger(parentFrame(), "세션 취소",
                "[" + program + " " + date + "] 세션을 취소하시겠습니까?\n" +
                "예약자 " + booked + "명의 예약이 자동 취소되고\nCOUNT 타입 회원권 횟수가 복구됩니다.")) {
            // TODO: UPDATE sessions SET status='CANCELLED' WHERE id=?
            //       UPDATE reservations SET status='CANCELLED', cancelled_by='ADMIN'
            //              WHERE session_id=? AND status IN ('CONFIRMED','MEMBERSHIP_ISSUED')
            //       COUNT 타입 회원권 remaining_count +1 (EXPIRED → ACTIVE)
            model.setValueAt("CANCELLED", mr, 7);
            HToast.success(parentFrame(), "세션이 취소되었습니다.");
        }
    }
}
