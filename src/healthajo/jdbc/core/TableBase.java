package healthajo.jdbc.core;

public abstract class TableBase {

    private final String tableName;

    protected TableBase(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public SelectStep select(Field... fields) {
        return new SelectStep(tableName, fields);
    }

    public InsertStep insertInto() {
        return new InsertStep(tableName);
    }

    public UpdateStep update() {
        return new UpdateStep(tableName);
    }

    public DeleteStep delete() {
        return new DeleteStep(tableName);
    }
}
