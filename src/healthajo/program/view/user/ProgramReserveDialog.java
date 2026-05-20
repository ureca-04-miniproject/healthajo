package healthajo.program.view.user;

import healthajo.component.HButton;
import healthajo.component.HComboBox;
import healthajo.component.HDialog;
import healthajo.component.HLabel;
import healthajo.component.HTable;
import healthajo.component.HToast;
import healthajo.component.theme.AppTheme;
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * 사용자 — 세션 선택 및 예약 다이얼로그.
 *
 * - 날짜 필터로 특정 날짜의 세션만 표시 가능.
 * - 세션이 많을 경우 페이지네이션 (PAGE_SIZE 행씩).
 * - 날짜별 1개 세션만 선택 가능 — 한 날짜에 세션을 선택하면 같은 날짜의 나머지 세션은 비활성.
 * - 잔여석 0 / 동일 시간대 기존 예약 중복 시 각각 다른 메시지로 차단.
 *
 * 세션 목록 조회:
 *   TODO: SELECT s.id, s.session_date,
 *                TIME_FORMAT(s.start_time,'%H:%i'), TIME_FORMAT(s.end_time,'%H:%i'),
 *                (s.capacity - s.booked_count) AS remaining,
 *                IFNULL(i.name,'-')
 *         FROM sessions s
 *         LEFT JOIN instructors i ON i.id = s.instructor_id
 *         WHERE s.program_id = ? AND s.session_date >= CURDATE()
 *           AND s.status = 'OPEN'
 *         ORDER BY s.session_date, s.start_time
 *
 * 시간대 중복 체크:
 *   TODO: SELECT s.session_date, s.start_time, s.end_time
 *         FROM reservations r
 *         JOIN sessions s ON s.id = r.session_id
 *         WHERE r.user_id = ? AND r.status = 'CONFIRMED'
 *         → 선택한 세션과 날짜 같고 시간 겹치면 REJECTED
 */
public class ProgramReserveDialog extends JDialog {

    // ── 세션 데이터 ───────────────────────────────────────────────────────────
    private record SessionRow(String date, String start, String end, int remaining, String instructor) {
        String slotKey() { return date + "|" + start + "|" + end; }
    }

    // ── 페이지네이션 ──────────────────────────────────────────────────────────
    private static final int PAGE_SIZE = 7;

    private final List<SessionRow> allSessions      = new ArrayList<>();
    private final List<SessionRow> filteredSessions = new ArrayList<>();
    private final List<SessionRow> pageSessions     = new ArrayList<>();
    private int currentPage = 0;

    // ── 선택 상태: 날짜 → slotKey ─────────────────────────────────────────────
    private final Map<String, String> selectedByDate = new LinkedHashMap<>();

    // ── 현재 페이지에서 비활성화된 모델 행 인덱스 ─────────────────────────────
    private final Set<Integer> disabledRows = new HashSet<>();

    // ── 이미 예약 확정된 슬롯 ─────────────────────────────────────────────────
    private static final Set<String> MOCK_CONFIRMED_SLOTS = Set.of(
        "2025-05-22|07:00|08:00"
    );

    // ── UI ────────────────────────────────────────────────────────────────────
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

    public ProgramReserveDialog(JFrame parent, Object[] programData) {
        super(parent, "프로그램 예약", true);
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

        initSessions();

        add(buildHeader(programData), BorderLayout.NORTH);
        add(buildBody(),              BorderLayout.CENTER);
        add(buildFooter(parent),      BorderLayout.SOUTH);

        installCellRenderer();
        installSelectionListener();
        installToggleDeselect();
        refreshTable();
    }

    // ── 세션 초기화 ───────────────────────────────────────────────────────────

