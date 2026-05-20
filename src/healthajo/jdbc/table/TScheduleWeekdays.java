package healthajo.jdbc.table;

import com.mysql.cj.xdevapi.Table;
import healthajo.jdbc.core.Column;
import healthajo.jdbc.core.TableBase;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TScheduleWeekdays extends TableBase {
    public static final TScheduleWeekdays SCHEDULE_WEEKDAYS = new TScheduleWeekdays();

    public final Column<Long>       ID                                  = new Column<>(getTableName(), "id", Long.class);
    public final Column<Long>       SCHEDULE_ID                          = new Column<>(getTableName(), "program_id", Long.class);
    public final Column<Integer>    WEEK_DAY                             = new Column<>(getTableName(), "weekday", Integer.class);
    public final Column<LocalTime>  CLASS_START_TIME                          = new Column<>(getTableName(), "class_start_time", LocalTime.class);
    public final Column<LocalTime>  CLASS_END_TIME                            = new Column<>(getTableName(), "class_end_time", LocalTime.class);
    public final Column<Integer>    CAPACITY                             = new Column<>(getTableName(), "capacity", Integer.class);

    private TScheduleWeekdays() {
        super("schedule_weekdays");
    }

    private TScheduleWeekdays(String alias) {
        super("schedule_weekdays", alias);
    }

    @Override
    public TScheduleWeekdays as(String alias) {
        return new TScheduleWeekdays(alias);
    }
}
