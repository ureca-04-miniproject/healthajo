package healthajo.jdbc.core;

import java.util.Arrays;
import java.util.List;

public class Column<T> implements Field {

    private final String tableName;
    private final String name;
    private final Class<T> type;

    public Column(String tableName, String name, Class<T> type) {
        this.tableName = tableName;
        this.name = name;
        this.type = type;
    }

    public String getTableName() { return tableName; }
    public String getName() { return name; }
    public Class<T> getType() { return type; }
    public String getQualifiedName() { return tableName + "." + name; }

    @Override
    public String toSqlWithAlias() { return getQualifiedName(); }

    // 값 비교
    public Condition eq(T value) { return Condition.binary(this, "=", value); }
    public Condition ne(T value) { return Condition.binary(this, "!=", value); }
    public Condition gt(T value) { return Condition.binary(this, ">", value); }
    public Condition lt(T value) { return Condition.binary(this, "<", value); }
    public Condition ge(T value) { return Condition.binary(this, ">=", value); }
    public Condition le(T value) { return Condition.binary(this, "<=", value); }
    public Condition like(String pattern) { return Condition.binary(this, "LIKE", pattern); }
    public Condition isNull() { return Condition.unary(this, "IS NULL"); }
    public Condition isNotNull() { return Condition.unary(this, "IS NOT NULL"); }

    @SafeVarargs
    public final Condition in(T... values) { return Condition.in(this, Arrays.asList(values)); }
    public Condition in(List<T> values) { return Condition.in(this, values); }

    // 컬럼-컬럼 비교 (JOIN ON 용)
    public Condition eq(Column<?> other) { return Condition.columnCompare(this, "=", other); }
    public Condition ne(Column<?> other) { return Condition.columnCompare(this, "!=", other); }
    public Condition gt(Column<?> other) { return Condition.columnCompare(this, ">", other); }
    public Condition lt(Column<?> other) { return Condition.columnCompare(this, "<", other); }
    public Condition ge(Column<?> other) { return Condition.columnCompare(this, ">=", other); }
    public Condition le(Column<?> other) { return Condition.columnCompare(this, "<=", other); }
}
