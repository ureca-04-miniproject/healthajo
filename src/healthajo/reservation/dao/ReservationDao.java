package healthajo.reservation.dao;

import healthajo.jdbc.core.Condition;
import healthajo.jdbc.core.Page;
import healthajo.jdbc.core.Record;
import healthajo.jdbc.core.SelectStep;
import healthajo.reservation.domain.Reservation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static healthajo.jdbc.table.TPrograms.PROGRAMS;
import static healthajo.jdbc.table.TReservation.RESERVATION;
import static healthajo.jdbc.table.TSessions.SESSIONS;
import static healthajo.jdbc.table.TUser.USER;

public class ReservationDao {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public long insert(Long userId, Long sessionId, Long programId) {
        return RESERVATION.insertInto()
            .set(RESERVATION.USER_ID, userId)
            .set(RESERVATION.SESSION_ID, sessionId)
            .set(RESERVATION.PROGRAM_ID, programId)
            .set(RESERVATION.STATUS, "CONFIRMED")
            .set(RESERVATION.ATTENDANCE_STATUS, "PENDING")
            .executeAndReturnKey();
    }

    public Page<Reservation> findAll(int pageNumber, int pageSize) {
        SelectStep step = buildBaseSelect()
            .orderByDesc(RESERVATION.RESERVED_AT);
        return Page.of(step, pageNumber, pageSize).map(this::toReservation);
    }

    public Page<Reservation> findByUserId(Long userId, int pageNumber, int pageSize, String keyword) {
        Condition where = RESERVATION.USER_ID.eq(userId);
        if (keyword != null && !keyword.isBlank()) {
            where = where.and(searchClause(keyword, "reservations.status", true));
        }
        SelectStep step = buildBaseSelect()
            .where(where)
            .orderByDesc(SESSIONS.SESSION_DATE, RESERVATION.RESERVED_AT);
        return Page.of(step, pageNumber, pageSize).map(this::toReservation);
    }

    public Page<Reservation> findAttendancesByUserId(Long userId, int pageNumber, int pageSize, String keyword) {
        Condition where = RESERVATION.USER_ID.eq(userId)
            .and(RESERVATION.STATUS.ne("CANCELLED"));
        if (keyword != null && !keyword.isBlank()) {
            where = where.and(searchClause(keyword, "reservations.attendance_status", false));
        }
        SelectStep step = buildBaseSelect()
            .where(where)
            .orderByDesc(SESSIONS.SESSION_DATE, RESERVATION.RESERVED_AT);
        return Page.of(step, pageNumber, pageSize).map(this::toReservation);
    }

    // ── 검색 조건 (프로그램명·날짜·시간·상태 전 컬럼) ──────────────────────────

    /** 상태 코드 → 배지에 표시되는 한글 라벨. 한글로도 상태 검색이 되도록 한다. */
    private static final java.util.Map<String, String> STATUS_KO = java.util.Map.of(
        "CONFIRMED", "예약 확정", "MEMBERSHIP_ISSUED", "회원권 사용", "CANCELLED", "취소",
        "PENDING", "미처리", "ATTENDED", "출석", "ABSENT", "결석");

    private Condition searchClause(String keyword, String statusColumn, boolean withReservedAt) {
        String kw   = keyword.trim();
        String like = "%" + kw + "%";
        Condition c = PROGRAMS.NAME.like(like)
            .or(Condition.raw("DATE_FORMAT(sessions.session_date, '%Y-%m-%d') LIKE ?", like))
            .or(Condition.raw("DATE_FORMAT(sessions.start_time, '%H:%i') LIKE ?", like))
            .or(Condition.raw("DATE_FORMAT(sessions.end_time, '%H:%i') LIKE ?", like))
            .or(Condition.raw(statusColumn + " LIKE ?", like));
        if (withReservedAt) {
            c = c.or(Condition.raw("DATE_FORMAT(reservations.reserved_at, '%Y-%m-%d %H:%i') LIKE ?", like));
        }
        String nkw = kw.replace(" ", "");
        for (var e : STATUS_KO.entrySet()) {
            if (e.getValue().replace(" ", "").contains(nkw)) {
                c = c.or(Condition.raw(statusColumn + " = ?", e.getKey()));
            }
        }
        return c;
    }

    public void checkIn(Long reservationId) {
        RESERVATION.update()
            .set(RESERVATION.ATTENDANCE_STATUS, "ATTENDED")
            .set(RESERVATION.ATTENDED_AT, LocalDateTime.now())
            .where(RESERVATION.ID.eq(reservationId))
            .execute();
    }

