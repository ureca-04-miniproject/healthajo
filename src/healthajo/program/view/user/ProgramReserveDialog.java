package healthajo.program.view.user;

import healthajo.component.HButton;
import healthajo.component.HComboBox;
import healthajo.component.HDialog;
import healthajo.component.HLabel;
import healthajo.component.HTable;
import healthajo.component.HToast;
import healthajo.component.theme.AppTheme;
import healthajo.program.domain.ProgramSummary;
import healthajo.reservation.application.ReservationApplication;
import healthajo.session.dao.SessionDao;
import healthajo.session.domain.SessionSummary;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class ProgramReserveDialog extends JDialog {

    // ── 세션 데이터 ───────────────────────────────────────────────────────────
    private record SessionRow(Long sessionId, String date, String start, String end,
                              int remaining, String instructor) {
        String slotKey() { return date + "|" + start + "|" + end; }
    }

    private static final int PAGE_SIZE = 7;

    private static final SessionDao          SESSION_DAO = new SessionDao();
    private static final ReservationApplication APP      = new ReservationApplication();

    private final Long          userId;
    private final ProgramSummary program;
    private       Set<String>   confirmedSlots = new HashSet<>();

    private final List<SessionRow> allSessions      = new ArrayList<>();
    private final List<SessionRow> filteredSessions = new ArrayList<>();
    private final List<SessionRow> pageSessions     = new ArrayList<>();
    private int currentPage = 0;

    private final Map<String, String> selectedByDate = new LinkedHashMap<>();
    private final Set<Integer>        disabledRows   = new HashSet<>();

    private final DefaultTableModel sessionModel;
    private final HTable            sessionTable;
    private HComboBox<String>       dateFilter;
    private JLabel                  pageLabel;
    private HButton                 prevBtn, nextBtn;
    private JLabel                  selCountLabel;
    private HButton                 cancelAllBtn;

    private boolean adjusting              = false;
    private boolean selectionListenerFired = false;
    private int     lastClickedViewRow     = -1;
    private boolean lastClickWasSelected   = false;

    public ProgramReserveDialog(JFrame parent, Long userId, ProgramSummary program) {
        super(parent, "프로그램 예약", true);
        this.userId  = userId;
        this.program = program;
        setSize(680, 560);
        setResizable(false);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(AppTheme.SURFACE);
        setLayout(new BorderLayout());

        sessionModel = new DefaultTableModel(
            new String[]{"날짜", "시작", "종료", "잔여석", "강사"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        sessionTable = new HTable(sessionModel);
        sessionTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        loadSessions();

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
        add(buildFooter(parent), BorderLayout.SOUTH);

        installCellRenderer();
        installSelectionListener();
        installToggleDeselect();
        refreshTable();
    }

    // ── 데이터 로드 ───────────────────────────────────────────────────────────

    private void loadSessions() {
        try {
            for (SessionSummary s : SESSION_DAO.findUpcomingByProgramId(program.id())) {
                allSessions.add(new SessionRow(s.id(), s.date(), s.start(), s.end(),
                    s.remaining(), s.instructor()));
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        if (userId != null) {
            try {
                confirmedSlots = SESSION_DAO.findConfirmedSlotsByUserId(userId);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    // ── 헤더 ──────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setBackground(AppTheme.SURFACE);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(AppTheme.SP_5, AppTheme.SP_6, AppTheme.SP_4, AppTheme.SP_6));

        JLabel title = HLabel.h2(program.name());
        JLabel sub   = HLabel.small("종목: " + program.category() + "  |  예약 기간: " + program.reservationPeriod());
        title.setAlignmentX(LEFT_ALIGNMENT);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(2));
        header.add(sub);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(AppTheme.SURFACE);
        wrapper.setBorder(new MatteBorder(0, 0, 1, 0, AppTheme.BORDER));
        wrapper.add(header, BorderLayout.CENTER);
        return wrapper;
    }

    // ── 바디 ──────────────────────────────────────────────────────────────────

    private JPanel buildBody() {
        sessionTable.getColumnModel().getColumn(0).setPreferredWidth(110);
        sessionTable.getColumnModel().getColumn(1).setPreferredWidth(70);
        sessionTable.getColumnModel().getColumn(2).setPreferredWidth(70);
        sessionTable.getColumnModel().getColumn(3).setPreferredWidth(70);
        sessionTable.getColumnModel().getColumn(4).setPreferredWidth(110);
        sessionTable.setCellAlignment(SwingConstants.CENTER);
        sessionTable.setHeaderAlignment(SwingConstants.CENTER);

        JLabel filterLbl = HLabel.label("날짜");
        filterLbl.setForeground(AppTheme.TEXT_SECONDARY);

        dateFilter = new HComboBox<>();
        dateFilter.setPreferredSize(new Dimension(160, 36));
        dateFilter.addItem("전체 날짜");
        Set<String> uniqueDates = new LinkedHashSet<>();
        for (SessionRow s : allSessions) uniqueDates.add(s.date());
        for (String d : uniqueDates) dateFilter.addItem(d);
        dateFilter.addActionListener(e -> { currentPage = 0; refreshTable(); });

        JPanel filterLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, AppTheme.SP_2, AppTheme.SP_2));
        filterLeft.setOpaque(false);
        filterLeft.add(Box.createHorizontalStrut(AppTheme.SP_4));
        filterLeft.add(filterLbl);
        filterLeft.add(dateFilter);

        selCountLabel = new JLabel("");
        selCountLabel.setFont(AppTheme.LABEL);
        selCountLabel.setForeground(AppTheme.PRIMARY);

        cancelAllBtn = HButton.ghost("전체 취소", HButton.Size.SM);
        cancelAllBtn.setVisible(false);
        cancelAllBtn.addActionListener(e -> clearAllSelections());

        JPanel filterRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SP_2, AppTheme.SP_2));
        filterRight.setOpaque(false);
        filterRight.add(selCountLabel);
        filterRight.add(cancelAllBtn);
        filterRight.add(Box.createHorizontalStrut(AppTheme.SP_2));

        JPanel filterBar = new JPanel(new BorderLayout());
        filterBar.setBackground(AppTheme.SURFACE);
        filterBar.setBorder(new MatteBorder(0, 0, 1, 0, AppTheme.BORDER_SUBTLE));
        filterBar.add(filterLeft,  BorderLayout.WEST);
        filterBar.add(filterRight, BorderLayout.EAST);

        prevBtn   = HButton.ghost("← 이전", HButton.Size.SM);
        nextBtn   = HButton.ghost("다음 →", HButton.Size.SM);
        pageLabel = new JLabel("1 / 1");
        pageLabel.setFont(AppTheme.BODY_SM);
        pageLabel.setForeground(AppTheme.TEXT_SECONDARY);

        prevBtn.addActionListener(e -> { if (currentPage > 0) { currentPage--; refreshTable(); } });
        nextBtn.addActionListener(e -> {
            if (currentPage < totalPages() - 1) { currentPage++; refreshTable(); }
        });

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, AppTheme.SP_2, AppTheme.SP_2));
        navPanel.setOpaque(false);
        navPanel.add(prevBtn);
        navPanel.add(pageLabel);
        navPanel.add(nextBtn);

        JPanel pageBar = new JPanel(new BorderLayout());
        pageBar.setBackground(AppTheme.SURFACE);
        pageBar.setBorder(new MatteBorder(1, 0, 0, 0, AppTheme.BORDER_SUBTLE));
        pageBar.add(navPanel, BorderLayout.WEST);

        JPanel guidePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, AppTheme.SP_6, AppTheme.SP_2));
        guidePanel.setBackground(AppTheme.SURFACE);
        guidePanel.add(HLabel.muted(
            "클릭으로 선택/해제, Ctrl+클릭으로 여러 날짜 선택.  날짜별 1개 세션만 선택 가능합니다."));

        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(AppTheme.SURFACE);
        south.add(pageBar,    BorderLayout.NORTH);
        south.add(guidePanel, BorderLayout.SOUTH);

        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(AppTheme.SURFACE);
        body.add(filterBar,                   BorderLayout.NORTH);
        body.add(sessionTable.inScrollPane(), BorderLayout.CENTER);
        body.add(south,                       BorderLayout.SOUTH);
        return body;
    }

    // ── 푸터 ──────────────────────────────────────────────────────────────────

    private JPanel buildFooter(JFrame parent) {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SP_2, AppTheme.SP_3));
        footer.setBackground(AppTheme.SURFACE);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, AppTheme.BORDER));

        HButton closeBtn   = HButton.ghost("닫기",     HButton.Size.SM);
        HButton reserveBtn = HButton.primary("예약하기", HButton.Size.SM);

        closeBtn.addActionListener(e -> dispose());
        reserveBtn.addActionListener(e -> onReserve(parent));

        footer.add(closeBtn);
        footer.add(reserveBtn);
        return footer;
    }

    // ── 테이블 갱신 ───────────────────────────────────────────────────────────

    private void refreshTable() {
        adjusting = true;
        try {
            String selected = (String) dateFilter.getSelectedItem();
            filteredSessions.clear();
            for (SessionRow s : allSessions) {
                if ("전체 날짜".equals(selected) || s.date().equals(selected))
                    filteredSessions.add(s);
            }

            currentPage = Math.min(currentPage, Math.max(0, totalPages() - 1));
            int from = currentPage * PAGE_SIZE;
            int to   = Math.min(filteredSessions.size(), from + PAGE_SIZE);
            pageSessions.clear();
            pageSessions.addAll(filteredSessions.subList(from, to));

            sessionModel.setRowCount(0);
            for (SessionRow s : pageSessions) {
                sessionModel.addRow(new Object[]{
                    s.date(), s.start(), s.end(),
                    String.valueOf(s.remaining()), s.instructor()
                });
            }

            rebuildDisabledRows();

            sessionTable.clearSelection();
            for (int mr = 0; mr < pageSessions.size(); mr++) {
                SessionRow s = pageSessions.get(mr);
                if (s.slotKey().equals(selectedByDate.get(s.date()))) {
                    int vr = sessionTable.convertRowIndexToView(mr);
                    if (vr >= 0) sessionTable.addRowSelectionInterval(vr, vr);
                }
            }

            int tp = totalPages();
            pageLabel.setText((currentPage + 1) + " / " + tp);
            prevBtn.setEnabled(currentPage > 0);
            nextBtn.setEnabled(currentPage < tp - 1);
            updateSelCount();
            sessionTable.repaint();
        } finally {
            adjusting = false;
        }
    }

    private int totalPages() {
        return Math.max(1, (filteredSessions.size() + PAGE_SIZE - 1) / PAGE_SIZE);
    }

    // ── 비활성 행 재계산 ──────────────────────────────────────────────────────

    private void rebuildDisabledRows() {
        disabledRows.clear();
        for (int mr = 0; mr < pageSessions.size(); mr++) {
            SessionRow s = pageSessions.get(mr);
            boolean noSeats      = s.remaining() == 0;
            boolean dateConflict = selectedByDate.containsKey(s.date())
                                && !selectedByDate.get(s.date()).equals(s.slotKey());
            if (noSeats || dateConflict) disabledRows.add(mr);
        }
    }

    // ── 셀 렌더러 ─────────────────────────────────────────────────────────────

    private void installCellRenderer() {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(CENTER);
                int mr = table.convertRowIndexToModel(row);
                if (disabledRows.contains(mr)) {
                    c.setBackground(AppTheme.SURFACE_RAISED);
                    c.setForeground(AppTheme.TEXT_MUTED);
                } else if (isSelected) {
                    c.setBackground(AppTheme.ROW_SELECTED);
                    c.setForeground(AppTheme.TEXT);
                } else {
                    c.setBackground(row % 2 == 0 ? AppTheme.ROW_EVEN : AppTheme.ROW_ODD);
                    c.setForeground(AppTheme.TEXT);
                }
                return c;
            }
        };
        for (int i = 0; i < sessionModel.getColumnCount(); i++)
            sessionTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
    }

    // ── 선택 리스너 ───────────────────────────────────────────────────────────

    private void installSelectionListener() {
        sessionTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || adjusting) return;
            adjusting = true;
            try {
                for (int vr : sessionTable.getSelectedRows()) {
                    int mr = sessionTable.convertRowIndexToModel(vr);
                    if (disabledRows.contains(mr))
                        sessionTable.getSelectionModel().removeSelectionInterval(vr, vr);
                }
                selectedByDate.entrySet().removeIf(en -> isOnCurrentPage(en.getValue()));
                for (int vr : sessionTable.getSelectedRows()) {
                    int mr = sessionTable.convertRowIndexToModel(vr);
                    if (mr < pageSessions.size()) {
                        SessionRow s = pageSessions.get(mr);
                        selectedByDate.put(s.date(), s.slotKey());
                    }
                }
                rebuildDisabledRows();
                updateSelCount();
                sessionTable.repaint();
                selectionListenerFired = true;
            } finally {
                adjusting = false;
            }
        });
    }

    private boolean isOnCurrentPage(String slotKey) {
        return pageSessions.stream().anyMatch(s -> s.slotKey().equals(slotKey));
    }

    private void updateSelCount() {
        int n = selectedByDate.size();
        selCountLabel.setText(n > 0 ? "선택 " + n + "개" : "");
        if (cancelAllBtn != null) cancelAllBtn.setVisible(n > 0);
    }

    private void clearAllSelections() {
        selectedByDate.clear();
        adjusting = true;
        try { sessionTable.clearSelection(); } finally { adjusting = false; }
        rebuildDisabledRows();
        updateSelCount();
        sessionTable.repaint();
    }

    private void installToggleDeselect() {
        sessionTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                int vr = sessionTable.rowAtPoint(e.getPoint());
                lastClickedViewRow   = vr;
                lastClickWasSelected = vr >= 0
                    && sessionTable.isRowSelected(vr)
                    && !selectionListenerFired;
                selectionListenerFired = false;
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (lastClickedViewRow >= 0 && lastClickWasSelected
                        && sessionTable.isRowSelected(lastClickedViewRow)) {
                    sessionTable.getSelectionModel()
                        .removeSelectionInterval(lastClickedViewRow, lastClickedViewRow);
                }
                lastClickedViewRow   = -1;
                lastClickWasSelected = false;
            }
        });
    }

    // ── 예약 처리 ─────────────────────────────────────────────────────────────

    private void onReserve(JFrame parent) {
        if (selectedByDate.isEmpty()) {
            HDialog.info(parent, "예약할 세션을 선택하세요.");
            return;
        }

        List<SessionRow> noSeats   = new ArrayList<>();
        List<SessionRow> conflicts = new ArrayList<>();
        List<SessionRow> valid     = new ArrayList<>();

        for (String slotKey : selectedByDate.values()) {
            SessionRow session = findBySlotKey(slotKey);
            if (session == null) continue;

            if (session.remaining() == 0) {
                noSeats.add(session);
            } else if (hasTimeConflict(slotKey)) {
                conflicts.add(session);
            } else {
                valid.add(session);
            }
        }

        if (!noSeats.isEmpty()) {
            HDialog.alert(parent, "예약 불가 — 잔여석 없음",
                "다음 세션은 잔여석이 없어 예약할 수 없습니다:\n\n  · " +
                toLabels(noSeats) + "\n\n다른 날짜의 세션을 선택하세요.",
                HDialog.Type.WARNING);
            return;
        }

        if (!conflicts.isEmpty()) {
            String conflictList = "  · " + toLabels(conflicts);
            if (valid.isEmpty()) {
                HDialog.alert(parent, "시간대 중복",
                    "선택한 모든 세션이 기존 예약과 시간대가 겹쳐 예약할 수 없습니다:\n\n" +
                    conflictList + "\n\n나의 예약에서 기존 예약을 확인하세요.",
                    HDialog.Type.WARNING);
                return;
            }
            boolean proceed = HDialog.confirm(parent, "일부 세션 시간대 중복",
                "다음 세션은 기존 예약과 시간대가 겹쳐 예약할 수 없습니다:\n" + conflictList +
                "\n\n나머지 " + valid.size() + "개 세션은 예약 가능합니다.\n예약 가능한 세션만 예약하시겠습니까?");
            if (!proceed) return;
        }

        if (valid.isEmpty()) return;

        String sessionList = "  · " + toLabels(valid);
        if (!HDialog.confirm(parent, "예약 확인",
                valid.size() + "개 세션을 예약하시겠습니까?\n\n" + sessionList)) return;

        int failed = 0;
        for (SessionRow s : valid) {
            try {
                APP.reserve(userId, s.sessionId(), program.id());
            } catch (RuntimeException e) {
                e.printStackTrace();
                failed++;
            }
        }

        if (failed == 0) {
            HToast.success(parent, valid.size() + "개 세션이 예약되었습니다.");
        } else {
            HToast.success(parent, (valid.size() - failed) + "개 예약 완료, " + failed + "개 실패.");
        }
        dispose();
    }

    // ── 시간대 중복 체크 (HH:mm 문자열 비교로 overlap 판정) ─────────────────

    private boolean hasTimeConflict(String slotKey) {
        String[] parts = slotKey.split("\\|");
        String newDate = parts[0], newStart = parts[1], newEnd = parts[2];
        for (String key : confirmedSlots) {
            String[] cp = key.split("\\|");
            String cDate = cp[0], cStart = cp[1], cEnd = cp[2];
            if (cDate.equals(newDate)
                    && cStart.compareTo(newEnd) < 0
                    && cEnd.compareTo(newStart) > 0) {
                return true;
            }
        }
        return false;
    }

    // ── 유틸 ──────────────────────────────────────────────────────────────────

    private SessionRow findBySlotKey(String slotKey) {
        return allSessions.stream()
            .filter(s -> s.slotKey().equals(slotKey))
            .findFirst().orElse(null);
    }

    private static String toLabels(List<SessionRow> sessions) {
        return String.join("\n  · ", sessions.stream()
            .map(s -> s.date() + " " + s.start())
            .toList());
    }
}
