package healthajo.memberships.dao;

import healthajo.jdbc.core.Record;
import healthajo.jdbc.core.Field;
import healthajo.jdbc.core.Column;
import healthajo.jdbc.core.Condition;
import healthajo.jdbc.core.TableBase;
import healthajo.jdbc.factory.JdbcConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static healthajo.jdbc.table.TMembership.MEMBERSHIP;
import static healthajo.jdbc.table.TUser.USER;

public class MembershipDAO {

    private static final ProgramRef PROGRAM = new ProgramRef();

    public void insertMembership(
            Long userId,
            Long programId,
            String name,
            int totalCount
    ) {
        if (userId == null) {
            throw new IllegalArgumentException("회원 ID가 필요합니다.");
        }

        if (programId == null) {
            throw new IllegalArgumentException("프로그램 ID가 필요합니다.");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("회원권명이 필요합니다.");
        }

        if (totalCount <= 0) {
            throw new IllegalArgumentException("총 횟수는 1 이상이어야 합니다.");
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());

        MEMBERSHIP.insertInto()
                .set(MEMBERSHIP.USER_ID, userId)
                .set(MEMBERSHIP.PROGRAM_ID, programId)
                .set(MEMBERSHIP.NAME, name)
                .set(MEMBERSHIP.TOTAL_COUNT, totalCount)
                .set(MEMBERSHIP.REMAINING_COUNT, totalCount)
                .set(MEMBERSHIP.STATUS, "ACTIVE")
                .set(MEMBERSHIP.ISSUED_AT, now)
                .set(MEMBERSHIP.CREATED_AT, now)
                .set(MEMBERSHIP.UPDATED_AT, now)
                .execute();
    }

    public List<Record> findAllMemberships() {
        return MEMBERSHIP.select(
                        MEMBERSHIP.ID,
                        MEMBERSHIP.USER_ID,
                        MEMBERSHIP.PROGRAM_ID,
                        MEMBERSHIP.NAME,
                        MEMBERSHIP.TOTAL_COUNT,
                        MEMBERSHIP.REMAINING_COUNT,
                        MEMBERSHIP.STATUS,
                        MEMBERSHIP.ISSUED_AT,
                        MEMBERSHIP.CREATED_AT,
                        MEMBERSHIP.UPDATED_AT
                )
                .orderByDesc(MEMBERSHIP.ID)
                .fetch();
    }

    public List<Record> findAllMembershipsWithUserAndProgram(int limit, int offset) {
        return MEMBERSHIP.select(
                        field(MEMBERSHIP.ID.getQualifiedName() + " AS membership_id"),
                        field(USER.NAME.getQualifiedName() + " AS user_name"),
                        field(PROGRAM.NAME.getQualifiedName() + " AS program_name"),
                        field(MEMBERSHIP.NAME.getQualifiedName() + " AS membership_name"),
                        field(MEMBERSHIP.TOTAL_COUNT.getQualifiedName() + " AS total_count"),
                        field(MEMBERSHIP.REMAINING_COUNT.getQualifiedName() + " AS remaining_count"),
                        field(MEMBERSHIP.STATUS.getQualifiedName() + " AS status"),
                        field(MEMBERSHIP.ISSUED_AT.getQualifiedName() + " AS issued_at")
                )
                .join(USER).on(MEMBERSHIP.USER_ID.eq(USER.ID))
                .join(PROGRAM).on(MEMBERSHIP.PROGRAM_ID.eq(PROGRAM.ID))
                .orderByDesc(MEMBERSHIP.ISSUED_AT)
                .limit(limit)
                .offset(offset * limit)
                .fetch();
    }

    private static Field field(String sql) {
        return () -> sql;
    }

    public long countAll() {
        return MEMBERSHIP.select().fetchCount();
    }

    private static final class ProgramRef extends TableBase {
        final Column<Long> ID = new Column<>(getPrefix(), "id", Long.class);
        final Column<String> NAME = new Column<>(getPrefix(), "name", String.class);

        ProgramRef() {
            super("programs");
        }
    }

