package healthajo.auth;

import healthajo.jdbc.core.Record;
import healthajo.jdbc.table.TUser;
import healthajo.users.UsersDAO;
import healthajo.util.HashUtils;
import healthajo.util.PhoneUtils;

/**
 * 인증 서비스.
 * - 회원: 전화번호로 users 테이블 조회 (deleted_at IS NULL)
 * - 관리자: 입력 코드를 SHA-256 해시해 login_code_hash와 비교 (role='ADMIN')
 */
public class AuthService {

    private static final TUser USER = TUser.USER;

    private final UsersDAO usersDao = new UsersDAO();

    public AuthResult authenticate(String input, boolean isAdmin) {
        if (input == null || input.isBlank()) {
            return AuthResult.fail("입력값이 비어 있습니다.");
        }
        return isAdmin ? authenticateAdmin(input) : authenticateUser(input);
    }

    private AuthResult authenticateAdmin(String code) {
        try {
            Record admin = usersDao.findActiveAdminByCodeHash(HashUtils.sha256(code));
            if (admin == null) {
                return AuthResult.fail("관리자 코드가 올바르지 않습니다.");
            }
            return AuthResult.ok(AuthResult.Role.ADMIN, admin.get(USER.ID));
        } catch (RuntimeException e) {
            return AuthResult.fail("로그인 처리 중 오류가 발생했습니다.");
        }
    }

    private AuthResult authenticateUser(String raw) {
        String phone = PhoneUtils.normalize(raw);
        if (phone == null) {
            return AuthResult.fail("전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678)");
        }
        try {
            Record user = usersDao.findByPhone(phone);
            if (user == null) {
                return AuthResult.fail("등록되지 않은 전화번호입니다.");
            }
            return AuthResult.ok(AuthResult.Role.USER, user.get(USER.ID));
        } catch (RuntimeException e) {
            return AuthResult.fail("로그인 처리 중 오류가 발생했습니다.");
        }
    }
}
