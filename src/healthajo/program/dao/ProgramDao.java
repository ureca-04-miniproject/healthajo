package healthajo.program.dao;

import healthajo.jdbc.core.Condition;
import healthajo.jdbc.core.Field;
import healthajo.jdbc.core.Page;
import healthajo.jdbc.core.Record;
import healthajo.jdbc.core.SelectStep;
import healthajo.program.domain.ProgramSummary;

import java.time.format.DateTimeFormatter;

import static healthajo.jdbc.table.TPrograms.PROGRAMS;

public class ProgramDao {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // 잔여석: 현재 날짜 이후 OPEN 세션들의 (capacity - booked_count) 합계
    private static final Field REMAINING_FIELD = () ->
        "(SELECT COALESCE(SUM(sessions.capacity - sessions.booked_count), 0) " +
        "FROM sessions WHERE sessions.program_id = programs.id " +
        "AND sessions.session_date >= CURDATE() AND sessions.status = 'OPEN') AS remaining";

    public Page<ProgramSummary> findAll(int pageNumber, int pageSize, String keyword) {
        Condition where = PROGRAMS.DELETED_AT.isNull();
        if (keyword != null && !keyword.isBlank()) {
            String like = "%" + keyword.trim() + "%";
            where = where.and(
                PROGRAMS.NAME.like(like)
                    .or(PROGRAMS.CATEGORY.like(like))
                    .or(Condition.raw("DATE_FORMAT(programs.reservation_open_at, '%Y-%m-%d') LIKE ?", like))
                    .or(Condition.raw("DATE_FORMAT(programs.reservation_close_at, '%Y-%m-%d') LIKE ?", like)));
        }
        SelectStep step = PROGRAMS.select(
                PROGRAMS.ID,
                PROGRAMS.NAME,
                PROGRAMS.CATEGORY,
                PROGRAMS.RESERVATION_OPEN_AT,
                PROGRAMS.RESERVATION_CLOSE_AT,
                REMAINING_FIELD
            )
            .where(where)
            .orderBy(PROGRAMS.NAME);
        return Page.of(step, pageNumber, pageSize).map(this::toProgramSummary);
    }

    private ProgramSummary toProgramSummary(Record r) {
        Long id = toLong(r.get("programs.id"));
        String name     = (String) r.get("programs.name");
        String category = (String) r.get("programs.category");
        String period   = formatPeriod(r.get("programs.reservation_open_at"),
                                       r.get("programs.reservation_close_at"));
        Object remRaw   = r.get("remaining");
        int remaining   = remRaw instanceof Number n ? n.intValue() : 0;
        return new ProgramSummary(id, name, category, period, remaining);
    }

    private static String formatPeriod(Object open, Object close) {
        String s = toDateStr(open);
        String e = toDateStr(close);
        if (s == null || e == null) return "-";
        return s + " ~ " + e;
    }

    private static String toDateStr(Object val) {
        if (val == null) return null;
        if (val instanceof java.sql.Timestamp ts)
            return ts.toLocalDateTime().format(DATE_FMT);
        if (val instanceof java.time.LocalDateTime ldt)
            return ldt.format(DATE_FMT);
        if (val instanceof java.sql.Date d)
            return d.toLocalDate().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        String s = val.toString();
        return s.length() >= 10 ? s.substring(0, 10) : s;
    }

    private static Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Number n) return n.longValue();
        return null;
    }
}
