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
                    false,                       // [0] 체크박스 (HTable.setCheckboxColumn(0)이 토글하는 자리)
                    dto.getProgramId(),          // [1] ID (숨김 컬럼, 체크박스 클릭과 무관하게 보존)
                    dto.getName(),               // [2] 프로그램명
                    dto.getType(),               // [3] 종목
                    dto.getReservationTime(),    // [4] 예약 가능 기간
                    "-",                         // [5] 총 정원
                    "-",                         // [6] 예약 수
                    dto.getStatus()              // [7] 상태
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
        if (!dialog.isSaved()) return;
        try {
            int capacity = Integer.parseInt(dialog.getCapacity());
            getService().createProgram(
                    dialog.getName(), dialog.getCategory(), dialog.getDescription(),
                    dialog.getResStart(), dialog.getResEnd(), dialog.getCancelDeadline(),
                    dialog.getOpStart(), dialog.getOpEnd(),
                    capacity,
                    dialog.getWeekdayRows()
            );
            reload();
            HToast.success(parentFrame(), "프로그램이 등록되었습니다.");
        } catch (NumberFormatException ex) {
            HDialog.error(parentFrame(), "기본 정원은 숫자로 입력하세요.");
        } catch (Exception ex) {
            ex.printStackTrace();   // IDE 콘솔에 전체 스택 출력
            // 다이얼로그에는 원인 메시지(SQLException 등)를 끝까지 펼쳐서 표시
            StringBuilder msg = new StringBuilder(String.valueOf(ex.getMessage()));
            Throwable c = ex.getCause();
            while (c != null) {
                msg.append("\n원인: ").append(c.getMessage());
                c = c.getCause();
            }
            HDialog.error(parentFrame(), "프로그램 등록 실패\n" + msg);
        }
    }

    private void onDelete() {
        int[] rows = getCheckedRows();
        if (rows.length == 0) return;

        if (!HDialog.confirmDanger(parentFrame(), "프로그램 삭제",
                "선택한 " + rows.length + "개의 프로그램을 삭제하시겠습니까?\n" +
                "세션이 등록된 프로그램은 삭제되지 않습니다.")) return;

        int success = 0;
        int blocked = 0;
        for (int r : rows) {
            // model[0]은 체크박스 자리(toggle되어 Boolean), ID는 model[1]에 보존됨
            long programId = Long.parseLong(model.getValueAt(r, 1).toString());
            try {
                if (getService().deleteProgram(programId)) success++;
                else                                       blocked++;
            } catch (Exception ex) {
                blocked++;
            }
        }
        reload();

        if (blocked > 0) {
            HDialog.alert(parentFrame(), "일부 삭제 불가",
                    "세션이 등록된 프로그램은 삭제되지 않았습니다.\n" +
                    "성공 " + success + "건, 차단 " + blocked + "건",
                    HDialog.Type.WARNING);
        } else {
            HToast.success(parentFrame(), success + "개가 삭제되었습니다.");
        }
    }

    private void reload() {
        model.setRowCount(0);
        loadData();
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
