package healthajo.jdbc.core;

import java.util.List;

public abstract class TableBase implements FromSource {

    private final String tableName;
    private final String prefix;

    protected TableBase(String tableName) {
        this.tableName = tableName;
        this.prefix = tableName;
    }

    // alias 생성자 — 서브클래스에서 as() 오버라이드 시 사용
    protected TableBase(String tableName, String alias) {
        this.tableName = tableName;
        this.prefix = alias;
    }

    public String getTableName() { return tableName; }

    // 컬럼 초기화 시 이 값을 사용 — alias 여부에 따라 "users" 또는 "a" 반환
    @Override
    public String getPrefix() { return prefix; }

    // alias가 없으면 "users", 있으면 "users AS a"
    @Override
    public String toFromSql() {
        return prefix.equals(tableName) ? tableName : tableName + " AS " + prefix;
    }

    @Override
    public List<Object> getBindings() { return List.of(); }

    // 기본 구현 — 서브클래스에서 covariant return type으로 오버라이드
    public FromSource as(String alias) {
        return new AliasedTable(tableName, alias);
    }

    public SelectStep select(Field... fields) {
        return new SelectStep(this, fields);
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
