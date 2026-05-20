package healthajo.program.view.admin;

import healthajo.component.HButton;
import healthajo.component.HDialog;
import healthajo.component.HLabel;
import healthajo.component.HTabPanel;
import healthajo.component.HTable;
import healthajo.component.HToast;
import healthajo.component.theme.AppTheme;
import healthajo.jdbc.core.Record;
import healthajo.programs.dto.ProgramResponseDTO;
import healthajo.programs.dto.ScheduleListItemDTO;
import healthajo.programs.dto.SessionListItemDTO;
import healthajo.programs.service.ProgramService;
import healthajo.schedule.view.admin.ScheduleFormDialog;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;



/**
 * 프로그램 상세 다이얼로그.
 *
 * 탭:
 *   1) 기본 정보 — 프로그램 속성
 *   2) 스케줄    — FIXED_WEEKLY / FREE_SLOT / EVENT 스케줄 목록 및 관리
 *   3) 세션      — 날짜별 세션 목록, 강사, 상태
 *   4) 예약자    — 전체 예약자 목록
 */
public class ProgramDetailDialog extends JDialog {

    private ProgramService service;
    private final String programName;
    private final Long programId;

    public ProgramDetailDialog(JFrame parent, Object[] data, ProgramService service) {
        super(parent, "프로그램 상세", true);
        this.service = service;
        // data: [0]=checkbox, [1]=programId, [2]=name, [3]=type, [4]=reservationTime, [5]=총정원, [6]=예약수, [7]=상태
        this.programId = Long.parseLong(data[1].toString());
        this.programName = data[2].toString();

        setLayout(new BorderLayout());
        setSize(720, 600);
        setLocationRelativeTo(parent);
        setResizable(true);
        getContentPane().setBackground(AppTheme.SURFACE);

        // ── 헤더 ─────────────────────────────────────────────────────────────────
        JPanel header = new JPanel();
        header.setBackground(AppTheme.SURFACE);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(AppTheme.SP_4, AppTheme.SP_6, AppTheme.SP_4, AppTheme.SP_6));

        JLabel title = HLabel.h2(data[2].toString());
        JLabel sub   = HLabel.small("종목: " + data[3] + "  |  예약 기간: " + data[4]);
        title.setAlignmentX(LEFT_ALIGNMENT);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(2));
        header.add(sub);

        JPanel headerBorder = new JPanel(new BorderLayout());
        headerBorder.setBackground(AppTheme.SURFACE);
        headerBorder.setBorder(new MatteBorder(0, 0, 1, 0, AppTheme.BORDER));
        headerBorder.add(header, BorderLayout.CENTER);

        // ── 탭 ───────────────────────────────────────────────────────────────────
        HTabPanel tabs = new HTabPanel();
        tabs.addTab("기본 정보", buildInfoTab(data));
        tabs.addTab("스케줄",    buildScheduleTab(parent));
        tabs.addTab("세션",      buildSessionTab());
        tabs.addTab("예약자",    buildReservationTab());

        // ── 푸터 ─────────────────────────────────────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SP_2, AppTheme.SP_3));
        footer.setBackground(AppTheme.SURFACE);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, AppTheme.BORDER));
        HButton closeBtn = HButton.secondary("닫기", HButton.Size.SM);
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);

        add(headerBorder, BorderLayout.NORTH);
        add(tabs,         BorderLayout.CENTER);
        add(footer,       BorderLayout.SOUTH);
    }

    // ── 기본 정보 탭 ─────────────────────────────────────────────────────────────

    private JPanel buildInfoTab(Object[] data) {
        JPanel p = new JPanel();
        p.setBackground(AppTheme.SURFACE);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(AppTheme.SP_5, AppTheme.SP_6, AppTheme.SP_5, AppTheme.SP_6));

        // TODO: SELECT p.*, ps.schedule_type, ps.start_date, ps.end_date, ps.default_capacity
        //       FROM programs p LEFT JOIN program_schedules ps ON ps.program_id = p.id
        //       WHERE p.id = ?

