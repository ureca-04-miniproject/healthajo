package healthajo.attendance.view.admin;

import healthajo.component.HButton;
import healthajo.component.HLabel;
import healthajo.component.theme.AppTheme;
import healthajo.template.BaseListPanel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;
import javax.swing.table.TableColumnModel;

/**
 * 관리자 — 출석 관리 화면.
 *
 * 오늘 날짜 기준 세션 목록을 표시. ◀ / 오늘 / ▶ 로 날짜 이동.
 * 세션 행 더블클릭 → AttendanceDetailDialog (출석 처리).
 *
 * TODO: SELECT s.id, p.name, s.session_date,
 *              TIME_FORMAT(s.start_time,'%H:%i'), TIME_FORMAT(s.end_time,'%H:%i'),
 *              s.booked_count,
 *              SUM(r.attendance_status='ATTENDED') AS attended,
 *              SUM(r.attendance_status='PENDING')  AS pending,
 *              s.attendance_closed
 *       FROM sessions s
 *       JOIN programs p ON p.id = s.program_id
 *       LEFT JOIN reservations r ON r.session_id = s.id
 *               AND r.status IN ('CONFIRMED','MEMBERSHIP_ISSUED')
 *       WHERE s.session_date = ?
 *         AND s.status = 'OPEN'
 *       GROUP BY s.id
 *       ORDER BY s.start_time
 */
public class AttendancePanel extends BaseListPanel {

    // NOTE: selectedDate and dateLabel are initialized inside toolbarButtons()
    // which is called from super(). This avoids the NPE caused by field initializers
    // running after super() returns.
    private LocalDate selectedDate;
    private JLabel    dateLabel;

    public AttendancePanel() { super(); }

    @Override protected String   pageTitle()         { return "출석 관리"; }
    @Override protected boolean  hasCheckbox()       { return false; }
    @Override protected String   searchPlaceholder() { return "프로그램명 검색"; }

    @Override
    protected String[] columnNames() {
        return new String[]{"프로그램명", "날짜", "시작", "종료", "전체", "출석", "미처리", "마감"};
    }

    @Override
    protected List<? extends JComponent> toolbarButtons() {
        // Initialize selectedDate here (before loadData() is called by super())
        selectedDate = LocalDate.now();
        dateLabel    = new JLabel("기준일: " + selectedDate);
        dateLabel.setFont(AppTheme.BODY_SM);
        dateLabel.setForeground(AppTheme.TEXT_SECONDARY);

        HButton prevBtn  = HButton.ghost("◀", HButton.Size.SM);
        HButton todayBtn = HButton.secondary("오늘", HButton.Size.SM);
        HButton nextBtn  = HButton.ghost("▶", HButton.Size.SM);

        prevBtn.setPreferredSize(new Dimension(36, 36));
        nextBtn.setPreferredSize(new Dimension(36, 36));

        prevBtn.addActionListener(e -> {
            selectedDate = selectedDate.minusDays(1);
            dateLabel.setText("기준일: " + selectedDate);
            reloadData();
        });
        nextBtn.addActionListener(e -> {
            selectedDate = selectedDate.plusDays(1);
            dateLabel.setText("기준일: " + selectedDate);
            reloadData();
        });
        todayBtn.addActionListener(e -> {
            selectedDate = LocalDate.now();
            dateLabel.setText("기준일: " + selectedDate);
            reloadData();
        });

        return List.of(dateLabel, prevBtn, todayBtn, nextBtn);
    }

    @Override
    protected void configureColumns(TableColumnModel cm) {
        int[] widths = {160, 100, 60, 60, 60, 60, 70, 60};
        for (int i = 0; i < widths.length; i++) cm.getColumn(i).setPreferredWidth(widths[i]);
    }

    @Override
    protected void loadData() {
        model.setRowCount(0);
        // selectedDate may be null on first call from super() if toolbarButtons()
        // hasn't been called yet — safe because toolbarButtons() is always called first
        // in buildHeader() which precedes loadData() in BaseListPanel constructor.
        LocalDate d = selectedDate != null ? selectedDate : LocalDate.now();
        String date = d.toString();

        // MOCK
        // TODO: DB 조회 (selectedDate 파라미터로 WHERE s.session_date = ?)
        model.addRow(new Object[]{"스피닝 A반",    date, "07:00", "08:00", "15", "12", "3", "미완료"});
        model.addRow(new Object[]{"요가 기초반",   date, "10:00", "11:00", "12", "10", "2", "미완료"});
        model.addRow(new Object[]{"필라테스 중급", date, "14:00", "15:00", "10",  "8", "2", "미완료"});
        model.addRow(new Object[]{"골프 입문반",   date, "09:00", "10:30",  "6",  "4", "2", "미완료"});
        setTotalCount(model.getRowCount());
    }

    @Override
    protected void onRowDoubleClick(int modelRow) {
        Object[] data = getRowData(modelRow);
        new AttendanceDetailDialog(parentFrame(), data).setVisible(true);
        reloadData();
    }

    private void reloadData() {
        model.setRowCount(0);
        loadData();
    }
}
