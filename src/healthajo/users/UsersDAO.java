package healthajo.users;

import healthajo.jdbc.table.TUser;
import healthajo.jdbc.core.Record;
import java.sql.Timestamp;
import java.util.List;

public class UsersDAO {

    public static final TUser T = TUser.USER;

    public List<Record> findAll() {
        return T.select()
                .where(T.DELETED_AT.isNull())
                .orderBy(T.ID)
                .fetch();
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
