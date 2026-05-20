package healthajo.user.view.admin;

import healthajo.component.HButton;
import healthajo.component.HDialog;
import healthajo.component.HLabel;
import healthajo.component.HTabPanel;
import healthajo.component.HTable;
import healthajo.component.HTextField;
import healthajo.component.HToast;
import healthajo.component.theme.AppTheme;
import healthajo.users.UsersDAO;
import healthajo.memberships.service.MembershipService;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import healthajo.jdbc.core.Record;

/**
 * 사용자 상세 다이얼로그.

 * 탭 구성:
 *   1) 기본 정보 — 이름, 전화번호, 이메일 (수정 가능)
 *   2) 회원권    — 보유 회원권 목록, 횟수 수정
 *   3) 예약 이력 — 예약 테이블

 * TODO: 각 탭 데이터를 DB에서 조회
 */
public class UserDetailDialog extends JDialog {

    private final Object[] rowData;
    private final UsersDAO dao;
    private final Long      userId;
    private final MembershipService membershipService = new MembershipService();

    // 기본 정보 탭 필드 (수정 모드용)
    private HTextField nameField;
    private HTextField phoneField;
    private HTextField emailField;
    private HButton    editBtn;
    private boolean    editMode = false;

    public UserDetailDialog(JFrame parent, Object[] rowData, int modelRow, UsersDAO dao, Long userId) {
        super(parent, "사용자 상세", true);
        this.rowData  = rowData;
        this.dao      = dao;
        this.userId   = userId;
        setLayout(new BorderLayout());
        setSize(600, 520);
        setLocationRelativeTo(parent);
        setResizable(true);
        getContentPane().setBackground(AppTheme.SURFACE);

        // ── 헤더 ─────────────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppTheme.SURFACE);
        header.setBorder(new EmptyBorder(AppTheme.SP_5, AppTheme.SP_6, AppTheme.SP_4, AppTheme.SP_6));
        header.setBorder(new MatteBorder(0, 0, 1, 0, AppTheme.BORDER));

        JPanel titleArea = new JPanel();
        titleArea.setOpaque(false);
        titleArea.setLayout(new BoxLayout(titleArea, BoxLayout.Y_AXIS));
        JLabel nameTitle  = HLabel.h2(rowData[0].toString());
        JLabel phoneTitle = HLabel.small(rowData[1].toString());
        nameTitle.setAlignmentX(LEFT_ALIGNMENT);
        phoneTitle.setAlignmentX(LEFT_ALIGNMENT);
        titleArea.setBorder(new EmptyBorder(AppTheme.SP_4, AppTheme.SP_6, AppTheme.SP_4, AppTheme.SP_6));
        titleArea.add(nameTitle);
        titleArea.add(Box.createVerticalStrut(2));
        titleArea.add(phoneTitle);
        header.add(titleArea, BorderLayout.WEST);

        // ── 탭 패널 ──────────────────────────────────────────────────────────────
        HTabPanel tabs = new HTabPanel();
        tabs.addTab("기본 정보", buildInfoTab(parent));
        tabs.addTab("회원권",    buildMembershipTab());
        tabs.addTab("예약 이력", buildReservationTab());

        // ── 푸터 ─────────────────────────────────────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SP_2, AppTheme.SP_3));
        footer.setBackground(AppTheme.SURFACE);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, AppTheme.BORDER));

        HButton closeBtn = HButton.secondary("닫기", HButton.Size.SM);
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);

        add(header, BorderLayout.NORTH);
        add(tabs,   BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    // ── 기본 정보 탭 ─────────────────────────────────────────────────────────────

    private JPanel buildInfoTab(JFrame parent) {
        JPanel p = new JPanel();
        p.setBackground(AppTheme.SURFACE);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(AppTheme.SP_5, AppTheme.SP_6, AppTheme.SP_5, AppTheme.SP_6));

        nameField  = new HTextField("");
        phoneField = new HTextField("");
        emailField = new HTextField("");
        nameField.setText(rowData[0].toString());
        phoneField.setText(rowData[1].toString());
        emailField.setText(rowData[2].toString());
        nameField.setEditable(false);
        phoneField.setEditable(false);
        emailField.setEditable(false);

        Dimension fieldSize = new Dimension(Short.MAX_VALUE, 44);
        nameField.setMaximumSize(fieldSize);
        phoneField.setMaximumSize(fieldSize);
        emailField.setMaximumSize(fieldSize);

        addRow(p, "이름", nameField);
        p.add(Box.createVerticalStrut(AppTheme.SP_3));
        addRow(p, "전화번호", phoneField);
        p.add(Box.createVerticalStrut(AppTheme.SP_3));
        addRow(p, "이메일", emailField);
        p.add(Box.createVerticalStrut(AppTheme.SP_4));

        // 수정 / 저장 버튼
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SP_2, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);

        HButton cancelEditBtn = HButton.ghost("취소", HButton.Size.SM);
        cancelEditBtn.setVisible(false);
        editBtn = HButton.secondary("수정", HButton.Size.SM);

        editBtn.addActionListener(e -> {
            editMode = !editMode;
            nameField.setEditable(editMode);
            phoneField.setEditable(editMode);
            emailField.setEditable(editMode);
            if (editMode) {
                editBtn.setText("저장");
                cancelEditBtn.setVisible(true);
            } else {
                // 저장 처리
                // TODO: UPDATE users SET name=?, phone=?, email=? WHERE id=?
                String name = nameField.getText().trim();
                String phone = phoneField.getText().trim();
                String email = emailField.getText().trim();
                dao.update(userId, name, phone, email.isEmpty() ? null : email);
                HToast.success(parent, "변경 사항이 저장되었습니다.");
                cancelEditBtn.setVisible(false);
            }
        });

        cancelEditBtn.addActionListener(e -> {
            editMode = false;
            nameField.setText(rowData[0].toString());
            phoneField.setText(rowData[1].toString());
            emailField.setText(rowData[2].toString());
            nameField.setEditable(false);
            phoneField.setEditable(false);
            emailField.setEditable(false);
            editBtn.setText("수정");
            cancelEditBtn.setVisible(false);
        });

        btnRow.add(cancelEditBtn);
        btnRow.add(editBtn);
        p.add(btnRow);

        JScrollPane sp = new JScrollPane(p);
        sp.setBorder(null);
        sp.getViewport().setBackground(AppTheme.SURFACE);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(AppTheme.SURFACE);
        wrapper.add(sp);
        return wrapper;
    }

    // ── 회원권 탭 ────────────────────────────────────────────────────────────────

    private JPanel buildMembershipTab() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(AppTheme.BG);

        DefaultTableModel mModel = new DefaultTableModel(
            new String[]{"회원권명", "프로그램명", "총 횟수", "잔여 횟수", "상태", "발급일"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        // MOCK: 회원권 목록
        // TODO: SELECT m.name, p.name, m.total_count, m.remaining_count, m.status,
        //              DATE_FORMAT(m.issued_at, '%Y-%m-%d')
        //       FROM memberships m
        //       JOIN programs p ON p.id = m.program_id
        //       WHERE m.user_id = ?
        //       ORDER BY m.issued_at DESC
        List<Record> memberships = membershipService.getMembershipsWithProgramByUserId(userId);
        for (Record r : memberships) {
            Object issuedAt = r.get("issued_at");
            String issuedStr = issuedAt == null ? "" : issuedAt.toString();
            if (issuedStr.length() >= 10) issuedStr = issuedStr.substring(0, 10);
            mModel.addRow(new Object[]{
                    r.get("membership_name"),
                    r.get("program_name"),
                    r.get("total_count"),
                    r.get("remaining_count"),
                    r.get("status"),
                    issuedStr
            });
        }

        HTable mTable = new HTable(mModel);
        mTable.setBadgeRenderer(4);

        // 횟수 수정 버튼
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SP_2, AppTheme.SP_2));
        toolbar.setBackground(AppTheme.BG);
        HButton adjustBtn = HButton.secondary("횟수 수정", HButton.Size.SM);
        adjustBtn.addActionListener(e -> onAdjustCount(mTable, mModel));
        toolbar.add(adjustBtn);

        p.add(toolbar,           BorderLayout.NORTH);
        p.add(mTable.inScrollPane(), BorderLayout.CENTER);
        return p;
    }

    private void onAdjustCount(HTable mTable, DefaultTableModel mModel) {
        int row = mTable.getSelectedRow();
        if (row < 0) {
            HDialog.info((JFrame) getOwner(), "횟수를 수정할 회원권을 선택하세요.");
            return;
        }
        int modelRow = mTable.convertRowIndexToModel(row);
        String membership = (String) mModel.getValueAt(modelRow, 0);
        int remaining = Integer.parseInt((String) mModel.getValueAt(modelRow, 3));

        String input = JOptionPane.showInputDialog(this,
            "[" + membership + "]\n현재 잔여 횟수: " + remaining +
            "회\n조정할 횟수 입력 (양수: 충전, 음수: 차감):",
            "횟수 수정", JOptionPane.PLAIN_MESSAGE);

        if (input != null && !input.isBlank()) {
            try {
                int delta    = Integer.parseInt(input.trim());
                int newCount = Math.max(0, remaining + delta);
                // MOCK: 테이블 직접 변경
                // TODO: UPDATE memberships SET remaining_count = remaining_count + ?
                //       WHERE id = ?
                //       remaining_count > 0 AND status='EXPIRED' → status='ACTIVE'
                //       remaining_count = 0 → status='EXPIRED'

                // membership 기능 개발되면 추후 반영

                mModel.setValueAt(String.valueOf(newCount), modelRow, 3);
                mModel.setValueAt(newCount > 0 ? "ACTIVE" : "EXPIRED", modelRow, 4);
                HToast.success((JFrame) getOwner(), "횟수가 수정되었습니다.");
            } catch (NumberFormatException ex) {
                HDialog.error((JFrame) getOwner(), "유효한 숫자를 입력하세요.");
            }
        }
    }

    // ── 예약 이력 탭 ─────────────────────────────────────────────────────────────

    private JPanel buildReservationTab() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(AppTheme.BG);

        DefaultTableModel rModel = new DefaultTableModel(
            new String[]{"프로그램명", "세션 날짜", "예약 상태", "출석 상태", "예약일"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        // MOCK: 예약 이력
        // TODO: SELECT p.name, s.session_date, r.status, r.attendance_status,
        //              DATE_FORMAT(r.reserved_at, '%Y-%m-%d')
        //       FROM reservations r
        //       JOIN sessions s ON s.id = r.session_id
        //       JOIN programs p ON p.id = s.program_id
        //       WHERE r.user_id = ?
        //       ORDER BY r.reserved_at DESC

        // reservations 기능 개발되면 추후 반영

        rModel.addRow(new Object[]{"스피닝 A반",    "2025-05-20", "CONFIRMED",        "PENDING",  "2025-05-01"});
        rModel.addRow(new Object[]{"요가 기초반",   "2025-05-15", "MEMBERSHIP_ISSUED", "ATTENDED", "2025-04-20"});
        rModel.addRow(new Object[]{"필라테스 중급", "2025-03-10", "CANCELLED",        "ABSENT",   "2025-03-01"});

        HTable rTable = new HTable(rModel);
        rTable.setBadgeRenderer(2);
        rTable.setBadgeRenderer(3);

        p.add(rTable.inScrollPane(), BorderLayout.CENTER);
        return p;
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────────────────

    private static void addRow(JPanel p, String label, JComponent field) {
        JLabel lbl = HLabel.label(label);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        p.add(lbl);
        p.add(Box.createVerticalStrut(AppTheme.SP_1));
        p.add(field);
    }
}
