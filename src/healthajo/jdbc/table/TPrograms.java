package healthajo.jdbc.table;

import healthajo.jdbc.core.Column;
import healthajo.jdbc.core.TableBase;

import java.sql.Timestamp;

public final class TPrograms extends TableBase {
    public static final TPrograms PROGRAMS = new TPrograms();

    public final Column<Long>       ID                          = new Column<>(getTableName(), "id", Long.class);
    public final Column<String>     NAME                        = new Column<>(getTableName(), "name", String.class);
    public final Column<String>     DESCRIPTION                 = new Column<>(getTableName(), "description", String.class);
    public final Column<String>     CATEGORY                    = new Column<>(getTableName(), "category", String.class);
    public final Column<Timestamp>  RESERVATION_OPEN_AT         = new Column<>(getTableName(), "reservation_open_at", Timestamp.class);
    public final Column<Timestamp>  RESERVATION_CLOSE_AT        = new Column<>(getTableName(), "reservation_close_at", Timestamp.class);
    public final Column<Timestamp>  CANCELLATION_OPEN_AT        = new Column<>(getTableName(), "cancellation_open_at", Timestamp.class);
    public final Column<Timestamp>  CANCELLATION_CLOSE_AT       = new Column<>(getTableName(), "cancellation_close_at", Timestamp.class);
    public final Column<Timestamp>  CREATED_AT                  = new Column<>(getTableName(), "created_at", Timestamp.class);
    public final Column<Timestamp>  UPDATED_AT                  = new Column<>(getTableName(), "updated_at", Timestamp.class);
    public final Column<Timestamp>  DELETED_AT                  = new Column<>(getTableName(), "deleted_at", Timestamp.class);
}