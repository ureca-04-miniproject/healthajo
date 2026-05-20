package healthajo.attendance.view.user;

import healthajo.component.HButton;
import healthajo.component.HDialog;
import healthajo.component.HToast;
import healthajo.jdbc.core.Page;
import healthajo.reservation.application.ReservationApplication;
import healthajo.reservation.domain.Reservation;
import healthajo.template.BaseListPanel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.TableColumnModel;

import static javax.swing.SwingConstants.CENTER;

/**
 * 사용자 — 출석 내역 + 자기 체크인.
 *
 * PENDING 상태이고 세션 날짜가 오늘 이전인 예약에 대해 직접 출석 체크 가능.
 */
public class MyAttendancePanel extends BaseListPanel {

    private static final ReservationApplication APP = new ReservationApplication();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private Long userId;
    private final List<Reservation> attendances = new ArrayList<>();

    private HButton checkInBtn;

    public MyAttendancePanel(Long userId) {
        super();  // loadData() 조기 리턴 (attendances == null)
        this.userId = userId;
        table.getSelectionModel().addListSelectionListener(e -> updateCheckInBtn());
        if (userId != null) {
            model.setRowCount(0);
            loadData();
        }
    }

    @Override protected String  pageTitle()         { return "출석 내역"; }
    @Override protected boolean hasCheckbox()       { return false; }
    @Override protected String  searchPlaceholder() { return "프로그램명 검색"; }
    @Override protected boolean serverSideSearch()  { return true; }

    @Override
    protected String[] columnNames() {
        return new String[]{"프로그램명", "날짜", "시작", "종료", "출석 상태"};
    }

    @Override
    protected List<? extends JComponent> toolbarButtons() {
        checkInBtn = HButton.primary("출석 체크", HButton.Size.SM);
        checkInBtn.setEnabled(false);
        checkInBtn.addActionListener(e -> onCheckIn());
        return List.of(checkInBtn);
    }

    @Override
    protected void configureColumns(TableColumnModel cm) {
        int[] widths = {160, 110, 70, 70, 100};
        for (int i = 0; i < widths.length; i++) cm.getColumn(i).setPreferredWidth(widths[i]);
        table.setKoreanBadgeRenderer(4);
        table.setRowStateColumn(4);
        table.setCellAlignment(CENTER);
        table.setHeaderAlignment(CENTER);
    }

    @Override
    protected void loadData() {
        if (attendances == null) return;  // super() 호출 시점
        attendances.clear();
        if (userId == null) { setTotalCount(0); return; }
        try {
            Page<Reservation> page = APP.findAttendancesByUserId(userId, currentPage - 1, pageSize, searchKeyword);
            for (Reservation r : page.getContent()) {
                attendances.add(r);
                model.addRow(new Object[]{
                    r.programName(),
                    r.sessionDate() != null ? r.sessionDate().format(DATE_FMT) : "",
                    r.startTime(),
                    r.endTime(),
                    r.attendanceStatus()
                });
            }
            setTotalCount((int) page.getTotalCount());
        } catch (RuntimeException ex) {
            HDialog.error(parentFrame(), "출석 내역을 불러오지 못했습니다.\n" + ex.getMessage());
            setTotalCount(0);
        }
    }

    @Override
    protected void onRowDoubleClick(int modelRow) {
        // 읽기 전용 — 더블클릭으로 출석 체크
        checkInSelectedRow(modelRow);
    }

    // ── 출석 체크 ─────────────────────────────────────────────────────────────

    private void onCheckIn() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return;
        checkInSelectedRow(table.convertRowIndexToModel(viewRow));
    }

    private void checkInSelectedRow(int modelRow) {
        if (modelRow >= attendances.size()) return;
        Reservation r = attendances.get(modelRow);

        if (!"PENDING".equals(r.attendanceStatus())) {
            HDialog.info(parentFrame(), "이미 출석 처리된 예약입니다.");
            return;
        }
        if (r.sessionDate() == null || r.sessionDate().toLocalDate().isAfter(LocalDate.now())) {
            HDialog.info(parentFrame(), "아직 수업일이 되지 않아 출석 체크할 수 없습니다.");
            return;
        }
        if (!HDialog.confirm(parentFrame(), "출석 체크",
                r.programName() + " (" + r.sessionDate().format(DATE_FMT) + ") 출석을 체크하시겠습니까?")) {
            return;
        }

        try {
            APP.checkIn(r.id());
            model.setValueAt("ATTENDED", modelRow, 4);
            attendances.set(modelRow, new Reservation(
                r.id(), r.userId(), r.userName(), r.userPhone(),
                r.sessionId(), r.sessionDate(), r.startTime(), r.endTime(),
                r.programId(), r.programName(), r.membershipId(),
                r.status(), r.cancelledBy(), r.reservedAt(), r.cancelledAt(),
                "ATTENDED", r.attendedAt()
            ));
            updateCheckInBtn();
            HToast.success(parentFrame(), "출석이 체크되었습니다.");
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            HDialog.error(parentFrame(), "출석 체크에 실패했습니다.\n" + ex.getMessage());
        }
    }

    private void updateCheckInBtn() {
        if (checkInBtn == null) return;
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { checkInBtn.setEnabled(false); return; }
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow >= attendances.size()) { checkInBtn.setEnabled(false); return; }
        Reservation r = attendances.get(modelRow);
        boolean canCheckIn = "PENDING".equals(r.attendanceStatus())
            && r.sessionDate() != null
            && !r.sessionDate().toLocalDate().isAfter(LocalDate.now());
        checkInBtn.setEnabled(canCheckIn);
    }
}
