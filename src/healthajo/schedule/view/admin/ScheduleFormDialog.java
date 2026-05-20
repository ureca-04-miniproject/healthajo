package healthajo.schedule.view.admin;

import healthajo.component.HButton;
import healthajo.component.HDatePicker;
import healthajo.component.HDialog;
import healthajo.component.HFormGroup;
import healthajo.component.HLabel;
import healthajo.component.HTextField;
import healthajo.component.theme.AppTheme;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;

/**
 * 스케줄 등록 / 수정 다이얼로그 (FIXED_WEEKLY 단일 유형).
 *
 * fixedProgram != null 이면 프로그램 필드를 고정(비활성화).
 * 프로그램 상세 화면의 스케줄 탭에서 호출.
 *
 * 저장 후 getValues():
 *   [0] 프로그램명  [1] "FIXED_WEEKLY"  [2] 시작일  [3] 종료일  [4] 정원  [5] 요일 수
 */
public class ScheduleFormDialog extends JDialog {

    private static final String[] WEEKDAY_NAMES = {"월", "화", "수", "목", "금", "토", "일"};

    private boolean  saved = false;
    private Object[] savedValues;

    private HTextField     programField;
    private HDatePicker    startDatePicker;
    private HDatePicker    endDatePicker;
    private HTextField     capacityField;
    private DefaultTableModel weekdayModel;
    private JTable         weekdayTable;

    public ScheduleFormDialog(JFrame parent, Object[] data) {
        this(parent, data, null, null);
    }

    public ScheduleFormDialog(JFrame parent, Object[] data, String fixedProgram) {
        this(parent, data, fixedProgram, null);
    }

    public ScheduleFormDialog(JFrame parent, Object[] data, String fixedProgram, List<Object[]> weekdayRows) {
        super(parent, data == null ? "스케줄 추가" : "스케줄 편집", true);
        setSize(620, 560);
        setResizable(false);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(AppTheme.SURFACE);
        setLayout(new BorderLayout());

        add(buildForm(data, fixedProgram, weekdayRows), BorderLayout.CENTER);
        add(buildFooter(),                              BorderLayout.SOUTH);
    }

    // ── Form ──────────────────────────────────────────────────────────────────

    private JPanel buildForm(Object[] data, String fixedProgram, List<Object[]> weekdayRows) {
        JPanel form = new JPanel();
        form.setBackground(AppTheme.SURFACE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(AppTheme.SP_5, AppTheme.SP_6, AppTheme.SP_4, AppTheme.SP_6));

        // ── 프로그램명 ──────────────────────────────────────────────────────────
        // TODO: programs 테이블 name 컬럼 표시 (fixedProgram 파라미터로 전달받음)
        programField = new HTextField("프로그램명 입력");
        programField.setMaximumSize(new Dimension(Short.MAX_VALUE, 44));
        if (fixedProgram != null) {
            programField.setText(fixedProgram);
            programField.setEditable(false);
            programField.setForeground(AppTheme.TEXT_MUTED);
        }
        HFormGroup pgGroup = new HFormGroup("프로그램명", programField);
        pgGroup.setAlignmentX(LEFT_ALIGNMENT);

        // ── 운영 기간 ───────────────────────────────────────────────────────────
        // TODO: program_schedules.start_date, end_date
        JPanel dateRow = new JPanel(new GridLayout(1, 2, AppTheme.SP_4, 0));
        dateRow.setOpaque(false);
        dateRow.setAlignmentX(LEFT_ALIGNMENT);
        dateRow.setMaximumSize(new Dimension(Short.MAX_VALUE, 80));

        startDatePicker = new HDatePicker("시작일");
        startDatePicker.setMaximumSize(new Dimension(Short.MAX_VALUE, 44));
        endDatePicker   = new HDatePicker("종료일");
        endDatePicker.setMaximumSize(new Dimension(Short.MAX_VALUE, 44));
        dateRow.add(new HFormGroup("시작일", startDatePicker));
        dateRow.add(new HFormGroup("종료일", endDatePicker));

        // ── 기본 정원 ───────────────────────────────────────────────────────────
        // TODO: program_schedules.default_capacity
        capacityField = new HTextField("예: 20");
        capacityField.setMaximumSize(new Dimension(Short.MAX_VALUE, 44));
        HFormGroup capGroup = new HFormGroup("기본 정원", capacityField);
        capGroup.setAlignmentX(LEFT_ALIGNMENT);

        // ── 요일별 시간 설정 (FIXED_WEEKLY 고정) ─────────────────────────────────
        JPanel weekdaySection = buildWeekdaySection();
        weekdaySection.setAlignmentX(LEFT_ALIGNMENT);

        form.add(pgGroup);
        form.add(Box.createVerticalStrut(AppTheme.SP_4));
        form.add(dateRow);
        form.add(Box.createVerticalStrut(AppTheme.SP_4));
        form.add(capGroup);
        form.add(Box.createVerticalStrut(AppTheme.SP_4));
        form.add(weekdaySection);

        // ── 편집 모드 데이터 채우기 ──────────────────────────────────────────────
        if (data != null) {
            // data: [program, type, startDate, endDate, capacity, weekdayCount]
            if (fixedProgram == null && data.length > 0)
                programField.setText(data[0].toString());
            if (data.length > 2) {
                try { startDatePicker.setDate(LocalDate.parse(data[2].toString(), HDatePicker.FMT)); }
                catch (Exception ignored) {}
            }
            if (data.length > 3) {
                try { endDatePicker.setDate(LocalDate.parse(data[3].toString(), HDatePicker.FMT)); }
                catch (Exception ignored) {}
            }
            if (data.length > 4) capacityField.setText(data[4].toString());

            // 외부에서 전달받은 요일별 시간 데이터 (Service에서 조회한 schedule_weekdays)
            if (weekdayRows != null) {
                for (Object[] row : weekdayRows) {
                    weekdayModel.addRow(row);
                }
            }
        }

        return form;
    }

