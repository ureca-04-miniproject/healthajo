package healthajo.jdbc.core;

import java.util.List;

public class SubqueryTable implements FromSource {

    private final SelectStep subquery;
    private final String alias;

    public SubqueryTable(SelectStep subquery, String alias) {
        this.subquery = subquery;
        this.alias = alias;
    }

    // 서브쿼리 결과 컬럼을 alias prefix로 참조
    public <T> Column<T> col(String name, Class<T> type) {
        return new Column<>(alias, name, type);
    }

    public SelectStep select(Field... fields) {
        return new SelectStep(this, fields);
    }

    @Override
    public String toFromSql() { return "(" + subquery.toSql() + ") AS " + alias; }

    @Override
    public String getPrefix() { return alias; }

    @Override
    public List<Object> getBindings() { return subquery.collectAllBindings(); }
}
