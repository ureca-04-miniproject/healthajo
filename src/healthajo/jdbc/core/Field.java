package healthajo.jdbc.core;

import java.util.List;

public interface Field {
    String toSqlWithAlias();
    default List<Object> getBindings() { return List.of(); }
}