    // ── 요일별 시간 설정 패널 ────────────────────────────────────────────────────

    private JPanel buildWeekdaySection() {
        JPanel panel = new JPanel(new BorderLayout(0, AppTheme.SP_2));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Short.MAX_VALUE, 290));
        panel.setPreferredSize(new Dimension(0, 290));

        // ── 헤더: 제목 + 버튼 ──────────────────────────────────────────────────
        HButton addRowBtn    = HButton.ghost("+ 요일 추가", HButton.Size.SM);
        HButton removeRowBtn = HButton.ghost("− 행 삭제",   HButton.Size.SM);

        JPanel header = new JPanel(new BorderLayout(AppTheme.SP_2, 0));
        header.setOpaque(false);

        JLabel title = HLabel.label("요일별 시간 설정");
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SP_2, 0));
        btnBar.setOpaque(false);
        btnBar.add(removeRowBtn);
        btnBar.add(addRowBtn);

        header.add(title,  BorderLayout.WEST);
        header.add(btnBar, BorderLayout.EAST);

        // ── 테이블 ─────────────────────────────────────────────────────────────
        // MOCK: 아래 컬럼은 schedule_weekdays 테이블과 매핑
        // TODO: weekday         → schedule_weekdays.weekday     (VARCHAR '월'~'일')
        //       시작 시간        → TIME_FORMAT(start_time,'%H:%i')
        //       종료 시간        → TIME_FORMAT(end_time,  '%H:%i')
        //       정원             → schedule_weekdays.capacity  (NULL이면 기본 정원 상속)
        weekdayModel = new DefaultTableModel(
            new String[]{"요일", "시작 시간", "종료 시간", "정원"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return true; }
        };

        weekdayTable = new JTable(weekdayModel);
        weekdayTable.setRowHeight(38);
        weekdayTable.setBackground(AppTheme.SURFACE);
        weekdayTable.setFont(AppTheme.BODY_SM);
        weekdayTable.setFillsViewportHeight(true);
        weekdayTable.setShowGrid(false);
        weekdayTable.setIntercellSpacing(new Dimension(0, 1));
        weekdayTable.setSelectionBackground(AppTheme.ROW_SELECTED);
        weekdayTable.setBorder(null);

        weekdayTable.getTableHeader().setFont(AppTheme.LABEL);
        weekdayTable.getTableHeader().setBackground(AppTheme.SURFACE_RAISED);
        weekdayTable.getTableHeader().setForeground(AppTheme.TEXT_MUTED);
        weekdayTable.getTableHeader().setPreferredSize(new Dimension(0, 38));
        weekdayTable.getTableHeader().setReorderingAllowed(false);

        // 요일 컬럼: JComboBox 편집기
        JComboBox<String> weekdayCb = new JComboBox<>(WEEKDAY_NAMES);
        weekdayTable.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(weekdayCb));
        weekdayTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        weekdayTable.getColumnModel().getColumn(1).setPreferredWidth(130);
        weekdayTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        weekdayTable.getColumnModel().getColumn(3).setPreferredWidth(90);

        JScrollPane scroll = new JScrollPane(weekdayTable);
        scroll.setPreferredSize(new Dimension(0, 210));
        scroll.setMinimumSize(new Dimension(0, 210));
        scroll.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER));
        scroll.getViewport().setBackground(AppTheme.SURFACE);

        // ── 버튼 액션 ──────────────────────────────────────────────────────────
        addRowBtn.addActionListener(e -> {
            if (weekdayModel.getRowCount() < 7) {
                weekdayModel.addRow(new Object[]{"월", "09:00", "10:00", "20"});
            }
        });
        removeRowBtn.addActionListener(e -> {
            int sel = weekdayTable.getSelectedRow();
            if (sel >= 0) {
                if (weekdayTable.isEditing()) weekdayTable.getCellEditor().stopCellEditing();
                weekdayModel.removeRow(sel);
            }
        });

        panel.add(header, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ── Footer ────────────────────────────────────────────────────────────────

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SP_2, AppTheme.SP_3));
        footer.setBackground(AppTheme.SURFACE);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, AppTheme.BORDER));

        HButton cancel  = HButton.ghost("취소",  HButton.Size.SM);
        HButton confirm = HButton.primary("저장", HButton.Size.SM);
        cancel.addActionListener(e -> dispose());
        confirm.addActionListener(e -> onSave());
        footer.add(cancel);
        footer.add(confirm);
        return footer;
    }

    // ── Save ──────────────────────────────────────────────────────────────────

    private void onSave() {
        // 편집 중인 셀이 있으면 먼저 완료
        if (weekdayTable != null && weekdayTable.isEditing())
            weekdayTable.getCellEditor().stopCellEditing();

        String program  = programField.getText().trim();
        String startDt  = startDatePicker.getText();
        String endDt    = endDatePicker.getText();
        String capacity = capacityField.getText().trim();

        if (program.isEmpty() || startDt.isEmpty() || endDt.isEmpty() || capacity.isEmpty()) {
            HDialog.error(getOwner() instanceof JFrame f ? f : null,
                "프로그램명, 기간, 정원은 필수 입력 항목입니다.");
            return;
        }
        try { Integer.parseInt(capacity); }
        catch (NumberFormatException ex) {
            HDialog.error(getOwner() instanceof JFrame f ? f : null, "정원은 숫자로 입력하세요.");
            return;
        }
        if (weekdayModel == null || weekdayModel.getRowCount() == 0) {
            HDialog.error(getOwner() instanceof JFrame f ? f : null,
                "요일별 시간을 1개 이상 등록해주세요.");
            return;
        }

        // TODO: INSERT / UPDATE program_schedules SET schedule_type='FIXED_WEEKLY',
        //             start_date=?, end_date=?, default_capacity=? WHERE id=?
        // TODO: DELETE FROM schedule_weekdays WHERE schedule_id=?
        //       INSERT INTO schedule_weekdays (schedule_id, weekday, start_time, end_time, capacity)
        //       VALUES ... (weekdayModel 각 행)
        savedValues = new Object[]{
            program,
            "FIXED_WEEKLY",   // 유형 고정
            startDt,
            endDt,
            capacity,
            String.valueOf(weekdayModel.getRowCount())
        };
        saved = true;
        dispose();
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public boolean  isSaved()   { return saved; }
    public Object[] getValues() { return savedValues; }

    // 저장 시점의 요일별 시간 입력 데이터 반환
    // 각 행: [weekday("월"~"일"), startTime("HH:mm"), endTime("HH:mm"), capacity(문자열)]
    public List<Object[]> getWeekdayRows() {
        List<Object[]> rows = new ArrayList<>();
        if (weekdayModel == null) return rows;
        for (int i = 0; i < weekdayModel.getRowCount(); i++) {
            rows.add(new Object[]{
                    weekdayModel.getValueAt(i, 0),
                    weekdayModel.getValueAt(i, 1),
                    weekdayModel.getValueAt(i, 2),
                    weekdayModel.getValueAt(i, 3)
            });
        }
        return rows;
    }
}
