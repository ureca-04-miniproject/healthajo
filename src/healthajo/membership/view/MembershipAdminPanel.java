package healthajo.membership.view;

import healthajo.component.HButton;
import healthajo.component.HDialog;
import healthajo.component.HFormGroup;
import healthajo.component.HLabel;
import healthajo.component.HTextField;
import healthajo.component.HToast;
import healthajo.component.theme.AppTheme;
import healthajo.jdbc.core.Record;
import healthajo.memberships.service.MembershipService;
import healthajo.template.BaseListPanel;

import java.awt.*;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.TableColumnModel;

import static healthajo.jdbc.table.TMembership.MEMBERSHIP;

/**
 * 관리자용 회원권 관리 화면.
 */
public class MembershipAdminPanel extends BaseListPanel {

    private static final MembershipService MEMBERSHIP_SERVICE = new MembershipService();
    private static final int COL_MEMBER_NAME = 0;
    private static final int COL_PROGRAM_NAME = 1;
    private static final int COL_MEMBERSHIP_NAME = 2;
    private static final int COL_TOTAL_COUNT = 3;
    private static final int COL_REMAINING_COUNT = 4;
    private static final int COL_STATUS = 5;
    private static final int COL_ISSUED_AT = 6;

    private List<Long> membershipIds;

    public MembershipAdminPanel() {
        super();
    }

    @Override protected String   pageTitle()         { return "회원권 관리"; }
    @Override protected boolean  hasCheckbox()       { return false; }
    @Override protected String   searchPlaceholder() { return "회원명 또는 프로그램명 검색"; }

    @Override
    protected String[] columnNames() {
        return new String[]{"회원명", "프로그램명", "회원권명", "총 횟수", "잔여 횟수", "상태", "발급일"};
    }

    @Override
    protected List<? extends JComponent> toolbarButtons() {
        HButton adjustBtn = HButton.secondary("횟수 수정", HButton.Size.SM);
        adjustBtn.addActionListener(e -> onAdjustCount());

        HButton deleteBtn = HButton.danger("회원권 삭제", HButton.Size.SM);
        deleteBtn.addActionListener(e -> onDeleteMembership());

        HButton issueBtn = HButton.primary("회원권 발급", HButton.Size.SM);
        issueBtn.addActionListener(e -> onIssueMembership());

        return List.of(adjustBtn, deleteBtn, issueBtn);
    }

    @Override
    protected void configureColumns(TableColumnModel cm) {
        int[] widths = {100, 140, 160, 70, 80, 80, 110};
        for (int i = 0; i < widths.length; i++) {
            cm.getColumn(i).setPreferredWidth(widths[i]);
        }
        table.setBadgeRenderer(5);
    }

    @Override
    protected void loadData() {
        ids().clear();
        long totalCount = 0;
        try {
            List<Record> memberships = MEMBERSHIP_SERVICE.getAllMembershipsWithUserAndProgram(pageSize, currentPage - 1);
            totalCount = MEMBERSHIP_SERVICE.countAllMemberships();
            for (Record record : memberships) {
                Long membershipId = firstLong(record, "membership_id", "id", "memberships.id");
                ids().add(membershipId);
                model.addRow(new Object[]{
                        text(record.get("user_name")),
                        text(record.get("program_name")),
                        text(record.get("membership_name")),
                        text(record.get("total_count")),
                        text(record.get("remaining_count")),
                        text(record.get("status")),
                        formatIssuedAt(record.get("issued_at"))
                });
            }
        } catch (RuntimeException ex) {
            HDialog.error(parentFrame(), "회원권 목록을 불러오지 못했습니다.\n" + ex.getMessage());
        }
        setTotalCount((int) totalCount);
    }

    @Override
    protected void onRowDoubleClick(int modelRow) {
        onAdjustCountFor(modelRow);
    }

