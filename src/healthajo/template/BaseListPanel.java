package healthajo.template;

import healthajo.component.HButton;
import healthajo.component.HLabel;
import healthajo.component.HTable;
import healthajo.component.HTextField;
import healthajo.component.theme.AppTheme;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

/**
 * 목록 화면 공통 템플릿 (Template Method 패턴).
 */
public abstract class BaseListPanel extends JPanel {

    protected HTable             table;
    protected DefaultTableModel  model;
    protected HTextField         searchField;

    private TableRowSorter<DefaultTableModel> rowSorter;

    // ── Pagination state ────────────────────────────────────────────────────
    protected int    currentPage   = 1;
    protected int    pageSize      = 20;
    private   int    totalCount    = 0;
    protected String searchKeyword = "";

    private Timer searchDebounce;

    private JLabel  pageInfoLabel;
    private HButton prevPageBtn;
    private HButton nextPageBtn;
    private JPanel  pageButtonsPanel;

    protected BaseListPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(AppTheme.BG);

        model    = buildModel();
        table    = new HTable(model);
        if (hasCheckbox()) table.setCheckboxColumn(0);
        configureColumns(table.getColumnModel());

        rowSorter = new TableRowSorter<>(model);
        table.setRowSorter(rowSorter);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int viewRow = table.rowAtPoint(e.getPoint());
                    if (viewRow >= 0) onRowDoubleClick(table.convertRowIndexToModel(viewRow));
                }
            }
        });

        add(buildHeader(),         BorderLayout.NORTH);
        add(table.inScrollPane(),  BorderLayout.CENTER);
        add(buildPagination(),     BorderLayout.SOUTH);

        loadData();
    }

    // ── Header ──────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(AppTheme.BORDER);
                g.fillRect(0, getHeight() - 1, getWidth(), 1);
            }
        };
        header.setBackground(AppTheme.SURFACE);
        header.setBorder(new EmptyBorder(AppTheme.SP_4, AppTheme.SP_6, AppTheme.SP_4, AppTheme.SP_6));
        header.add(HLabel.h3(pageTitle()), BorderLayout.WEST);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SP_2, 0));
        toolbar.setOpaque(false);

        searchField = new HTextField(searchPlaceholder());
        searchField.setPreferredSize(new Dimension(220, 36));
        searchField.setMaximumSize(new Dimension(220, 36));
        // 기본 세로 패딩(SP_3)이 36px 높이에선 글자를 잘라 SP_2로 축소
        searchField.setBorder(new EmptyBorder(AppTheme.SP_2, AppTheme.SP_4, AppTheme.SP_2, AppTheme.SP_4));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { scheduleSearch(); }
            public void removeUpdate(DocumentEvent e)  { scheduleSearch(); }
            public void changedUpdate(DocumentEvent e) {}
        });
        toolbar.add(searchField);

        List<? extends JComponent> buttons = toolbarButtons();
        if (buttons != null) buttons.forEach(toolbar::add);

        header.add(toolbar, BorderLayout.EAST);
        return header;
    }

    // ── Pagination ────────────────────────────────────────────────────────────

    private JPanel buildPagination() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(AppTheme.SURFACE);
        bar.setBorder(new MatteBorder(1, 0, 0, 0, AppTheme.BORDER));

        // 이전 / 다음 화살표
        prevPageBtn = HButton.ghost("←", HButton.Size.SM);
        nextPageBtn = HButton.ghost("→", HButton.Size.SM);
        prevPageBtn.setEnabled(false);
        nextPageBtn.setEnabled(false);

        prevPageBtn.addActionListener(e -> {
            if (currentPage > 1) { currentPage--; reloadPage(); }
        });
        nextPageBtn.addActionListener(e -> {
            int totalPages = (int) Math.ceil((double) totalCount / pageSize);
            if (currentPage < totalPages) { currentPage++; reloadPage(); }
        });

        // 번호 버튼 패널
        pageButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, AppTheme.SP_1, 0));
        pageButtonsPanel.setBackground(AppTheme.SURFACE);

        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, AppTheme.SP_2, AppTheme.SP_2));
        center.setBackground(AppTheme.SURFACE);
        center.add(prevPageBtn);
        center.add(pageButtonsPanel);
        center.add(nextPageBtn);

        // 총 건수 — 우측
        pageInfoLabel = new JLabel("0건");
        pageInfoLabel.setFont(AppTheme.SMALL);
        pageInfoLabel.setForeground(AppTheme.TEXT_MUTED);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SP_5, 0));
        right.setBackground(AppTheme.SURFACE);
        right.add(pageInfoLabel);

        bar.add(center, BorderLayout.CENTER);
        bar.add(right,  BorderLayout.EAST);
        return bar;
    }

    /** loadData() 안에서 호출해 총 건수를 전달. 페이지 번호 버튼을 자동으로 갱신한다. */
    protected void setTotalCount(int count) {
        this.totalCount = count;
        int totalPages  = Math.max(1, (int) Math.ceil((double) count / pageSize));

        pageInfoLabel.setText("총 " + count + "건");

        // 번호 버튼 재생성
        pageButtonsPanel.removeAll();
        List<Integer> pages = computePageRange(currentPage, totalPages);
        int prev = -1;
        for (int page : pages) {
            if (prev != -1 && page > prev + 1) {
                JLabel dots = new JLabel("···");
                dots.setFont(AppTheme.SMALL);
                dots.setForeground(AppTheme.TEXT_MUTED);
                pageButtonsPanel.add(dots);
            }
            pageButtonsPanel.add(buildPageButton(page));
            prev = page;
        }
        pageButtonsPanel.revalidate();
        pageButtonsPanel.repaint();

        prevPageBtn.setEnabled(currentPage > 1);
        nextPageBtn.setEnabled(currentPage < totalPages);
    }

    /** 표시할 페이지 번호 목록 계산. 최대 7개, 끝 페이지 항상 포함. */
    private List<Integer> computePageRange(int current, int total) {
        if (total <= 7) {
            List<Integer> all = new ArrayList<>();
            for (int i = 1; i <= total; i++) all.add(i);
            return all;
        }
        TreeSet<Integer> set = new TreeSet<>();
        set.add(1);
        set.add(total);
        for (int i = Math.max(2, current - 2); i <= Math.min(total - 1, current + 2); i++) {
            set.add(i);
        }
        return new ArrayList<>(set);
    }

    private JButton buildPageButton(int page) {
        boolean isCurrent = page == currentPage;
        JButton btn = new JButton(String.valueOf(page)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isCurrent) {
                    g2.setColor(AppTheme.PRIMARY);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppTheme.R_SM, AppTheme.R_SM);
                } else if (getModel().isRollover()) {
                    g2.setColor(AppTheme.ROW_HOVER);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppTheme.R_SM, AppTheme.R_SM);
                }
                g2.setFont(isCurrent ? AppTheme.font(Font.BOLD, 12) : AppTheme.SMALL);
                g2.setColor(isCurrent ? Color.WHITE : AppTheme.TEXT_SECONDARY);
                FontMetrics fm = g2.getFontMetrics();
                String t = getText();
                g2.drawString(t,
                    (getWidth()  - fm.stringWidth(t)) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        btn.setPreferredSize(new Dimension(32, 32));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setRolloverEnabled(!isCurrent);
        btn.setCursor(isCurrent
            ? Cursor.getDefaultCursor()
            : Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (!isCurrent) btn.addActionListener(e -> { currentPage = page; reloadPage(); });
        return btn;
    }

    private void reloadPage() {
        model.setRowCount(0);
        loadData();
    }

    private void scheduleSearch() {
        if (!serverSideSearch()) { applyClientSearch(); return; }
        if (searchDebounce == null) {
            searchDebounce = new Timer(300, e -> applyServerSearch());
            searchDebounce.setRepeats(false);
        }
        searchDebounce.restart();
    }

    private void applyClientSearch() {
        String kw = searchField.getText().trim();
        rowSorter.setRowFilter(
            kw.isEmpty() ? null
                : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(kw)));
    }

    private void applyServerSearch() {
        String kw = searchField.getText().trim();
        if (kw.equals(searchKeyword)) return;
        searchKeyword = kw;
        currentPage   = 1;
        reloadPage();
    }

    /** 서버 측 검색 사용 여부. true면 searchKeyword를 loadData()에서 쿼리에 반영해야 한다. */
    protected boolean serverSideSearch() { return false; }

    // ── Model ────────────────────────────────────────────────────────────────

    private DefaultTableModel buildModel() {
        String[] allCols = hasCheckbox() ? prepend("", columnNames()) : columnNames();
        return new DefaultTableModel(allCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return hasCheckbox() && c == 0; }
            @Override public Class<?> getColumnClass(int c) {
                return hasCheckbox() && c == 0 ? Boolean.class : String.class;
            }
        };
    }

    private static String[] prepend(String first, String[] rest) {
        String[] arr = new String[rest.length + 1];
        arr[0] = first;
        System.arraycopy(rest, 0, arr, 1, rest.length);
        return arr;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    protected int[] getCheckedRows() {
        if (!hasCheckbox()) return new int[0];
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (Boolean.TRUE.equals(model.getValueAt(i, 0))) list.add(i);
        }
        return list.stream().mapToInt(Integer::intValue).toArray();
    }

    protected Object getDataValue(int modelRow, int dataCol) {
        return model.getValueAt(modelRow, hasCheckbox() ? dataCol + 1 : dataCol);
    }

    protected Object[] getRowData(int modelRow) {
        int offset = hasCheckbox() ? 0 : 0;
        int count  = model.getColumnCount() - offset;
        Object[] data = new Object[count];
        for (int i = 0; i < count; i++) data[i] = model.getValueAt(modelRow, i + offset);
        return data;
    }

    protected JFrame parentFrame() {
        return (JFrame) SwingUtilities.getWindowAncestor(this);
    }

    protected int getOffset()   { return (currentPage - 1) * pageSize; }
    protected int getPageSize() { return pageSize; }

    protected void configureColumns(TableColumnModel cm) {}

    // ── Abstract ──────────────────────────────────────────────────────────────

    protected abstract String                      pageTitle();
    protected abstract String[]                    columnNames();
    protected abstract boolean                     hasCheckbox();
    protected abstract List<? extends JComponent>  toolbarButtons();
    protected abstract void                        loadData();
    protected abstract void                        onRowDoubleClick(int modelRow);
    protected abstract String                      searchPlaceholder();
}
