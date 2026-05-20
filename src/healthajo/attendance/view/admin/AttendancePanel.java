package healthajo.attendance.view.admin;

import healthajo.attendance.application.AttendanceApplication;
import healthajo.attendance.domain.AttendanceSession;
import healthajo.component.HButton;
import healthajo.component.HDatePicker;
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

    // selectedDate·datePicker는 super()에서 호출되는 toolbarButtons() 안에서 초기화한다
    // (필드 이니셜라이저는 super() 이후 실행되므로 NPE 방지).
    private LocalDate   selectedDate;
    private HDatePicker datePicker;
    private JLabel      contextLabel;

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
    @Override protected boolean  serverSideSearch()  { return true; }

    @Override
    protected String[] columnNames() {
        return new String[]{"프로그램명", "날짜", "시작", "종료", "예약", "정원", "마감"};
    }

    @Override
    protected List<? extends JComponent> toolbarButtons() {
        // super()에서 loadData()가 호출되기 전에 기준일 초기화
        selectedDate = LocalDate.now();

        contextLabel = new JLabel("");
        contextLabel.setFont(AppTheme.SMALL);
        contextLabel.setForeground(AppTheme.TEXT_MUTED);

        JLabel dateCaption = new JLabel("출석 조회 날짜");
        dateCaption.setFont(AppTheme.BODY_SM);
        dateCaption.setForeground(AppTheme.TEXT_SECONDARY);

        datePicker = new HDatePicker("날짜 선택");
        datePicker.setDate(selectedDate);
        datePicker.setPreferredSize(new Dimension(150, 36));
        datePicker.setMaximumSize(new Dimension(150, 36));

        HButton searchBtn = HButton.primary("조회", HButton.Size.SM);
        HButton prevBtn   = HButton.ghost("◀", HButton.Size.SM);
        HButton todayBtn  = HButton.secondary("오늘", HButton.Size.SM);
        HButton nextBtn   = HButton.ghost("▶", HButton.Size.SM);

        prevBtn.setPreferredSize(new Dimension(36, 36));
        nextBtn.setPreferredSize(new Dimension(36, 36));

        searchBtn.addActionListener(e -> {
            LocalDate d = datePicker.getDate();
            applyDate(d != null ? d : LocalDate.now());
        });
        prevBtn.addActionListener(e -> applyDate(currentDate().minusDays(1)));
        nextBtn.addActionListener(e -> applyDate(currentDate().plusDays(1)));
        todayBtn.addActionListener(e -> applyDate(LocalDate.now()));

        return List.of(contextLabel, dateCaption, datePicker, searchBtn, prevBtn, todayBtn, nextBtn);
    }

    private void updateContext(LocalDate d, int total) {
        if (contextLabel == null) return;
        contextLabel.setText(total <= 0
            ? d + " — 해당 날짜에 세션이 없습니다 (날짜를 바꿔 조회하세요)"
            : d + " 기준 세션 " + total + "건");
    }

    private LocalDate currentDate() {
        return selectedDate != null ? selectedDate : LocalDate.now();
    }

    private void applyDate(LocalDate d) {
        selectedDate = d;
        if (datePicker != null) datePicker.setDate(d);
        currentPage = 1;
        reloadData();
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
            Page<AttendanceSession> page = APP.findSessionsByDate(d, currentPage - 1, pageSize, searchKeyword);
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
            int total = (int) page.getTotalCount();
            updateContext(d, total);
            setTotalCount(total);
        } catch (RuntimeException ex) {
            HDialog.error(parentFrame(), "세션 목록을 불러오지 못했습니다.\n" + ex.getMessage());
            updateContext(d, 0);
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
