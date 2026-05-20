package healthajo.programs.dao;

import healthajo.jdbc.core.Field;
import healthajo.jdbc.core.Record;
import healthajo.jdbc.core.WindowFunction;
import healthajo.jdbc.table.TProgramSchedules;
import healthajo.jdbc.table.TScheduleWeekdays;
import healthajo.programs.entity.Schedule;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;

public class ScheduleDAO {
    private static final TProgramSchedules PS = TProgramSchedules.PROGRAM_SCHEDULES;
    private static final TScheduleWeekdays SW = TScheduleWeekdays.SCHEDULE_WEEKDAYS;

    // CREATE
    public long insert(Schedule sc) {
        return PS.insertInto()
                .set(PS.ID,                      sc.getId())
                .set(PS.PROGRAM_ID,              sc.getProgramId())
                .set(PS.START_DATE,              sc.getStartDate())
                .set(PS.END_DATE,                sc.getEndDate())
                .set(PS.DEFAULT_CAPACITY,        sc.getDefaultCapacity())
                .set(PS.SLOT_OPEN_TIME,          sc.getSlotOpenTime())
                .set(PS.SLOT_CLOSE_TIME,         sc.getSlotCloseTime())
                .set(PS.SLOT_DURATION_TIME,      sc.getSlotDurationTime())
                .set(PS.CREATED_AT,              sc.getCreatedAt())
                .set(PS.UPDATED_AT,              sc.getUpdatedAt())
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

    public List<Schedule> findByProgramId(long programId) {
        List<Record> rows = PS.select(
                        PS.ID, PS.PROGRAM_ID, PS.START_DATE, PS.END_DATE,
                        PS.DEFAULT_CAPACITY, PS.SLOT_OPEN_TIME,
                        PS.SLOT_CLOSE_TIME, PS.SLOT_DURATION_TIME,
                        PS.CREATED_AT, PS.UPDATED_AT)
                .where(PS.PROGRAM_ID.eq(programId))
                .orderByDesc(PS.CREATED_AT)
                .fetch();
        List<Schedule> result = new ArrayList<>(rows.size());
        for (Record r : rows) result.add(toEntity(r));
        return result;
    }

    public List<Record> findListItemsByProgramId(long programId) {
        // 일반 집계 함수 COUNT — GROUP BY와 함께 사용하기 위해 익명 Field 람다.
        // (WindowFunction.count(...).over()는 OVER ()가 강제로 붙어 윈도우 함수가 되므로 그룹별 집계가 안 됨)
        Field weekdayCount = () ->
                "COUNT(" + SW.ID.getQualifiedName() + ") AS weekday_count";

        return PS.select(
                        PS.ID, PS.START_DATE, PS.END_DATE,
                        PS.DEFAULT_CAPACITY,
                        weekdayCount
                )
                .leftJoin(SW).on(SW.SCHEDULE_ID.eq(PS.ID))
                .where(PS.PROGRAM_ID.eq(programId))
                .groupBy(PS.ID)
                .orderByDesc(PS.CREATED_AT)
                .fetch();
    }

    // 신규 생성 — 폼 입력 값으로 직접 받음. LocalDate → java.sql.Date 변환은 DAO 내부에서 처리.
    public long insertSimple(long programId, LocalDate startDate, LocalDate endDate, int defaultCapacity) {
        return PS.insertInto()
                .set(PS.PROGRAM_ID,       programId)
                .set(PS.START_DATE,       Date.valueOf(startDate))
                .set(PS.END_DATE,         Date.valueOf(endDate))
                .set(PS.DEFAULT_CAPACITY, defaultCapacity)
                .executeAndReturnKey();
    }

    // 수정 — 폼 입력 값으로 직접 받음.
    public int updateSimple(long scheduleId, LocalDate startDate, LocalDate endDate, int defaultCapacity) {
        return PS.update()
                .set(PS.START_DATE,       Date.valueOf(startDate))
                .set(PS.END_DATE,         Date.valueOf(endDate))
                .set(PS.DEFAULT_CAPACITY, defaultCapacity)
                .where(PS.ID.eq(scheduleId))
                .execute();
    }

    // 하드 삭제 — schedule_weekdays는 호출자가 먼저 삭제해야 함
    public int delete(long scheduleId) {
        return PS.delete()
                .where(PS.ID.eq(scheduleId))
                .execute();
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
                r.get(PS.START_DATE),
                r.get(PS.END_DATE),
                r.get(PS.DEFAULT_CAPACITY),
                r.get(PS.SLOT_OPEN_TIME),
                r.get(PS.SLOT_CLOSE_TIME),
                r.get(PS.SLOT_DURATION_TIME),
                r.get(PS.CREATED_AT),
                r.get(PS.UPDATED_AT)
        );
    }

    private static LocalTime toLt(LocalDateTime ldt) {
        return ldt == null ? null : ldt.toLocalTime();
    }
}
