package healthajo.session.dao;

import healthajo.jdbc.core.Condition;
import healthajo.jdbc.core.Field;
import healthajo.jdbc.core.Record;
import healthajo.session.domain.SessionSummary;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static healthajo.jdbc.table.TInstructors.INSTRUCTORS;
import static healthajo.jdbc.table.TReservation.RESERVATION;
import static healthajo.jdbc.table.TSessions.SESSIONS;

public class SessionDao {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private static final Field REMAINING_FIELD =
        () -> "(sessions.capacity - sessions.booked_count) AS remaining";

    public List<SessionSummary> findUpcomingByProgramId(Long programId) {
        List<Record> records = SESSIONS.select(
                SESSIONS.ID,
                SESSIONS.SESSION_DATE,
                SESSIONS.START_TIME,
                SESSIONS.END_TIME,
                REMAINING_FIELD,
                INSTRUCTORS.NAME.as("instructor_name")
            )
            .leftJoin(INSTRUCTORS).on(SESSIONS.INSTRUCTOR_ID.eq(INSTRUCTORS.ID))
            .where(
                SESSIONS.PROGRAM_ID.eq(programId)
                    .and(Condition.raw("sessions.session_date >= CURDATE()"))
                    .and(SESSIONS.STATUS.eq("OPEN"))
            )
            .orderBy(SESSIONS.SESSION_DATE, SESSIONS.START_TIME)
            .fetch();

        List<SessionSummary> result = new ArrayList<>();
        for (Record r : records) result.add(toSessionSummary(r));
        return result;
    }

    /** 특정 사용자의 CONFIRMED 예약 슬롯을 "date|start|end" Set으로 반환 — 시간대 중복 체크용. */
    public Set<String> findConfirmedSlotsByUserId(Long userId) {
        List<Record> records = SESSIONS.select(
                SESSIONS.SESSION_DATE,
                SESSIONS.START_TIME,
                SESSIONS.END_TIME
            )
            .join(RESERVATION).on(RESERVATION.SESSION_ID.eq(SESSIONS.ID))
            .where(
                RESERVATION.USER_ID.eq(userId)
                    .and(RESERVATION.STATUS.eq("CONFIRMED"))
            )
            .fetch();

        Set<String> slots = new HashSet<>();
        for (Record r : records) {
            String date  = toDateStr(r.get("session_date"));
            String start = toTimeStr(r.get("start_time"));
            String end   = toTimeStr(r.get("end_time"));
            if (date != null && !date.isEmpty()) slots.add(date + "|" + start + "|" + end);
        }
        return slots;
    }

    // ── 변환 ─────────────────────────────────────────────────────────────────

    private SessionSummary toSessionSummary(Record r) {
        Long   id         = toLong(r.get("sessions.id"));
        String date       = toDateStr(r.get("sessions.session_date"));
        String start      = toTimeStr(r.get("sessions.start_time"));
        String end        = toTimeStr(r.get("sessions.end_time"));
        Object remRaw     = r.get("remaining");
        int    remaining  = remRaw instanceof Number n ? n.intValue() : 0;
        Object instrRaw   = r.get("instructor_name");
        String instructor = instrRaw instanceof String s ? s : "-";
        return new SessionSummary(id, date, start, end, remaining, instructor);
    }

    private static Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Number n) return n.longValue();
        return null;
    }

    private static String toDateStr(Object val) {
        if (val == null) return "";
        if (val instanceof java.sql.Date d)
            return d.toLocalDate().format(DATE_FMT);
        if (val instanceof java.time.LocalDate ld)
            return ld.format(DATE_FMT);
        if (val instanceof java.time.LocalDateTime ldt)
            return ldt.format(DATE_FMT);
        if (val instanceof java.sql.Timestamp ts)
            return ts.toLocalDateTime().format(DATE_FMT);
        String s = val.toString();
        return s.length() >= 10 ? s.substring(0, 10) : s;
    }

    private static String toTimeStr(Object val) {
        if (val == null) return "";
        if (val instanceof java.sql.Time t) return t.toLocalTime().format(TIME_FMT);
        if (val instanceof java.time.LocalTime lt) return lt.format(TIME_FMT);
        String s = val.toString();
        return s.length() >= 5 ? s.substring(0, 5) : s;
    }
}
