package healthajo.membership.view;

import healthajo.component.HButton;
import healthajo.component.HComboBox;
import healthajo.component.HDialog;
import healthajo.component.HFormGroup;
import healthajo.component.HLabel;
import healthajo.component.HTextField;
import healthajo.component.HToast;
import healthajo.component.theme.AppTheme;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * 회원권 발급 다이얼로그.
 *
 * 회원 선택 → 프로그램 선택 → 회원권 정보 입력 → 발급
 *
 * TODO: INSERT INTO memberships (user_id, program_id, name, total_count,
 *                               remaining_count, status, issued_at)
 *       VALUES (?, ?, ?, ?, ?, 'ACTIVE', NOW())
 */
public class MembershipIssueDialog extends JDialog {

    // MOCK 데이터 (TODO: DB에서 조회)
    private static final String[] MOCK_USERS     = {"홍길동 (010-1234-5678)", "김영희 (010-2345-6789)",
                                                    "이철수 (010-3456-7890)", "박민준 (010-4567-8901)",
                                                    "최서연 (010-5678-9012)", "강동원 (010-7890-1234)"};
    private static final String[] MOCK_PROGRAMS  = {"스피닝 A반", "요가 기초반", "필라테스 중급",
                                                    "골프 입문반", "여름 특강"};

    private boolean  issued = false;
    private Object[] issuedRow;

    private HComboBox<String> userCombo;
    private HComboBox<String> programCombo;
    private HTextField        nameField;
    private HTextField        countField;
    private JLabel            previewLabel;

