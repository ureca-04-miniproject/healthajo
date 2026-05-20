package healthajo.auth;

import healthajo.util.PhoneUtils;

/**
 * 인증 서비스.
 * - 회원: 전화번호로 DB 조회 (deleted_at IS NULL)
 * - 관리자: 코드 해시 비교 (현재 평문 stub — BCrypt로 교체 예정)
 */
public class AuthService {

    private static final String ADMIN_CODE_STUB = "ADMIN1234";

    public AuthResult authenticate(String input, boolean isAdmin) {
        if (input == null || input.isBlank()) {
            return AuthResult.fail("입력값이 비어 있습니다.");
        }
        return isAdmin ? authenticateAdmin(input) : authenticateUser(input);
    }

    private AuthResult authenticateAdmin(String code) {
        // TODO: BCrypt.checkpw(code, storedHash)
        if (ADMIN_CODE_STUB.equals(code)) {
            return AuthResult.ok(AuthResult.Role.ADMIN);
        }
        return AuthResult.fail("관리자 코드가 올바르지 않습니다.");
    }

    private AuthResult authenticateUser(String raw) {
        String phone = PhoneUtils.normalize(raw);
        if (phone == null) {
            return AuthResult.fail("전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678)");
        }
        try {
          // TODO 전화 번호 가져오기
            if (phone.isEmpty()) {
                return AuthResult.fail("등록되지 않은 전화번호입니다.");
            }
            return AuthResult.ok(AuthResult.Role.USER, 1L);
        } catch (RuntimeException e) {
            // DB 미연결 상태(개발 중) — 형식만 통과하면 stub 허용
            return AuthResult.ok(AuthResult.Role.USER, null);
        }
    }
}
