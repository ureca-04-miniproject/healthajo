package healthajo.users;

import healthajo.jdbc.core.Page;
import healthajo.jdbc.table.TUser;
import healthajo.jdbc.table.TMembership;
import healthajo.jdbc.table.TPrograms;
import healthajo.jdbc.table.TReservation;
import healthajo.jdbc.table.TSessions;
import healthajo.jdbc.core.Record;
import java.sql.Timestamp;
import java.util.List;
import healthajo.jdbc.core.Field;

public class UsersDAO {

    public static final TUser T = TUser.USER;
    private static final TMembership MEMBERSHIP = TMembership.MEMBERSHIP;
    private static final TPrograms PROGRAMS = TPrograms.PROGRAMS;
    private static final TReservation RESERVATION = TReservation.RESERVATION;
    private static final TSessions SESSIONS = TSessions.SESSIONS;

    public List<Record> findAll() {
        return T.select()
                .where(T.DELETED_AT.isNull())
                .orderBy(T.ID)
                .fetch();
    }

    public Page<Record> findAllWithStats(int pageNumber, int pageSize) {
        return Page.of(
                T.select(
                                T.ID,
                                T.NAME,
                                T.PHONE,
                                T.EMAIL,
                                T.CREATED_AT,
                                field("(SELECT COALESCE(SUM(m.remaining_count), 0) " +
                                        "FROM memberships m WHERE m.user_id = users.id) AS membership_count")
                        )
                        .where(T.DELETED_AT.isNull())
                        .orderBy(T.ID),
                pageNumber,
                pageSize
        );
    }
    private static Field field(String sql) {
        return () -> sql;
    }

    public List<Record> search(String keyword) {
        return T.select()
                .where(T.DELETED_AT.isNull()
                        .and(T.NAME.like("%" + keyword + "%")
                                .or(T.PHONE.like("%" + keyword + "%"))))
                .orderBy(T.ID)
                .fetch();
    }

    public Record findById(Long id) {
        return T.select()
                .where(T.ID.eq(id))
                .fetchOne();
    }

    public Record findByPhone(String phone) {
        return T.select()
                .where(T.DELETED_AT.isNull()
                        .and(T.PHONE.eq(phone)))
                .fetchOne();
    }

    public Record findActiveAdminByCodeHash(String codeHash) {
        return T.select()
                .where(T.DELETED_AT.isNull()
                        .and(T.ROLE.eq("ADMIN"))
                        .and(T.LOGIN_CODE_HASH.eq(codeHash)))
                .fetchOne();
    }

    public int insert(String name, String phone, String email, String role) {
        return T.insertInto()
                .set(T.NAME, name)
                .set(T.PHONE, phone)
                .set(T.EMAIL, email)
                .set(T.ROLE, role)
                .set(T.UPDATED_AT, new Timestamp(System.currentTimeMillis()))
                .execute();
    }

    public void update(Long id, String name, String phone, String email) {
        T.update()
                .set(T.NAME, name)
                .set(T.PHONE, phone)
                .set(T.EMAIL, email)
                .set(T.UPDATED_AT, new Timestamp(System.currentTimeMillis()))
                .where(T.ID.eq(id))
                .execute();
    }

    /**
     * 특정 사용자의 회원권 목록 (프로그램명 조인).
     * 컬럼: membership_name, program_name, total_count, remaining_count, status, issued_at
     */
    public List<Record> findMembershipsWithProgramByUserId(Long userId) {
        return MEMBERSHIP.select(
                        MEMBERSHIP.NAME.as("membership_name"),
                        PROGRAMS.NAME.as("program_name"),
                        MEMBERSHIP.TOTAL_COUNT,
                        MEMBERSHIP.REMAINING_COUNT,
                        MEMBERSHIP.STATUS,
                        MEMBERSHIP.ISSUED_AT
                )
                .join(PROGRAMS).on(MEMBERSHIP.PROGRAM_ID.eq(PROGRAMS.ID))
                .where(MEMBERSHIP.USER_ID.eq(userId))
                .orderByDesc(MEMBERSHIP.ISSUED_AT)
                .fetch();
    }

    /**
     * 특정 사용자의 예약 이력 (프로그램·세션 조인).
     * 컬럼: program_name, session_date, status, attendance_status, reserved_at
     */
    public List<Record> findReservationsByUserId(Long userId) {
        return RESERVATION.select(
                        PROGRAMS.NAME.as("program_name"),
                        SESSIONS.SESSION_DATE,
                        RESERVATION.STATUS,
                        RESERVATION.ATTENDANCE_STATUS,
                        RESERVATION.RESERVED_AT
                )
                .join(SESSIONS).on(RESERVATION.SESSION_ID.eq(SESSIONS.ID))
                .join(PROGRAMS).on(RESERVATION.PROGRAM_ID.eq(PROGRAMS.ID))
                .where(RESERVATION.USER_ID.eq(userId))
                .orderByDesc(SESSIONS.SESSION_DATE)
                .fetch();
    }

    public void delete(Long id) {
        T.update()
                .set(T.DELETED_AT, new Timestamp(System.currentTimeMillis()))
                .set(T.UPDATED_AT, new Timestamp(System.currentTimeMillis()))
                .where(T.ID.eq(id))
                .execute();
    }

}
