package healthajo.util;

public final class PhoneUtils {

    private PhoneUtils() {}

    /**
     * 입력값에서 숫자만 추출해 DB 저장 형식으로 변환.
     *   10자리: 011-123-4567
     *   11자리: 010-1234-5678
     * 형식이 맞지 않으면 null 반환.
     */
    public static String normalize(String raw) {
        if (raw == null) return null;
        String digits = raw.replaceAll("[^0-9]", "");
        return switch (digits.length()) {
            case 10 -> digits.substring(0, 3) + "-" + digits.substring(3, 6) + "-" + digits.substring(6);
            case 11 -> digits.substring(0, 3) + "-" + digits.substring(3, 7) + "-" + digits.substring(7);
            default -> null;
        };
    }

    /** 숫자만 남긴 자릿수가 10~11자리인지 확인 (포맷 변환 없이). */
    public static boolean isValid(String raw) {
        return normalize(raw) != null;
    }
}
