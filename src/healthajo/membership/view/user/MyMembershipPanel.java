package healthajo.membership.view.user;

import healthajo.component.HDialog;
import healthajo.component.theme.AppTheme;
import healthajo.jdbc.core.Record;
import healthajo.memberships.service.MembershipService;
import healthajo.template.BaseListPanel;

import java.awt.*;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;

import static javax.swing.SwingConstants.CENTER;

public class MyMembershipPanel extends BaseListPanel {

    private static final MembershipService SVC = new MembershipService();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private Long userId;

    public MyMembershipPanel(Long userId) {
        super();  // loadData() 조기 리턴 (userId == null)
        this.userId = userId;
        if (userId != null) {
            model.setRowCount(0);
            loadData();
        }
    }

    @Override protected String  pageTitle()         { return "나의 회원권"; }
    @Override protected boolean hasCheckbox()       { return false; }
    @Override protected String  searchPlaceholder() { return "프로그램명 또는 회원권명 검색"; }

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
        if (userId == null) { setTotalCount(0); return; }
        try {
            List<Record> records = SVC.getMembershipsWithProgramByUserId(userId);
            for (Record r : records) {
                String programName    = str(r.get("program_name"));
                String membershipName = str(r.get("membership_name"));
                int    total          = toInt(r.get("total_count"));
                int    remaining      = toInt(r.get("remaining_count"));
                String status         = str(r.get("status"));
                String issuedAt       = toDateStr(r.get("issued_at"));
                model.addRow(new Object[]{programName, membershipName, total, remaining, status, issuedAt});
            }
            setTotalCount(records.size());
        } catch (RuntimeException ex) {
            HDialog.error(parentFrame(), "회원권 목록을 불러오지 못했습니다.\n" + ex.getMessage());
            setTotalCount(0);
        }
    }

    @Override
    protected void onRowDoubleClick(int modelRow) {
        new MembershipDetailDialog(parentFrame(), getRowData(modelRow)).setVisible(true);
    }

    // ── 유틸 ──────────────────────────────────────────────────────────────────

    private static String str(Object v) { return v != null ? v.toString() : ""; }

    private static int toInt(Object v) {
        if (v instanceof Number n) return n.intValue();
        try { return Integer.parseInt(String.valueOf(v)); }
        catch (Exception e) { return 0; }
    }

    private static String toDateStr(Object v) {
        if (v == null) return "";
        if (v instanceof Timestamp ts) return ts.toLocalDateTime().format(DATE_FMT);
        if (v instanceof java.time.LocalDateTime ldt) return ldt.format(DATE_FMT);
        if (v instanceof java.sql.Date d) return d.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
        return v.toString().substring(0, Math.min(10, v.toString().length()));
    }

    // ── RemainingRenderer ─────────────────────────────────────────────────────

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
}
