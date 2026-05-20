package healthajo.reservation.view.admin;

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

/**
 * 관리자 — 예약 관리 화면.
 */
public class ReservationPanel extends BaseListPanel {

    private static final ReservationApplication APP = new ReservationApplication();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DT_FMT   = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final List<Reservation> reservations = new ArrayList<>();

    private HButton cancelBtn;

    public ReservationPanel() {
        super();  // loadData() 호출됨 — reservations 필드 초기화 전이므로 null 가드로 조기 리턴
        model.addTableModelListener(e -> {
            if (cancelBtn != null) cancelBtn.setEnabled(getCheckedRows().length > 0);
        });
        // 필드 초기화 완료 후 실제 데이터 로드
        model.setRowCount(0);
        loadData();
    }

    @Override protected String   pageTitle()         { return "예약 관리"; }
    @Override protected boolean  hasCheckbox()       { return true; }
    @Override protected String   searchPlaceholder() { return "회원명 또는 프로그램명 검색"; }

    @Override
    protected String[] columnNames() {
        return new String[]{"회원명", "전화번호", "프로그램명", "세션 날짜", "예약 상태", "출석 상태", "예약일"};
    }

    @Override
    protected List<? extends JComponent> toolbarButtons() {
        cancelBtn = HButton.danger("강제 취소", HButton.Size.SM);
        cancelBtn.setEnabled(false);
        cancelBtn.addActionListener(e -> onForceCancel());
        return List.of(cancelBtn);
    }

    @Override
    protected void configureColumns(TableColumnModel cm) {
        int[] widths = {100, 130, 150, 110, 120, 100, 130};
        for (int i = 0; i < widths.length; i++) {
            cm.getColumn(i + 1).setPreferredWidth(widths[i]);
        }
        table.setBadgeRenderer(5);
        table.setBadgeRenderer(6);
    }

    @Override
    protected void loadData() {
        if (reservations == null) return;  // super() 호출 시점엔 필드 미초기화
        reservations.clear();
        try {
            Page<Reservation> page = APP.findAll(currentPage - 1, pageSize);
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
        Reservation r = reservations.get(modelRow);
        HDialog.alert(parentFrame(), "예약 상세",
            "회원: " + r.userName() + " (" + r.userPhone() + ")\n" +
            "프로그램: " + r.programName() + "\n" +
            "세션 날짜: " + fmt(r.sessionDate(), DATE_FMT) + "\n" +
            "예약 상태: " + r.status() + "\n" +
            "출석 상태: " + r.attendanceStatus() + "\n" +
            "예약일: " + fmt(r.reservedAt(), DT_FMT),
            HDialog.Type.INFO);
    }

    private void onForceCancel() {
        int[] rows = getCheckedRows();
        if (rows.length == 0) return;

        for (int r : rows) {
            String status = (String) getDataValue(r, 4);
            if ("CANCELLED".equals(status)) {
                HDialog.alert(parentFrame(), "안내",
                    "이미 취소된 예약이 포함되어 있습니다. 취소되지 않은 예약만 선택하세요.",
                    HDialog.Type.WARNING);
                return;
            }
        }

        String names = buildNames(rows);
        if (!HDialog.confirmDanger(parentFrame(), "강제 취소",
                "선택한 " + rows.length + "건의 예약을 강제 취소하시겠습니까?\n" +
                "대상: " + names + "\n\n강제 취소 시 세션 예약 인원이 감소합니다.")) {
            return;
        }

        int failed = 0;
        for (int i = rows.length - 1; i >= 0; i--) {
            int modelRow = rows[i];
            if (modelRow >= reservations.size()) continue;
            Reservation r = reservations.get(modelRow);
            try {
                APP.cancelByAdmin(r);
                model.setValueAt("CANCELLED", modelRow, 5);
                model.setValueAt("ABSENT",    modelRow, 6);
                model.setValueAt(false,        modelRow, 0);
            } catch (RuntimeException ex) {
                failed++;
            }
        }

        if (failed == 0) {
            HToast.success(parentFrame(), rows.length + "건이 강제 취소되었습니다.");
        } else {
            HToast.success(parentFrame(), (rows.length - failed) + "건 취소 완료, " + failed + "건 실패.");
        }

        // 목록 새로고침 — Reservation 상태 동기화
        model.setRowCount(0);
        loadData();
    }

    // ── 유틸 ──────────────────────────────────────────────────────────────────

    private static Object[] toRow(Reservation r) {
        return new Object[]{
            false,
            r.userName(),
            r.userPhone(),
            r.programName(),
            fmt(r.sessionDate(), DATE_FMT),
            r.status(),
            r.attendanceStatus(),
            fmt(r.reservedAt(), DT_FMT)
        };
    }

    private static String fmt(java.time.LocalDateTime dt, DateTimeFormatter f) {
        return dt != null ? dt.format(f) : "";
    }

    private String buildNames(int[] rows) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(rows.length, 3); i++) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(getDataValue(rows[i], 0));
        }
        if (rows.length > 3) sb.append(" 외 ").append(rows.length - 3).append("명");
        return sb.toString();
    }
}
