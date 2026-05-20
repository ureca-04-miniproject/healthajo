package healthajo.membership.view.user;

import healthajo.component.theme.AppTheme;
import healthajo.template.BaseListPanel;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;
import static javax.swing.SwingConstants.CENTER;

/**
 * 사용자 — 나의 회원권 목록.
 *
 * TODO: SELECT p.name, m.name, m.total_count, m.remaining_count, m.status,
 *              DATE_FORMAT(m.issued_at,'%Y-%m-%d')
 *       FROM memberships m
 *       JOIN programs p ON p.id = m.program_id
 *       WHERE m.user_id = ?
 *       ORDER BY m.status ASC, m.issued_at DESC
 */
public class MyMembershipPanel extends BaseListPanel {

    public MyMembershipPanel() { super(); }

    @Override protected String   pageTitle()         { return "나의 회원권"; }
    @Override protected boolean  hasCheckbox()       { return false; }
    @Override protected String   searchPlaceholder() { return "프로그램명 또는 회원권명 검색"; }

    @Override
    protected String[] columnNames() {
        return new String[]{"프로그램명", "회원권명", "총 횟수", "잔여 횟수", "상태", "발급일"};
    }

    @Override
    protected List<? extends JComponent> toolbarButtons() {
        return List.of();
    }

    @Override
    protected void configureColumns(TableColumnModel cm) {
        int[] widths = {140, 190, 70, 80, 80, 110};
        for (int i = 0; i < widths.length; i++) cm.getColumn(i).setPreferredWidth(widths[i]);
        table.setKoreanBadgeRenderer(4);
        table.setRowStateColumn(4);
        table.setCellAlignment(CENTER);
        table.setHeaderAlignment(CENTER);
        cm.getColumn(3).setCellRenderer(new RemainingRenderer());
    }

    @Override
    protected void loadData() {
        // MOCK
        // TODO: DB 조회 후 교체 (현재 로그인한 user_id 기준)
        model.addRow(new Object[]{"스피닝 A반",    "스피닝 A반 수강권",    "20", "15", "ACTIVE",  "2025-03-01"});
        model.addRow(new Object[]{"요가 기초반",   "요가 기초반 수강권",   "20", "20", "ACTIVE",  "2025-04-01"});
        model.addRow(new Object[]{"필라테스 중급", "필라테스 중급 수강권", "24",  "0", "EXPIRED", "2025-02-01"});
        setTotalCount(model.getRowCount());
    }

    private class RemainingRenderer extends DefaultTableCellRenderer {
        RemainingRenderer() {
            setHorizontalAlignment(CENTER);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, val, sel, foc, row, col);
            Object statusVal = t.getValueAt(row, 4);
            boolean expired = "EXPIRED".equals(statusVal != null ? statusVal.toString() : "");
            boolean zero    = "0".equals(val != null ? val.toString() : "");

            // 배경: HTable 행 상태 색상과 일치
            if (expired) {
                setBackground(AppTheme.DANGER_DIM);
            } else if (sel) {
                setBackground(AppTheme.ROW_SELECTED);
            } else {
                setBackground(row % 2 == 0 ? AppTheme.ROW_EVEN : AppTheme.ROW_ODD);
            }

            if (expired && zero) {
                setText("소진");
                setForeground(AppTheme.DANGER);
                setFont(AppTheme.BODY_SM.deriveFont(Font.BOLD));
            } else {
                setText(val == null ? "" : val.toString());
                setForeground(expired ? AppTheme.TEXT_MUTED : AppTheme.TEXT);
                setFont(AppTheme.BODY_SM);
            }
            return this;
        }
    }

    @Override
    protected void onRowDoubleClick(int modelRow) {
        new MembershipDetailDialog(parentFrame(), getRowData(modelRow)).setVisible(true);
    }
}
