package healthajo.attendance.view.user;

import healthajo.template.BaseListPanel;
import java.util.List;
import javax.swing.*;
import javax.swing.table.TableColumnModel;
import static javax.swing.SwingConstants.CENTER;

/**
 * 사용자 — 나의 출석 내역.
 *
 * TODO: SELECT p.name, s.session_date,
 *              TIME_FORMAT(s.start_time,'%H:%i'), TIME_FORMAT(s.end_time,'%H:%i'),
 *              r.attendance_status
 *       FROM reservations r
 *       JOIN sessions s ON s.id = r.session_id
 *       JOIN programs p ON p.id = s.program_id
 *       WHERE r.user_id = ? AND r.status != 'CANCELLED'
 *       ORDER BY s.session_date DESC, s.start_time DESC
 */
public class MyAttendancePanel extends BaseListPanel {

    public MyAttendancePanel() { super(); }

    @Override protected String   pageTitle()         { return "출석 내역"; }
    @Override protected boolean  hasCheckbox()       { return false; }
    @Override protected String   searchPlaceholder() { return "프로그램명 검색"; }

    @Override
    protected String[] columnNames() {
        return new String[]{"프로그램명", "날짜", "시작", "종료", "출석 상태"};
    }

    @Override
    protected List<? extends JComponent> toolbarButtons() {
        return List.of();
    }

    @Override
    protected void configureColumns(TableColumnModel cm) {
        int[] widths = {160, 110, 70, 70, 100};
        for (int i = 0; i < widths.length; i++) cm.getColumn(i).setPreferredWidth(widths[i]);
        table.setKoreanBadgeRenderer(4);
        table.setRowStateColumn(4);       // ABSENT → 흐림
        table.setCellAlignment(CENTER);
        table.setHeaderAlignment(CENTER);
    }

    @Override
    protected void loadData() {
        // MOCK
        // TODO: DB 조회 후 교체 (현재 로그인한 user_id 기준)
        model.addRow(new Object[]{"스피닝 A반",    "2025-05-20", "07:00", "08:00", "ATTENDED"});
        model.addRow(new Object[]{"요가 기초반",   "2025-05-20", "10:00", "11:00", "ATTENDED"});
        model.addRow(new Object[]{"필라테스 중급", "2025-05-15", "14:00", "15:00", "ATTENDED"});
        model.addRow(new Object[]{"스피닝 A반",    "2025-05-13", "07:00", "08:00", "ABSENT"});
        model.addRow(new Object[]{"요가 기초반",   "2025-05-06", "10:00", "11:00", "ATTENDED"});
        model.addRow(new Object[]{"필라테스 중급", "2025-04-30", "14:00", "15:00", "ATTENDED"});
        model.addRow(new Object[]{"스피닝 A반",    "2025-04-29", "07:00", "08:00", "ATTENDED"});
        setTotalCount(model.getRowCount());
    }

    @Override
    protected void onRowDoubleClick(int modelRow) {
        // 출석 내역은 읽기 전용
    }
}
