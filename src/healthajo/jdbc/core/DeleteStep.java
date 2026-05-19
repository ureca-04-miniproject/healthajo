package healthajo.jdbc.core;

import healthajo.jdbc.factory.JdbcConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class DeleteStep {

    private final String tableName;
    private Condition condition;

    DeleteStep(String tableName) {
        this.tableName = tableName;
    }

    public DeleteStep where(Condition condition) {
        this.condition = condition;
        return this;
    }

    public int execute() {
        if (condition == null) {
            throw new IllegalStateException("WHERE 없는 DELETE는 허용하지 않습니다.");
        }
        String sql = buildSql();
        try (JdbcConnectionFactory.JdbcConnection jc = JdbcConnectionFactory.getInstance().getConnection()) {
            Connection conn = jc.get();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                bindParameters(ps);
                return ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("DELETE 실행 실패: " + sql, e);
        }
    }

    private String buildSql() {
        StringBuilder sb = new StringBuilder("DELETE FROM ").append(tableName);
        if (condition != null) {
            sb.append(" WHERE ").append(condition.getSql());
        }
        return sb.toString();
    }

    private void bindParameters(PreparedStatement ps) throws SQLException {
        if (condition == null) return;
        List<Object> bindings = condition.getBindings();
        for (int i = 0; i < bindings.size(); i++) {
            ps.setObject(i + 1, bindings.get(i));
        }
    }
}
