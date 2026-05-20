package healthajo.programs.dao;

import healthajo.jdbc.core.Condition;
import healthajo.jdbc.core.Field;
import healthajo.jdbc.core.Page;
import healthajo.jdbc.core.Record;
import healthajo.jdbc.core.SelectStep;
import healthajo.jdbc.factory.JdbcConnectionFactory;
import healthajo.jdbc.table.TInstructors;
import healthajo.jdbc.table.TReservation;
import healthajo.jdbc.table.TSessions;
import healthajo.jdbc.table.TUser;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.util.List;

public class SessionDAO {
    private static final TSessions    S = TSessions.SESSIONS;
    private static final TInstructors I = TInstructors.INSTRUCTORS;
    private static final TReservation R = TReservation.RESERVATION;
    private static final TUser        U = TUser.USER;

    // 세션 1건 생성 (스케줄로부터 자동 생성). booked_count·status·attendance_closed는 DB 기본값 사용.
    public long insert(long programId, long scheduleId, Date sessionDate,
                       Time startTime, Time endTime, int capacity) {
        return S.insertInto()
                .set(S.PROGRAM_ID,   programId)
                .set(S.SCHEDULE_ID,  scheduleId)
                .set(S.SESSION_TYPE, "FIXED")
                .set(S.SESSION_DATE, sessionDate)
                .set(S.START_TIME,   startTime)
                .set(S.END_TIME,     endTime)
                .set(S.CAPACITY,     capacity)
                .executeAndReturnKey();
    }

    // 활성 강사 목록 (강사 배정 콤보용)
    public List<Record> findActiveInstructors() {
        return I.select(I.ID, I.NAME)
                .where(I.STATUS.eq("ACTIVE").and(Condition.raw("instructors.deleted_at IS NULL")))
                .orderBy(I.NAME)
                .fetch();
    }

    public int assignInstructor(long sessionId, long instructorId) {
        return S.update()
                .set(S.INSTRUCTOR_ID, instructorId)
                .where(S.ID.eq(sessionId))
                .execute();
    }

    // 프로그램 ID로 예약자 목록 (취소 제외) — reservations.program_id(역정규화)로 직접 필터
    public List<Record> findReservationsByProgramId(long programId) {
        return R.select(
                        U.NAME.as("user_name"),
                        U.PHONE.as("user_phone"),
                        R.STATUS,
                        R.ATTENDANCE_STATUS,
                        R.RESERVED_AT
                )
                .join(U).on(R.USER_ID.eq(U.ID))
                .where(R.PROGRAM_ID.eq(programId).and(R.STATUS.ne("CANCELLED")))
                .orderByDesc(R.RESERVED_AT)
                .fetch();
    }

    // 프로그램 ID로 예약자 목록 (취소 제외) — 페이지네이션
    public Page<Record> findReservationsByProgramId(long programId, int pageNumber, int pageSize) {
        SelectStep step = R.select(
                        U.NAME.as("user_name"),
                        U.PHONE.as("user_phone"),
                        R.STATUS,
                        R.ATTENDANCE_STATUS,
                        R.RESERVED_AT
                )
                .join(U).on(R.USER_ID.eq(U.ID))
                .where(R.PROGRAM_ID.eq(programId).and(R.STATUS.ne("CANCELLED")))
                .orderByDesc(R.RESERVED_AT);
        return Page.of(step, pageNumber, pageSize);
    }

    /**
     * 세션 취소 (cascade) — 한 커넥션에서 순차 처리.
     * 1) 회원권 사용 예약의 잔여 횟수 복구  2) 예약 취소  3) 세션 취소.
     */
    public void cancelSessionCascade(long sessionId) {
        String restoreSql =
                "UPDATE memberships m " +
                "JOIN reservations r ON r.membership_id = m.id " +
                "SET m.remaining_count = m.remaining_count + 1, m.status = 'ACTIVE', m.updated_at = NOW() " +
                "WHERE r.session_id = ? AND r.status <> 'CANCELLED' AND r.membership_id IS NOT NULL";
        String cancelReservationsSql =
                "UPDATE reservations SET status = 'CANCELLED', cancelled_by = 'ADMIN', cancelled_at = NOW() " +
                "WHERE session_id = ? AND status <> 'CANCELLED'";
        String cancelSessionSql =
                "UPDATE sessions SET status = 'CANCELLED' WHERE id = ?";
        try (JdbcConnectionFactory.JdbcConnection jc = JdbcConnectionFactory.getInstance().getConnection()) {
            Connection conn = jc.get();
            exec(conn, restoreSql, sessionId);
            exec(conn, cancelReservationsSql, sessionId);
            exec(conn, cancelSessionSql, sessionId);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("세션 취소 실패", e);
        }
    }