    public Record findById(Long membershipId) {
        List<Record> result = MEMBERSHIP.select(
                        MEMBERSHIP.ID,
                        MEMBERSHIP.USER_ID,
                        MEMBERSHIP.PROGRAM_ID,
                        MEMBERSHIP.NAME,
                        MEMBERSHIP.TOTAL_COUNT,
                        MEMBERSHIP.REMAINING_COUNT,
                        MEMBERSHIP.STATUS,
                        MEMBERSHIP.ISSUED_AT,
                        MEMBERSHIP.CREATED_AT,
                        MEMBERSHIP.UPDATED_AT
                )
                .where(MEMBERSHIP.ID.eq(membershipId))
                .fetch();

        return result.isEmpty() ? null : result.get(0);
    }

    public List<Record> findMembershipsByUserId(Long userId) {
        return MEMBERSHIP.select(
                        MEMBERSHIP.ID,
                        MEMBERSHIP.USER_ID,
                        MEMBERSHIP.PROGRAM_ID,
                        MEMBERSHIP.NAME,
                        MEMBERSHIP.TOTAL_COUNT,
                        MEMBERSHIP.REMAINING_COUNT,
                        MEMBERSHIP.STATUS,
                        MEMBERSHIP.ISSUED_AT,
                        MEMBERSHIP.CREATED_AT,
                        MEMBERSHIP.UPDATED_AT
                )
                .where(MEMBERSHIP.USER_ID.eq(userId))
                .orderByDesc(MEMBERSHIP.ID)
                .fetch();
    }

    public List<Record> findMembershipsWithProgramByUserId(Long userId) {
        return MEMBERSHIP.select(
                        field(MEMBERSHIP.ID.getQualifiedName()             + " AS membership_id"),
                        field(MEMBERSHIP.NAME.getQualifiedName()           + " AS membership_name"),
                        field(PROGRAM.NAME.getQualifiedName()              + " AS program_name"),
                        field(MEMBERSHIP.TOTAL_COUNT.getQualifiedName()    + " AS total_count"),
                        field(MEMBERSHIP.REMAINING_COUNT.getQualifiedName()+ " AS remaining_count"),
                        field(MEMBERSHIP.STATUS.getQualifiedName()         + " AS status"),
                        field(MEMBERSHIP.ISSUED_AT.getQualifiedName()      + " AS issued_at")
                )
                .join(PROGRAM).on(MEMBERSHIP.PROGRAM_ID.eq(PROGRAM.ID))
                .where(MEMBERSHIP.USER_ID.eq(userId))
                .orderByDesc(MEMBERSHIP.ISSUED_AT)
                .fetch();
    }

    public Record findActiveByUserAndProgram(Long userId, Long programId) {
        List<Record> memberships = findMembershipsByUserId(userId);

        for (Record record : memberships) {
            Long recordProgramId = record.get(MEMBERSHIP.PROGRAM_ID);
            String status = record.get(MEMBERSHIP.STATUS);
            Integer remainingCount = record.get(MEMBERSHIP.REMAINING_COUNT);

            boolean sameProgram = programId != null && programId.equals(recordProgramId);
            boolean active = "ACTIVE".equals(status);
            boolean hasCount = remainingCount != null && remainingCount > 0;

            if (sameProgram && active && hasCount) {
                return record;
            }
        }

        return null;
    }

    public void updateMembership(
            Long membershipId,
            Long userId,
            Long programId,
            String name,
            int totalCount,
            int remainingCount
    ) {
        if (membershipId == null) {
            throw new IllegalArgumentException("회원권 ID가 필요합니다.");
        }

        if (remainingCount < 0) {
            throw new IllegalArgumentException("잔여 횟수는 0보다 작을 수 없습니다.");
        }

        String status = remainingCount > 0 ? "ACTIVE" : "EXPIRED";
        Timestamp now = new Timestamp(System.currentTimeMillis());

        MEMBERSHIP.update()
                .set(MEMBERSHIP.USER_ID, userId)
                .set(MEMBERSHIP.PROGRAM_ID, programId)
                .set(MEMBERSHIP.NAME, name)
                .set(MEMBERSHIP.TOTAL_COUNT, totalCount)
                .set(MEMBERSHIP.REMAINING_COUNT, remainingCount)
                .set(MEMBERSHIP.STATUS, status)
                .set(MEMBERSHIP.UPDATED_AT, now)
                .where(membershipIdEq(membershipId))
                .execute();
    }

