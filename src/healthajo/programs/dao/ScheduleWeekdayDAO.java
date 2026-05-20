package healthajo.programs.dao;
import healthajo.jdbc.core.Record;
import healthajo.jdbc.table.TScheduleWeekdays;

import java.sql.Time;
import java.util.*;

public class ScheduleWeekdayDAO {
    private static final TScheduleWeekdays SW = TScheduleWeekdays.SCHEDULE_WEEKDAYS;

    public long insert(long scheduleId, int weekday, Time startTime, Time endTime, Integer capacity) {
        return SW.insertInto()
                .set(SW.SCHEDULE_ID,      scheduleId)
                .set(SW.WEEK_DAY,         weekday)
                .set(SW.CLASS_START_TIME, startTime)
                .set(SW.CLASS_END_TIME,   endTime)
                .set(SW.CAPACITY,         capacity)
                .executeAndReturnKey();
    }

    public int deleteByScheduleId(long scheduleId) {
        return SW.delete()
                .where(SW.SCHEDULE_ID.eq(scheduleId))
                .execute();
    }

    public List<Record> findByScheduleId(long scheduleId) {
        return SW.select(SW.ID, SW.SCHEDULE_ID, SW.WEEK_DAY,
                        SW.CLASS_START_TIME, SW.CLASS_END_TIME, SW.CAPACITY)
                .where(SW.SCHEDULE_ID.eq(scheduleId))
                .orderBy(SW.WEEK_DAY)
                .fetch();
    }
}
