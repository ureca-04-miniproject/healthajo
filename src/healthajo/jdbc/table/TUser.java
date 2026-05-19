package healthajo.jdbc.table;

import healthajo.jdbc.core.Column;
import healthajo.jdbc.core.TableBase;

import java.sql.Timestamp;

public final class TUser extends TableBase {

    public static final TUser USER = new TUser();

    public final Column<Long>      ID              = new Column<>(getPrefix(), "id",              Long.class);
    public final Column<String>    NAME            = new Column<>(getPrefix(), "name",            String.class);
    public final Column<String>    PHONE           = new Column<>(getPrefix(), "phone",           String.class);
    public final Column<String>    EMAIL           = new Column<>(getPrefix(), "email",           String.class);
    public final Column<String>    ROLE            = new Column<>(getPrefix(), "role",            String.class);
    public final Column<String>    LOGIN_CODE_HASH = new Column<>(getPrefix(), "login_code_hash", String.class);
    public final Column<Timestamp> CREATED_AT      = new Column<>(getPrefix(), "created_at",      Timestamp.class);
    public final Column<Timestamp> UPDATED_AT      = new Column<>(getPrefix(), "updated_at",      Timestamp.class);
    public final Column<Timestamp> DELETED_AT      = new Column<>(getPrefix(), "deleted_at",      Timestamp.class);

    private TUser() {
        super("users");
    }

    private TUser(String alias) {
        super("users", alias);
    }

    @Override
    public TUser as(String alias) {
        return new TUser(alias);
    }
}
