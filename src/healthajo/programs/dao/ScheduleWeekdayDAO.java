package healthajo.programs.dao;
import healthajo.jdbc.core.Record;
import healthajo.jdbc.table.TScheduleWeekdays;

import java.util.*;

public class ScheduleWeekdayDAO {
    private static final TScheduleWeekdays SW = TScheduleWeekdays.SCHEDULE_WEEKDAYS;

    public List<Record> findByScheduleId(long scheduleId) {
        return SW.select(SW.ID, SW.SCHEDULE_ID, SW.WEEK_DAY,
                        SW.CLASS_START_TIME, SW.CLASS_END_TIME, SW.CAPACITY)
                .where(SW.SCHEDULE_ID.eq(scheduleId))
                .orderBy(SW.WEEK_DAY)
                .fetch();
    }
}
