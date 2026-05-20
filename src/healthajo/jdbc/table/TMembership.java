package healthajo.jdbc.table;

import healthajo.jdbc.core.Column;
import healthajo.jdbc.core.TableBase;

import java.sql.Timestamp;

public final class TMembership extends TableBase {

    public static final TMembership MEMBERSHIP = new TMembership();

    public final Column<Long>      ID              = new Column<>(getPrefix(), "id",              Long.class);
    public final Column<Long>      USER_ID         = new Column<>(getPrefix(), "user_id",         Long.class);
    public final Column<Long>      PROGRAM_ID      = new Column<>(getPrefix(), "program_id",      Long.class);
    public final Column<String>    NAME            = new Column<>(getPrefix(), "name",            String.class);
    public final Column<Integer>   TOTAL_COUNT     = new Column<>(getPrefix(), "total_count",     Integer.class);
    public final Column<Integer>   REMAINING_COUNT = new Column<>(getPrefix(), "remaining_count", Integer.class);
    public final Column<String>    STATUS          = new Column<>(getPrefix(), "status",          String.class);
    public final Column<Timestamp> ISSUED_AT       = new Column<>(getPrefix(), "issued_at",       Timestamp.class);
    public final Column<Timestamp> CREATED_AT      = new Column<>(getPrefix(), "created_at",      Timestamp.class);
    public final Column<Timestamp> UPDATED_AT      = new Column<>(getPrefix(), "updated_at",      Timestamp.class);

    private TMembership() {
        super("memberships");
    }

    private TMembership(String alias) {
        super("memberships", alias);
    }

    @Override
    public TMembership as(String alias) {
        if (alias == null || !alias.matches("[a-zA-Z_][a-zA-Z0-9_]*"))
            throw new IllegalArgumentException("유효하지 않은 SQL 식별자: " + alias);
        return new TMembership(alias);
    }
}