//        service.getProgramWithSchedules(Integer.parseInt(data[0].toString()));

        addInfo(p, "프로그램명",    data[2].toString());
        addInfo(p, "종목",          data[3].toString());
        addInfo(p, "예약 가능 기간", data[4].toString());
        addInfo(p, "총 정원",       data[5].toString());
        addInfo(p, "현재 예약 수",  data[6].toString());
        addInfo(p, "상태",          data[7].toString());

        JScrollPane sp = new JScrollPane(p);
        sp.setBorder(null);
        sp.getViewport().setBackground(AppTheme.SURFACE);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(AppTheme.SURFACE);
        wrapper.add(sp);
        return wrapper;
    }

    // ── 스케줄 탭 ───────────────────────────────────────────────────────────────

    private JPanel buildScheduleTab(JFrame parent) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(AppTheme.BG);

        DefaultTableModel m = new DefaultTableModel(
            new String[]{"유형", "시작일", "종료일", "기본 정원", "요일 수"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        // 행 인덱스 ↔ schedule_id 매핑 (CRUD 시 PK 조회용)
        final java.util.List<Long> scheduleIds = new java.util.ArrayList<>();

        // 목록 새로고침 (생성/수정/삭제 후 재호출)
        Runnable reload = () -> {
            m.setRowCount(0);
            scheduleIds.clear();
            for (ScheduleListItemDTO dto : service.findListItemsByProgramId(programId)) {
                scheduleIds.add(dto.getId());
                m.addRow(new Object[]{
                        "FIXED_WEEKLY",
                        dto.getClassStartDate(),
                        dto.getClassEndDate(),
                        dto.getCapacity(),
                        dto.getWeekdayCount()
                });
            }
        };
        reload.run();

        HTable sTable = new HTable(m);
        sTable.setBadgeRenderer(0);
        sTable.getColumnModel().getColumn(0).setPreferredWidth(110);
        sTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        sTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        sTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        sTable.getColumnModel().getColumn(4).setPreferredWidth(60);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SP_2, AppTheme.SP_2));
        toolbar.setBackground(AppTheme.BG);

        HButton addBtn    = HButton.primary("스케줄 추가",   HButton.Size.SM);
        HButton editBtn   = HButton.secondary("편집",       HButton.Size.SM);
        HButton deleteBtn = HButton.danger("삭제",          HButton.Size.SM);

        addBtn.addActionListener(e -> {
            ScheduleFormDialog dlg = new ScheduleFormDialog(parent, null, programName);
            dlg.setVisible(true);
            if (!dlg.isSaved()) return;
            try {
                Object[] vals = dlg.getValues();
                // vals = [programName, "FIXED_WEEKLY", startDate, endDate, capacity, weekdayCount]
                int cap = Integer.parseInt(vals[4].toString());
                service.createSchedule(programId,
                        vals[2].toString(), vals[3].toString(),
                        cap, dlg.getWeekdayRows());
                reload.run();
                HToast.success(parent, "스케줄이 추가되었습니다.");
            } catch (Exception ex) {
                HDialog.error(parent, "스케줄 추가 실패: " + ex.getMessage());
            }
        });

        editBtn.addActionListener(e -> {
            int viewRow = sTable.getSelectedRow();
            if (viewRow < 0) { HDialog.info(parent, "편집할 스케줄을 선택하세요."); return; }
            int mr = sTable.convertRowIndexToModel(viewRow);
            long scheduleId = scheduleIds.get(mr);

            Object[] rowData = new Object[]{
                programName,
                m.getValueAt(mr, 0), m.getValueAt(mr, 1),
                m.getValueAt(mr, 2), m.getValueAt(mr, 3), m.getValueAt(mr, 4)
            };
            java.util.List<Object[]> weekdayRows = service.getWeekdayRowsByScheduleId(scheduleId);
            ScheduleFormDialog dlg = new ScheduleFormDialog(parent, rowData, programName, weekdayRows);
            dlg.setVisible(true);
            if (!dlg.isSaved()) return;
            try {
                Object[] vals = dlg.getValues();
                int cap = Integer.parseInt(vals[4].toString());
                service.updateSchedule(scheduleId,
                        vals[2].toString(), vals[3].toString(),
                        cap, dlg.getWeekdayRows());
                reload.run();
                HToast.success(parent, "스케줄이 수정되었습니다.");
            } catch (Exception ex) {
                HDialog.error(parent, "스케줄 수정 실패: " + ex.getMessage());
            }
        });

        deleteBtn.addActionListener(e -> {
            int viewRow = sTable.getSelectedRow();
            if (viewRow < 0) { HDialog.info(parent, "삭제할 스케줄을 선택하세요."); return; }
            int mr   = sTable.convertRowIndexToModel(viewRow);
            long scheduleId = scheduleIds.get(mr);
            String type = String.valueOf(m.getValueAt(mr, 0));

            if (!HDialog.confirmDanger(parent, "스케줄 삭제",
                    "[" + programName + " / " + type + "] 스케줄을 삭제하시겠습니까?\n" +
                    "요일별 설정도 함께 삭제됩니다.")) return;

            try {
                boolean deleted = service.deleteSchedule(scheduleId);
                if (deleted) {
                    reload.run();
                    HToast.success(parent, "스케줄이 삭제되었습니다.");
                } else {
                    HDialog.alert(parent, "삭제 불가",
                            "해당 스케줄로 생성된 세션이 있어 삭제할 수 없습니다.\n세션을 먼저 정리하세요.",
                            HDialog.Type.WARNING);
                }
            } catch (Exception ex) {
                HDialog.error(parent, "스케줄 삭제 실패: " + ex.getMessage());
            }
        });

        toolbar.add(deleteBtn);
        toolbar.add(editBtn);
        toolbar.add(addBtn);
        p.add(toolbar,            BorderLayout.NORTH);
        p.add(sTable.inScrollPane(), BorderLayout.CENTER);
        return p;
    }

    // ── 세션 탭 ──────────────────────────────────────────────────────────────────

    private JPanel buildSessionTab() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(AppTheme.BG);

        DefaultTableModel m = new DefaultTableModel(
            new String[]{"날짜", "시작", "종료", "정원", "예약 수", "강사", "상태"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        List<SessionListItemDTO> sessions = service.findSessionListByProgramId(programId);
        for (SessionListItemDTO dto : sessions) {
            m.addRow(new Object[]{
                    dto.getSessionDate(),
                    dto.getStartTime(),
                    dto.getEndTime(),
                    String.valueOf(dto.getCapacity()),
                    String.valueOf(dto.getBookedCount()),
                    dto.getInstructorName(),
                    dto.getStatus()
            });
        }

        HTable sTable = new HTable(m);
        sTable.setBadgeRenderer(6);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SP_2, AppTheme.SP_2));
        toolbar.setBackground(AppTheme.BG);

        HButton assignBtn = HButton.secondary("강사 배정", HButton.Size.SM);
        HButton cancelBtn = HButton.danger("세션 취소",   HButton.Size.SM);

        assignBtn.addActionListener(e -> {
            int row = sTable.getSelectedRow();
            if (row < 0) { HDialog.info((JFrame) getOwner(), "세션을 선택하세요."); return; }
            // TODO: 강사 목록 조회 → JComboBox 선택 → UPDATE sessions SET instructor_id = ?
            HDialog.info((JFrame) getOwner(), "강사 배정 기능은 구현 예정입니다.");
        });

        cancelBtn.addActionListener(e -> {
            int viewRow = sTable.getSelectedRow();
            if (viewRow < 0) { HDialog.info((JFrame) getOwner(), "세션을 선택하세요."); return; }
            int mr     = sTable.convertRowIndexToModel(viewRow);
            String date   = (String) m.getValueAt(mr, 0);
            String booked = (String) m.getValueAt(mr, 4);
            if (HDialog.confirmDanger((JFrame) getOwner(), "세션 취소",
                    "[" + date + "] 세션을 취소하시겠습니까?\n" +
                    "예약자 " + booked + "명의 예약이 자동 취소되고 횟수가 복구됩니다.")) {
                // TODO: UPDATE sessions SET status='CANCELLED' WHERE id=?
                //       UPDATE reservations SET status='CANCELLED' WHERE session_id=?
                m.setValueAt("CANCELLED", mr, 6);
                HToast.success((JFrame) getOwner(), "세션이 취소되었습니다.");
            }
        });

        toolbar.add(assignBtn);
        toolbar.add(cancelBtn);
        p.add(toolbar,            BorderLayout.NORTH);
        p.add(sTable.inScrollPane(), BorderLayout.CENTER);
        return p;
    }

    // ── 예약자 탭 ────────────────────────────────────────────────────────────────

    private JPanel buildReservationTab() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(AppTheme.BG);

        DefaultTableModel m = new DefaultTableModel(
            new String[]{"이름", "전화번호", "예약 상태", "출석 상태", "예약일"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        // MOCK
        // TODO: SELECT u.name, u.phone, r.status, r.attendance_status,
        //              DATE_FORMAT(r.reserved_at,'%Y-%m-%d')
        //       FROM reservations r JOIN users u ON u.id = r.user_id
        //       JOIN sessions s ON s.id = r.session_id
        //       WHERE s.program_id = ? AND r.status != 'CANCELLED'
        m.addRow(new Object[]{"홍길동", "010-1234-5678", "CONFIRMED",        "PENDING",  "2025-05-01"});
        m.addRow(new Object[]{"김영희", "010-2345-6789", "MEMBERSHIP_ISSUED", "ATTENDED", "2025-04-20"});

        HTable rTable = new HTable(m);
        rTable.setBadgeRenderer(2);
        rTable.setBadgeRenderer(3);

        p.add(rTable.inScrollPane(), BorderLayout.CENTER);
        return p;
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────────────────

    private static void addInfo(JPanel p, String label, String value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, AppTheme.SP_2, AppTheme.SP_1));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        JLabel lbl = HLabel.label(label + ":");
        lbl.setPreferredSize(new Dimension(120, 20));
        row.add(lbl);
        row.add(HLabel.body(value));
        p.add(row);
    }
}
