package healthajo.jdbc.core;

import healthajo.jdbc.factory.JdbcConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class InsertStep {

    private final String tableName;
    private final List<Column<?>> columns = new ArrayList<>();
    private final List<Object> values = new ArrayList<>();

    InsertStep(String tableName) {
        this.tableName = tableName;
    }

    public <T> InsertStep set(Column<T> column, T value) {
        columns.add(column);
        values.add(value);
        return this;
    }

    public int execute() {
        if (columns.isEmpty()) {
            throw new IllegalStateException("INSERT할 컬럼이 하나도 지정되지 않았습니다.");
        }
        String sql = buildSql();
        try (JdbcConnectionFactory.JdbcConnection jc = JdbcConnectionFactory.getInstance().getConnection()) {
            Connection conn = jc.get();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                bindParameters(ps);
                return ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("INSERT 실행 실패: " + sql, e);
        }
    }

    public long executeAndReturnKey() {
        if (columns.isEmpty()) {
            throw new IllegalStateException("INSERT할 컬럼이 하나도 지정되지 않았습니다.");
        }
        String sql = buildSql();
        try (JdbcConnectionFactory.JdbcConnection jc = JdbcConnectionFactory.getInstance().getConnection()) {
            Connection conn = jc.get();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                bindParameters(ps);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getLong(1);
                    throw new RuntimeException("생성된 키를 가져올 수 없습니다.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("INSERT 실행 실패: " + sql, e);
        }
    }

    private String buildSql() {
        StringJoiner cols = new StringJoiner(", ", "(", ")");
        StringJoiner placeholders = new StringJoiner(", ", "(", ")");
        for (Column<?> col : columns) {
            cols.add(col.getName());
            placeholders.add("?");
        }
        return "INSERT INTO " + tableName + " " + cols + " VALUES " + placeholders;
    }

    private void bindParameters(PreparedStatement ps) throws SQLException {
        for (int i = 0; i < values.size(); i++) {
            ps.setObject(i + 1, values.get(i));
        }
    }
}
