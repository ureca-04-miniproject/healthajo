package healthajo.membership.view;

import healthajo.component.HButton;
import healthajo.component.HComboBox;
import healthajo.component.HDialog;
import healthajo.component.HFormGroup;
import healthajo.component.HLabel;
import healthajo.component.HTextField;
import healthajo.component.theme.AppTheme;
import healthajo.jdbc.core.Column;
import healthajo.jdbc.core.Record;
import healthajo.jdbc.core.TableBase;
import healthajo.memberships.service.MembershipService;
import healthajo.users.UsersDAO;

import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.*;

import static healthajo.jdbc.table.TUser.USER;

/**
 * 회원권 발급 다이얼로그.
 *
 * 회원/프로그램 콤보박스에는 화면 표시 문자열이 아니라 DB id를 함께 가진 option 객체를 담는다.
 */
public class MembershipIssueDialog extends JDialog {

    private static final ProgramRef PROGRAM = new ProgramRef();
    private static final Pattern MEMBERSHIP_NAME_PATTERN =
            Pattern.compile("^[가-힣ㄱ-ㅎㅏ-ㅣA-Za-z0-9\\s()\\[\\]._\\-/]+$");

    private final MembershipService membershipService = new MembershipService();
    private final UsersDAO usersDAO = new UsersDAO();

    private boolean  issued = false;
    private Object[] issuedRow;

    private HComboBox<UserOption> userCombo;
    private HComboBox<ProgramOption> programCombo;
    private HTextField        nameField;
    private HTextField        countField;
    private JLabel            previewLabel;

    public MembershipIssueDialog(JFrame parent) {
        super(parent, "회원권 발급", true);
        setSize(460, 540);
        setResizable(false);
        getContentPane().setBackground(AppTheme.SURFACE);
        setLayout(new BorderLayout());

        add(buildForm(),   BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
        setLocationRelativeTo(parent);
    }

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

        userCombo = new HComboBox<>(loadUsers());
        userCombo.setMaximumSize(new Dimension(Short.MAX_VALUE, 44));
        userCombo.addActionListener(e -> updatePreview());
        HFormGroup userGroup = new HFormGroup("회원 선택", userCombo);
        userGroup.setAlignmentX(LEFT_ALIGNMENT);

        programCombo = new HComboBox<>(loadPrograms());
        programCombo.setMaximumSize(new Dimension(Short.MAX_VALUE, 44));
        programCombo.addActionListener(e -> autoFillName());
        HFormGroup programGroup = new HFormGroup("프로그램 선택", programCombo);
        programGroup.setAlignmentX(LEFT_ALIGNMENT);

        nameField = new HTextField("예: 스피닝 A반 수강권 20회");
        nameField.setMaximumSize(new Dimension(Short.MAX_VALUE, 44));
        HFormGroup nameGroup = new HFormGroup("회원권명", nameField);
        nameGroup.setAlignmentX(LEFT_ALIGNMENT);

        countField = new HTextField("예: 20");
        countField.setMaximumSize(new Dimension(Short.MAX_VALUE, 44));
        HFormGroup countGroup = new HFormGroup("총 횟수", countField);
        countGroup.setAlignmentX(LEFT_ALIGNMENT);
        countField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updatePreview(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updatePreview(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });

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
        ProgramOption program = (ProgramOption) programCombo.getSelectedItem();
        if (program != null && nameField.getText().isBlank()) {
            String count = countField.getText().isBlank() ? "20" : countField.getText().trim();
            nameField.setText(program.name() + " 수강권 " + count + "회");
        }
        updatePreview();
    }