    private static void exec(Connection conn, String sql, long sessionId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sessionId);
            ps.executeUpdate();
        }
    }

    // 프로그램 ID로 세션 목록 조회 (강사명 LEFT JOIN)
    public List<Record> findListItemsByProgramId(long programId) {
        return S.select(
                        S.ID, S.SESSION_DATE, S.START_TIME, S.END_TIME,
                        S.CAPACITY, S.BOOKED_COUNT, S.STATUS,
                        I.NAME
                )
                .leftJoin(I).on(I.ID.eq(S.INSTRUCTOR_ID))
                .where(S.PROGRAM_ID.eq(programId))
                .orderBy(S.SESSION_DATE, S.START_TIME)
                .fetch();
    }

    // 프로그램 ID로 세션 목록 조회 (강사명 LEFT JOIN) — 페이지네이션
    public Page<Record> findListItemsByProgramId(long programId, int pageNumber, int pageSize) {
        SelectStep step = S.select(
                        S.ID, S.SESSION_DATE, S.START_TIME, S.END_TIME,
                        S.CAPACITY, S.BOOKED_COUNT, S.STATUS,
                        I.NAME
                )
                .leftJoin(I).on(I.ID.eq(S.INSTRUCTOR_ID))
                .where(S.PROGRAM_ID.eq(programId))
                .orderBy(S.SESSION_DATE, S.START_TIME);
        return Page.of(step, pageNumber, pageSize);
    }

    // 스케줄 ID로 세션 개수 (스케줄 삭제 전 체크용)
    public int countByScheduleId(long scheduleId) {
        Field cnt = () -> "COUNT(*) AS cnt";
        Record r = S.select(cnt).where(S.SCHEDULE_ID.eq(scheduleId)).fetchOne();
        if (r == null) return 0;
        Object v = r.get("cnt");
        return v == null ? 0 : ((Number) v).intValue();
    }

    // 프로그램 ID로 세션 개수 (프로그램 삭제 전 체크용)
    public int countByProgramId(long programId) {
        Field cnt = () -> "COUNT(*) AS cnt";
        Record r = S.select(cnt).where(S.PROGRAM_ID.eq(programId)).fetchOne();
        if (r == null) return 0;
        Object v = r.get("cnt");
        return v == null ? 0 : ((Number) v).intValue();
    }

    // 스케줄의 세션을 참조하는 예약 수 (스케줄 삭제 가능 여부 판단)
    public long countReservationsByScheduleId(long scheduleId) {
        return R.select().join(S).on(R.SESSION_ID.eq(S.ID))
                .where(S.SCHEDULE_ID.eq(scheduleId))
                .fetchCount();
    }

    // 스케줄의 모든 세션 삭제 (예약이 없을 때만 호출해야 FK 안전)
    public int deleteByScheduleId(long scheduleId) {
        return S.delete().where(S.SCHEDULE_ID.eq(scheduleId)).execute();
    }

    // 스케줄의 예약 없는 세션만 삭제 (스케줄 편집 시 재생성 전 정리)
    public int deleteReservationFreeByScheduleId(long scheduleId) {
        return S.delete()
                .where(Condition.raw(
                        "schedule_id = ? AND id NOT IN (SELECT session_id FROM reservations)",
                        scheduleId))
                .execute();
    }

    // 스케줄에 남아있는 세션의 "yyyy-MM-dd|HH:mm:ss|HH:mm:ss" 키 집합 (재생성 시 중복 방지용)
    public java.util.Set<String> findSessionKeysByScheduleId(long scheduleId) {
        java.util.Set<String> keys = new java.util.HashSet<>();
        for (Record r : S.select(S.SESSION_DATE, S.START_TIME, S.END_TIME)
                .where(S.SCHEDULE_ID.eq(scheduleId)).fetch()) {
            keys.add(String.valueOf(r.get("session_date")) + "|"
                    + String.valueOf(r.get("start_time")) + "|"
                    + String.valueOf(r.get("end_time")));
        }
        return keys;
    }

    // 프로그램의 CONFIRMED 예약자 user_id 목록 (중복 제거) — 회원권 일괄 발급용
    public java.util.List<Long> findConfirmedReserverUserIds(long programId) {
        java.util.List<Long> ids = new java.util.ArrayList<>();
        for (Record r : R.select(R.USER_ID)
                .join(S).on(R.SESSION_ID.eq(S.ID))
                .where(S.PROGRAM_ID.eq(programId).and(R.STATUS.eq("CONFIRMED")))
                .groupBy(R.USER_ID)
                .fetch()) {
            Object v = r.get("user_id");
            if (v instanceof Number n) ids.add(n.longValue());
        }
        return ids;
    }

    // 특정 프로그램·회원의 CONFIRMED 예약을 MEMBERSHIP_ISSUED로 전환 + membership_id 연결
    public int markReservationsMembershipIssued(long programId, long userId, long membershipId) {
        return R.update()
                .set(R.STATUS, "MEMBERSHIP_ISSUED")
                .set(R.MEMBERSHIP_ID, membershipId)
                .where(Condition.raw("program_id = ? AND user_id = ? AND status = 'CONFIRMED'",
                        programId, userId))
                .execute();
    }
}
