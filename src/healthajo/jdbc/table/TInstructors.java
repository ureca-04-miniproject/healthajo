package healthajo.jdbc.table;

import healthajo.jdbc.core.Column;
import healthajo.jdbc.core.TableBase;

public final class TInstructors extends TableBase {
    public static final TInstructors INSTRUCTORS = new TInstructors();

    public final Column<Long>   ID        = new Column<>(getTableName(), "id",        Long.class);
    public final Column<String> NAME      = new Column<>(getTableName(), "name",      String.class);
    public final Column<String> PHONE     = new Column<>(getTableName(), "phone",     String.class);
    public final Column<String> SPECIALTY = new Column<>(getTableName(), "specialty", String.class);
    public final Column<String> STATUS    = new Column<>(getTableName(), "status",    String.class);

    private TInstructors() { super("instructors"); }
    private TInstructors(String alias) { super("instructors", alias); }

    @Override
    public TInstructors as(String alias) {
        return new TInstructors(alias);
    }
}
