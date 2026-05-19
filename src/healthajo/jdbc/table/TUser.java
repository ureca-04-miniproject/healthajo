package healthajo.jdbc.table;

import healthajo.jdbc.core.Column;
import healthajo.jdbc.core.TableBase;

import java.sql.Timestamp;

public final class TUser extends TableBase {

    public static final TUser USER = new TUser();

    public final Column<Long>      ID              = new Column<>(getTableName(), "id",              Long.class);
    public final Column<String>    NAME            = new Column<>(getTableName(), "name",            String.class);
    public final Column<String>    PHONE           = new Column<>(getTableName(), "phone",           String.class);
    public final Column<String>    EMAIL           = new Column<>(getTableName(), "email",           String.class);
    public final Column<String>    ROLE            = new Column<>(getTableName(), "role",            String.class);
    public final Column<String>    LOGIN_CODE_HASH = new Column<>(getTableName(), "login_code_hash", String.class);
    public final Column<Timestamp> CREATED_AT      = new Column<>(getTableName(), "created_at",      Timestamp.class);
    public final Column<Timestamp> UPDATED_AT      = new Column<>(getTableName(), "updated_at",      Timestamp.class);
    public final Column<Timestamp> DELETED_AT      = new Column<>(getTableName(), "deleted_at",      Timestamp.class);

    private TUser() {
        super("users");
    }


}
