package healthajo.programs.dao;

import healthajo.jdbc.core.Page;
import healthajo.jdbc.core.Record;
import healthajo.jdbc.core.SelectStep;
import healthajo.jdbc.core.WindowFunction;
import healthajo.jdbc.table.TProgramSchedules;
import healthajo.jdbc.table.TPrograms;
import healthajo.jdbc.table.TSessions;
import healthajo.programs.dto.ProgramResponseDTO;
import healthajo.programs.entity.Program;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

public class ProgramDAO {

    private static final TPrograms P = TPrograms.PROGRAMS;
    private static final TProgramSchedules PS = TProgramSchedules.PROGRAM_SCHEDULES;
    private static final TSessions S = TSessions.SESSIONS;

    // CREATE — 생성된 PK 반환
    public long insert(Program p) {
        return P.insertInto()
                .set(P.NAME,                  p.getName())
                .set(P.DESCRIPTION,           p.getDescription())
                .set(P.CATEGORY,              p.getCategory())
                .set(P.RESERVATION_OPEN_AT,   p.getReservationOpenAt())
                .set(P.RESERVATION_CLOSE_AT,  p.getReservationCloseAt())
                .set(P.CANCELLATION_OPEN_AT,  p.getCancellationOpenAt())
                .set(P.CANCELLATION_CLOSE_AT, p.getCancellationCloseAt())
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

    // READ - 프로그램 목록 조회
    // 프로그램명, 종목, 예약 가능 시간, 총 정원, 예약 수, 상태
    public List<Record> findAll() {
        return P.select(
                        P.ID,
                        P.NAME, P.DESCRIPTION, P.CATEGORY,
                        P.RESERVATION_OPEN_AT, P.RESERVATION_CLOSE_AT,
                        P.CANCELLATION_OPEN_AT, P.CANCELLATION_CLOSE_AT,
                        P.CREATED_AT, P.UPDATED_AT
                )
                .orderByDesc(P.CREATED_AT)
                .fetch();
    }

    // READ - 프로그램 목록 페이지 조회 (검색: 프로그램명·종목)
    public Page<Record> findAll(int pageNumber, int pageSize, String keyword) {
        healthajo.jdbc.core.Condition where = P.DELETED_AT.isNull();
        if (keyword != null && !keyword.isBlank()) {
            String like = "%" + keyword.trim() + "%";
            where = where.and(P.NAME.like(like).or(P.CATEGORY.like(like)));
        }
        SelectStep step = P.select(
                P.ID,
                P.NAME, P.DESCRIPTION, P.CATEGORY,
                P.RESERVATION_OPEN_AT, P.RESERVATION_CLOSE_AT,
                P.CANCELLATION_OPEN_AT, P.CANCELLATION_CLOSE_AT,
                P.CREATED_AT, P.UPDATED_AT
        ).where(where).orderByDesc(P.CREATED_AT);
        return Page.of(step, pageNumber, pageSize);
    }

    public Record findByProgramID(int id) {
        // TODO: SELECT p.*, ps.schedule_type, ps.start_date, ps.end_date, ps.default_capacity
        //       FROM programs p LEFT JOIN program_schedules ps ON ps.program_id = p.id
        //       WHERE p.id = ?
        return P.select(
                P.ID,
                P.NAME,
                P.DESCRIPTION,
                P.CATEGORY,
                P.RESERVATION_OPEN_AT,
                P.RESERVATION_CLOSE_AT,
                P.CANCELLATION_OPEN_AT,
                P.CANCELLATION_CLOSE_AT,
                P.CREATED_AT,
                P.UPDATED_AT,
                P.DELETED_AT,

                PS.START_DATE,
                PS.END_DATE,
                PS.DEFAULT_CAPACITY
        ).leftJoin(PS).on(P.ID.eq(PS.PROGRAM_ID))
                .where(P.ID.eq((long)id)).fetchOne();
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
                .set(P.RESERVATION_OPEN_AT,   p.getReservationOpenAt())
                .set(P.RESERVATION_CLOSE_AT,  p.getReservationCloseAt())
                .set(P.CANCELLATION_OPEN_AT,  p.getCancellationOpenAt())
                .set(P.CANCELLATION_CLOSE_AT, p.getCancellationCloseAt())
                .where(P.ID.eq(p.getId()).and(P.DELETED_AT.isNull()))
                .execute();
    }

    // DELETE — 소프트 삭제 (deleted_at = NOW)
    public int softDelete(long id) {
        return P.update()
                .set(P.DELETED_AT, LocalDateTime.now())
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
                r.get(P.RESERVATION_OPEN_AT),
                r.get(P.RESERVATION_CLOSE_AT),
                r.get(P.CANCELLATION_OPEN_AT),
                r.get(P.CANCELLATION_CLOSE_AT),
                r.get(P.CREATED_AT),
                r.get(P.UPDATED_AT),
                r.get(P.DELETED_AT)
        );
    }

    private static Timestamp toTs(LocalDateTime ldt) {
        return ldt == null ? null : Timestamp.valueOf(ldt);
    }

    private static LocalDateTime toLdt(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}
