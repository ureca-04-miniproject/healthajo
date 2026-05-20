package healthajo.reservation.view.user;

import healthajo.component.HButton;
import healthajo.component.HDialog;
import healthajo.component.HToast;
import healthajo.template.BaseListPanel;
import java.util.List;
import javax.swing.*;
import javax.swing.table.TableColumnModel;
import static javax.swing.SwingConstants.CENTER;

/**
 * 사용자 — 나의 예약 목록.
 *
 * 출석 상태는 관리자가 처리하는 정보로, 사용자는 예약 확정/취소 여부만 확인.
 * 출석 결과는 [출석 내역] 탭에서 별도 확인 가능.
 *
 * TODO: SELECT p.name, s.session_date,
 *              TIME_FORMAT(s.start_time,'%H:%i'), TIME_FORMAT(s.end_time,'%H:%i'),
 *              r.status,
 *              DATE_FORMAT(r.reserved_at,'%Y-%m-%d')
 *       FROM reservations r
 *       JOIN sessions s ON s.id = r.session_id
 *       JOIN programs p ON p.id = s.program_id
 *       WHERE r.user_id = ?
 *       ORDER BY s.session_date DESC, r.reserved_at DESC
 */
public class MyReservationPanel extends BaseListPanel {

    private HButton cancelBtn;

    public MyReservationPanel() {
        super();
        model.addTableModelListener(e -> {
            if (cancelBtn != null) cancelBtn.setEnabled(getCheckedRows().length > 0);
        });
    }

    @Override protected String   pageTitle()         { return "나의 예약"; }
    @Override protected boolean  hasCheckbox()       { return true; }
    @Override protected String   searchPlaceholder() { return "프로그램명 또는 날짜 검색"; }

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
        int[] widths = {160, 100, 60, 60, 130, 100};
        for (int i = 0; i < widths.length; i++) cm.getColumn(i + 1).setPreferredWidth(widths[i]);
        table.setKoreanBadgeRenderer(5);
        table.setRowStateColumn(5);       // CANCELLED → 잠김, MEMBERSHIP_ISSUED → 흐림
        table.setCellAlignment(CENTER);
        table.setHeaderAlignment(CENTER);
    }

    @Override
    protected void loadData() {
        // MOCK
        // TODO: DB 조회 후 교체 (현재 로그인한 user_id 기준)
        model.addRow(new Object[]{false, "스피닝 A반",    "2025-05-22", "07:00", "08:00", "CONFIRMED",        "2025-05-10"});
        model.addRow(new Object[]{false, "요가 기초반",   "2025-05-20", "10:00", "11:00", "MEMBERSHIP_ISSUED", "2025-04-20"});
        model.addRow(new Object[]{false, "필라테스 중급", "2025-05-15", "14:00", "15:00", "MEMBERSHIP_ISSUED", "2025-05-01"});
        model.addRow(new Object[]{false, "골프 입문반",   "2025-05-10", "09:00", "10:30", "CANCELLED",        "2025-04-28"});
        setTotalCount(model.getRowCount());
    }

    @Override
    protected void onRowDoubleClick(int modelRow) {
        new ReservationDetailDialog(parentFrame(), getRowData(modelRow)).setVisible(true);
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

        if (HDialog.confirmDanger(parentFrame(), "예약 취소",
                rows.length + "건의 예약을 취소하시겠습니까?\n" +
                "COUNT 타입 회원권의 잔여 횟수가 복구됩니다.")) {
            // TODO: UPDATE reservations SET status='CANCELLED', cancelled_by='USER', cancelled_at=NOW()
            //       WHERE id IN (?) AND user_id = ?
            //       UPDATE sessions SET booked_count = booked_count - 1 WHERE id IN (...)
            //       COUNT 타입 회원권: remaining_count +1 (EXPIRED → ACTIVE)
            for (int i = rows.length - 1; i >= 0; i--) {
                model.setValueAt("CANCELLED", rows[i], 5);
                model.setValueAt(false,        rows[i], 0);
            }
            HToast.success(parentFrame(), rows.length + "건의 예약이 취소되었습니다.");
        }
    }

    private static String toKorean(String status) {
        return switch (status) {
            case "CONFIRMED"         -> "예약 확정";
            case "MEMBERSHIP_ISSUED" -> "회원권 사용";
            case "CANCELLED"         -> "취소";
            case "ACTIVE"            -> "활성";
            case "EXPIRED"           -> "만료";
            case "ATTENDED"          -> "출석";
            case "ABSENT"            -> "결석";
            case "PENDING"           -> "미처리";
            case "OPEN"              -> "운영 중";
            default                  -> status;
        };
    }
}
