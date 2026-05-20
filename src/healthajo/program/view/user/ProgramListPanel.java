package healthajo.program.view.user;

import healthajo.component.HButton;
import healthajo.component.HDialog;
import healthajo.jdbc.core.Page;
import healthajo.program.application.ProgramApplication;
import healthajo.program.domain.ProgramSummary;
import healthajo.template.BaseListPanel;

import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.TableColumnModel;

import static javax.swing.SwingConstants.CENTER;

public class ProgramListPanel extends BaseListPanel {

    private static final ProgramApplication APP = new ProgramApplication();

    private Long userId;
    private final List<ProgramSummary> programs = new ArrayList<>();

    public ProgramListPanel(Long userId) {
        super();  // loadData() 조기 리턴 (programs == null)
        this.userId = userId;
        model.setRowCount(0);
        loadData();
    }

    @Override protected String  pageTitle()         { return "프로그램 예약"; }
    @Override protected boolean hasCheckbox()       { return false; }
    @Override protected String  searchPlaceholder() { return "프로그램명 또는 종목 검색"; }
    @Override protected boolean serverSideSearch()  { return true; }

    @Override
    protected String[] columnNames() {
        return new String[]{"프로그램명", "종목", "예약 기간", "잔여석", "상태"};
    }

    @Override
    protected List<? extends JComponent> toolbarButtons() {
        HButton reserveBtn = HButton.primary("예약하기", HButton.Size.SM);
        reserveBtn.addActionListener(e -> onReserve());
        return List.of(reserveBtn);
    }

    @Override
    protected void configureColumns(TableColumnModel cm) {
        int[] widths = {160, 90, 220, 70, 80};
        for (int i = 0; i < widths.length; i++) cm.getColumn(i).setPreferredWidth(widths[i]);
        table.setKoreanBadgeRenderer(4);
        table.setCellAlignment(CENTER);
        table.setHeaderAlignment(CENTER);
    }

    @Override
    protected void loadData() {
        if (programs == null) return;  // super() 호출 시점
        programs.clear();
        try {
            Page<ProgramSummary> page = APP.findAll(currentPage - 1, pageSize, searchKeyword);
            for (ProgramSummary p : page.getContent()) {
                programs.add(p);
                model.addRow(new Object[]{
                    p.name(), p.category(), p.reservationPeriod(),
                    p.remaining(), "ACTIVE"
                });
            }
            setTotalCount((int) page.getTotalCount());
        } catch (RuntimeException ex) {
            HDialog.error(parentFrame(), "프로그램 목록을 불러오지 못했습니다.\n" + ex.getMessage());
            setTotalCount(0);
        }
    }

    @Override
    protected void onRowDoubleClick(int modelRow) {
        openReserveDialog(modelRow);
    }

    private void onReserve() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            HDialog.info(parentFrame(), "예약할 프로그램을 선택하세요.");
            return;
        }
        openReserveDialog(table.convertRowIndexToModel(viewRow));
    }

    private void openReserveDialog(int modelRow) {
        if (modelRow >= programs.size()) return;
        new ProgramReserveDialog(parentFrame(), userId, programs.get(modelRow)).setVisible(true);
    }
}