    private void updatePreview() {
        if (previewLabel == null || userCombo == null || programCombo == null || countField == null) {
            return;
        }
        UserOption user = (UserOption) userCombo.getSelectedItem();
        ProgramOption program = (ProgramOption) programCombo.getSelectedItem();
        String count = countField.getText().trim();
        String userName = user != null ? user.name() : "?";
        String programName = program != null ? program.name() : "?";

        if (!count.isEmpty()) {
            try {
                int n = Integer.parseInt(count);
                previewLabel.setText("발급 대상: " + userName + " / " + programName + " / " + n + "회 (잔여 " + n + "회)");
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

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SP_2, AppTheme.SP_3));
        footer.setBackground(AppTheme.SURFACE);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, AppTheme.BORDER));

        HButton cancel  = HButton.ghost("취소", HButton.Size.SM);
        HButton confirm = HButton.primary("발급", HButton.Size.SM);

        cancel.addActionListener(e -> dispose());
        confirm.addActionListener(e -> onIssue());

        footer.add(cancel);
        footer.add(confirm);
        return footer;
    }

    private void onIssue() {
        String countStr = countField.getText().trim();
        String name = nameField.getText().trim();
        UserOption user = (UserOption) userCombo.getSelectedItem();
        ProgramOption program = (ProgramOption) programCombo.getSelectedItem();

        if (user == null) {
            HDialog.error(getOwner() instanceof JFrame f ? f : null, "회원을 선택하세요.");
            return;
        }
        if (program == null) {
            HDialog.error(getOwner() instanceof JFrame f ? f : null, "프로그램을 선택하세요.");
            return;
        }
        if (name.isEmpty()) {
            HDialog.error(getOwner() instanceof JFrame f ? f : null, "회원권명을 입력하세요.");
            return;
        }
        if (!isValidMembershipName(name)) {
            HDialog.error(getOwner() instanceof JFrame f ? f : null,
                    "회원권명에는 한글, 영문, 숫자, 공백, (), [], ., _, -, / 만 사용할 수 있습니다.");
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

        try {
            membershipService.issueMembership(user.id(), program.id(), name, count);
        } catch (RuntimeException ex) {
            HDialog.error(getOwner() instanceof JFrame f ? f : null, ex.getMessage());
            return;
        }

        issuedRow = new Object[]{
                user.name(),
                program.name(),
                name,
                String.valueOf(count),
                String.valueOf(count),
                "ACTIVE",
                LocalDate.now().toString()
        };

        issued = true;
        dispose();
    }

    private UserOption[] loadUsers() {
        try {
            List<Record> users = usersDAO.findAll();
            return users.stream()
                    .map(record -> new UserOption(
                            record.get(USER.ID),
                            record.get(USER.NAME),
                            record.get(USER.PHONE)))
                    .toArray(UserOption[]::new);
        } catch (RuntimeException ex) {
            HDialog.error(getOwner() instanceof JFrame f ? f : null, "회원 목록을 불러오지 못했습니다.\n" + ex.getMessage());
            return new UserOption[0];
        }
    }

    private ProgramOption[] loadPrograms() {
        try {
            List<Record> programs = PROGRAM.select(PROGRAM.ID, PROGRAM.NAME)
                    .where(PROGRAM.DELETED_AT.isNull())
                    .orderBy(PROGRAM.ID)
                    .fetch();
            return programs.stream()
                    .map(record -> new ProgramOption(
                            record.get(PROGRAM.ID),
                            record.get(PROGRAM.NAME)))
                    .toArray(ProgramOption[]::new);
        } catch (RuntimeException ex) {
            HDialog.error(getOwner() instanceof JFrame f ? f : null, "프로그램 목록을 불러오지 못했습니다.\n" + ex.getMessage());
            return new ProgramOption[0];
        }
    }

    private static boolean isValidMembershipName(String name) {
        return name.length() <= 100 && MEMBERSHIP_NAME_PATTERN.matcher(name).matches();
    }

    public boolean isIssued() {
        return issued;
    }

    public Object[] getIssuedRow() {
        return issuedRow;
    }

    private record UserOption(Long id, String name, String phone) {
        @Override
        public String toString() {
            return name + " (" + phone + ")";
        }
    }

    private record ProgramOption(Long id, String name) {
        @Override
        public String toString() {
            return name;
        }
    }

    private static final class ProgramRef extends TableBase {
        final Column<Long> ID = new Column<>(getPrefix(), "id", Long.class);
        final Column<String> NAME = new Column<>(getPrefix(), "name", String.class);
        final Column<java.sql.Timestamp> DELETED_AT = new Column<>(getPrefix(), "deleted_at", java.sql.Timestamp.class);

        ProgramRef() {
            super("programs");
        }
    }
}
