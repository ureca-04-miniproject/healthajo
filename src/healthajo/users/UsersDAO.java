package healthajo.users;

import healthajo.jdbc.core.Page;
import healthajo.jdbc.table.TUser;
import healthajo.jdbc.core.Record;
import java.sql.Timestamp;
import java.util.List;
import healthajo.jdbc.core.Field;

public class UsersDAO {

    public static final TUser T = TUser.USER;

    public List<Record> findAll() {
        return T.select()
                .where(T.DELETED_AT.isNull())
                .orderBy(T.ID)
                .fetch();
    }

    public Page<Record> findAllWithStats(int pageNumber, int pageSize) {
        return Page.of(
                T.select(
                                T.ID,
                                T.NAME,
                                T.PHONE,
                                T.EMAIL,
                                T.CREATED_AT,
                                field("(SELECT COALESCE(SUM(m.remaining_count), 0) " +
                                        "FROM memberships m WHERE m.user_id = users.id) AS membership_count")
                        )
                        .where(T.DELETED_AT.isNull())
                        .orderBy(T.ID),
                pageNumber,
                pageSize
        );
    }
    private static Field field(String sql) {
        return () -> sql;
    }

    public List<Record> search(String keyword) {
        return T.select()
                .where(T.DELETED_AT.isNull()
                        .and(T.NAME.like("%" + keyword + "%")
                                .or(T.PHONE.like("%" + keyword + "%"))))
                .orderBy(T.ID)
                .fetch();
    }

    public Record findById(Long id) {
        return T.select()
                .where(T.ID.eq(id))
                .fetchOne();
    }

    public Record findByPhone(String phone) {
        return T.select()
                .where(T.DELETED_AT.isNull()
                        .and(T.PHONE.eq(phone)))
                .fetchOne();
    }

    public int insert(String name, String phone, String email, String role) {
        return T.insertInto()
                .set(T.NAME, name)
                .set(T.PHONE, phone)
                .set(T.EMAIL, email)
                .set(T.ROLE, role)
                .set(T.UPDATED_AT, new Timestamp(System.currentTimeMillis()))
                .execute();
    }

    public void update(Long id, String name, String phone, String email) {
        T.update()
                .set(T.NAME, name)
                .set(T.PHONE, phone)
                .set(T.EMAIL, email)
                .set(T.UPDATED_AT, new Timestamp(System.currentTimeMillis()))
                .where(T.ID.eq(id))
                .execute();
    }

    public void delete(Long id) {
        T.update()
                .set(T.DELETED_AT, new Timestamp(System.currentTimeMillis()))
                .set(T.UPDATED_AT, new Timestamp(System.currentTimeMillis()))
                .where(T.ID.eq(id))
                .execute();
    }

}
