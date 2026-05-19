package healthajo.users;

import healthajo.jdbc.core.Record;
import healthajo.jdbc.table.TUser;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UsersView extends JPanel {

    private static final TUser T = TUser.USER;

    private final UsersDAO dao;

    private final JTextField searchField = new JTextField(15);
    private final JButton searchBtn = new JButton("검색");
    private final JButton refreshBtn = new JButton("전체보기");

    private final String[] COLUMNS = {"ID", "이름", "전화번호", "이메일", "역할", "등록일"};
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    private final JButton insBtn = new JButton("등록");
    private final JButton upBtn  = new JButton("수정");
    private final JButton delBtn = new JButton("삭제");

    public UsersView(UsersDAO dao) {
        this.dao = dao;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildTopPanel(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildBtnPanel(), BorderLayout.SOUTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        bindEvents();
        loadData(null);
    }

    private JPanel buildTopPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(new JLabel("검색:"));
        p.add(searchField);
        p.add(searchBtn);
        p.add(refreshBtn);
        return p;
    }

    private JPanel buildBtnPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p.add(insBtn);
        p.add(upBtn);
        p.add(delBtn);
        return p;
    }

    private void bindEvents() {
        searchBtn.addActionListener(e -> loadData(searchField.getText().trim()));
        refreshBtn.addActionListener(e -> { searchField.setText(""); loadData(null); });

        insBtn.addActionListener(e -> openInsertDialog());

        upBtn.addActionListener(e -> {
            Long id = getSelectedId();
            if (id == null) { JOptionPane.showMessageDialog(this, "수정할 회원을 선택하세요."); return; }
            openUpdateDialog(id);
        });

        delBtn.addActionListener(e -> {
            Long id = getSelectedId();
            if (id == null) { JOptionPane.showMessageDialog(this, "삭제할 회원을 선택하세요."); return; }
            int ok = JOptionPane.showConfirmDialog(this, "정말 삭제하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                dao.delete(id);
                loadData(null);
            }
        });
    }

    public void loadData(String keyword) {
        tableModel.setRowCount(0);
        List<Record> list = (keyword == null || keyword.isEmpty())
                ? dao.findAll()
                : dao.search(keyword);
        for (Record r : list) {
            tableModel.addRow(new Object[]{
                    r.get(T.ID),
                    r.get(T.NAME),
                    r.get(T.PHONE),
                    r.get(T.EMAIL),
                    r.get(T.ROLE),
                    r.get("created_at")
            });
        }
    }

    private Long getSelectedId() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return (Long) tableModel.getValueAt(row, 0);
    }

    // 등록
    private void openInsertDialog() {
        JDialog dlg = new JDialog(
                SwingUtilities.getWindowAncestor(this), "회원 등록",
                Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setLayout(new BorderLayout(5, 5));

        JPanel form = new JPanel(new GridLayout(4, 2, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField nameField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField emailField = new JTextField();
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"user", "admin"});

        form.add(new JLabel("이름:")); form.add(nameField);
        form.add(new JLabel("전화번호:")); form.add(phoneField);
        form.add(new JLabel("이메일:")); form.add(emailField);
        form.add(new JLabel("역할:")); form.add(roleBox);

        JButton saveBtn = new JButton("저장");
        JButton cancelBtn = new JButton("취소");
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            String role = (String) roleBox.getSelectedItem();

            if (name.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "이름과 전화번호는 필수입니다.");
                return;
            }

            dao.insert(name, phone, email, role);
            loadData(null);
            dlg.dispose();
        });
        cancelBtn.addActionListener(e -> dlg.dispose());

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    // 수정
    private void openUpdateDialog(Long id) {
        Record r = dao.findById(id);
        if (r == null) { JOptionPane.showMessageDialog(this, "회원 정보를 찾을 수 없습니다."); return; }

        JDialog dlg = new JDialog(
                SwingUtilities.getWindowAncestor(this), "회원 수정",
                Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setLayout(new BorderLayout(5, 5));

        JPanel form = new JPanel(new GridLayout(3, 2, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField nameField = new JTextField(r.get(T.NAME));
        JTextField phoneField = new JTextField(r.get(T.PHONE));
        JTextField emailField = new JTextField(r.get(T.EMAIL) == null ? "" : r.get(T.EMAIL));

        form.add(new JLabel("이름:")); form.add(nameField);
        form.add(new JLabel("전화번호:")); form.add(phoneField);
        form.add(new JLabel("이메일:")); form.add(emailField);

        JButton saveBtn = new JButton("저장");
        JButton cancelBtn = new JButton("취소");
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "이름과 전화번호는 필수입니다.");
                return;
            }
            dao.update(id, name, phone, email);
            loadData(null);
            dlg.dispose();
        });
        cancelBtn.addActionListener(e -> dlg.dispose());

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }
}