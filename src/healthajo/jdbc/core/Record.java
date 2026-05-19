package healthajo.jdbc.core;

import java.util.Collections;
import java.util.Map;

public class Record {

    private final Map<String, Object> values;

    public Record(Map<String, Object> values) {
        this.values = Collections.unmodifiableMap(values);
    }

    public <T> T get(Column<T> column) {
        String qualifiedKey = column.getQualifiedName().toLowerCase();
        String simpleKey    = column.getName().toLowerCase();
        // containsKey로 "키 없음"과 "값이 진짜 NULL"을 구분
        // → LEFT JOIN 불일치 시 null을 올바르게 반환
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
        return values.get(name);
    }

    public Map<String, Object> toMap() {
        return values;
    }
}