    public void updateRemainingCount(Long membershipId, int remainingCount) {
        if (membershipId == null) {
            throw new IllegalArgumentException("회원권 ID가 필요합니다.");
        }

        if (remainingCount < 0) {
            throw new IllegalArgumentException("잔여 횟수는 0보다 작을 수 없습니다.");
        }

        String status = remainingCount > 0 ? "ACTIVE" : "EXPIRED";
        Timestamp now = new Timestamp(System.currentTimeMillis());

        String sql = "UPDATE memberships SET remaining_count = ?, status = ?, updated_at = ? WHERE id = ?";
        try (JdbcConnectionFactory.JdbcConnection jc = JdbcConnectionFactory.getInstance().getConnection()) {
            Connection conn = jc.get();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, remainingCount);
                ps.setString(2, status);
                ps.setTimestamp(3, now);
                ps.setLong(4, membershipId);
                int updated = ps.executeUpdate();
                if (updated == 0) {
                    throw new IllegalArgumentException("회원을 찾을 수 없습니다: " + membershipId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("잔여 횟수 수정 실패: " + sql, e);
        }
    }

    public void decreaseCount(Long membershipId) {
        Record membership = findById(membershipId);

        if (membership == null) {
            throw new IllegalArgumentException("회원권을 찾을 수 없습니다.");
        }

        Integer currentCount = membership.get(MEMBERSHIP.REMAINING_COUNT);

        if (currentCount == null || currentCount <= 0) {
            throw new IllegalStateException("잔여 횟수가 부족합니다.");
        }

        updateRemainingCount(membershipId, currentCount - 1);
    }

    public void increaseCount(Long membershipId) {
        Record membership = findById(membershipId);

        if (membership == null) {
            throw new IllegalArgumentException("회원권을 찾을 수 없습니다.");
        }

        Integer currentCount = membership.get(MEMBERSHIP.REMAINING_COUNT);

        if (currentCount == null) {
            currentCount = 0;
        }

        updateRemainingCount(membershipId, currentCount + 1);
    }

    public void adjustCount(Long membershipId, int changeCount) {
        Record membership = findById(membershipId);

        if (membership == null) {
            throw new IllegalArgumentException("회원권을 찾을 수 없습니다.");
        }

        Integer currentCount = membership.get(MEMBERSHIP.REMAINING_COUNT);

        if (currentCount == null) {
            currentCount = 0;
        }

        int newCount = currentCount + changeCount;

        if (newCount < 0) {
            throw new IllegalArgumentException("잔여 횟수는 0보다 작을 수 없습니다.");
        }

        updateRemainingCount(membershipId, newCount);
    }

    public void deleteMembershipById(Long membershipId) {
        if (membershipId == null) {
            throw new IllegalArgumentException("회원권 ID가 필요합니다.");
        }

        MEMBERSHIP.delete()
                .where(membershipIdEq(membershipId))
                .execute();
    }

    private static Condition membershipIdEq(Long membershipId) {
        return Condition.raw("id = ?", membershipId);
    }

    /*
     * 예약 신청 전에 사용할 검증용 메서드
     */
    public boolean hasUsableMembership(Long userId, Long programId) {
        return findActiveByUserAndProgram(userId, programId) != null;
    }

    /*
     * 특정 회원의 ACTIVE 회원권만 조회
     */
    public List<Record> findActiveMembershipsByUserId(Long userId) {
        List<Record> memberships = findMembershipsByUserId(userId);
        List<Record> activeMemberships = new ArrayList<>();

        for (Record record : memberships) {
            String status = record.get(MEMBERSHIP.STATUS);
            Integer remainingCount = record.get(MEMBERSHIP.REMAINING_COUNT);

            if ("ACTIVE".equals(status) && remainingCount != null && remainingCount > 0) {
                activeMemberships.add(record);
            }
        }

        return activeMemberships;
    }
}