    private void onAdjustCount() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            HDialog.info(parentFrame(), "잔수를 수정할 회원권을 선택하세요.");
            return;
        }
        onAdjustCountFor(table.convertRowIndexToModel(viewRow));
    }

    private void onAdjustCountFor(int mr) {
        Long membershipId = getMembershipId(mr);
        if (membershipId == null) {
            HDialog.error(parentFrame(), "회원권 ID를 찾을 수 없습니다.");
            return;
        }

        String name       = (String) model.getValueAt(mr, COL_MEMBER_NAME);
        String membership = (String) model.getValueAt(mr, COL_MEMBERSHIP_NAME);
        int total         = Integer.parseInt((String) model.getValueAt(mr, COL_TOTAL_COUNT));
        int remaining     = Integer.parseInt((String) model.getValueAt(mr, COL_REMAINING_COUNT));

        JDialog dlg = new JDialog(parentFrame(), "잔수 수정", true);
        dlg.setLayout(new BorderLayout());
        dlg.setSize(400, 300);
        dlg.setResizable(false);
        dlg.getContentPane().setBackground(AppTheme.SURFACE);

        JPanel form = new JPanel();
        form.setBackground(AppTheme.SURFACE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(AppTheme.SP_5, AppTheme.SP_6, AppTheme.SP_4, AppTheme.SP_6));

        JLabel title = HLabel.h3("[" + name + "] " + membership);
        title.setAlignmentX(LEFT_ALIGNMENT);

        JLabel current = HLabel.small("현재 잔여 횟수: " + remaining + "회");
        current.setAlignmentX(LEFT_ALIGNMENT);

        HTextField deltaField = new HTextField("예: +5 또는 -3");
        deltaField.setMaximumSize(new Dimension(Short.MAX_VALUE, 44));
        HFormGroup group = new HFormGroup("조정할 횟수 (양수: 충전, 음수: 차감)", deltaField);
        group.setAlignmentX(LEFT_ALIGNMENT);

        JLabel preview = HLabel.small("조정 후 잔여: " + remaining + "회");
        preview.setAlignmentX(LEFT_ALIGNMENT);
        preview.setForeground(AppTheme.PRIMARY);

        deltaField.addActionListener(e -> updatePreview(deltaField, preview, remaining, total));
        deltaField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updatePreview(deltaField, preview, remaining, total); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updatePreview(deltaField, preview, remaining, total); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });

        form.add(title);
        form.add(Box.createVerticalStrut(AppTheme.SP_2));
        form.add(current);
        form.add(Box.createVerticalStrut(AppTheme.SP_4));
        form.add(group);
        form.add(Box.createVerticalStrut(AppTheme.SP_2));
        form.add(preview);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SP_2, AppTheme.SP_3));
        footer.setBackground(AppTheme.SURFACE);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, AppTheme.BORDER));

        HButton cancel  = HButton.ghost("취소", HButton.Size.SM);
        HButton confirm = HButton.primary("저장", HButton.Size.SM);

        cancel.addActionListener(e -> dlg.dispose());
        confirm.addActionListener(e -> {
            try {
                int delta = parseDelta(deltaField.getText());
                int newCount = remaining + delta;
                if (newCount < 0) {
                    HDialog.error(parentFrame(), "잔여 횟수는 0보다 작을 수 없습니다.");
                    return;
                }

                if (newCount > total) {
                    HDialog.error(parentFrame(), "잔여 횟수는 총 횟수(" + total + "회)를 초과할 수 없습니다.");
                    return;
                }

                MEMBERSHIP_SERVICE.updateRemainingCount(membershipId, newCount);

                reloadFromDatabase();
                dlg.dispose();
                HToast.success(parentFrame(), "잔수가 수정되었습니다. (" + remaining + " -> " + newCount + "회)");
            } catch (NumberFormatException ex) {
                HDialog.error(parentFrame(), "유효한 숫자를 입력하세요. 예: 5 또는 -3");
            } catch (RuntimeException ex) {
                HDialog.error(parentFrame(), "잔수 수정에 실패했습니다.\n" + ex.getMessage());
            }
        });

        footer.add(cancel);
        footer.add(confirm);

        dlg.add(form,   BorderLayout.CENTER);
        dlg.add(footer, BorderLayout.SOUTH);
        dlg.setLocationRelativeTo(parentFrame());
        dlg.setVisible(true);
    }

    private static void updatePreview(HTextField field, JLabel preview, int remaining, int total) {
        try {
            int delta = parseDelta(field.getText());
            int newCount = remaining + delta;
            if (newCount < 0) {
                preview.setText("조정 후 잔여: 0보다 작을 수 없음");
                preview.setForeground(AppTheme.DANGER);
                return;
            }
            preview.setText("조정 후 잔여: " + newCount + "회" + (newCount == 0 ? " (만료)" : ""));
            if (newCount > total) {
                preview.setText("조정 후 잔여: 총 횟수(" + total + "회) 초과");
                preview.setForeground(AppTheme.DANGER);
                return;
            }
            preview.setForeground(newCount > 0 ? AppTheme.PRIMARY : AppTheme.DANGER);
        } catch (NumberFormatException e) {
            preview.setText("조정 후 잔여: ?");
            preview.setForeground(AppTheme.TEXT_MUTED);
        }
    }

    private static int parseDelta(String input) {
        String text = input == null ? "" : input.trim();
        if (text.isEmpty() || text.equals("+") || text.equals("-")) {
            throw new NumberFormatException("empty delta");
        }
        if (text.startsWith("+")) {
            text = text.substring(1);
        }
        return Integer.parseInt(text);
    }

    private void onIssueMembership() {
        MembershipIssueDialog dlg = new MembershipIssueDialog(parentFrame());
        dlg.setVisible(true);
        if (dlg.isIssued()) {
            reloadFromDatabase();
            HToast.success(parentFrame(), "회원권이 발급되었습니다.");
        }
    }

    private void onDeleteMembership() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            HDialog.info(parentFrame(), "삭제할 회원권을 선택하세요.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(viewRow);
        Long membershipId = getMembershipId(modelRow);
        if (membershipId == null) {
            HDialog.error(parentFrame(), "회원권 ID를 찾을 수 없습니다.");
            return;
        }

        String userName = (String) model.getValueAt(modelRow, COL_MEMBER_NAME);
        String membershipName = (String) model.getValueAt(modelRow, COL_MEMBERSHIP_NAME);
        if (!HDialog.confirmDanger(parentFrame(), "회원권 삭제",
                "[" + userName + "] " + membershipName + "\n회원권을 삭제하시겠습니까?")) {
            return;
        }

        try {
            MEMBERSHIP_SERVICE.deleteMembership(membershipId);
            reloadFromDatabase();
            HToast.success(parentFrame(), "회원권이 삭제되었습니다.");
        } catch (RuntimeException ex) {
            HDialog.error(parentFrame(), "회원권 삭제에 실패했습니다.\n" + ex.getMessage());
        }
    }

    private void reloadFromDatabase() {
        model.setRowCount(0);
        loadData();
    }

    private Long getMembershipId(int modelRow) {
        List<Long> ids = ids();
        if (modelRow < 0 || modelRow >= ids.size()) {
            return null;
        }
        return ids.get(modelRow);
    }

    private List<Long> ids() {
        if (membershipIds == null) {
            membershipIds = new ArrayList<>();
        }
        return membershipIds;
    }

    private static String text(Object value) {
        return value == null ? "" : value.toString();
    }

    private static Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    private static Long firstLong(Record record, String... keys) {
        for (String key : keys) {
            Long value = asLong(record.get(key));
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static String formatIssuedAt(Object issuedAt) {
        if (issuedAt == null) {
            return "";
        }
        if (issuedAt instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime().toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        return issuedAt.toString();
    }
}
