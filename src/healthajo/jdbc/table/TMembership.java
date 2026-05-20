package healthajo.jdbc.table;

import healthajo.jdbc.core.Column;
import healthajo.jdbc.core.TableBase;

import java.sql.Timestamp;

public final class TMembership extends TableBase {

    public static final TMembership MEMBERSHIP = new TMembership();

    public final Column<Long>      ID              = new Column<>(getTableName(), "id",              Long.class);
    public final Column<Long>      USER_ID         = new Column<>(getTableName(), "user_id",         Long.class);
    public final Column<Long>      PROGRAM_ID      = new Column<>(getTableName(), "program_id",      Long.class);
    public final Column<String>    NAME            = new Column<>(getTableName(), "name",            String.class);
    public final Column<Integer>   TOTAL_COUNT     = new Column<>(getTableName(), "total_count",     Integer.class);
    public final Column<Integer>   REMAINING_COUNT = new Column<>(getTableName(), "remaining_count", Integer.class);
    public final Column<String>    STATUS          = new Column<>(getTableName(), "status",          String.class);
    public final Column<Timestamp> ISSUED_AT       = new Column<>(getTableName(), "issued_at",       Timestamp.class);
    public final Column<Timestamp> CREATED_AT      = new Column<>(getTableName(), "created_at",      Timestamp.class);
    public final Column<Timestamp> UPDATED_AT      = new Column<>(getTableName(), "updated_at",      Timestamp.class);

    private TMembership() {
        super("memberships");
    }
}