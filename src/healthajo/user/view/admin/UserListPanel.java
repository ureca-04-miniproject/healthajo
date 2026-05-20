package healthajo.user.view.admin;

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
 * 관리자 — 사용자 관리 화면.
 *
 * TODO: SELECT u.id, u.name, u.phone, u.email,
 *              (SELECT COUNT(*) FROM memberships WHERE user_id = u.id AND status = 'ACTIVE') AS membership_count,
 *              DATE_FORMAT(u.created_at, '%Y-%m-%d')
 *       FROM users u
 *       WHERE u.deleted_at IS NULL
 *       ORDER BY u.created_at DESC
 */
public class UserListPanel extends BaseListPanel {

    private HButton deleteBtn;

    public UserListPanel() {
        super();
        // 체크박스 상태 변화 → 삭제 버튼 활성화/비활성화
        model.addTableModelListener(e -> {
            if (deleteBtn != null) {
                deleteBtn.setEnabled(getCheckedRows().length > 0);
            }
        });
    }

    @Override protected String   pageTitle()       { return "사용자 관리"; }
    @Override protected boolean  hasCheckbox()     { return true; }
    @Override protected String   searchPlaceholder() { return "이름 또는 전화번호 검색"; }

    @Override
    protected String[] columnNames() {
        return new String[]{"이름", "전화번호", "이메일", "보유 회원권 수", "등록일"};
    }

    @Override
    protected List<? extends JComponent> toolbarButtons() {
        deleteBtn = HButton.danger("삭제", HButton.Size.SM);
        deleteBtn.setEnabled(false);
        deleteBtn.addActionListener(e -> onDelete());

        HButton addBtn = HButton.primary("사용자 추가", HButton.Size.SM);
        addBtn.addActionListener(e -> onAdd());

        return List.of(deleteBtn, addBtn);
    }

    @Override
    protected void configureColumns(TableColumnModel cm) {
        int[] widths = {120, 140, 180, 110, 110};
        for (int i = 0; i < widths.length; i++) {
            cm.getColumn(i + 1).setPreferredWidth(widths[i]);
        }
    }

    @Override
    protected void loadData() {
        // MOCK: 개발용 목 데이터
        // TODO: DB 조회 후 model.addRow(...) 교체
        model.addRow(new Object[]{false, "홍길동", "010-1234-5678", "hong@example.com", "2", "2025-01-15"});
        model.addRow(new Object[]{false, "김영희", "010-2345-6789", "kim@example.com",  "1", "2025-02-01"});
        model.addRow(new Object[]{false, "이철수", "010-3456-7890", "",                 "1", "2025-02-10"});
        model.addRow(new Object[]{false, "박민준", "010-4567-8901", "park@example.com", "1", "2025-03-05"});
        model.addRow(new Object[]{false, "최서연", "010-5678-9012", "choi@example.com", "0", "2025-03-20"});
        model.addRow(new Object[]{false, "강동원", "010-7890-1234", "kang@example.com", "1", "2025-04-01"});
        setTotalCount(model.getRowCount());
    }

    @Override
    protected void onRowDoubleClick(int modelRow) {
        Object[] data = getRowData(modelRow);
        new UserDetailDialog(parentFrame(), data, modelRow).setVisible(true);
        // TODO: 상세 다이얼로그 닫힌 후 변경 사항 있으면 해당 행 갱신
    }

    private void onAdd() {
        UserFormDialog dialog = new UserFormDialog(parentFrame(), null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            // MOCK: 입력값으로 새 행 추가
            String[] values = dialog.getValues();
            model.addRow(new Object[]{false, values[0], values[1], values[2], "0",
                                      java.time.LocalDate.now().toString()});
            HToast.success(parentFrame(), "사용자가 등록되었습니다.");
        }
    }

    private void onDelete() {
        int[] rows = getCheckedRows();
        if (rows.length == 0) return;

        StringBuilder names = new StringBuilder();
        for (int r : rows) {
            if (names.length() > 0) names.append(", ");
            names.append(getDataValue(r, 0));
        }

        if (HDialog.confirmDanger(parentFrame(), "사용자 삭제",
                "선택한 " + rows.length + "명을 삭제하시겠습니까?\n" +
                "대상: " + names + "\n예약 이력은 보존됩니다.")) {

            // TODO: UPDATE users SET deleted_at = NOW() WHERE id IN (...)
            for (int i = rows.length - 1; i >= 0; i--) {
                model.removeRow(rows[i]);
            }
            HToast.success(parentFrame(), rows.length + "명이 삭제되었습니다.");
        }
    }
}
