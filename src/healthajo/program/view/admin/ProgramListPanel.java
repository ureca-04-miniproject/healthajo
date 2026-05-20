package healthajo.program.view.admin;

import com.mysql.cj.xdevapi.Table;
import healthajo.component.HButton;
import healthajo.component.HDialog;
import healthajo.component.HToast;
import healthajo.component.theme.AppTheme;
import healthajo.jdbc.table.TPrograms;
import healthajo.programs.dao.ProgramDAO;
import healthajo.programs.dto.ProgramResponseDTO;
import healthajo.programs.service.ProgramService;
import healthajo.template.BaseListPanel;
import java.util.*;
import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * 관리자 — 프로그램 관리 화면.
 *
 * TODO: SELECT p.id, p.name, p.category,
 *              (SELECT COUNT(*) FROM reservations r
 *               JOIN sessions s ON s.id = r.session_id
 *               WHERE s.program_id = p.id AND r.status != 'CANCELLED') AS booked_count,
 *              p.reservation_start, p.reservation_end, p.status
 *       FROM programs p
 *       WHERE p.deleted_at IS NULL
 *       ORDER BY p.created_at DESC
 */
public class ProgramListPanel extends BaseListPanel {
    private static final TPrograms T = TPrograms.PROGRAMS;
    private ProgramService service;

    private HButton deleteBtn;

    public ProgramListPanel() {
        super();
        model.addTableModelListener(e -> {
            if (deleteBtn != null) deleteBtn.setEnabled(getCheckedRows().length > 0);
        });
    }

    @Override protected String   pageTitle()       { return "프로그램 관리"; }
    @Override protected boolean  hasCheckbox()     { return true; }
    @Override protected String   searchPlaceholder() { return "프로그램명 또는 종목 검색"; }

    @Override
    protected String[] columnNames() {
        return new String[]{ "ID", "프로그램명", "종목", "예약 가능 기간", "총 정원", "예약 수", "상태"};
    }

    @Override
    protected List<? extends JComponent> toolbarButtons() {
        deleteBtn = HButton.danger("삭제", HButton.Size.SM);
        deleteBtn.setEnabled(false);
        deleteBtn.addActionListener(e -> onDelete());

        HButton addBtn = HButton.primary("프로그램 추가", HButton.Size.SM);
        addBtn.addActionListener(e -> onAdd());

        HButton issueBtn = HButton.secondary("회원권 일괄 발급", HButton.Size.SM);
        issueBtn.addActionListener(e -> onBulkIssueMembership());

        return List.of(deleteBtn, issueBtn, addBtn);
    }

    @Override
    protected void configureColumns(TableColumnModel cm) {
        TableColumn idCol = cm.getColumn(1);
        idCol.setMinWidth(0);
        idCol.setMaxWidth(0);
        idCol.setPreferredWidth(0);
        idCol.setResizable(false);

        int[] widths = {160, 80, 200, 70, 70, 80};
        for (int i = 0; i < widths.length; i++) {
            cm.getColumn(i + 2).setPreferredWidth(widths[i]);
        }
        table.setBadgeRenderer(7); // 상태 컬럼 (checkbox[0] + 5 data cols + 상태[6])
    }

    @Override
    protected void loadData() {
        // TODO: DB 조회 후 교체
//        dao.findAll();
        List<ProgramResponseDTO> dtos = getService().getProgramList();

        for(ProgramResponseDTO dto : dtos) {
            model.addRow(new Object[]{
                    dto.getProgramId(),
                    false,
                    dto.getName(),
                    dto.getType(),
                    dto.getReservationTime(),
                    "-",
                    "-",
                    dto.getStatus()
            });
        }
        // MOCK: 개발용 목 데이터
//        model.addRow(new Object[]{false, "스피닝 A반",    "SPINNING",  "2025-04-01 ~ 2025-09-30", "20", "15", "ACTIVE"});
        setTotalCount(model.getRowCount());
    }

    @Override
    protected void onRowDoubleClick(int modelRow) {
        Object[] data = getRowData(modelRow);
        new ProgramDetailDialog(parentFrame(), data, getService()).setVisible(true);
    }

    private void onAdd() {
        ProgramFormDialog dialog = new ProgramFormDialog(parentFrame());
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            String[] v = dialog.getValues();
            model.addRow(new Object[]{false, v[0], v[1], v[2] + " ~ " + v[3], v[4], "0", "ACTIVE"});
            HToast.success(parentFrame(), "프로그램이 등록되었습니다.");
        }
    }

    private void onDelete() {
        int[] rows = getCheckedRows();
        if (rows.length == 0) return;

        // 예약이 있는 프로그램 삭제 불가 체크
        for (int r : rows) {
            String booked = (String) getDataValue(r, 4);
            if (!"0".equals(booked)) {
                HDialog.alert(parentFrame(), "삭제 불가",
                    "예약자가 존재하는 프로그램은 삭제할 수 없습니다.\n해당 프로그램의 예약을 먼저 취소하세요.",
                    HDialog.Type.WARNING);
                return;
            }
        }

        if (HDialog.confirmDanger(parentFrame(), "프로그램 삭제",
                "선택한 " + rows.length + "개의 프로그램을 삭제하시겠습니까?\n연결된 스케줄 및 세션도 함께 삭제됩니다.")) {
            // TODO: DELETE FROM programs WHERE id IN (...)
            for (int i = rows.length - 1; i >= 0; i--) model.removeRow(rows[i]);
            HToast.success(parentFrame(), rows.length + "개가 삭제되었습니다.");
        }
    }

    private void onBulkIssueMembership() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            HDialog.info(parentFrame(), "회원권을 일괄 발급할 프로그램을 선택하세요.");
            return;
        }
        int modelRow   = table.convertRowIndexToModel(viewRow);
        String program = (String) getDataValue(modelRow, 0);
        String booked  = (String) getDataValue(modelRow, 4);

        if (HDialog.confirm(parentFrame(), "회원권 일괄 발급",
                "[" + program + "]\n" +
                "CONFIRMED 상태 예약자 " + booked + "명에게 회원권을 발급하시겠습니까?\n" +
                "발급되는 회원권: " + program + " 수강권")) {
            // TODO: INSERT INTO memberships (user_id, program_id, name, type, total_count,
            //                               remaining_count, status, issued_at)
            //       SELECT r.user_id, ?, ?, 'COUNT', ?, ?, 'ACTIVE', NOW()
            //       FROM reservations r
            //       WHERE r.session_id IN (SELECT id FROM sessions WHERE program_id = ?)
            //         AND r.status = 'CONFIRMED'
            //       UPDATE reservations SET status = 'MEMBERSHIP_ISSUED', membership_id = ...

            HToast.success(parentFrame(), booked + "명에게 회원권이 발급되었습니다.");
        }
    }

    private ProgramService getService() {
        if (service == null) {
            service = new ProgramService();
        }
        return service;
    }
}
