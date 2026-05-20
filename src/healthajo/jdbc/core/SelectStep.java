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
    private Integer offsetVal;

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

    /** 페이지네이션 offset 지정. LIMIT 없이 단독 사용 시 LIMIT 200 적용. */
    public SelectStep offset(int n) {
        this.offsetVal = n;
        return this;
    }

    /**
     * step 상태를 변형하지 않고 LIMIT/OFFSET을 적용한 결과를 반환.
     * {@link Page#of} 내부에서 사용 — 호출 후에도 step을 재사용할 수 있다.
     */
    List<Record> fetchWithRange(int limit, int rawOffset) {
        String sql = buildCoreSql();
        StringBuilder sb = new StringBuilder(sql);
        if (!orderByClauses.isEmpty())
            sb.append(" ORDER BY ").append(String.join(", ", orderByClauses));
        sb.append(" LIMIT ").append(limit).append(" OFFSET ").append(rawOffset);

        List<Object> bindings = collectAllBindings();
        logSql(sb.toString(), bindings);
        try (JdbcConnectionFactory.JdbcConnection jc = JdbcConnectionFactory.getInstance().getConnection()) {
            Connection conn = jc.get();
            try (PreparedStatement ps = conn.prepareStatement(sb.toString())) {
                int idx = 1;
                for (Object binding : bindings) ps.setObject(idx++, binding);
                try (ResultSet rs = ps.executeQuery()) { return mapResults(rs); }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("SELECT 실행 실패: " + sb, e);
        }
    }

    public String toSql() { return buildSql(); }

    // ── 실행 ──────────────────────────────────────────────────────────
    public List<Record> fetch() {
        String sql = buildSql();
        List<Object> bindings = collectAllBindings();
        logSql(sql, bindings);
        try (JdbcConnectionFactory.JdbcConnection jc = JdbcConnectionFactory.getInstance().getConnection()) {
            Connection conn = jc.get();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int idx = 1;
                for (Object binding : bindings) {
                    ps.setObject(idx++, binding);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    return mapResults(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
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

    /**
     * WHERE·JOIN 조건은 유지한 채 전체 행 수를 반환.
     * ORDER BY·LIMIT·OFFSET은 COUNT에 영향을 주지 않으므로 제거 후
     * {@code SELECT COUNT(*) FROM (inner) AS _count_wrap} 으로 감쌈.
     */
    public long fetchCount() {
        String sql = "SELECT COUNT(*) FROM (" + buildCoreSql() + ") AS _count_wrap";
        List<Object> bindings = collectAllBindings();
        logSql(sql, bindings);
        try (JdbcConnectionFactory.JdbcConnection jc = JdbcConnectionFactory.getInstance().getConnection()) {
            Connection conn = jc.get();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int idx = 1;
                for (Object binding : bindings) {
                    ps.setObject(idx++, binding);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getLong(1) : 0L;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("COUNT 실행 실패: " + sql, e);
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

    /** SELECT … FROM … JOIN … WHERE … GROUP BY … HAVING — ORDER BY·LIMIT·OFFSET 제외 */
    private String buildCoreSql() {
        StringBuilder sb = new StringBuilder("SELECT ");
        if (fields == null || fields.length == 0) {
            if (joins.isEmpty()) {
                sb.append(fromSource.getPrefix()).append(".*");
            } else {
                // 필드 미지정 + JOIN → a.*, b.*, c.* 자동 생성
                StringJoiner sj = new StringJoiner(", ");
                sj.add(fromSource.getPrefix() + ".*");
                for (JoinClause join : joins) sj.add(join.getSourcePrefix() + ".*");
                sb.append(sj);
            }
        } else {
            StringJoiner cols = new StringJoiner(", ");
            for (Field field : fields) cols.add(field.toSqlWithAlias());
            sb.append(cols);
        }
        sb.append(" FROM ").append(fromSource.toFromSql());
        for (JoinClause join : joins) sb.append(" ").append(join.toSql());
        if (condition != null)        sb.append(" WHERE ").append(condition.getSql());
        if (!groupByClauses.isEmpty()) sb.append(" GROUP BY ").append(String.join(", ", groupByClauses));
        if (havingCondition != null)  sb.append(" HAVING ").append(havingCondition.getSql());
        return sb.toString();
    }

    private String buildSql() {
        StringBuilder sb = new StringBuilder(buildCoreSql());
        if (!orderByClauses.isEmpty()) {
            sb.append(" ORDER BY ").append(String.join(", ", orderByClauses));
        }
        if (limitVal != null || offsetVal != null) {
            int effectiveLimit = limitVal != null ? limitVal : 200;
            sb.append(" LIMIT ").append(effectiveLimit);
            if (offsetVal != null) {
                // limit 명시 → offset은 raw 값
                // limit 미지정 → offset을 page number로 해석해 effectiveLimit * page
                int effectiveOffset = limitVal != null ? offsetVal : effectiveLimit * offsetVal;
                sb.append(" OFFSET ").append(effectiveOffset);
            }
        }
        return sb.toString();
    }

    private static void logSql(String sql, List<Object> bindings) {
        System.out.println("[SQL] " + sql);
        if (!bindings.isEmpty()) System.out.println("[BIND] " + bindings);
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
