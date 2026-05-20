package healthajo.attendance.dao;

import healthajo.attendance.domain.AttendanceSession;
import healthajo.attendance.domain.Attendee;
import healthajo.jdbc.core.Condition;
import healthajo.jdbc.core.Page;
import healthajo.jdbc.core.Record;
import healthajo.jdbc.core.SelectStep;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static healthajo.jdbc.table.TPrograms.PROGRAMS;
import static healthajo.jdbc.table.TReservation.RESERVATION;
import static healthajo.jdbc.table.TSessions.SESSIONS;
import static healthajo.jdbc.table.TUser.USER;

public class AttendanceDao {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DT_FMT   = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ── 조회 ──────────────────────────────────────────────────────────────────

    /** 선택한 날짜의 OPEN 세션 목록을 시작 시각순으로 페이지 조회. */
    public Page<AttendanceSession> findSessionsByDate(LocalDate date, int pageNumber, int pageSize) {
        return findSessionsByDate(date, pageNumber, pageSize, null);
    }

    /** 선택한 날짜의 OPEN 세션 목록 — 키워드(프로그램명) 서버 측 검색 지원. */
    public Page<AttendanceSession> findSessionsByDate(LocalDate date, int pageNumber, int pageSize, String keyword) {
        Condition where = Condition.raw("sessions.session_date = ?", java.sql.Date.valueOf(date))
            .and(SESSIONS.STATUS.eq("OPEN"));
        if (keyword != null && !keyword.isBlank()) {
            where = where.and(PROGRAMS.NAME.like("%" + keyword.trim() + "%"));
        }
        SelectStep step = SESSIONS.select(
                SESSIONS.ID.as("session_id"),
                PROGRAMS.NAME.as("program_name"),
                SESSIONS.SESSION_DATE,
                SESSIONS.START_TIME,
                SESSIONS.END_TIME,
                SESSIONS.BOOKED_COUNT,
                SESSIONS.CAPACITY,
                SESSIONS.ATTENDANCE_CLOSED
            )
            .join(PROGRAMS).on(SESSIONS.PROGRAM_ID.eq(PROGRAMS.ID))
            .where(where)
            .orderBy(SESSIONS.START_TIME);
        return Page.of(step, pageNumber, pageSize).map(this::toSession);
    }

    /** 세션의 (취소 제외) 수강자 목록을 이름순으로 조회. */
    public List<Attendee> findAttendees(Long sessionId) {
        List<Record> records = RESERVATION.select(
                RESERVATION.ID,
                USER.NAME.as("user_name"),
                USER.PHONE.as("user_phone"),
                RESERVATION.ATTENDANCE_STATUS,
                RESERVATION.ATTENDED_AT
            )
            .join(USER).on(RESERVATION.USER_ID.eq(USER.ID))
            .where(
                RESERVATION.SESSION_ID.eq(sessionId)
                    .and(RESERVATION.STATUS.ne("CANCELLED"))
            )
            .orderBy(USER.NAME)
            .fetch();

        List<Attendee> result = new ArrayList<>();
        for (Record r : records) result.add(toAttendee(r));
        return result;
    }

    // ── 출석 처리 ──────────────────────────────────────────────────────────────

    public void markAttended(Long reservationId) {
        RESERVATION.update()
            .set(RESERVATION.ATTENDANCE_STATUS, "ATTENDED")
            .set(RESERVATION.ATTENDED_AT, LocalDateTime.now())
            .where(RESERVATION.ID.eq(reservationId))
            .execute();
    }

    public void markAbsent(Long reservationId) {
        RESERVATION.update()
            .set(RESERVATION.ATTENDANCE_STATUS, "ABSENT")
            .set(RESERVATION.ATTENDED_AT, (LocalDateTime) null)
            .where(RESERVATION.ID.eq(reservationId))
            .execute();
    }

    public void revertToPending(Long reservationId) {
        RESERVATION.update()
            .set(RESERVATION.ATTENDANCE_STATUS, "PENDING")
            .set(RESERVATION.ATTENDED_AT, (LocalDateTime) null)
            .where(RESERVATION.ID.eq(reservationId))
            .execute();
    }

    public void closeAttendance(Long sessionId) {
        SESSIONS.update()
            .set(SESSIONS.ATTENDANCE_CLOSED, true)
            .set(SESSIONS.ATTENDANCE_CLOSED_AT, LocalDateTime.now())
            .where(SESSIONS.ID.eq(sessionId))
            .execute();
    }

    // ── Record → 도메인 변환 ────────────────────────────────────────────────────

    private AttendanceSession toSession(Record r) {
        return new AttendanceSession(
            toLong(r.get("session_id")),
            (String) r.get("program_name"),
            toDateStr(r.get("session_date")),
            toTimeStr(r.get("start_time")),
            toTimeStr(r.get("end_time")),
            toInt(r.get("booked_count")),
            toInt(r.get("capacity")),
            toBool(r.get("attendance_closed"))
        );
    }

    private Attendee toAttendee(Record r) {
        return new Attendee(
            toLong(r.get("id")),
            (String) r.get("user_name"),
            (String) r.get("user_phone"),
            (String) r.get("attendance_status"),
            toDateTimeStr(r.get("attended_at"))
        );
    }

    // ── 타입 변환 유틸 ──────────────────────────────────────────────────────────

    private static Long toLong(Object val) {
        if (val instanceof Number n) return n.longValue();
        return null;
    }

    private static int toInt(Object val) {
        if (val instanceof Number n) return n.intValue();
        return 0;
    }

    private static boolean toBool(Object val) {
        if (val instanceof Boolean b) return b;
        if (val instanceof Number n) return n.intValue() != 0;
        return false;
    }

    private static String toDateStr(Object val) {
        if (val == null) return "";
        if (val instanceof java.sql.Date d)         return d.toLocalDate().format(DATE_FMT);
        if (val instanceof java.time.LocalDate ld)  return ld.format(DATE_FMT);
        if (val instanceof java.sql.Timestamp ts)   return ts.toLocalDateTime().format(DATE_FMT);
        String s = val.toString();
        return s.length() >= 10 ? s.substring(0, 10) : s;
    }

    private static String toTimeStr(Object val) {
        if (val == null) return "";
        if (val instanceof java.sql.Time t)         return t.toLocalTime().format(TIME_FMT);
        if (val instanceof java.time.LocalTime lt)  return lt.format(TIME_FMT);
        String s = val.toString();
        return s.length() >= 5 ? s.substring(0, 5) : s;
    }

    private static String toDateTimeStr(Object val) {
        if (val == null) return "";
        if (val instanceof java.sql.Timestamp ts)        return ts.toLocalDateTime().format(DT_FMT);
        if (val instanceof java.time.LocalDateTime ldt)  return ldt.format(DT_FMT);
        String s = val.toString();
        return s.length() >= 16 ? s.substring(0, 16) : s;
    }
}
