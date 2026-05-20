package healthajo.membership.view;

import healthajo.component.HButton;
import healthajo.component.HDialog;
import healthajo.component.HFormGroup;
import healthajo.component.HLabel;
import healthajo.component.HTable;
import healthajo.component.HTextField;
import healthajo.component.HToast;
import healthajo.component.theme.AppTheme;
import healthajo.template.BaseListPanel;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.TableColumnModel;

/**
 * 관리자 — 회원권 관리 화면.
 *
 * TODO: SELECT u.name, p.name AS program_name, m.name AS membership_name,
 *              m.total_count, m.remaining_count, m.status,
 *              DATE_FORMAT(m.issued_at, '%Y-%m-%d')
 *       FROM memberships m
 *       JOIN users u ON u.id = m.user_id
 *       JOIN programs p ON p.id = m.program_id
 *       ORDER BY m.issued_at DESC
 */
public class MembershipAdminPanel extends BaseListPanel {

    public MembershipAdminPanel() { super(); }

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

        HButton issueBtn = HButton.primary("회원권 발급", HButton.Size.SM);
        issueBtn.addActionListener(e -> onIssueMembership());

        return List.of(adjustBtn, issueBtn);
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
        // MOCK
        // TODO: DB 조회 후 교체
        model.addRow(new Object[]{"홍길동", "스피닝 A반",    "스피닝 A반 수강권",    "20", "15", "ACTIVE",  "2025-03-01"});
        model.addRow(new Object[]{"김영희", "요가 기초반",   "요가 기초반 수강권",   "20", "20", "ACTIVE",  "2025-04-01"});
        model.addRow(new Object[]{"이철수", "스피닝 A반",    "스피닝 A반 수강권",    "20",  "5", "ACTIVE",  "2025-03-01"});
        model.addRow(new Object[]{"박민준", "골프 입문반",   "골프 입문반 수강권",   "16", "16", "ACTIVE",  "2025-05-01"});
        model.addRow(new Object[]{"최서연", "필라테스 중급", "필라테스 중급 수강권", "24",  "0", "EXPIRED", "2025-02-01"});
        model.addRow(new Object[]{"강동원", "요가 기초반",   "요가 기초반 수강권",   "20", "18", "ACTIVE",  "2025-04-15"});
        setTotalCount(model.getRowCount());
    }

    @Override
    protected void onRowDoubleClick(int modelRow) {
        onAdjustCountFor(modelRow);
    }

    private void onAdjustCount() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            HDialog.info(parentFrame(), "횟수를 수정할 회원권을 선택하세요.");
            return;
        }
        onAdjustCountFor(table.convertRowIndexToModel(viewRow));
    }

    private void onAdjustCountFor(int mr) {
        String name       = (String) model.getValueAt(mr, 0);
        String membership = (String) model.getValueAt(mr, 2);
        int remaining     = Integer.parseInt((String) model.getValueAt(mr, 4));

        // 횟수 수정 다이얼로그
        JDialog dlg = new JDialog(parentFrame(), "횟수 수정", true);
        dlg.setLayout(new BorderLayout());
        dlg.setSize(380, 250);
        dlg.setLocationRelativeTo(parentFrame());
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

        deltaField.addActionListener(e -> updatePreview(deltaField, preview, remaining));
        deltaField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updatePreview(deltaField, preview, remaining); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updatePreview(deltaField, preview, remaining); }
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
                int delta    = Integer.parseInt(deltaField.getText().trim().replace("+", ""));
                int newCount = Math.max(0, remaining + delta);
                // TODO: UPDATE memberships SET remaining_count = remaining_count + ?
                //       WHERE id = ?
                //       remaining_count > 0 AND status='EXPIRED' → status='ACTIVE'
                //       remaining_count = 0 → status='EXPIRED'
                model.setValueAt(String.valueOf(newCount), mr, 4);
                model.setValueAt(newCount > 0 ? "ACTIVE" : "EXPIRED", mr, 5);
                dlg.dispose();
                HToast.success(parentFrame(), "횟수가 수정되었습니다. (" + remaining + " → " + newCount + "회)");
            } catch (NumberFormatException ex) {
                HDialog.error(parentFrame(), "유효한 숫자를 입력하세요. (예: 5 또는 -3)");
            }
        });

        footer.add(cancel);
        footer.add(confirm);

        dlg.add(form,   BorderLayout.CENTER);
        dlg.add(footer, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private static void updatePreview(HTextField field, JLabel preview, int remaining) {
        try {
            int delta    = Integer.parseInt(field.getText().trim().replace("+", ""));
            int newCount = Math.max(0, remaining + delta);
            preview.setText("조정 후 잔여: " + newCount + "회" + (newCount == 0 ? " (만료)" : ""));
            preview.setForeground(newCount > 0 ? AppTheme.PRIMARY : AppTheme.DANGER);
        } catch (NumberFormatException e) {
            preview.setText("조정 후 잔여: ?");
            preview.setForeground(AppTheme.TEXT_MUTED);
        }
    }

    private void onIssueMembership() {
        MembershipIssueDialog dlg = new MembershipIssueDialog(parentFrame());
        dlg.setVisible(true);
        if (dlg.isIssued()) {
            model.addRow(dlg.getIssuedRow());
            HToast.success(parentFrame(), "회원권이 발급되었습니다.");
        }
    }
}
