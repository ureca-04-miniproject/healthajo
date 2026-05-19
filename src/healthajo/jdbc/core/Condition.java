package healthajo.jdbc.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class Condition {

    private final String sql;
    private final List<Object> bindings;

    private Condition(String sql, List<Object> bindings) {
        this.sql = sql;
        this.bindings = Collections.unmodifiableList(bindings);
    }

    public String getSql() { return sql; }
    public List<Object> getBindings() { return bindings; }

    public Condition and(Condition other) {
        List<Object> merged = new ArrayList<>(this.bindings);
        merged.addAll(other.bindings);
        return new Condition("(" + this.sql + " AND " + other.sql + ")", merged);
    }

    public Condition or(Condition other) {
        List<Object> merged = new ArrayList<>(this.bindings);
        merged.addAll(other.bindings);
        return new Condition("(" + this.sql + " OR " + other.sql + ")", merged);
    }

    static Condition binary(Column<?> column, String op, Object value) {
        Objects.requireNonNull(value, "null 비교는 isNull() / isNotNull() 을 사용하세요.");
        return new Condition(column.getQualifiedName() + " " + op + " ?", Collections.singletonList(value));
    }

    static Condition unary(Column<?> column, String op) {
        return new Condition(column.getQualifiedName() + " " + op, List.of());
    }

    static Condition in(Column<?> column, List<?> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("IN 조건에 값이 하나 이상 필요합니다.");
        }
        StringJoiner placeholders = new StringJoiner(", ", "(", ")");
        values.forEach(v -> placeholders.add("?"));
        return new Condition(column.getQualifiedName() + " IN " + placeholders, new ArrayList<>(values));
    }

    static Condition columnCompare(Column<?> left, String op, Column<?> right) {
        return new Condition(left.getQualifiedName() + " " + op + " " + right.getQualifiedName(), List.of());
    }

    // 서브쿼리 비교: col = (SELECT ...), col > (SELECT ...) 등
    static Condition binarySubquery(Column<?> col, String op, SelectStep subquery) {
        return new Condition(
            col.getQualifiedName() + " " + op + " (" + subquery.toSql() + ")",
            new ArrayList<>(subquery.collectAllBindings())
        );
    }

    // col IN (SELECT ...)
    static Condition inSubquery(Column<?> col, SelectStep subquery) {
        return new Condition(
            col.getQualifiedName() + " IN (" + subquery.toSql() + ")",
            new ArrayList<>(subquery.collectAllBindings())
        );
    }

    // col NOT IN (SELECT ...)
    static Condition notInSubquery(Column<?> col, SelectStep subquery) {
        return new Condition(
            col.getQualifiedName() + " NOT IN (" + subquery.toSql() + ")",
            new ArrayList<>(subquery.collectAllBindings())
        );
    }

    // EXISTS (SELECT ...)
    public static Condition exists(SelectStep subquery) {
        return new Condition(
            "EXISTS (" + subquery.toSql() + ")",
            new ArrayList<>(subquery.collectAllBindings())
        );
    }

    // NOT EXISTS (SELECT ...)
    public static Condition notExists(SelectStep subquery) {
        return new Condition(
            "NOT EXISTS (" + subquery.toSql() + ")",
            new ArrayList<>(subquery.collectAllBindings())
        );
    }

    public static Condition raw(String sql, Object... bindings) {
        return new Condition(sql, new ArrayList<>(Arrays.asList(bindings)));
    }
}
