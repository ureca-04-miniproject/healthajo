package healthajo.jdbc.table;

import healthajo.jdbc.core.Column;
import healthajo.jdbc.core.TableBase;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class TSessions extends TableBase {
    public static final TSessions SESSIONS = new TSessions();

    public final Column<Long>      ID                   = new Column<>(getTableName(), "id", Long.class);
    public final Column<Long>      PROGRAM_ID           = new Column<>(getTableName(), "program_id", Long.class);
    public final Column<Long>      SCHEDULE_ID          = new Column<>(getTableName(), "schedule_id", Long.class);
    public final Column<Long>      INSTRUCTOR_ID        = new Column<>(getTableName(), "instructor_id", Long.class);
    public final Column<String>    SESSION_TYPE         = new Column<>(getTableName(), "session_type", String.class);
    public final Column<Date>      SESSION_DATE         = new Column<>(getTableName(), "session_date", Date.class);
    public final Column<Time>      START_TIME           = new Column<>(getTableName(), "start_time", Time.class);
    public final Column<Time>      END_TIME             = new Column<>(getTableName(), "end_time", Time.class);
    public final Column<Integer>   CAPACITY             = new Column<>(getTableName(), "capacity", Integer.class);
    public final Column<Integer>   BOOKED_COUNT         = new Column<>(getTableName(), "booked_count", Integer.class);
    public final Column<String>    STATUS               = new Column<>(getTableName(), "status", String.class);
    public final Column<Boolean>   ATTENDANCE_CLOSED    = new Column<>(getTableName(), "attendance_closed", Boolean.class);
    public final Column<LocalDateTime> ATTENDANCE_CLOSED_AT = new Column<>(getTableName(), "attendance_closed_at", LocalDateTime.class);
    public final Column<LocalDateTime> CREATED_AT           = new Column<>(getTableName(), "created_at", LocalDateTime.class);
    public final Column<LocalDateTime> UPDATED_AT           = new Column<>(getTableName(), "updated_at", LocalDateTime.class);

    private TSessions() {
        super("sessions");
    }

    private TSessions(String alias) {
        super("sessions", alias);
    }

    @Override
    public TSessions as(String alias) {
        return new TSessions(alias);
    }
}
