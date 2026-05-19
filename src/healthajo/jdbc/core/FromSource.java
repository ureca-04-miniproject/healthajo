package healthajo.jdbc.core;

import java.util.List;

public interface FromSource {
    String toFromSql();         // FROM/JOIN 절에 들어갈 SQL: "users" | "users AS a" | "(SELECT ...) AS sub"
    String getPrefix();         // SELECT * 시 prefix: "users" | "a" | "sub"
    List<Object> getBindings(); // 서브쿼리의 바인딩 값 (일반 테이블은 빈 리스트)
}
