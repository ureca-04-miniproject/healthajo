package healthajo.jdbc.core;

/**
 * 테이블에 SQL 별칭을 붙인 뷰. {@code TableBase.as(String)} 또는 구체 테이블의 {@code as()} 로 생성.
 * {@code TableBase}를 상속하므로 {@code asterisk()}, {@code select()}, {@code insertInto()} 등
 * 모든 TableBase 기능을 그대로 사용할 수 있다.
 *
 * <pre>
 * TUser u = USER.as("u");            // TUser.as() 오버라이드 → 타입 컬럼 접근 가능
 * TableBase t = SOME_TABLE.as("t");  // 기본 fallback → col() 로 컬럼 접근
 * u.asterisk()  // → u.*
 * t.asterisk()  // → t.*
 * </pre>
 */
public class AliasedTable extends TableBase {

    AliasedTable(String realName, String alias) {
        super(realName, alias);
    }

    /**
     * alias를 prefix로 사용하는 컬럼 반환.
     * 셀프 조인 등에서 같은 테이블을 다른 alias로 구분할 때 사용.
     *
     * <pre>
     * AliasedTable a1 = (AliasedTable) USER.as("a1");
     * AliasedTable a2 = (AliasedTable) USER.as("a2");
     * a1.select(a1.col(USER.ID), a2.col(USER.ID).as("other_id"))
     * </pre>
     */
    public <T> Column<T> col(Column<T> original) {
        return new Column<>(getPrefix(), original.getName(), original.getType());
    }
}
