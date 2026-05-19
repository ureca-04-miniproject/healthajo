package healthajo.programs.dao;

import healthajo.jdbc.core.Record;
import healthajo.jdbc.table.TPrograms;
import healthajo.programs.app.Program;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

public class ProgramDAO {

    private static final TPrograms P = TPrograms.PROGRAMS;

    // CREATE — 생성된 PK 반환
    public long insert(Program p) {
        return P.insertInto()
                .set(P.NAME,                  p.getName())
                .set(P.DESCRIPTION,           p.getDescription())
                .set(P.CATEGORY,              p.getCategory())
                .set(P.RESERVATION_OPEN_AT,   toTs(p.getReservationOpenAt()))
                .set(P.RESERVATION_CLOSE_AT,  toTs(p.getReservationCloseAt()))
                .set(P.CANCELLATION_OPEN_AT,  toTs(p.getCancellationOpenAt()))
                .set(P.CANCELLATION_CLOSE_AT, toTs(p.getCancellationCloseAt()))
                .executeAndReturnKey();
    }

    // READ — 단건 (소프트 삭제 제외)
    public Optional<Program> findById(long id) {
        Record r = P.select(
                        P.ID, P.NAME, P.DESCRIPTION, P.CATEGORY,
                        P.RESERVATION_OPEN_AT, P.RESERVATION_CLOSE_AT,
                        P.CANCELLATION_OPEN_AT, P.CANCELLATION_CLOSE_AT,
                        P.CREATED_AT, P.UPDATED_AT, P.DELETED_AT)
                .where(P.ID.eq(id).and(P.DELETED_AT.isNull()))
                .fetchOne();
        return Optional.ofNullable(r).map(this::toEntity);
    }

    // READ — 전체 목록 (소프트 삭제 제외, 최신순)
    public List<Program> findAll() {
        List<Record> rows = P.select(
                        P.ID, P.NAME, P.DESCRIPTION, P.CATEGORY,
                        P.RESERVATION_OPEN_AT, P.RESERVATION_CLOSE_AT,
                        P.CANCELLATION_OPEN_AT, P.CANCELLATION_CLOSE_AT,
                        P.CREATED_AT, P.UPDATED_AT, P.DELETED_AT)
                .where(P.DELETED_AT.isNull())
                .orderByDesc(P.CREATED_AT)
                .fetch();
        List<Program> result = new ArrayList<>(rows.size());
        for (Record r : rows) result.add(toEntity(r));
        return result;
    }

    // UPDATE — id 기준 전체 필드 갱신
    public int update(Program p) {
        if (p.getId() == null) {
            throw new IllegalArgumentException("update 대상 Program의 id가 null입니다");
        }
        return P.update()
                .set(P.NAME,                  p.getName())
                .set(P.DESCRIPTION,           p.getDescription())
                .set(P.CATEGORY,              p.getCategory())
                .set(P.RESERVATION_OPEN_AT,   toTs(p.getReservationOpenAt()))
                .set(P.RESERVATION_CLOSE_AT,  toTs(p.getReservationCloseAt()))
                .set(P.CANCELLATION_OPEN_AT,  toTs(p.getCancellationOpenAt()))
                .set(P.CANCELLATION_CLOSE_AT, toTs(p.getCancellationCloseAt()))
                .where(P.ID.eq(p.getId()).and(P.DELETED_AT.isNull()))
                .execute();
    }

    // DELETE — 소프트 삭제 (deleted_at = NOW)
    public int softDelete(long id) {
        return P.update()
                .set(P.DELETED_AT, Timestamp.valueOf(LocalDateTime.now()))
                .where(P.ID.eq(id).and(P.DELETED_AT.isNull()))
                .execute();
    }

    // ── Record → Entity 매핑 ────────────────────────────────
    private Program toEntity(Record r) {
        return new Program(
                r.get(P.ID),
                r.get(P.NAME),
                r.get(P.DESCRIPTION),
                r.get(P.CATEGORY),
                toLdt(r.get(P.RESERVATION_OPEN_AT)),
                toLdt(r.get(P.RESERVATION_CLOSE_AT)),
                toLdt(r.get(P.CANCELLATION_OPEN_AT)),
                toLdt(r.get(P.CANCELLATION_CLOSE_AT)),
                toLdt(r.get(P.CREATED_AT)),
                toLdt(r.get(P.UPDATED_AT)),
                toLdt(r.get(P.DELETED_AT))
        );
    }

    private static Timestamp toTs(LocalDateTime ldt) {
        return ldt == null ? null : Timestamp.valueOf(ldt);
    }

    private static LocalDateTime toLdt(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}
