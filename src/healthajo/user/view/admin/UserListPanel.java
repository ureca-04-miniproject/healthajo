package healthajo.user.view.admin;

import healthajo.component.HButton;
import healthajo.component.HDialog;
import healthajo.component.HTable;
import healthajo.component.HToast;
import healthajo.component.theme.AppTheme;
import healthajo.template.BaseListPanel;

import java.util.Comparator;
import java.util.List;
import javax.swing.*;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import healthajo.jdbc.core.Record;
import healthajo.jdbc.table.TUser;
import healthajo.users.UsersDAO;
import java.util.ArrayList;

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
    private static final TUser T = TUser.USER;
    private UsersDAO dao;
    private List<Long> idList;

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


        HButton addBtn = HButton.primary("사용자 추가", HButton.Size.SM);
        addBtn.addActionListener(e -> onAdd());

        HButton editBtn = HButton.secondary("수정", HButton.Size.SM);
        editBtn.addActionListener(e -> onEdit());

        deleteBtn = HButton.danger("삭제", HButton.Size.SM);
        deleteBtn.setEnabled(false);
        deleteBtn.addActionListener(e -> onDelete());

        return List.of(addBtn, editBtn, deleteBtn);
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
        // TODO: DB 조회 후 model.addRow(...) 교체
        if (table.getRowSorter() instanceof TableRowSorter<?> sorter) {
            sorter.setComparator(4, Comparator.comparingInt(o -> Integer.parseInt(o.toString())));
        }
        if (dao == null) dao = new UsersDAO();
        model.setRowCount(0);
        idList = new ArrayList<>();
        List<Record> list = dao.findAllWithStats();
        for (Record r : list) {
            idList.add(r.get(T.ID));
            model.addRow(new Object[]{
                    false,
                    r.get(T.NAME),
                    r.get(T.PHONE),
                    r.get(T.EMAIL) == null ? "" : r.get(T.EMAIL),
                    r.get("membership_count") == null ? "0" : r.get("membership_count").toString(),
                    r.get("created_at") == null ? "" : r.get("created_at").toString().substring(0, 10)
            });
        }
        setTotalCount(model.getRowCount());
    }

    @Override
    protected void onRowDoubleClick(int modelRow) {
        Object[] data = getRowData(modelRow);
        // TODO: 상세 다이얼로그 닫힌 후 변경 사항 있으면 해당 행 갱신
        Long userId = idList.get(modelRow);
        new UserDetailDialog(parentFrame(), data, modelRow, dao, userId).setVisible(true);
        loadData();
    }

    private void onAdd() {
        UserFormDialog dialog = new UserFormDialog(parentFrame(), null, dao);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            // MOCK: 입력값으로 새 행 추가
            String[] values = dialog.getValues();
            HToast.success(parentFrame(), "사용자가 등록되었습니다.");
            loadData();
        }
    }

    private void onEdit() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            HDialog.info(parentFrame(), "수정할 사용자를 선택하세요.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Object[] data = getRowData(modelRow);
        Long userId = idList.get(modelRow);
        new UserDetailDialog(parentFrame(), data, modelRow, dao, userId).setVisible(true);
        loadData();
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
                dao.delete(idList.get(rows[i]));
            }
            loadData();
            HToast.success(parentFrame(), rows.length + "명이 삭제되었습니다.");
        }
    }
}
