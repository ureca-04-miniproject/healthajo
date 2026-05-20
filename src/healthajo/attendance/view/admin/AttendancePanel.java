package healthajo.attendance.view.admin;

import healthajo.attendance.application.AttendanceApplication;
import healthajo.attendance.domain.AttendanceSession;
import healthajo.component.HButton;
import healthajo.component.HDialog;
import healthajo.component.theme.AppTheme;
import healthajo.jdbc.core.Page;
import healthajo.template.BaseListPanel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.TableColumnModel;

/**
 * 관리자 — 출석 관리 화면.
 *
 * 오늘 날짜 기준 세션 목록을 표시. ◀ / 오늘 / ▶ 로 날짜 이동.
 * 세션 행 더블클릭 → AttendanceDetailDialog (출석 처리).
 *
 * SELECT s.id, p.name, s.session_date, s.start_time, s.end_time,
 *        s.booked_count, s.capacity, s.attendance_closed
 *   FROM sessions s JOIN programs p ON p.id = s.program_id
 *  WHERE s.session_date = ? AND s.status = 'OPEN'
 *  ORDER BY s.start_time
 */
public class AttendancePanel extends BaseListPanel {

    private static final AttendanceApplication APP = new AttendanceApplication();

    // NOTE: selectedDate and dateLabel are initialized inside toolbarButtons()
    // which is called from super(). This avoids the NPE caused by field initializers
    // running after super() returns.
    private LocalDate selectedDate;
    private JLabel    dateLabel;

    // 행 인덱스 ↔ 세션 도메인 매핑 (더블클릭 시 세션 ID 전달용)
    private final List<AttendanceSession> sessions = new ArrayList<>();

    public AttendancePanel() {
        super(); // loadData() 호출됨 — sessions 필드 초기화 전이므로 null 가드로 조기 리턴
        // 필드 초기화 완료 후 실제 데이터 로드
        reloadData();
    }

    @Override protected String   pageTitle()         { return "출석 관리"; }
    @Override protected boolean  hasCheckbox()       { return false; }
    @Override protected String   searchPlaceholder() { return "프로그램명 검색"; }

    @Override
    protected String[] columnNames() {
        return new String[]{"프로그램명", "날짜", "시작", "종료", "예약", "정원", "마감"};
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
            currentPage = 1;
            reloadData();
        });
        nextBtn.addActionListener(e -> {
            selectedDate = selectedDate.plusDays(1);
            dateLabel.setText("기준일: " + selectedDate);
            currentPage = 1;
            reloadData();
        });
        todayBtn.addActionListener(e -> {
            selectedDate = LocalDate.now();
            dateLabel.setText("기준일: " + selectedDate);
            currentPage = 1;
            reloadData();
        });

        return List.of(dateLabel, prevBtn, todayBtn, nextBtn);
    }

    @Override
    protected void configureColumns(TableColumnModel cm) {
        int[] widths = {200, 110, 70, 70, 60, 60, 70};
        for (int i = 0; i < widths.length; i++) cm.getColumn(i).setPreferredWidth(widths[i]);
    }

    @Override
    protected void loadData() {
        // selectedDate may be null on first call from super() before toolbarButtons()
        // has assigned it — guard with today's date. sessions list is field-initialized
        // before super() returns? No: super() runs first, so guard null here too.
        if (sessions == null) return;
        model.setRowCount(0);
        sessions.clear();
        LocalDate d = selectedDate != null ? selectedDate : LocalDate.now();
        try {
            Page<AttendanceSession> page = APP.findSessionsByDate(d, currentPage - 1, pageSize);
            for (AttendanceSession s : page.getContent()) {
                sessions.add(s);
                model.addRow(new Object[]{
                    s.programName(),
                    s.sessionDate(),
                    s.startTime(),
                    s.endTime(),
                    String.valueOf(s.bookedCount()),
                    String.valueOf(s.capacity()),
                    s.attendanceClosed() ? "마감" : "미완료"
                });
            }
            setTotalCount((int) page.getTotalCount());
        } catch (RuntimeException ex) {
            HDialog.error(parentFrame(), "세션 목록을 불러오지 못했습니다.\n" + ex.getMessage());
            setTotalCount(0);
        }
    }

    @Override
    protected void onRowDoubleClick(int modelRow) {
        if (modelRow < 0 || modelRow >= sessions.size()) return;
        AttendanceSession s = sessions.get(modelRow);
        new AttendanceDetailDialog(parentFrame(), s).setVisible(true);
        reloadData();
    }

    private void reloadData() {
        model.setRowCount(0);
        loadData();
    }
}
