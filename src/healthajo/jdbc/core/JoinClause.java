package healthajo.jdbc.core;

import java.util.ArrayList;
import java.util.List;

class JoinClause {

    enum Type { INNER, LEFT, RIGHT, CROSS }

    private final Type type;
    private final FromSource joinSource;
    private final Condition on;

    JoinClause(Type type, FromSource joinSource, Condition on) {
        this.type = type;
        this.joinSource = joinSource;
        this.on = on;
    }

    String getSourcePrefix() { return joinSource.getPrefix(); }

    String toSql() {
        String keyword = switch (type) {
            case LEFT  -> "LEFT JOIN";
            case RIGHT -> "RIGHT JOIN";
            case CROSS -> "CROSS JOIN";
            default    -> "INNER JOIN";
        };
        StringBuilder sb = new StringBuilder(keyword).append(" ").append(joinSource.toFromSql());
        if (on != null) sb.append(" ON ").append(on.getSql());
        return sb.toString();
    }

    List<Object> getBindings() {
        List<Object> all = new ArrayList<>(joinSource.getBindings()); // 서브쿼리 JOIN 시 바인딩
        if (on != null) all.addAll(on.getBindings());
        return all;
    }
}
