package healthajo.session.view.admin;

import healthajo.attendance.domain.AttendanceSession;
import healthajo.attendance.view.admin.AttendanceDetailDialog;
import healthajo.component.HButton;
import healthajo.component.HDialog;
import healthajo.component.HTable;
import healthajo.component.HToast;
import healthajo.component.theme.AppTheme;
import healthajo.jdbc.core.Page;
import healthajo.session.application.SessionApplication;
import healthajo.session.domain.AdminSession;
import healthajo.session.domain.Instructor;
import healthajo.template.BaseListPanel;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.TableColumnModel;

/**
 * 관리자 — 세션 관리 화면.
 */
public class SessionListPanel extends BaseListPanel {

    private static final SessionApplication APP = new SessionApplication();

    private final List<AdminSession> sessions = new ArrayList<>();

    public SessionListPanel() { super(); }

    @Override protected String   pageTitle()         { return "세션 관리"; }
    @Override protected boolean  hasCheckbox()       { return false; }
    @Override protected String   searchPlaceholder() { return "프로그램명 또는 날짜 검색"; }

    @Override
    protected String[] columnNames() {
        return new String[]{"프로그램명", "날짜", "시작", "종료", "정원", "예약 수", "강사", "상태"};
    }

    @Override
    protected List<? extends JComponent> toolbarButtons() {
        HButton assignBtn = HButton.secondary("강사 배정", HButton.Size.SM);
        assignBtn.addActionListener(e -> onAssignInstructor());

        HButton cancelBtn = HButton.danger("세션 취소", HButton.Size.SM);
        cancelBtn.addActionListener(e -> onCancelSession());

        return List.of(assignBtn, cancelBtn);
    }

    @Override
    protected void configureColumns(TableColumnModel cm) {
        int[] widths = {150, 100, 60, 60, 60, 60, 90, 80};
        for (int i = 0; i < widths.length; i++) {
            cm.getColumn(i).setPreferredWidth(widths[i]);
        }
        table.setBadgeRenderer(7);
    }

    @Override
    protected void loadData() {
        if (sessions == null) return;  // super() 호출 시점엔 필드 미초기화
        sessions.clear();
        try {
            Page<AdminSession> page = APP.findAllForAdmin(currentPage - 1, pageSize);
            for (AdminSession s : page.getContent()) {
                sessions.add(s);
                model.addRow(toRow(s));
            }
            setTotalCount((int) page.getTotalCount());
        } catch (RuntimeException ex) {
            HDialog.error(parentFrame(), "세션 목록을 불러오지 못했습니다.\n" + ex.getMessage());
            setTotalCount(0);
        }
    }

    private static Object[] toRow(AdminSession s) {
        return new Object[]{
            s.programName(),
            s.date(),
            s.start(),
            s.end(),
            String.valueOf(s.capacity()),
            String.valueOf(s.bookedCount()),
            s.instructorName() != null ? s.instructorName() : "-",
            s.status()
        };
    }

    @Override
    protected void onRowDoubleClick(int modelRow) {
        if (modelRow >= sessions.size()) return;
        AdminSession s = sessions.get(modelRow);
        AttendanceSession detail = new AttendanceSession(
            s.id(), s.programName(), s.date(), s.start(), s.end(),
            s.bookedCount(), s.capacity(), false);
        new AttendanceDetailDialog(parentFrame(), detail).setVisible(true);
    }

    private void onAssignInstructor() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            HDialog.info(parentFrame(), "강사를 배정할 세션을 선택하세요.");
            return;
        }
        int mr = table.convertRowIndexToModel(viewRow);
        if (mr >= sessions.size()) return;
        AdminSession session = sessions.get(mr);

        List<Instructor> instructors;
        try {
            instructors = APP.findActiveInstructors();
        } catch (RuntimeException ex) {
            HDialog.error(parentFrame(), "강사 목록을 불러오지 못했습니다.\n" + ex.getMessage());
            return;
        }
        if (instructors.isEmpty()) {
            HDialog.alert(parentFrame(), "안내", "배정 가능한 활성 강사가 없습니다.", HDialog.Type.WARNING);
            return;
        }

        Instructor selected = (Instructor) JOptionPane.showInputDialog(
            parentFrame(),
            "[" + session.programName() + " " + session.date() + "]\n배정할 강사를 선택하세요:",
            "강사 배정", JOptionPane.PLAIN_MESSAGE, null,
            instructors.toArray(), instructors.get(0));

        if (selected == null) return;
        try {
            APP.assignInstructor(session.id(), selected.id());
            HToast.success(parentFrame(), "강사가 배정되었습니다: " + selected.name());
        } catch (RuntimeException ex) {
            HDialog.error(parentFrame(), "강사 배정에 실패했습니다.\n" + ex.getMessage());
            return;
        }
        model.setRowCount(0);
        loadData();
    }

    private void onCancelSession() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            HDialog.info(parentFrame(), "취소할 세션을 선택하세요.");
            return;
        }
        int mr = table.convertRowIndexToModel(viewRow);
        if (mr >= sessions.size()) return;
        AdminSession session = sessions.get(mr);
        if ("CANCELLED".equals(session.status())) {
            HDialog.alert(parentFrame(), "안내", "이미 취소된 세션입니다.", HDialog.Type.WARNING);
            return;
        }

        if (HDialog.confirmDanger(parentFrame(), "세션 취소",
                "[" + session.programName() + " " + session.date() + "] 세션을 취소하시겠습니까?\n" +
                "예약자 " + session.bookedCount() + "명에게 영향을 줄 수 있습니다.")) {
            try {
                APP.cancelSession(session.id());
                HToast.success(parentFrame(), "세션이 취소되었습니다.");
            } catch (RuntimeException ex) {
                HDialog.error(parentFrame(), "세션 취소에 실패했습니다.\n" + ex.getMessage());
                return;
            }
            model.setRowCount(0);
            loadData();
        }
    }
}