    public void cancel(Long reservationId, String cancelledBy) {
        RESERVATION.update()
            .set(RESERVATION.STATUS, "CANCELLED")
            .set(RESERVATION.CANCELLED_BY, cancelledBy)
            .set(RESERVATION.CANCELLED_AT, LocalDateTime.now())
            .where(RESERVATION.ID.eq(reservationId))
            .execute();
    }

    public void incrementSessionBookedCount(Long sessionId) {
        Record session = SESSIONS.select(SESSIONS.BOOKED_COUNT)
            .where(SESSIONS.ID.eq(sessionId))
            .fetchOne();
        if (session == null) return;
        Object raw = session.get("booked_count");
        if (!(raw instanceof Number n)) return;
        SESSIONS.update()
            .set(SESSIONS.BOOKED_COUNT, n.intValue() + 1)
            .where(SESSIONS.ID.eq(sessionId))
            .execute();
    }

    public void decrementSessionBookedCount(Long sessionId) {
        Record session = SESSIONS.select(SESSIONS.BOOKED_COUNT)
            .where(SESSIONS.ID.eq(sessionId))
            .fetchOne();
        if (session == null) return;
        Object raw = session.get("booked_count");
        if (!(raw instanceof Number n)) return;
        int current = n.intValue();
        if (current > 0) {
            SESSIONS.update()
                .set(SESSIONS.BOOKED_COUNT, current - 1)
                .where(SESSIONS.ID.eq(sessionId))
                .execute();
        }
    }

    // ── 공통 SELECT 구성 ──────────────────────────────────────────────────────

    private SelectStep buildBaseSelect() {
        return RESERVATION.select(
                RESERVATION.ID,
                USER.ID.as("user_id"),
                USER.NAME.as("user_name"),
                USER.PHONE.as("user_phone"),
                SESSIONS.ID.as("session_id"),
                SESSIONS.SESSION_DATE,
                SESSIONS.START_TIME,
                SESSIONS.END_TIME,
                PROGRAMS.ID.as("program_id"),
                PROGRAMS.NAME.as("program_name"),
                RESERVATION.MEMBERSHIP_ID,
                RESERVATION.STATUS,
                RESERVATION.CANCELLED_BY,
                RESERVATION.RESERVED_AT,
                RESERVATION.CANCELLED_AT,
                RESERVATION.ATTENDANCE_STATUS,
                RESERVATION.ATTENDED_AT
            )
            .join(USER).on(RESERVATION.USER_ID.eq(USER.ID))
            .join(PROGRAMS).on(RESERVATION.PROGRAM_ID.eq(PROGRAMS.ID))
            .join(SESSIONS).on(RESERVATION.SESSION_ID.eq(SESSIONS.ID));
    }

    // ── Record → Reservation ─────────────────────────────────────────────────

    private Reservation toReservation(Record r) {
        return new Reservation(
            toLong(r.get("id")),
            toLong(r.get("user_id")),
            (String)  r.get("user_name"),
            (String)  r.get("user_phone"),
            toLong(r.get("session_id")),
            toLocalDateTime(r.get("session_date")),
            toTimeStr(r.get("start_time")),
            toTimeStr(r.get("end_time")),
            toLong(r.get("program_id")),
            (String)  r.get("program_name"),
            toLong(r.get("membership_id")),
            (String)  r.get("status"),
            (String)  r.get("cancelled_by"),
            toLocalDateTime(r.get("reserved_at")),
            toLocalDateTime(r.get("cancelled_at")),
            (String)  r.get("attendance_status"),
            toLocalDateTime(r.get("attended_at"))
        );
    }

    // ── 타입 변환 유틸 ────────────────────────────────────────────────────────

    private static Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Number n) return n.longValue();
        return null;
    }

    private static LocalDateTime toLocalDateTime(Object val) {
        if (val == null) return null;
        if (val instanceof LocalDateTime ldt) return ldt;
        if (val instanceof java.sql.Timestamp ts) return ts.toLocalDateTime();
        if (val instanceof java.sql.Date d) return d.toLocalDate().atStartOfDay();
        return null;
    }

    private String toTimeStr(Object val) {
        if (val == null) return "";
        if (val instanceof java.sql.Time t) return t.toLocalTime().format(TIME_FMT);
        if (val instanceof java.time.LocalTime lt) return lt.format(TIME_FMT);
        String s = val.toString();
        return s.length() >= 5 ? s.substring(0, 5) : s;
    }
}
