package healthajo.program.view.user;

import healthajo.component.HButton;
import healthajo.component.HDialog;
import healthajo.template.BaseListPanel;
import java.util.List;
import javax.swing.*;
import javax.swing.table.TableColumnModel;
import static javax.swing.SwingConstants.CENTER;

/**
 * 사용자 — 프로그램 예약 화면.
 *
 * TODO: SELECT p.id, p.name, p.category,
 *              DATE_FORMAT(p.reservation_start,'%Y-%m-%d'), DATE_FORMAT(p.reservation_end,'%Y-%m-%d'),
 *              (SELECT SUM(s.capacity - s.booked_count)
 *               FROM sessions s WHERE s.program_id = p.id AND s.session_date >= CURDATE()
 *               AND s.status = 'OPEN') AS remaining,
 *              p.status
 *       FROM programs p
 *       WHERE p.deleted_at IS NULL AND p.status = 'ACTIVE'
 *       ORDER BY p.name
 */
public class ProgramListPanel extends BaseListPanel {

    public ProgramListPanel() { super(); }

    @Override protected String   pageTitle()         { return "프로그램 예약"; }
    @Override protected boolean  hasCheckbox()       { return false; }
    @Override protected String   searchPlaceholder() { return "프로그램명 또는 종목 검색"; }

    @Override
    protected String[] columnNames() {
        return new String[]{"프로그램명", "종목", "예약 기간", "잔여석", "상태"};
    }

    @Override
    protected List<? extends JComponent> toolbarButtons() {
        HButton reserveBtn = HButton.primary("예약하기", HButton.Size.SM);
        reserveBtn.addActionListener(e -> onReserve());
        return List.of(reserveBtn);
    }

    @Override
    protected void configureColumns(TableColumnModel cm) {
        int[] widths = {160, 90, 220, 70, 80};
        for (int i = 0; i < widths.length; i++) cm.getColumn(i).setPreferredWidth(widths[i]);
        table.setKoreanBadgeRenderer(4);
        table.setCellAlignment(CENTER);
        table.setHeaderAlignment(CENTER);
    }

    @Override
    protected void loadData() {
        // MOCK
        // TODO: DB 조회 후 교체
        model.addRow(new Object[]{"스피닝 A반",    "SPINNING", "2025-04-01 ~ 2025-09-30",  "5", "ACTIVE"});
        model.addRow(new Object[]{"요가 기초반",   "YOGA",     "2025-04-01 ~ 2025-09-30",  "3", "ACTIVE"});
        model.addRow(new Object[]{"필라테스 중급", "PILATES",  "2025-03-01 ~ 2025-08-31",  "2", "ACTIVE"});
        model.addRow(new Object[]{"골프 입문반",   "GOLF",     "2025-05-01 ~ 2025-10-31",  "6", "ACTIVE"});
        setTotalCount(model.getRowCount());
    }

    @Override
    protected void onRowDoubleClick(int modelRow) {
        openReserveDialog(modelRow);
    }

    private void onReserve() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            HDialog.info(parentFrame(), "예약할 프로그램을 선택하세요.");
            return;
        }
        openReserveDialog(table.convertRowIndexToModel(viewRow));
    }

    private void openReserveDialog(int modelRow) {
        Object[] data = getRowData(modelRow);
        new ProgramReserveDialog(parentFrame(), data).setVisible(true);
    }
}
