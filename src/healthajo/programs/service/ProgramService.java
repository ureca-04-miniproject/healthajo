package healthajo.programs.service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import healthajo.jdbc.core.Field;
import healthajo.jdbc.core.Record;
import healthajo.jdbc.table.TInstructors;
import healthajo.jdbc.table.TProgramSchedules;
import healthajo.jdbc.table.TPrograms;
import healthajo.jdbc.table.TScheduleWeekdays;
import healthajo.jdbc.table.TSessions;
import healthajo.programs.dao.ProgramDAO;
import healthajo.programs.dao.ScheduleDAO;
import healthajo.programs.dao.ScheduleWeekdayDAO;
import healthajo.programs.dao.SessionDAO;
import healthajo.programs.dto.ProgramResponseDTO;
import healthajo.programs.dto.ScheduleListItemDTO;
import healthajo.programs.dto.SessionListItemDTO;
import healthajo.programs.entity.Schedule;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class ProgramService {
    private final ProgramDAO programDAO = new ProgramDAO();
    private final ScheduleDAO scheduleDAO = new ScheduleDAO();
    private final ScheduleWeekdayDAO weekdayDAO = new ScheduleWeekdayDAO();
    private final SessionDAO sessionDAO = new SessionDAO();

    private static final String[] WEEKDAY_NAMES = {"월", "화", "수", "목", "금", "토", "일"};
    private static final Map<String, Integer> WEEKDAY_KOR_TO_INT = Map.of(
            "월", 0, "화", 1, "수", 2, "목", 3, "금", 4, "토", 5, "일", 6
    );

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final TPrograms P = TPrograms.PROGRAMS;
    private static final TSessions S = TSessions.SESSIONS;
    private static final TProgramSchedules PS = TProgramSchedules.PROGRAM_SCHEDULES;
    private static final TScheduleWeekdays SW = TScheduleWeekdays.SCHEDULE_WEEKDAYS;
    private static final TInstructors I = TInstructors.INSTRUCTORS;

    public List<ProgramResponseDTO> getProgramList() {
        return programDAO.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public Record getProgramWithSchedules(int id) {
        return programDAO.findByProgramID(id);
    }

    public List<ScheduleListItemDTO> findListItemsByProgramId(long programId) {
        return scheduleDAO.findListItemsByProgramId(programId).stream().map(this::toScheduleListItemDTO).toList();
    }

    // 프로그램 상세 — 세션 탭 목록 조회
    public List<SessionListItemDTO> findSessionListByProgramId(long programId) {
        return sessionDAO.findListItemsByProgramId(programId).stream()
                .map(this::toSessionListItemDTO)
                .toList();
    }

    private SessionListItemDTO toSessionListItemDTO(Record record) {
        SessionListItemDTO dto = new SessionListItemDTO();
        dto.setId(record.get(S.ID));

        java.sql.Date sessionDate = record.get(S.SESSION_DATE);
        java.sql.Time startTime   = record.get(S.START_TIME);
        java.sql.Time endTime     = record.get(S.END_TIME);

        dto.setSessionDate(sessionDate == null ? "" : sessionDate.toString());
        dto.setStartTime(toHourMinute(startTime));
        dto.setEndTime(toHourMinute(endTime));
        dto.setCapacity(record.get(S.CAPACITY));
        dto.setBookedCount(record.get(S.BOOKED_COUNT));

        String name = record.get(I.NAME);
        dto.setInstructorName(name == null ? "-" : name);
        dto.setStatus(record.get(S.STATUS));
        return dto;
    }

    // java.sql.Time → "HH:mm" 포맷 (toString()이 "HH:mm:ss"로 떨어지므로 잘라냄)
    private static String toHourMinute(java.sql.Time t) {
        if (t == null) return "";
        String s = t.toString();
        return s.length() >= 5 ? s.substring(0, 5) : s;
    }

    // ── 스케줄 CRUD ─────────────────────────────────────────────────────────────

    // 편집 다이얼로그에 채워줄 요일 행 데이터 (한글 요일 + "HH:mm" 시간 + 정원 문자열)
    public List<Object[]> getWeekdayRowsByScheduleId(long scheduleId) {
        List<Object[]> rows = new ArrayList<>();
        for (Record r : weekdayDAO.findByScheduleId(scheduleId)) {
            Integer day = r.get(SW.WEEK_DAY);
            Time start  = r.get(SW.CLASS_START_TIME);
            Time end    = r.get(SW.CLASS_END_TIME);
            Integer cap = r.get(SW.CAPACITY);
            rows.add(new Object[]{
                    day == null || day < 0 || day >= WEEKDAY_NAMES.length ? "월" : WEEKDAY_NAMES[day],
                    toHourMinute(start),
                    toHourMinute(end),
                    cap == null ? "" : cap.toString()
            });
        }
        return rows;
    }

    // 스케줄 신규 생성 (program_schedules + schedule_weekdays N건)
    public long createSchedule(long programId, String startDateStr, String endDateStr,
                               int defaultCapacity, List<Object[]> weekdayRows) {
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate   = LocalDate.parse(endDateStr);
        long scheduleId = scheduleDAO.insertSimple(programId, startDate, endDate, defaultCapacity);
        insertWeekdayRows(scheduleId, weekdayRows);
        return scheduleId;
    }

    // 스케줄 수정 — weekdays는 전체 삭제 후 재삽입
    public void updateSchedule(long scheduleId, String startDateStr, String endDateStr,
                               int defaultCapacity, List<Object[]> weekdayRows) {
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate   = LocalDate.parse(endDateStr);
        scheduleDAO.updateSimple(scheduleId, startDate, endDate, defaultCapacity);
        weekdayDAO.deleteByScheduleId(scheduleId);
        insertWeekdayRows(scheduleId, weekdayRows);
    }

    // 스케줄 삭제 — 연결된 세션이 있으면 false 반환 (UI에서 안내)
    public boolean deleteSchedule(long scheduleId) {
        if (sessionDAO.countByScheduleId(scheduleId) > 0) return false;
        weekdayDAO.deleteByScheduleId(scheduleId);
        scheduleDAO.delete(scheduleId);
        return true;
    }

    // ── 프로그램 CRUD ───────────────────────────────────────────────────────────

    /**
     * 프로그램 + 기본 스케줄 + weekdays를 한 번에 생성.
     * 폼이 DATE만 받으므로 시각은 보충(예약 시작 00:00, 종료 23:59:59).
     * cancellation_open_at은 예약 시작과 동일, cancellation_close_at은 사용자가 입력한 취소 마감일.
     */
    public long createProgram(
            String name, String category, String description,
            String resStartStr, String resEndStr, String cancelDeadlineStr,
            String opStartStr, String opEndStr, int capacity,
            List<Object[]> weekdayRows
    ) {
        LocalDateTime resOpenAt    = LocalDate.parse(resStartStr).atStartOfDay();
        LocalDateTime resCloseAt   = LocalDate.parse(resEndStr).atTime(LocalTime.of(23, 59, 59));
        LocalDateTime cancelOpenAt = resOpenAt;
        LocalDateTime cancelCloseAt = LocalDate.parse(cancelDeadlineStr).atTime(LocalTime.of(23, 59, 59));

        healthajo.programs.entity.Program program = healthajo.programs.entity.Program.toCreate(
                name, description, category,
                resOpenAt, resCloseAt,
                cancelOpenAt, cancelCloseAt
        );
        long programId = programDAO.insert(program);

        createSchedule(programId, opStartStr, opEndStr, capacity, weekdayRows);
        return programId;
    }

    /**
     * 프로그램 삭제 (soft delete).
     * 연결된 세션이 있으면 false 반환 — 스케줄/요일 데이터는 보존.
     */
    public boolean deleteProgram(long programId) {
        if (sessionDAO.countByProgramId(programId) > 0) return false;
        programDAO.softDelete(programId);
        return true;
    }

    private void insertWeekdayRows(long scheduleId, List<Object[]> weekdayRows) {
        if (weekdayRows == null) return;
        for (Object[] row : weekdayRows) {
            String dayKor = row[0] == null ? "" : row[0].toString();
            Integer dayInt = WEEKDAY_KOR_TO_INT.get(dayKor);
            if (dayInt == null) continue;
            Time startTime = parseHourMinute(row[1] == null ? "" : row[1].toString());
            Time endTime   = parseHourMinute(row[2] == null ? "" : row[2].toString());
            if (startTime == null || endTime == null) continue;
            Integer cap = parseIntOrNull(row[3] == null ? "" : row[3].toString());
            weekdayDAO.insert(scheduleId, dayInt, startTime, endTime, cap);
        }
    }

    // "HH:mm" → java.sql.Time
    private static Time parseHourMinute(String hhmm) {
        try { return Time.valueOf(LocalTime.parse(hhmm.trim())); }
        catch (Exception e) { return null; }
    }

    private static Integer parseIntOrNull(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return null; }
    }

    private ScheduleListItemDTO toScheduleListItemDTO(Record record) {
        Long id = record.get(PS.ID);
        Date startDate  = record.get(PS.START_DATE);
        Date endDate = record.get(PS.END_DATE);

        Integer capacity = record.get(PS.DEFAULT_CAPACITY);
        // Record.mapResults가 컬럼 라벨을 소문자로 정규화 저장하므로 "weekday_count" 키.
        // 집계 결과는 BIGINT(Long)로 올 수 있어 Number 캐스팅이 안전.
        Object countVal = record.get("weekday_count");
        Integer weekdayCount = countVal == null ? 0 : ((Number) countVal).intValue();

        ScheduleListItemDTO dto = new ScheduleListItemDTO();
        dto.setId(id);
        dto.setClassStartDate(startDate);
        dto.setClassEndDate(endDate);
        dto.setCapacity(capacity);
        dto.setWeekdayCount(weekdayCount);
        return dto;
    }

    private ProgramResponseDTO toResponseDTO(Record record) {
        Long id = record.get(P.ID);
        LocalDateTime openAt  = record.get(P.RESERVATION_OPEN_AT);
        LocalDateTime closeAt = record.get(P.RESERVATION_CLOSE_AT);

        Integer totalCapacity = record.get(S.CAPACITY);
        Integer sessionCount  = record.get(S.BOOKED_COUNT);

        ProgramResponseDTO dto = new ProgramResponseDTO();
        dto.setProgramId(record.get(P.ID));
        dto.setName(record.get(P.NAME));
        dto.setType(record.get(P.CATEGORY));
        dto.setReservationTime(formatPeriod(openAt, closeAt));
        dto.setStudentCount(totalCapacity == null ? 0 : totalCapacity);
        dto.setSessionCount(sessionCount == null ? 0 : sessionCount);
//        dto.setStatus(record.get(S.STATUS)
        dto.setStatus("-");
        return dto;
    }

    private String formatPeriod(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) return "-";
        return from.format(DATE_FMT) + " ~ " + to.format(DATE_FMT);
    }
}
