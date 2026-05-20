package healthajo.programs.dao;

import healthajo.jdbc.core.Field;
import healthajo.jdbc.core.Record;
import healthajo.jdbc.table.TInstructors;
import healthajo.jdbc.table.TSessions;

import java.util.List;

public class SessionDAO {
    private static final TSessions    S = TSessions.SESSIONS;
    private static final TInstructors I = TInstructors.INSTRUCTORS;

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
}
