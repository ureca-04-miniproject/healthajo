package healthajo.component;

import healthajo.component.theme.AppTheme;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/**
 * 스타일이 적용된 테이블.
 *
 * String[] cols = {"이름", "전화번호", "상태"};
 * HTable table = new HTable(model);
 * add(table.inScrollPane());
 */
public class HTable extends JTable {

    public HTable(TableModel model) {
        super(model);
        setup();
    }

    public HTable(Object[][] data, String[] columns) {
        super(data, columns);
        setup();
    }

    private void setup() {
        setBackground(AppTheme.SURFACE);
        setForeground(AppTheme.TEXT);
        setFont(AppTheme.BODY_SM);
        setRowHeight(44);
        setShowGrid(false);
        setIntercellSpacing(new Dimension(0, 1));
        setSelectionBackground(AppTheme.ROW_SELECTED);
        setSelectionForeground(AppTheme.TEXT);
        setFillsViewportHeight(true);
        setBorder(null);
        setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

        JTableHeader header = getTableHeader();
        header.setBackground(AppTheme.SURFACE_RAISED);
        header.setForeground(AppTheme.TEXT_MUTED);
        header.setFont(AppTheme.LABEL);
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER));
        header.setPreferredSize(new Dimension(0, 44));
        header.setDefaultRenderer(new HeaderRenderer());

        setDefaultRenderer(Object.class, new CellRenderer());
    }

    /** HBadge를 셀에 직접 표시하는 렌더러 등록 */
    public void setBadgeRenderer(int col) {
        getColumnModel().getColumn(col).setCellRenderer(new BadgeCellRenderer());
    }

    /** 첫 컬럼을 체크박스 컬럼으로 설정 */
    public void setCheckboxColumn(int col) {
        TableColumn column = getColumnModel().getColumn(col);
        column.setMaxWidth(40);
        column.setMinWidth(40);
        column.setPreferredWidth(40);
        column.setHeaderValue("");
        column.setCellRenderer(new CheckboxCellRenderer());
        JCheckBox cbEditor = new JCheckBox();
        cbEditor.setHorizontalAlignment(JCheckBox.CENTER);
        cbEditor.setBackground(AppTheme.SURFACE);
        column.setCellEditor(new DefaultCellEditor(cbEditor));
    }

    /** JScrollPane으로 감싸서 반환 */
    public JScrollPane inScrollPane() {
        JScrollPane sp = new JScrollPane(this);
        sp.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER, 1));
        sp.getViewport().setBackground(AppTheme.SURFACE);
        sp.setBackground(AppTheme.SURFACE);
        // 스크롤바 색상
        sp.getVerticalScrollBar().setBackground(AppTheme.SURFACE);
        sp.getHorizontalScrollBar().setBackground(AppTheme.SURFACE);
        return sp;
    }

    // ── Header renderer ──────────────────────────────────────────────────────

    private static class HeaderRenderer implements TableCellRenderer {
        private final JLabel label = new JLabel();

        HeaderRenderer() {
            label.setFont(AppTheme.LABEL);
            label.setForeground(AppTheme.TEXT_MUTED);
            label.setBackground(AppTheme.SURFACE_RAISED);
            label.setOpaque(true);
            label.setBorder(new EmptyBorder(0, AppTheme.SP_4, 0, AppTheme.SP_4));
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
            label.setText(val == null ? "" : val.toString());
            return label;
        }
    }

    // ── Cell renderer ────────────────────────────────────────────────────────

    private static class CellRenderer extends DefaultTableCellRenderer {
        CellRenderer() {
            setOpaque(true);
            setVerticalAlignment(SwingConstants.CENTER);
            setBorder(new EmptyBorder(0, AppTheme.SP_4, 0, AppTheme.SP_4));
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
            setText(val == null ? "" : val.toString());
            setFont(AppTheme.BODY_SM);
            if (sel) {
                setBackground(AppTheme.ROW_SELECTED);
                setForeground(AppTheme.TEXT);
            } else {
                setBackground(row % 2 == 0 ? AppTheme.ROW_EVEN : AppTheme.ROW_ODD);
                setForeground(AppTheme.TEXT);
            }
            return this;
        }
    }

    // ── Checkbox cell renderer ───────────────────────────────────────────────────

    private static class CheckboxCellRenderer implements TableCellRenderer {
        private final JCheckBox cb = new JCheckBox();

        CheckboxCellRenderer() {
            cb.setHorizontalAlignment(JCheckBox.CENTER);
            cb.setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
            cb.setSelected(Boolean.TRUE.equals(val));
            cb.setBackground(sel ? AppTheme.ROW_SELECTED
                                 : row % 2 == 0 ? AppTheme.ROW_EVEN : AppTheme.ROW_ODD);
            return cb;
        }
    }

    // ── Badge cell renderer ───────────────────────────────────────────────────

    private static class BadgeCellRenderer implements TableCellRenderer {
        private final JPanel wrapper = new JPanel(new GridBagLayout());

        BadgeCellRenderer() {
            wrapper.setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
            wrapper.removeAll();
            Color bg = sel ? AppTheme.ROW_SELECTED
                           : row % 2 == 0 ? AppTheme.ROW_EVEN : AppTheme.ROW_ODD;
            wrapper.setBackground(bg);
            if (val != null) {
                String s = val.toString();
                HBadge badge = switch (s) {
                    case "ACTIVE"            -> HBadge.active();
                    case "EXPIRED"           -> HBadge.expired();
                    case "CONFIRMED"         -> HBadge.confirmed();
                    case "MEMBERSHIP_ISSUED" -> HBadge.membershipIssued();
                    case "CANCELLED"         -> HBadge.cancelled();
                    case "ATTENDED"          -> HBadge.attended();
                    case "ABSENT"            -> HBadge.absent();
                    case "PENDING"           -> HBadge.pending();
                    case "OPEN"              -> HBadge.open();
                    default                  -> HBadge.of(s, AppTheme.BORDER, AppTheme.TEXT_SECONDARY);
                };
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.anchor = GridBagConstraints.WEST;
                gbc.insets = new Insets(0, AppTheme.SP_4, 0, 0);
                wrapper.add(badge, gbc);
            }
            return wrapper;
        }
    }
}
