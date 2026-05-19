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

    private final String tableName;
    private final Field[] fields;
    private final List<JoinClause> joins = new ArrayList<>();
    private Condition condition;
    private final List<String> groupByClauses = new ArrayList<>();
    private Condition havingCondition;
    private final List<String> orderByClauses = new ArrayList<>();
    private Integer limitVal;

    SelectStep(String tableName, Field[] fields) {
        this.tableName = tableName;
        this.fields = fields;
    }

    void addJoin(JoinClause join) {
        joins.add(join);
    }

    // ── JOIN ──────────────────────────────────────────────────────────
    public JoinOnStep join(TableBase table) {
        return new JoinOnStep(this, JoinClause.Type.INNER, table.getTableName());
    }

    public JoinOnStep leftJoin(TableBase table) {
        return new JoinOnStep(this, JoinClause.Type.LEFT, table.getTableName());
    }

    public JoinOnStep rightJoin(TableBase table) {
        return new JoinOnStep(this, JoinClause.Type.RIGHT, table.getTableName());
    }

    public SelectStep crossJoin(TableBase table) {
        addJoin(new JoinClause(JoinClause.Type.CROSS, table.getTableName(), null));
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
                bindParameters(ps);
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

    // ── SQL 빌드 ──────────────────────────────────────────────────────
    private String buildSql() {
        StringBuilder sb = new StringBuilder("SELECT ");
        if (fields == null || fields.length == 0) {
            sb.append(joins.isEmpty() ? tableName + ".*" : "*");
        } else {
            StringJoiner cols = new StringJoiner(", ");
            for (Field field : fields) cols.add(field.toSqlWithAlias());
            sb.append(cols);
        }
        sb.append(" FROM ").append(tableName);
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

    private void bindParameters(PreparedStatement ps) throws SQLException {
        int idx = 1;
        if (fields != null) {
            for (Field field : fields) {
                for (Object binding : field.getBindings()) {
                    ps.setObject(idx++, binding);
                }
            }
        }
        for (JoinClause join : joins) {
            for (Object binding : join.getBindings()) {
                ps.setObject(idx++, binding);
            }
        }
        if (condition != null) {
            for (Object binding : condition.getBindings()) {
                ps.setObject(idx++, binding);
            }
        }
        if (havingCondition != null) {
            for (Object binding : havingCondition.getBindings()) {
                ps.setObject(idx++, binding);
            }
        }
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
                    // 정규화된 키(table.column)로 저장 — JOIN 시 동명 컬럼 충돌 방지
                    row.put(tableRef.toLowerCase() + "." + label, value);
                    // 단순 이름은 먼저 나온 컬럼이 우선
                    row.putIfAbsent(label, value);
                } else {
                    // 윈도우 함수 alias 등 테이블 정보 없는 표현식
                    row.put(label, value);
                }
            }
            records.add(new Record(row));
        }
        return records;
    }
}
