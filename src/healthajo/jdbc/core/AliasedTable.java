package healthajo.jdbc.core;

import java.util.List;

public class AliasedTable implements FromSource {

    private final String realName;
    private final String alias;

    AliasedTable(String realName, String alias) {
        this.realName = realName;
        this.alias = alias;
    }

    // alias를 prefix로 사용하는 컬럼 반환 (셀프 조인 등 alias별 컬럼 구분에 사용)
    public <T> Column<T> col(Column<T> original) {
        return new Column<>(alias, original.getName(), original.getType());
    }

    public SelectStep select(Field... fields) {
        return new SelectStep(this, fields);
    }

    @Override
    public String toFromSql() { return realName + " AS " + alias; }

    @Override
    public String getPrefix() { return alias; }

    @Override
    public List<Object> getBindings() { return List.of(); }
}