    public MembershipIssueDialog(JFrame parent) {
        super(parent, "회원권 발급", true);
        setSize(460, 480);
        setResizable(false);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(AppTheme.SURFACE);
        setLayout(new BorderLayout());

        add(buildForm(),   BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    // ── Form ──────────────────────────────────────────────────────────────────

    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setBackground(AppTheme.SURFACE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(AppTheme.SP_5, AppTheme.SP_6, AppTheme.SP_4, AppTheme.SP_6));

        JLabel title = HLabel.h3("회원권 발급");
        title.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtitle = HLabel.small("회원과 프로그램을 선택하고 회원권 정보를 입력하세요.");
        subtitle.setAlignmentX(LEFT_ALIGNMENT);
        subtitle.setForeground(AppTheme.TEXT_SECONDARY);

        // 회원 선택
        userCombo = new HComboBox<>(MOCK_USERS);
        userCombo.setMaximumSize(new Dimension(Short.MAX_VALUE, 44));
        HFormGroup userGroup = new HFormGroup("회원 선택", userCombo);
        userGroup.setAlignmentX(LEFT_ALIGNMENT);

        // 프로그램 선택
        programCombo = new HComboBox<>(MOCK_PROGRAMS);
        programCombo.setMaximumSize(new Dimension(Short.MAX_VALUE, 44));
        HFormGroup programGroup = new HFormGroup("프로그램 선택", programCombo);
        programGroup.setAlignmentX(LEFT_ALIGNMENT);
        programCombo.addActionListener(e -> autoFillName());

        // 회원권명
        nameField = new HTextField("예: 스피닝 A반 수강권 20회");
        nameField.setMaximumSize(new Dimension(Short.MAX_VALUE, 44));
        HFormGroup nameGroup = new HFormGroup("회원권명", nameField);
        nameGroup.setAlignmentX(LEFT_ALIGNMENT);

        // 총 횟수
        countField = new HTextField("예: 20");
        countField.setMaximumSize(new Dimension(Short.MAX_VALUE, 44));
        HFormGroup countGroup = new HFormGroup("총 횟수", countField);
        countGroup.setAlignmentX(LEFT_ALIGNMENT);
        countField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updatePreview(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updatePreview(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });

        // 발급 미리보기
        previewLabel = HLabel.small("회원권 정보를 입력하면 미리보기가 표시됩니다.");
        previewLabel.setAlignmentX(LEFT_ALIGNMENT);
        previewLabel.setForeground(AppTheme.TEXT_MUTED);
        previewLabel.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 0, 0, AppTheme.BORDER_SUBTLE),
            new EmptyBorder(AppTheme.SP_3, 0, 0, 0)
        ));

        autoFillName();

        form.add(title);
        form.add(Box.createVerticalStrut(AppTheme.SP_1));
        form.add(subtitle);
        form.add(Box.createVerticalStrut(AppTheme.SP_5));
        form.add(userGroup);
        form.add(Box.createVerticalStrut(AppTheme.SP_4));
        form.add(programGroup);
        form.add(Box.createVerticalStrut(AppTheme.SP_4));
        form.add(nameGroup);
        form.add(Box.createVerticalStrut(AppTheme.SP_4));
        form.add(countGroup);
        form.add(Box.createVerticalStrut(AppTheme.SP_4));
        form.add(previewLabel);

        return form;
    }

    private void autoFillName() {
        String program = (String) programCombo.getSelectedItem();
        if (program != null && nameField.getText().isBlank()) {
            String count = countField.getText().isBlank() ? "20" : countField.getText().trim();
            nameField.setText(program + " 수강권 " + count + "회");
        }
        updatePreview();
    }

    private void updatePreview() {
        String user    = userCombo.getSelectedItem() != null
            ? ((String) userCombo.getSelectedItem()).split(" ")[0] : "?";
        String program = (String) programCombo.getSelectedItem();
        String count   = countField.getText().trim();
        if (!count.isEmpty()) {
            try {
                int n = Integer.parseInt(count);
                previewLabel.setText("발급 대상: " + user + " / " + program + " / " + n + "회 (잔여 " + n + "회)");
                previewLabel.setForeground(AppTheme.PRIMARY);
            } catch (NumberFormatException ex) {
                previewLabel.setText("총 횟수를 올바른 숫자로 입력하세요.");
                previewLabel.setForeground(AppTheme.DANGER);
            }
        } else {
            previewLabel.setText("총 횟수를 입력하면 미리보기가 표시됩니다.");
            previewLabel.setForeground(AppTheme.TEXT_MUTED);
        }
    }

    // ── Footer ────────────────────────────────────────────────────────────────

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SP_2, AppTheme.SP_3));
        footer.setBackground(AppTheme.SURFACE);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, AppTheme.BORDER));

        HButton cancel  = HButton.ghost("취소",    HButton.Size.SM);
        HButton confirm = HButton.primary("발급",  HButton.Size.SM);

        cancel.addActionListener(e -> dispose());
        confirm.addActionListener(e -> onIssue());

        footer.add(cancel);
        footer.add(confirm);
        return footer;
    }

    // ── Issue ─────────────────────────────────────────────────────────────────

    private void onIssue() {
        String countStr = countField.getText().trim();
        String name     = nameField.getText().trim();
        String user     = userCombo.getSelectedItem() != null
            ? ((String) userCombo.getSelectedItem()).split(" ")[0] : null;
        String program  = (String) programCombo.getSelectedItem();

        if (name.isEmpty()) {
            HDialog.error(getOwner() instanceof JFrame f ? f : null, "회원권명을 입력하세요.");
            return;
        }
        int count;
        try {
            count = Integer.parseInt(countStr);
            if (count <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            HDialog.error(getOwner() instanceof JFrame f ? f : null, "총 횟수는 1 이상의 정수를 입력하세요.");
            return;
        }

        // TODO: INSERT INTO memberships ...
        issuedRow = new Object[]{user, program, name, String.valueOf(count), String.valueOf(count),
                                 "ACTIVE", java.time.LocalDate.now().toString()};
        issued = true;
        dispose();
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public boolean  isIssued()   { return issued; }
    public Object[] getIssuedRow() { return issuedRow; }
}
