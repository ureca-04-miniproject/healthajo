package healthajo.jdbc.table;

import healthajo.jdbc.core.Column;
import healthajo.jdbc.core.TableBase;

import java.sql.Timestamp;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public final class TProgramSchedules extends TableBase {
    public static final TProgramSchedules PROGRAM_SCHEDULES = new TProgramSchedules();

    public final Column<Long>       ID                                  = new Column<>(getTableName(), "id", Long.class);
    public final Column<Long>       PROGRAM_ID                          = new Column<>(getTableName(), "program_id", Long.class);
    public final Column<Date>  START_DATE                          = new Column<>(getTableName(), "start_date", Date.class);
    public final Column<Date>  END_DATE                            = new Column<>(getTableName(), "end_date", Date.class);
    public final Column<Integer>    DEFAULT_CAPACITY                    = new Column<>(getTableName(), "default_capacity", Integer.class);
    public final Column<LocalTime>  SLOT_OPEN_TIME                      = new Column<>(getTableName(), "slot_open_time", LocalTime.class);
    public final Column<LocalTime>  SLOT_CLOSE_TIME                      = new Column<>(getTableName(), "slot_close_time", LocalTime.class);
    public final Column<Integer>    SLOT_DURATION_TIME                      = new Column<>(getTableName(), "slot_duration_min", Integer.class);
    public final Column<LocalDateTime>  CREATED_AT                          = new Column<>(getTableName(), "created_at", LocalDateTime.class);
    public final Column<LocalDateTime>  UPDATED_AT                          = new Column<>(getTableName(), "updated_at", LocalDateTime.class);

    private TProgramSchedules() {
        super("program_schedules");
    }

    private TProgramSchedules(String alias) {
        super("program_schedules", alias);
    }

    @Override
    public TProgramSchedules as(String alias) {
        return new TProgramSchedules(alias);
    }
}