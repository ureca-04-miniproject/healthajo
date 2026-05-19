package healthajo.jdbc.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

public class WindowFunction implements Field {

    private final String functionSql;
    private final List<Object> boundValues = new ArrayList<>();
    private final List<Column<?>> partitionCols = new ArrayList<>();
    private final List<String> orderByClauses = new ArrayList<>();
    private String alias;

    private WindowFunction(String functionSql) {
        this.functionSql = functionSql;
    }

    private WindowFunction copy() {
        WindowFunction wf = new WindowFunction(this.functionSql);
        wf.boundValues.addAll(this.boundValues);
        wf.partitionCols.addAll(this.partitionCols);
        wf.orderByClauses.addAll(this.orderByClauses);
        wf.alias = this.alias;
        return wf;
    }

    @Override
    public List<Object> getBindings() {
        return Collections.unmodifiableList(boundValues);
    }

    // ── 순위 함수 ──────────────────────────────────────────────────────
    public static WindowFunction rowNumber()   { return new WindowFunction("ROW_NUMBER()"); }
    public static WindowFunction rank()        { return new WindowFunction("RANK()"); }
    public static WindowFunction denseRank()   { return new WindowFunction("DENSE_RANK()"); }
    public static WindowFunction percentRank() { return new WindowFunction("PERCENT_RANK()"); }
    public static WindowFunction cumeDist()    { return new WindowFunction("CUME_DIST()"); }
    public static WindowFunction ntile(int n)  { return new WindowFunction("NTILE(" + n + ")"); }

    // ── 집계 함수 ──────────────────────────────────────────────────────
    public static WindowFunction sum(Column<?> col)   { return new WindowFunction("SUM(" + col.getQualifiedName() + ")"); }
    public static WindowFunction count(Column<?> col) { return new WindowFunction("COUNT(" + col.getQualifiedName() + ")"); }
    public static WindowFunction countStar()          { return new WindowFunction("COUNT(*)"); }
    public static WindowFunction avg(Column<?> col)   { return new WindowFunction("AVG(" + col.getQualifiedName() + ")"); }
    public static WindowFunction min(Column<?> col)   { return new WindowFunction("MIN(" + col.getQualifiedName() + ")"); }
    public static WindowFunction max(Column<?> col)   { return new WindowFunction("MAX(" + col.getQualifiedName() + ")"); }

    // ── 오프셋 함수 ────────────────────────────────────────────────────
    public static WindowFunction lag(Column<?> col)                    { return new WindowFunction("LAG(" + col.getQualifiedName() + ")"); }
    public static WindowFunction lag(Column<?> col, int offset)        { return new WindowFunction("LAG(" + col.getQualifiedName() + ", " + offset + ")"); }
    public static WindowFunction lag(Column<?> col, int offset, long defaultVal)   { return new WindowFunction("LAG(" + col.getQualifiedName() + ", " + offset + ", " + defaultVal + ")"); }
    public static WindowFunction lag(Column<?> col, int offset, String defaultVal) {
        WindowFunction wf = new WindowFunction("LAG(" + col.getQualifiedName() + ", " + offset + ", ?)");
        wf.boundValues.add(defaultVal);
        return wf;
    }

    public static WindowFunction lead(Column<?> col)                   { return new WindowFunction("LEAD(" + col.getQualifiedName() + ")"); }
    public static WindowFunction lead(Column<?> col, int offset)       { return new WindowFunction("LEAD(" + col.getQualifiedName() + ", " + offset + ")"); }
    public static WindowFunction lead(Column<?> col, int offset, long defaultVal)  { return new WindowFunction("LEAD(" + col.getQualifiedName() + ", " + offset + ", " + defaultVal + ")"); }

    public static WindowFunction firstValue(Column<?> col) { return new WindowFunction("FIRST_VALUE(" + col.getQualifiedName() + ")"); }
    public static WindowFunction lastValue(Column<?> col)  { return new WindowFunction("LAST_VALUE(" + col.getQualifiedName() + ")"); }
    public static WindowFunction nthValue(Column<?> col, int n) { return new WindowFunction("NTH_VALUE(" + col.getQualifiedName() + ", " + n + ")"); }

    // ── OVER 절 빌더 (각 메서드는 새 인스턴스를 반환 — 상태 누적 방지) ────────
    public WindowFunction over() { return this; }

    public WindowFunction partitionBy(Column<?>... cols) {
        WindowFunction wf = copy();
        wf.partitionCols.addAll(Arrays.asList(cols));
        return wf;
    }

    public WindowFunction orderBy(Column<?>... cols) {
        WindowFunction wf = copy();
        for (Column<?> col : cols) wf.orderByClauses.add(col.getQualifiedName());
        return wf;
    }

    public WindowFunction orderByDesc(Column<?>... cols) {
        WindowFunction wf = copy();
        for (Column<?> col : cols) wf.orderByClauses.add(col.getQualifiedName() + " DESC");
        return wf;
    }

    public WindowFunction as(String alias) {
        WindowFunction wf = copy();
        wf.alias = alias;
        return wf;
    }

    @Override
    public String toSqlWithAlias() {
        StringBuilder sb = new StringBuilder(functionSql).append(" OVER (");
        if (!partitionCols.isEmpty()) {
            StringJoiner sj = new StringJoiner(", ");
            for (Column<?> col : partitionCols) sj.add(col.getQualifiedName());
            sb.append("PARTITION BY ").append(sj);
        }
        if (!orderByClauses.isEmpty()) {
            if (!partitionCols.isEmpty()) sb.append(" ");
            sb.append("ORDER BY ").append(String.join(", ", orderByClauses));
        }
        sb.append(")");
        if (alias != null) sb.append(" AS ").append(alias);
        return sb.toString();
    }
}
