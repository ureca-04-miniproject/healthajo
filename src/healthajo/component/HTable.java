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

    // setRowStateColumn()으로 지정한 상태 컬럼 인덱스 (-1 = 비활성)
    private int rowStateCol = -1;

    // 셀 텍스트/배지 수평 정렬 (기본 LEFT)
    private int cellHAlignment = SwingConstants.LEADING;

    // 행 상태별 배경색
    private static final Color LOCKED_BG  = AppTheme.SURFACE_RAISED;      // CANCELLED
    private static final Color EXPIRED_BG = AppTheme.DANGER_DIM;          // EXPIRED (사용 완료)
    private static final Color DIMMED_BG  = new Color(250, 251, 253);     // MEMBERSHIP_ISSUED, ABSENT

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

    // ── 행 상태 컬럼 설정 ──────────────────────────────────────────────────────

    /**
     * 행 배경색 및 체크박스 활성 여부를 결정할 상태 컬럼 인덱스 지정.
     * CANCELLED, EXPIRED → 잠김(회색), MEMBERSHIP_ISSUED, ABSENT → 흐림
     */
    public void setRowStateColumn(int col) {
        this.rowStateCol = col;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        if (rowStateCol >= 0 && col == 0) {
            String s = statusAt(row);
            if (s != null && (isLocked(s) || isExpired(s) || isDimmed(s))) return false;
        }
        return super.isCellEditable(row, col);
    }

    // ── 상태 분류 헬퍼 ────────────────────────────────────────────────────────

    private String statusAt(int row) {
        try {
            Object v = getValueAt(row, rowStateCol);
            return v != null ? v.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isLocked(String s)  { return "CANCELLED".equals(s); }
    private static boolean isExpired(String s) { return "EXPIRED".equals(s); }
    private static boolean isDimmed(String s)  { return "MEMBERSHIP_ISSUED".equals(s) || "ABSENT".equals(s); }

    // ── 행 배경색 계산 ────────────────────────────────────────────────────────

    private Color rowBg(int row, boolean sel, String status) {
        if (status != null && isLocked(status))  return LOCKED_BG;
        if (status != null && isExpired(status)) return EXPIRED_BG;
        if (status != null && isDimmed(status))  return DIMMED_BG;
        if (sel)                                  return AppTheme.ROW_SELECTED;
        return row % 2 == 0 ? AppTheme.ROW_EVEN : AppTheme.ROW_ODD;
    }

    private Color rowFg(String status) {
        if (status != null && isLocked(status))  return AppTheme.TEXT_MUTED;
        if (status != null && isExpired(status)) return AppTheme.TEXT_SECONDARY;
        if (status != null && isDimmed(status))  return AppTheme.TEXT_SECONDARY;
        return AppTheme.TEXT;
    }

    // ── 공개 API ──────────────────────────────────────────────────────────────

    /** 셀 텍스트·배지 수평 정렬 설정 (SwingConstants.CENTER 등) */
    public void setCellAlignment(int alignment) {
        this.cellHAlignment = alignment;
        setDefaultRenderer(Object.class, new CellRenderer());
    }

    /** 헤더 텍스트 정렬 설정 (SwingConstants.CENTER 등) */
    public void setHeaderAlignment(int alignment) {
        getTableHeader().setDefaultRenderer(new HeaderRenderer(alignment));
    }

    /** HBadge를 셀에 직접 표시하는 렌더러 등록 (영문 enum 그대로 표시) */
    public void setBadgeRenderer(int col) {
        getColumnModel().getColumn(col).setCellRenderer(new BadgeCellRenderer());
    }

    /** HBadge를 한국어 레이블로 표시하는 렌더러 등록 (사용자 화면용) */
    public void setKoreanBadgeRenderer(int col) {
        getColumnModel().getColumn(col).setCellRenderer(new KoreanBadgeCellRenderer());
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
        sp.getVerticalScrollBar().setBackground(AppTheme.SURFACE);
        sp.getHorizontalScrollBar().setBackground(AppTheme.SURFACE);
        return sp;
    }

    // ── Header renderer ──────────────────────────────────────────────────────

    private static class HeaderRenderer implements TableCellRenderer {
        private final JLabel label = new JLabel();

        HeaderRenderer() { this(SwingConstants.LEFT); }

        HeaderRenderer(int alignment) {
            label.setHorizontalAlignment(alignment);
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

    private class CellRenderer extends DefaultTableCellRenderer {
        CellRenderer() {
            setOpaque(true);
            setVerticalAlignment(SwingConstants.CENTER);
            setBorder(new EmptyBorder(0, AppTheme.SP_4, 0, AppTheme.SP_4));
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
            String status = rowStateCol >= 0 ? statusAt(row) : null;
            setText(val == null ? "" : val.toString());
            setFont(AppTheme.BODY_SM);
            setHorizontalAlignment(cellHAlignment);
            setBackground(rowBg(row, sel, status));
            setForeground(rowFg(status));
            return this;
        }
    }

    // ── Checkbox cell renderer ───────────────────────────────────────────────

    private class CheckboxCellRenderer implements TableCellRenderer {
        private final JCheckBox cb = new JCheckBox();

        CheckboxCellRenderer() {
            cb.setHorizontalAlignment(JCheckBox.CENTER);
            cb.setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
            String status    = rowStateCol >= 0 ? statusAt(row) : null;
            boolean locked   = status != null && isLocked(status);
            boolean dimmed   = status != null && isDimmed(status);
            boolean inactive = locked || dimmed;

            cb.setSelected(!inactive && Boolean.TRUE.equals(val));
            cb.setEnabled(!inactive);
            cb.setBackground(rowBg(row, sel, status));
            return cb;
        }
    }

    // ── Badge cell renderer ───────────────────────────────────────────────────

    private class BadgeCellRenderer implements TableCellRenderer {
        private final JPanel wrapper = new JPanel(new GridBagLayout());

        BadgeCellRenderer() { wrapper.setOpaque(true); }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
            wrapper.removeAll();
            String status = rowStateCol >= 0 ? statusAt(row) : null;
            wrapper.setBackground(rowBg(row, sel, status));
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
                wrapper.add(badge, badgeConstraints());
            }
            return wrapper;
        }
    }

    // ── Korean badge cell renderer (사용자 화면용) ────────────────────────────

    private class KoreanBadgeCellRenderer implements TableCellRenderer {
        private final JPanel wrapper = new JPanel(new GridBagLayout());

        KoreanBadgeCellRenderer() { wrapper.setOpaque(true); }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
            wrapper.removeAll();
            String status = rowStateCol >= 0 ? statusAt(row) : null;
            wrapper.setBackground(rowBg(row, sel, status));
            if (val != null) {
                HBadge badge = switch (val.toString()) {
                    case "ACTIVE"            -> HBadge.of("활성",        AppTheme.SUCCESS_DIM,     AppTheme.SUCCESS);
                    case "EXPIRED"           -> HBadge.of("만료",        AppTheme.DANGER_DIM,      AppTheme.DANGER);
                    case "CONFIRMED"         -> HBadge.of("예약 확정",   AppTheme.PRIMARY_TINT,    AppTheme.PRIMARY);
                    case "MEMBERSHIP_ISSUED" -> HBadge.of("회원권 사용", new Color(204, 251, 241), new Color(13, 148, 136));
                    case "CANCELLED"         -> HBadge.of("취소",        AppTheme.WARNING_DIM,     AppTheme.WARNING);
                    case "ATTENDED"          -> HBadge.of("출석",        AppTheme.SUCCESS_DIM,     AppTheme.SUCCESS);
                    case "ABSENT"            -> HBadge.of("결석",        AppTheme.DANGER_DIM,      AppTheme.DANGER);
                    case "PENDING"           -> HBadge.of("미처리",      AppTheme.WARNING_DIM,     AppTheme.WARNING);
                    case "OPEN"              -> HBadge.of("운영 중",     AppTheme.SUCCESS_DIM,     AppTheme.SUCCESS);
                    default                  -> HBadge.of(val.toString(), AppTheme.BORDER,         AppTheme.TEXT_SECONDARY);
                };
                wrapper.add(badge, badgeConstraints());
            }
            return wrapper;
        }
    }

    private GridBagConstraints badgeConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        if (cellHAlignment == SwingConstants.CENTER) {
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.insets = new Insets(0, 0, 0, 0);
        } else {
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(0, AppTheme.SP_4, 0, 0);
        }
        return gbc;
    }
}
