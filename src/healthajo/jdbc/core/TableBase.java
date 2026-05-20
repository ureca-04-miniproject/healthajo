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

    /**
     * 별칭을 붙인 테이블 반환. 반환 타입이 {@code TableBase}이므로 {@code asterisk()} 등
     * 모든 TableBase 기능을 캐스트 없이 사용 가능.
     * 구체 테이블 클래스는 covariant return으로 오버라이드해 타입 컬럼 접근을 제공한다.
     */
    public TableBase as(String alias) {
        if (alias == null || !alias.matches("[a-zA-Z_][a-zA-Z0-9_]*"))
            throw new IllegalArgumentException("유효하지 않은 SQL 식별자: " + alias);
        return new AliasedTable(tableName, alias);
    }

    /** {@code prefix.*} Field 반환 — JOIN SELECT 절에서 명시적 애스터리스크 지정 시 사용 */
    public TableWildcard asterisk() {
        return new TableWildcard(getPrefix());
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
