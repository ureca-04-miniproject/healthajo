package healthajo.jdbc.table;

import healthajo.jdbc.core.Column;
import healthajo.jdbc.core.TableBase;

import java.sql.Timestamp;

public final class TProgramSchedules extends TableBase {
    public static final TProgramSchedules PROGRAM_SCHEDULES = new TProgramSchedules();

    public final Column<Long>       ID                                  = new Column<>(getTableName(), "id", Long.class);
    public final Column<Long>       PROGRAM_ID                          = new Column<>(getTableName(), "program_id", Long.class);
    public final Column<Timestamp>  START_DATE                          = new Column<>(getTableName(), "start_date", Timestamp.class);
    public final Column<Timestamp>  END_DATE                            = new Column<>(getTableName(), "end_date", Timestamp.class);
    public final Column<Integer>    DEFAULT_CAPACITY                    = new Column<>(getTableName(), "default_capacity", Integer.class);
    public final Column<Timestamp>  SLOT_OPEN_TIME                      = new Column<>(getTableName(), "slot_open_time", Timestamp.class);
    public final Column<Timestamp>  SLOT_CLOSE_TIME                      = new Column<>(getTableName(), "slot_close_time", Timestamp.class);
    public final Column<Integer>    SLOT_DURATION_TIME                      = new Column<>(getTableName(), "slot_duration_time", Integer.class);
    public final Column<Timestamp>  CREATED_AT                          = new Column<>(getTableName(), "created_at", Timestamp.class);
    public final Column<Timestamp>  UPDATED_AT                          = new Column<>(getTableName(), "updated_at", Timestamp.class);
}