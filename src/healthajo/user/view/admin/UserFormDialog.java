package healthajo.user.view.admin;

import healthajo.component.HButton;
import healthajo.component.HFormGroup;
import healthajo.component.HLabel;
import healthajo.component.HTextField;
import healthajo.component.theme.AppTheme;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/**
 * 사용자 등록 / 수정 다이얼로그.
 *
 * editData가 null이면 신규 등록, 아니면 수정.
 */
public class UserFormDialog extends JDialog {

    private final HTextField nameField;
    private final HTextField phoneField;
    private final HTextField emailField;
    private final JLabel     errLabel;

    private boolean saved = false;

    public UserFormDialog(JFrame parent, Object[] editData) {
        super(parent, editData == null ? "사용자 등록" : "사용자 수정", true);
        setLayout(new BorderLayout());
        setResizable(false);
        setSize(440, 360);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(AppTheme.SURFACE);

        boolean isEdit = editData != null;

        // ── 폼 영역 ─────────────────────────────────────────────────────────────
        JPanel form = new JPanel();
        form.setBackground(AppTheme.SURFACE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(AppTheme.SP_6, AppTheme.SP_6, AppTheme.SP_4, AppTheme.SP_6));

        nameField  = new HTextField("홍길동");
        phoneField = new HTextField("010-0000-0000");
        emailField = new HTextField("example@email.com (선택)");

        nameField.setMaximumSize(new Dimension(Short.MAX_VALUE, 44));
        phoneField.setMaximumSize(new Dimension(Short.MAX_VALUE, 44));
        emailField.setMaximumSize(new Dimension(Short.MAX_VALUE, 44));

        if (isEdit) {
            nameField.setText(editData[0].toString());
            phoneField.setText(editData[1].toString());
            emailField.setText(editData[2].toString());
        }

        errLabel = HLabel.of("", AppTheme.SMALL, AppTheme.DANGER);
        errLabel.setAlignmentX(LEFT_ALIGNMENT);
        errLabel.setVisible(false);

        HFormGroup nameGroup  = new HFormGroup("이름 *",    nameField);
        HFormGroup phoneGroup = new HFormGroup("전화번호 *", phoneField);
        HFormGroup emailGroup = new HFormGroup("이메일",    emailField);

        nameGroup.setAlignmentX(LEFT_ALIGNMENT);
        phoneGroup.setAlignmentX(LEFT_ALIGNMENT);
        emailGroup.setAlignmentX(LEFT_ALIGNMENT);

        form.add(nameGroup);
        form.add(Box.createVerticalStrut(AppTheme.SP_4));
        form.add(phoneGroup);
        form.add(Box.createVerticalStrut(AppTheme.SP_4));
        form.add(emailGroup);
        form.add(Box.createVerticalStrut(AppTheme.SP_2));
        form.add(errLabel);

        // ── 푸터 영역 ────────────────────────────────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SP_2, AppTheme.SP_3));
        footer.setBackground(AppTheme.SURFACE);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, AppTheme.BORDER));

        HButton cancel  = HButton.ghost("취소", HButton.Size.SM);
        HButton confirm = HButton.primary(isEdit ? "저장" : "등록", HButton.Size.SM);

        cancel.addActionListener(e -> dispose());
        confirm.addActionListener(e -> onConfirm());

        footer.add(cancel);
        footer.add(confirm);

        add(form,   BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        nameField.requestFocusInWindow();
    }

    private void onConfirm() {
        String name  = nameField.getText().trim();
        String phone = phoneField.getText().trim();

        if (name.isEmpty()) {
            showError("이름을 입력하세요.");
            return;
        }
        if (phone.isEmpty()) {
            showError("전화번호를 입력하세요.");
            return;
        }
        if (!phone.matches("\\d{3}-\\d{3,4}-\\d{4}")) {
            showError("전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678)");
            return;
        }

        // TODO: INSERT INTO users (name, phone, email, created_at)
        //       VALUES (?, ?, ?, NOW())
        //       중복 전화번호 → showError("이미 등록된 전화번호입니다.")
        saved = true;
        dispose();
    }

    private void showError(String message) {
        errLabel.setText(message);
        errLabel.setVisible(true);
        errLabel.revalidate();
    }

    public boolean isSaved() { return saved; }

    public String[] getValues() {
        return new String[]{
            nameField.getText().trim(),
            phoneField.getText().trim(),
            emailField.getText().trim()
        };
    }
}
