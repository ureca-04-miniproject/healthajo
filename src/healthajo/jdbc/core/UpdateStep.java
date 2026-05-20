package healthajo.jdbc.core;

import healthajo.jdbc.factory.JdbcConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class UpdateStep {

    private final String tableName;
    private final List<Column<?>> columns = new ArrayList<>();
    private final List<Object> values = new ArrayList<>();
    private Condition condition;

    UpdateStep(String tableName) {
        this.tableName = tableName;
    }

    public <T> UpdateStep set(Column<T> column, T value) {
        columns.add(column);
        values.add(value);
        return this;
    }

    public UpdateStep where(Condition condition) {
        this.condition = condition;
        return this;
    }

    public int execute() {
        if (columns.isEmpty()) {
            throw new IllegalStateException("UPDATE할 컬럼이 하나도 지정되지 않았습니다.");
        }
        if (condition == null) {
            throw new IllegalStateException("WHERE 없는 UPDATE는 허용하지 않습니다.");
        }
        String sql = buildSql();
        List<Object> allBindings = new ArrayList<>(values);
        if (condition != null) allBindings.addAll(condition.getBindings());
        System.out.println("[SQL] " + sql);
        System.out.println("[BIND] " + allBindings);
        try (JdbcConnectionFactory.JdbcConnection jc = JdbcConnectionFactory.getInstance().getConnection()) {
            Connection conn = jc.get();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                bindParameters(ps);
                return ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("UPDATE 실행 실패: " + sql, e);
        }
    }

    private String buildSql() {
        StringJoiner setClauses = new StringJoiner(", ");
        for (Column<?> col : columns) {
            setClauses.add(col.getName() + " = ?");
        }
        StringBuilder sb = new StringBuilder("UPDATE ").append(tableName).append(" SET ").append(setClauses);
        if (condition != null) {
            sb.append(" WHERE ").append(condition.getSql());
        }
        return sb.toString();
    }

    private void bindParameters(PreparedStatement ps) throws SQLException {
        int idx = 1;
        for (Object val : values) {
            ps.setObject(idx++, val);
        }
        if (condition != null) {
            for (Object binding : condition.getBindings()) {
                ps.setObject(idx++, binding);
            }
        }
    }
}
