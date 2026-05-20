package healthajo.reservation.view.user;

import healthajo.component.HButton;
import healthajo.component.HDialog;
import healthajo.component.HToast;
import healthajo.jdbc.core.Page;
import healthajo.reservation.application.ReservationApplication;
import healthajo.reservation.domain.Reservation;
import healthajo.template.BaseListPanel;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.TableColumnModel;

import static javax.swing.SwingConstants.CENTER;

/**
 * 사용자 — 나의 예약 목록.
 *
 * 출석 상태는 관리자가 처리하는 정보로, 사용자는 예약 확정/취소 여부만 확인.
 * 출석 결과는 [출석 내역] 탭에서 별도 확인 가능.
 */
public class MyReservationPanel extends BaseListPanel {

    private static final ReservationApplication APP = new ReservationApplication();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DT_FMT   = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Long userId;
    private final List<Reservation> reservations = new ArrayList<>();

    private HButton cancelBtn;

    public MyReservationPanel(Long userId) {
        super();  // loadData() 호출됨 — 이때 this.userId == null → 조기 리턴
        this.userId = userId;
        model.addTableModelListener(e -> {
            if (cancelBtn != null) cancelBtn.setEnabled(getCheckedRows().length > 0);
        });
        // userId 설정 후 실제 데이터 로드
        if (userId != null) {
            model.setRowCount(0);
            loadData();
        }
    }

    @Override protected String   pageTitle()         { return "나의 예약"; }
    @Override protected boolean  hasCheckbox()       { return true; }
    @Override protected String   searchPlaceholder() { return "프로그램명 검색"; }
    @Override protected boolean  serverSideSearch()  { return true; }

    @Override
    protected String[] columnNames() {
        return new String[]{"프로그램명", "세션 날짜", "시작", "종료", "예약 상태", "예약일"};
    }

    @Override
    protected List<? extends JComponent> toolbarButtons() {
        cancelBtn = HButton.danger("예약 취소", HButton.Size.SM);
        cancelBtn.setEnabled(false);
        cancelBtn.addActionListener(e -> onCancel());
        return List.of(cancelBtn);
    }

    @Override
    protected void configureColumns(TableColumnModel cm) {
        // checkbox[0] + 프로그램명[1] 날짜[2] 시작[3] 종료[4] 예약상태[5] 예약일[6]
        int[] widths = {160, 110, 60, 60, 130, 130};
        for (int i = 0; i < widths.length; i++) cm.getColumn(i + 1).setPreferredWidth(widths[i]);
        table.setKoreanBadgeRenderer(5);
        table.setRowStateColumn(5);
        table.setCellAlignment(CENTER);
        table.setHeaderAlignment(CENTER);
    }

    @Override
    protected void loadData() {
        if (reservations == null) return;  // super() 호출 시점엔 필드 미초기화
        reservations.clear();
        if (userId == null) {
            setTotalCount(0);
            return;
        }
        try {
            Page<Reservation> page = APP.findByUserId(userId, currentPage - 1, pageSize, searchKeyword);
            for (Reservation r : page.getContent()) {
                reservations.add(r);
                model.addRow(toRow(r));
            }
            setTotalCount((int) page.getTotalCount());
        } catch (RuntimeException ex) {
            HDialog.error(parentFrame(), "예약 목록을 불러오지 못했습니다.\n" + ex.getMessage());
            setTotalCount(0);
        }
    }

    @Override
    protected void onRowDoubleClick(int modelRow) {
        if (modelRow >= reservations.size()) return;
        new ReservationDetailDialog(parentFrame(), reservations.get(modelRow)).setVisible(true);
    }

    private void onCancel() {
        int[] rows = getCheckedRows();
        if (rows.length == 0) return;

        for (int r : rows) {
            String status = (String) getDataValue(r, 4);
            if (!"CONFIRMED".equals(status)) {
                HDialog.alert(parentFrame(), "취소 불가",
                    "예약 확정 상태의 예약만 취소할 수 있습니다.\n" +
                    "이미 회원권이 사용되었거나 취소된 예약은 선택에서 제외하세요.",
                    HDialog.Type.WARNING);
                return;
            }
        }

        if (!HDialog.confirmDanger(parentFrame(), "예약 취소",
                rows.length + "건의 예약을 취소하시겠습니까?")) {
            return;
        }

        int failed = 0;
        for (int i = rows.length - 1; i >= 0; i--) {
            int modelRow = rows[i];
            if (modelRow >= reservations.size()) continue;
            Reservation r = reservations.get(modelRow);
            try {
                APP.cancelByUser(r);
                model.setValueAt("CANCELLED", modelRow, 5);
                model.setValueAt(false,        modelRow, 0);
            } catch (RuntimeException ex) {
                failed++;
            }
        }

        if (failed == 0) {
            HToast.success(parentFrame(), rows.length + "건의 예약이 취소되었습니다.");
        } else {
            HToast.success(parentFrame(), (rows.length - failed) + "건 취소 완료, " + failed + "건 실패.");
        }

        model.setRowCount(0);
        loadData();
    }

    // ── 유틸 ──────────────────────────────────────────────────────────────────

    private static Object[] toRow(Reservation r) {
        return new Object[]{
            false,
            r.programName(),
            r.sessionDate() != null ? r.sessionDate().format(DATE_FMT) : "",
            r.startTime(),
            r.endTime(),
            r.status(),
            r.reservedAt() != null ? r.reservedAt().format(DT_FMT) : ""
        };
    }
}
