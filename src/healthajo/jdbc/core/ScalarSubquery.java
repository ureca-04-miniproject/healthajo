package healthajo.jdbc.core;

import java.util.List;

public class ScalarSubquery implements Field {

    private final SelectStep subquery;
    private final String alias;

    public ScalarSubquery(SelectStep subquery) {
        this.subquery = subquery;
        this.alias = null;
    }

    private ScalarSubquery(SelectStep subquery, String alias) {
        this.subquery = subquery;
        this.alias = alias;
    }

    public ScalarSubquery as(String alias) {
        return new ScalarSubquery(subquery, alias);
    }

    @Override
    public String toSqlWithAlias() {
        String sql = "(" + subquery.toSql() + ")";
        return alias != null ? sql + " AS " + alias : sql;
    }

    @Override
    public List<Object> getBindings() {
        return subquery.collectAllBindings();
    }
}