    private void initSessions() {
        // MOCK — 동일 날짜 두 슬롯 포함: 날짜 제약 UI 확인용
        // TODO: DB 조회 (program_id 기준, OPEN 상태 세션)
        allSessions.add(new SessionRow("2025-05-22", "07:00", "08:00", 5,  "김강사"));
        allSessions.add(new SessionRow("2025-05-22", "10:00", "11:00", 3,  "박강사")); // 같은 날 다른 슬롯
        allSessions.add(new SessionRow("2025-05-24", "07:00", "08:00", 6,  "김강사"));
        allSessions.add(new SessionRow("2025-05-27", "07:00", "08:00", 0,  "김강사")); // 잔여석 없음
        allSessions.add(new SessionRow("2025-05-29", "07:00", "08:00", 5,  "-"));
        allSessions.add(new SessionRow("2025-05-31", "07:00", "08:00", 8,  "김강사"));
        allSessions.add(new SessionRow("2025-06-03", "07:00", "08:00", 7,  "김강사"));
        allSessions.add(new SessionRow("2025-06-05", "07:00", "08:00", 4,  "박강사"));
        allSessions.add(new SessionRow("2025-06-07", "07:00", "08:00", 6,  "박강사"));
        allSessions.add(new SessionRow("2025-06-10", "07:00", "08:00", 3,  "박강사"));
        allSessions.add(new SessionRow("2025-06-12", "07:00", "08:00", 5,  "김강사"));
        allSessions.add(new SessionRow("2025-06-14", "07:00", "08:00", 8,  "김강사"));
    }

    // ── 헤더 ──────────────────────────────────────────────────────────────────

    private JPanel buildHeader(Object[] data) {
        JPanel header = new JPanel();
        header.setBackground(AppTheme.SURFACE);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(AppTheme.SP_5, AppTheme.SP_6, AppTheme.SP_4, AppTheme.SP_6));

        JLabel title = HLabel.h2(data[0].toString());
        JLabel sub   = HLabel.small("종목: " + data[1] + "  |  예약 기간: " + data[2]);
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
        // 열 너비
        sessionTable.getColumnModel().getColumn(0).setPreferredWidth(110);
        sessionTable.getColumnModel().getColumn(1).setPreferredWidth(70);
        sessionTable.getColumnModel().getColumn(2).setPreferredWidth(70);
        sessionTable.getColumnModel().getColumn(3).setPreferredWidth(70);
        sessionTable.getColumnModel().getColumn(4).setPreferredWidth(110);
        sessionTable.setCellAlignment(SwingConstants.CENTER);
        sessionTable.setHeaderAlignment(SwingConstants.CENTER);

        // ── 날짜 필터 바 (left: 날짜 콤보 / right: 선택 N개 + 전체 취소) ──
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

        // ── 페이지네이션 바 ──
        prevBtn   = HButton.ghost("← 이전", HButton.Size.SM);
        nextBtn   = HButton.ghost("다음 →", HButton.Size.SM);
        pageLabel = new JLabel("1 / 1");
        pageLabel.setFont(AppTheme.BODY_SM);
        pageLabel.setForeground(AppTheme.TEXT_SECONDARY);

