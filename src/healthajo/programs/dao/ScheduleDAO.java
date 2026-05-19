package healthajo.programs.dao;

import healthajo.jdbc.core.Record;
import healthajo.jdbc.table.TProgramSchedules;
import healthajo.jdbc.table.TPrograms;
import healthajo.programs.app.Program;
import healthajo.programs.app.Schedule;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

public class ScheduleDAO {
    private static final TProgramSchedules PS = TProgramSchedules.PROGRAM_SCHEDULES;

    // CREATE
    public long insert(Schedule sc) {
        return PS.insertInto()
                .set(PS.ID,                      sc.getId())
                .set(PS.PROGRAM_ID,              sc.getProgramId())
                .set(PS.START_DATE,              toTs(sc.getStartDate()))
                .set(PS.END_DATE,                toTs(sc.getEndDate()))
                .set(PS.DEFAULT_CAPACITY,        sc.getDefaultCapacity())
                .set(PS.SLOT_OPEN_TIME,          toTs(sc.getSlotOpenTime()))
                .set(PS.SLOT_CLOSE_TIME,         toTs(sc.getSlotCloseTime()))
                .set(PS.SLOT_DURATION_TIME,      sc.getSlotDurationTime())
                .set(PS.CREATED_AT,              toTs(sc.getCreatedAt()))
                .set(PS.UPDATED_AT,              toTs(sc.getUpdatedAt()))
                .executeAndReturnKey();
    }

    // READ - 단건
    public Optional<Schedule> findById(long id) {
        Record r = PS.select(
                        PS.ID, PS.PROGRAM_ID, PS.START_DATE, PS.END_DATE,
                        PS.DEFAULT_CAPACITY, PS.SLOT_OPEN_TIME,
                        PS.SLOT_CLOSE_TIME, PS.SLOT_DURATION_TIME,
                        PS.CREATED_AT, PS.UPDATED_AT)
                .where(PS.ID.eq(id))
                .fetchOne();
        return Optional.ofNullable(r).map(this::toEntity);
    }

    public List<Schedule> findAll() {
        List<Record> rows = PS.select(
                        PS.ID, PS.PROGRAM_ID, PS.START_DATE, PS.END_DATE,
                        PS.DEFAULT_CAPACITY, PS.SLOT_OPEN_TIME,
                        PS.SLOT_CLOSE_TIME, PS.SLOT_DURATION_TIME,
                        PS.CREATED_AT, PS.UPDATED_AT)
                .orderByDesc(PS.CREATED_AT)
                .fetch();
        List<Schedule> result = new ArrayList<>(rows.size());
        for (Record r : rows) result.add(toEntity(r));
        return result;
    }

    private Schedule toEntity(Record r) {
        return new Schedule(
                r.get(PS.ID),
                r.get(PS.PROGRAM_ID),
                toLdt(r.get(PS.START_DATE)),
                toLdt(r.get(PS.END_DATE)),
                r.get(PS.DEFAULT_CAPACITY),
                toLdt(r.get(PS.SLOT_OPEN_TIME)),
                toLdt(r.get(PS.SLOT_CLOSE_TIME)),
                r.get(PS.SLOT_DURATION_TIME),
                toLdt(r.get(PS.CREATED_AT)),
                toLdt(r.get(PS.UPDATED_AT))
        );
    }

    private static Timestamp toTs(LocalDateTime ldt) {
        return ldt == null ? null : Timestamp.valueOf(ldt);
    }

    private static LocalDateTime toLdt(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}
