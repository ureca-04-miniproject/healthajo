package healthajo.reservation.view.admin;

import healthajo.component.HButton;
import healthajo.component.HDialog;
import healthajo.component.HToast;
import healthajo.component.theme.AppTheme;
import healthajo.template.BaseListPanel;
import java.util.List;
import javax.swing.*;
import javax.swing.table.TableColumnModel;

/**
 * 관리자 — 예약 관리 화면.
 *
 * TODO: SELECT u.name, u.phone, p.name AS program_name,
 *              s.session_date, r.status, r.attendance_status,
 *              DATE_FORMAT(r.reserved_at, '%Y-%m-%d')
 *       FROM reservations r
 *       JOIN users u ON u.id = r.user_id
 *       JOIN sessions s ON s.id = r.session_id
 *       JOIN programs p ON p.id = s.program_id
 *       WHERE r.status != 'CANCELLED'
 *       ORDER BY r.reserved_at DESC
 */
public class ReservationPanel extends BaseListPanel {

    private HButton cancelBtn;

    public ReservationPanel() {
        super();
        model.addTableModelListener(e -> {
            if (cancelBtn != null) cancelBtn.setEnabled(getCheckedRows().length > 0);
        });
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
        int[] widths = {100, 130, 150, 100, 120, 100, 100};
        for (int i = 0; i < widths.length; i++) {
            cm.getColumn(i + 1).setPreferredWidth(widths[i]);
        }
        table.setBadgeRenderer(5);  // 예약 상태 (offset 포함: 실제 컬럼 5)
        table.setBadgeRenderer(6);  // 출석 상태
    }

    @Override
    protected void loadData() {
        // MOCK
        model.addRow(new Object[]{false, "홍길동", "010-1234-5678", "스피닝 A반",    "2025-05-20", "CONFIRMED",        "PENDING",  "2025-05-01"});
        model.addRow(new Object[]{false, "김영희", "010-2345-6789", "요가 기초반",   "2025-05-20", "MEMBERSHIP_ISSUED", "ATTENDED", "2025-04-20"});
        model.addRow(new Object[]{false, "이철수", "010-3456-7890", "스피닝 A반",    "2025-05-22", "CONFIRMED",        "PENDING",  "2025-05-03"});
        model.addRow(new Object[]{false, "박민준", "010-4567-8901", "골프 입문반",   "2025-05-21", "CONFIRMED",        "PENDING",  "2025-05-05"});
        model.addRow(new Object[]{false, "최서연", "010-5678-9012", "필라테스 중급", "2025-05-19", "MEMBERSHIP_ISSUED", "ATTENDED", "2025-04-18"});
        model.addRow(new Object[]{false, "강동원", "010-7890-1234", "요가 기초반",   "2025-05-22", "CONFIRMED",        "PENDING",  "2025-05-06"});
        setTotalCount(model.getRowCount());
    }

    @Override
    protected void onRowDoubleClick(int modelRow) {
        // 예약 상세 팝업 (간단 정보 표시)
        Object[] data = getRowData(modelRow);
        HDialog.alert(parentFrame(), "예약 상세",
            "회원: " + data[0] + " (" + data[1] + ")\n" +
            "프로그램: " + data[2] + "\n" +
            "세션 날짜: " + data[3] + "\n" +
            "예약 상태: " + data[4] + "\n" +
            "출석 상태: " + data[5] + "\n" +
            "예약일: " + data[6],
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
        if (HDialog.confirmDanger(parentFrame(), "강제 취소",
                "선택한 " + rows.length + "건의 예약을 강제 취소하시겠습니까?\n" +
                "대상: " + names + "\n\n강제 취소 시 COUNT 타입 회원권의 잔여 횟수가 복구됩니다.")) {

            // TODO: UPDATE reservations SET status='CANCELLED', cancelled_by='ADMIN', cancelled_at=NOW()
            //       WHERE id IN (...)
            //       sessions.booked_count -1
            //       COUNT 타입: memberships.remaining_count +1 (EXPIRED → ACTIVE)
            for (int i = rows.length - 1; i >= 0; i--) {
                model.setValueAt("CANCELLED", rows[i], hasCheckbox() ? 5 : 4);
                model.setValueAt("ABSENT",    rows[i], hasCheckbox() ? 6 : 5);
                model.setValueAt(false, rows[i], 0);
            }
            HToast.success(parentFrame(), rows.length + "건이 강제 취소되었습니다.");
        }
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