        prevBtn.addActionListener(e -> { if (currentPage > 0) { currentPage--; refreshTable(); } });
        nextBtn.addActionListener(e -> {
            int total = totalPages();
            if (currentPage < total - 1) { currentPage++; refreshTable(); }
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

        // ── 안내 텍스트 ──
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
            // 1. 날짜 필터 적용
            String selected = (String) dateFilter.getSelectedItem();
            filteredSessions.clear();
            for (SessionRow s : allSessions) {
                if ("전체 날짜".equals(selected) || s.date().equals(selected))
                    filteredSessions.add(s);
            }

            // 2. 현재 페이지 클램핑 후 슬라이싱
            currentPage = Math.min(currentPage, Math.max(0, totalPages() - 1));
            int from = currentPage * PAGE_SIZE;
            int to   = Math.min(filteredSessions.size(), from + PAGE_SIZE);
            pageSessions.clear();
            pageSessions.addAll(filteredSessions.subList(from, to));

            // 3. 모델 재구성
            sessionModel.setRowCount(0);
            for (SessionRow s : pageSessions) {
                sessionModel.addRow(new Object[]{
                    s.date(), s.start(), s.end(),
                    String.valueOf(s.remaining()), s.instructor()
                });
            }

            // 4. 비활성 행 계산 (선택 복원 전에 해야 renderer가 올바르게 동작)
            rebuildDisabledRows();

            // 5. 현재 페이지에서 이전 선택 복원
            sessionTable.clearSelection();
            for (int mr = 0; mr < pageSessions.size(); mr++) {
                SessionRow s = pageSessions.get(mr);
                if (s.slotKey().equals(selectedByDate.get(s.date()))) {
                    int vr = sessionTable.convertRowIndexToView(mr);
                    if (vr >= 0) sessionTable.addRowSelectionInterval(vr, vr);
                }
            }

            // 6. 페이지 레이블 및 버튼 상태
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

    // ── 셀 렌더러 (비활성 행 시각화) ─────────────────────────────────────────

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
                // 비활성 행이 선택되었으면 즉시 해제
                for (int vr : sessionTable.getSelectedRows()) {
                    int mr = sessionTable.convertRowIndexToModel(vr);
                    if (disabledRows.contains(mr))
                        sessionTable.getSelectionModel().removeSelectionInterval(vr, vr);
                }

                // selectedByDate를 현재 페이지 기준으로 재동기화
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
                lastClickedViewRow = vr;
                // 선택 리스너가 발화하지 않았다는 것 = 클릭 전부터 이미 선택된 행
                // 선택 리스너가 발화했다면 = 새로 선택된 행 (토글 불필요)
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

        List<String> noSeats   = new ArrayList<>();
        List<String> conflicts = new ArrayList<>();
        List<String> valid     = new ArrayList<>();

        for (String slotKey : selectedByDate.values()) {
            String[] p     = slotKey.split("\\|");
            String   label = p[0] + " " + p[1];

            int remaining = allSessions.stream()
                .filter(s -> s.slotKey().equals(slotKey))
                .mapToInt(SessionRow::remaining)
                .findFirst().orElse(0);

            if (remaining == 0) {
                noSeats.add(label);
            } else if (hasTimeConflict(slotKey)) {
                conflicts.add(label);
            } else {
                valid.add(label);
            }
        }

        // ── 잔여석 없음 ──────────────────────────────────────────────────────
        if (!noSeats.isEmpty()) {
            HDialog.alert(parent, "예약 불가 — 잔여석 없음",
                "다음 세션은 잔여석이 없어 예약할 수 없습니다:\n\n  · " +
                String.join("\n  · ", noSeats) + "\n\n다른 날짜의 세션을 선택하세요.",
                HDialog.Type.WARNING);
            return;
        }

        // ── 시간대 중복 ──────────────────────────────────────────────────────
        if (!conflicts.isEmpty()) {
            String conflictList = "  · " + String.join("\n  · ", conflicts);
            if (valid.isEmpty()) {
                HDialog.alert(parent, "시간대 중복",
                    "선택한 모든 세션이 기존 예약과 시간대가 겹쳐 예약할 수 없습니다:\n\n" +
                    conflictList + "\n\n나의 예약에서 기존 예약을 확인하세요.",
                    HDialog.Type.WARNING);
                return;
            }
            String validList = "  · " + String.join("\n  · ", valid);
            boolean proceed = HDialog.confirm(parent, "일부 세션 시간대 중복",
                "다음 세션은 기존 예약과 시간대가 겹쳐 예약할 수 없습니다:\n" + conflictList +
                "\n\n나머지 " + valid.size() + "개 세션은 예약 가능합니다:\n" + validList +
                "\n\n예약 가능한 세션만 예약하시겠습니까?");
            if (!proceed) return;
        }

        // ── 최종 예약 확인 ───────────────────────────────────────────────────
        if (valid.isEmpty()) return;

        String sessionList = "  · " + String.join("\n  · ", valid);
        if (HDialog.confirm(parent, "예약 확인",
                valid.size() + "개 세션을 예약하시겠습니까?\n\n" + sessionList)) {
            // TODO: for each valid session_id:
            //   INSERT INTO reservations (user_id, session_id, status, reserved_at) VALUES (?, ?, 'CONFIRMED', NOW())
            //   UPDATE sessions SET booked_count = booked_count + 1 WHERE id = ?
            //   COUNT 타입 회원권 보유 시:
            //     UPDATE memberships SET remaining_count = remaining_count - 1
            //     WHERE user_id = ? AND program_id = ? AND status = 'ACTIVE' AND remaining_count > 0
            //     ORDER BY issued_at LIMIT 1
            HToast.success(parent, valid.size() + "개 세션이 예약되었습니다.");
            dispose();
        }
    }

    // ── 시간대 중복 체크 ─────────────────────────────────────────────────────

    private static boolean hasTimeConflict(String slotKey) {
        // TODO: SELECT COUNT(*) FROM reservations r
        //       JOIN sessions s ON s.id = r.session_id
        //       WHERE r.user_id = ? AND r.status = 'CONFIRMED'
        //         AND s.session_date = ?
        //         AND s.start_time < ? AND s.end_time > ?
        return MOCK_CONFIRMED_SLOTS.contains(slotKey);
    }
}
