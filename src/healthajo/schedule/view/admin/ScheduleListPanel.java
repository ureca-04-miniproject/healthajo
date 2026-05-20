package healthajo.schedule.view.admin;

import healthajo.component.HButton;
import healthajo.component.HDialog;
import healthajo.component.HToast;
import healthajo.template.BaseListPanel;
import java.util.List;
import javax.swing.*;
import javax.swing.table.TableColumnModel;

/**
 * 관리자 — 스케줄 관리 화면.
 *
 * 프로그램별 반복 스케줄(FIXED_WEEKLY / FREE_SLOT / EVENT)을 등록·수정·삭제.
 * 스케줄 저장 시 sessions 테이블에 세션이 자동 생성된다.
 *
 * TODO: SELECT ps.id, p.name, ps.schedule_type,
 *              DATE_FORMAT(ps.start_date,'%Y-%m-%d'),
 *              DATE_FORMAT(ps.end_date,'%Y-%m-%d'),
 *              ps.default_capacity,
 *              COUNT(sw.id) AS weekday_count
 *       FROM program_schedules ps
 *       JOIN programs p ON p.id = ps.program_id
 *       LEFT JOIN schedule_weekdays sw ON sw.schedule_id = ps.id
 *       GROUP BY ps.id
 *       ORDER BY p.name, ps.start_date
 */
public class ScheduleListPanel extends BaseListPanel {

    public ScheduleListPanel() { super(); }

    @Override protected String  pageTitle()         { return "스케줄 관리"; }
    @Override protected boolean hasCheckbox()       { return false; }
    @Override protected String  searchPlaceholder() { return "프로그램명 검색"; }

    @Override
    protected String[] columnNames() {
        return new String[]{"프로그램명", "유형", "시작일", "종료일", "기본 정원", "요일 수"};
    }

    @Override
    protected List<? extends JComponent> toolbarButtons() {
        HButton addBtn = HButton.primary("스케줄 추가", HButton.Size.SM);
        addBtn.addActionListener(e -> onAdd());

        HButton editBtn = HButton.secondary("편집", HButton.Size.SM);
        editBtn.addActionListener(e -> onEdit());

        HButton deleteBtn = HButton.danger("삭제", HButton.Size.SM);
        deleteBtn.addActionListener(e -> onDelete());

        return List.of(deleteBtn, editBtn, addBtn);
    }

    @Override
    protected void configureColumns(TableColumnModel cm) {
        int[] widths = {160, 110, 100, 100, 80, 60};
        for (int i = 0; i < widths.length; i++) cm.getColumn(i).setPreferredWidth(widths[i]);
        table.setBadgeRenderer(1);
    }

    @Override
    protected void loadData() {
        // MOCK
        // TODO: DB 조회 후 교체
        model.addRow(new Object[]{"스피닝 A반",    "FIXED_WEEKLY", "2025-01-01", "2025-12-31", "20", "3"});
        model.addRow(new Object[]{"요가 기초반",   "FIXED_WEEKLY", "2025-01-01", "2025-12-31", "15", "5"});
        model.addRow(new Object[]{"필라테스 중급", "FIXED_WEEKLY", "2025-03-01", "2025-08-31", "12", "3"});
        model.addRow(new Object[]{"골프 입문반",   "FREE_SLOT",    "2025-04-01", "2025-09-30", "10", "0"});
        model.addRow(new Object[]{"여름 특강",     "EVENT",        "2025-07-01", "2025-07-31", "30", "0"});
        setTotalCount(model.getRowCount());
    }

    @Override
    protected void onRowDoubleClick(int modelRow) {
        openEditDialog(modelRow);
    }

    private void onAdd() {
        ScheduleFormDialog dlg = new ScheduleFormDialog(parentFrame(), null);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            Object[] row = dlg.getValues();
            model.addRow(row);
            HToast.success(parentFrame(), "스케줄이 등록되었습니다.");
        }
    }

    private void onEdit() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            HDialog.info(parentFrame(), "편집할 스케줄을 선택하세요.");
            return;
        }
        openEditDialog(table.convertRowIndexToModel(viewRow));
    }

    private void openEditDialog(int mr) {
        Object[] data = getRowData(mr);
        ScheduleFormDialog dlg = new ScheduleFormDialog(parentFrame(), data);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            Object[] updated = dlg.getValues();
            for (int i = 0; i < updated.length; i++) model.setValueAt(updated[i], mr, i);
            HToast.success(parentFrame(), "스케줄이 수정되었습니다.");
        }
    }

    private void onDelete() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            HDialog.info(parentFrame(), "삭제할 스케줄을 선택하세요.");
            return;
        }
        int mr = table.convertRowIndexToModel(viewRow);
        String program = (String) model.getValueAt(mr, 0);
        String type    = (String) model.getValueAt(mr, 1);

        if (HDialog.confirmDanger(parentFrame(), "스케줄 삭제",
                "[" + program + " / " + type + "] 스케줄을 삭제하시겠습니까?\n" +
                "향후 예정된 세션도 함께 삭제됩니다.")) {
            // TODO: DELETE FROM program_schedules WHERE id = ?
            //       DELETE FROM sessions WHERE schedule_id = ? AND session_date >= CURDATE()
            model.removeRow(mr);
            HToast.success(parentFrame(), "스케줄이 삭제되었습니다.");
        }
    }
}
