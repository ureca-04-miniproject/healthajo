package healthajo.jdbc.core;

import healthajo.jdbc.factory.JdbcConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class SelectStep {

    private final FromSource fromSource;
    private final Field[] fields;
    private final List<JoinClause> joins = new ArrayList<>();
    private Condition condition;
    private final List<String> groupByClauses = new ArrayList<>();
    private Condition havingCondition;
    private final List<String> orderByClauses = new ArrayList<>();
    private Integer limitVal;

    SelectStep(FromSource fromSource, Field[] fields) {
        this.fromSource = fromSource;
        this.fields = fields;
    }

    void addJoin(JoinClause join) {
        joins.add(join);
    }

    // ── JOIN ──────────────────────────────────────────────────────────
    public JoinOnStep join(FromSource source) {
        return new JoinOnStep(this, JoinClause.Type.INNER, source);
    }

    public JoinOnStep leftJoin(FromSource source) {
        return new JoinOnStep(this, JoinClause.Type.LEFT, source);
    }

    public JoinOnStep rightJoin(FromSource source) {
        return new JoinOnStep(this, JoinClause.Type.RIGHT, source);
    }

    public SelectStep crossJoin(FromSource source) {
        addJoin(new JoinClause(JoinClause.Type.CROSS, source, null));
        return this;
    }

    // ── WHERE / GROUP BY / HAVING ─────────────────────────────────────
    public SelectStep where(Condition condition) {
        this.condition = condition;
        return this;
    }

    public SelectStep groupBy(Column<?>... cols) {
        for (Column<?> col : cols) groupByClauses.add(col.getQualifiedName());
        return this;
    }

    public SelectStep having(Condition condition) {
        this.havingCondition = condition;
        return this;
    }

    // ── ORDER BY / LIMIT ──────────────────────────────────────────────
    public SelectStep orderBy(Column<?>... cols) {
        for (Column<?> col : cols) orderByClauses.add(col.getQualifiedName());
        return this;
    }

    public SelectStep orderByDesc(Column<?>... cols) {
        for (Column<?> col : cols) orderByClauses.add(col.getQualifiedName() + " DESC");
        return this;
    }

    public SelectStep limit(int n) {
        this.limitVal = n;
        return this;
    }

    public String toSql() { return buildSql(); }

    // ── 실행 ──────────────────────────────────────────────────────────
    public List<Record> fetch() {
        String sql = buildSql();
        try (JdbcConnectionFactory.JdbcConnection jc = JdbcConnectionFactory.getInstance().getConnection()) {
            Connection conn = jc.get();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int idx = 1;
                for (Object binding : collectAllBindings()) {
                    ps.setObject(idx++, binding);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    return mapResults(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("SELECT 실행 실패: " + sql, e);
        }
    }

    public Record fetchOne() {
        Integer prev = this.limitVal;
        this.limitVal = 1;
        try {
            List<Record> results = fetch();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            this.limitVal = prev;
        }
    }

    // ── 바인딩 수집 (서브쿼리 중첩 포함) ─────────────────────────────
    public List<Object> collectAllBindings() {
        List<Object> all = new ArrayList<>();
        // SELECT 절 (윈도우 함수 바인딩)
        if (fields != null) {
            for (Field f : fields) all.addAll(f.getBindings());
        }
        // FROM 절 (서브쿼리 바인딩)
        all.addAll(fromSource.getBindings());
        // JOIN 절 (서브쿼리 JOIN 바인딩 + ON 바인딩)
        for (JoinClause join : joins) all.addAll(join.getBindings());
        // WHERE 절
        if (condition != null) all.addAll(condition.getBindings());
        // HAVING 절
        if (havingCondition != null) all.addAll(havingCondition.getBindings());
        return all;
    }

    // ── SQL 빌드 ──────────────────────────────────────────────────────
    private String buildSql() {
        StringBuilder sb = new StringBuilder("SELECT ");
        if (fields == null || fields.length == 0) {
            sb.append(joins.isEmpty() ? fromSource.getPrefix() + ".*" : "*");
        } else {
            StringJoiner cols = new StringJoiner(", ");
            for (Field field : fields) cols.add(field.toSqlWithAlias());
            sb.append(cols);
        }
        sb.append(" FROM ").append(fromSource.toFromSql());
        for (JoinClause join : joins) {
            sb.append(" ").append(join.toSql());
        }
        if (condition != null) {
            sb.append(" WHERE ").append(condition.getSql());
        }
        if (!groupByClauses.isEmpty()) {
            sb.append(" GROUP BY ").append(String.join(", ", groupByClauses));
        }
        if (havingCondition != null) {
            sb.append(" HAVING ").append(havingCondition.getSql());
        }
        if (!orderByClauses.isEmpty()) {
            sb.append(" ORDER BY ").append(String.join(", ", orderByClauses));
        }
        if (limitVal != null) {
            sb.append(" LIMIT ").append(limitVal);
        }
        return sb.toString();
    }

    private List<Record> mapResults(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();
        List<Record> records = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String tableRef = meta.getTableName(i);
                String label = meta.getColumnLabel(i).toLowerCase();
                Object value = rs.getObject(i);
                if (tableRef != null && !tableRef.isEmpty()) {
                    row.put(tableRef.toLowerCase() + "." + label, value);
                    row.putIfAbsent(label, value);
                } else {
                    row.put(label, value);
                }
            }
            records.add(new Record(row));
        }
        return records;
    }
}
