package healthajo.jdbc.core;

import java.util.List;

class JoinClause {

    enum Type { INNER, LEFT, RIGHT, CROSS }

    private final Type type;
    private final String tableName;
    private final Condition on;

    JoinClause(Type type, String tableName, Condition on) {
        this.type = type;
        this.tableName = tableName;
        this.on = on;
    }

    String toSql() {
        String keyword = switch (type) {
          case LEFT -> "LEFT JOIN";
          case RIGHT -> "RIGHT JOIN";
          case CROSS -> "CROSS JOIN";
          default -> "INNER JOIN";
        };
      StringBuilder sb = new StringBuilder(keyword).append(" ").append(tableName);
        if (on != null) sb.append(" ON ").append(on.getSql());
        return sb.toString();
    }

    List<Object> getBindings() {
        return on != null ? on.getBindings() : List.of();
    }
}
