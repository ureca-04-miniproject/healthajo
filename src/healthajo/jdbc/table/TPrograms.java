package healthajo.jdbc.table;

import healthajo.jdbc.core.Column;
import healthajo.jdbc.core.TableBase;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public final class TPrograms extends TableBase {
    public static final TPrograms PROGRAMS = new TPrograms();

    public final Column<Long>       ID                          = new Column<>(getTableName(), "id", Long.class);
    public final Column<String>     NAME                        = new Column<>(getTableName(), "name", String.class);
    public final Column<String>     DESCRIPTION                 = new Column<>(getTableName(), "description", String.class);
    public final Column<String>     CATEGORY                    = new Column<>(getTableName(), "category", String.class);
    public final Column<LocalDateTime>  RESERVATION_OPEN_AT         = new Column<>(getTableName(), "reservation_open_at", LocalDateTime.class);
    public final Column<LocalDateTime>  RESERVATION_CLOSE_AT        = new Column<>(getTableName(), "reservation_close_at", LocalDateTime.class);
    public final Column<LocalDateTime>  CANCELLATION_OPEN_AT        = new Column<>(getTableName(), "cancellation_open_at", LocalDateTime.class);
    public final Column<LocalDateTime>  CANCELLATION_CLOSE_AT       = new Column<>(getTableName(), "cancellation_close_at", LocalDateTime.class);
    public final Column<LocalDateTime>  CREATED_AT                  = new Column<>(getTableName(), "created_at", LocalDateTime.class);
    public final Column<LocalDateTime>  UPDATED_AT                  = new Column<>(getTableName(), "updated_at", LocalDateTime.class);
    public final Column<LocalDateTime>  DELETED_AT                  = new Column<>(getTableName(), "deleted_at", LocalDateTime.class);

    private TPrograms() {
        super("programs");
    }

    private TPrograms(String alias) {
        super("programs", alias);
    }

    @Override
    public TPrograms as(String alias) {
        return new TPrograms(alias);
    }
}