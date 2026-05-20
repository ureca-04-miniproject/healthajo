package healthajo.jdbc.core;

import java.util.Collections;
import java.util.Map;

public class Record {

    private final Map<String, Object> values;

    public Record(Map<String, Object> values) {
        this.values = Collections.unmodifiableMap(values);
    }

    public <T> T get(Column<T> column) {
        // alias가 있으면 ResultSet 컬럼 레이블이 alias 이름으로 저장되므로 우선 조회
        String alias = column.getAlias();
        if (alias != null) {
            String aliasQualKey  = column.getTableName().toLowerCase() + "." + alias.toLowerCase();
            String aliasSimpleKey = alias.toLowerCase();
            if (values.containsKey(aliasQualKey)) {
                Object val = values.get(aliasQualKey);
                return val == null ? null : column.getType().cast(val);
            }
            if (values.containsKey(aliasSimpleKey)) {
                Object val = values.get(aliasSimpleKey);
                return val == null ? null : column.getType().cast(val);
            }
        }
        // alias 없음 또는 alias 조회 실패 → 원래 컬럼명으로 fallback
        // containsKey로 "키 없음"과 "값이 진짜 NULL"을 구분
        // → LEFT JOIN 불일치 시 null을 올바르게 반환
        String qualifiedKey = column.getQualifiedName().toLowerCase();
        String simpleKey    = column.getName().toLowerCase();
        if (values.containsKey(qualifiedKey)) {
            Object val = values.get(qualifiedKey);
            return val == null ? null : column.getType().cast(val);
        }
        if (values.containsKey(simpleKey)) {
            Object val = values.get(simpleKey);
            return val == null ? null : column.getType().cast(val);
        }
        return null;
    }

    public Object get(String name) {
        return values.get(name.toLowerCase());
    }

    public <T> T get(String name, Class<T> type) {
        Object val = values.get(name.toLowerCase());
        return val == null ? null : type.cast(val);
    }

    public Map<String, Object> toMap() {
        return values;
    }
}
