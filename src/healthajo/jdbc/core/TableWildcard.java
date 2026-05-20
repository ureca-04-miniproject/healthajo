package healthajo.jdbc.core;

import java.util.List;

/**
 * {@code prefix.*} 를 SELECT 절 Field로 표현.
 *
 * <pre>
 * TUser u = USER.as("u");
 * TMembership m = MEMBERSHIP.as("m");
 * u.select(u.asterisk(), m.asterisk()).join(m).on(u.ID.eq(m.USER_ID)).fetch();
 * // → SELECT u.*, m.* FROM users AS u INNER JOIN memberships AS m ON u.id = m.user_id
 * </pre>
 */
public class TableWildcard implements Field {

    private final String prefix;

    TableWildcard(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String toSqlWithAlias() {
        return prefix + ".*";
    }

    @Override
    public List<Object> getBindings() {
        return List.of();
    }
}
