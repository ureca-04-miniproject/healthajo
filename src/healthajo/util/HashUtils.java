package healthajo.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 해시 유틸. 관리자 로그인 코드는 SHA-256(hex)으로 저장·비교한다.
 *
 * 시드용 해시 생성:
 *   java healthajo.util.HashUtils ADMIN1234
 */
public final class HashUtils {

    private HashUtils() {}

    public static String sha256(String input) {
        if (input == null) return null;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("usage: HashUtils <code>");
            return;
        }
        System.out.println(sha256(args[0]));
    }
}
