package healthajo.attendance.domain;

/**
 * 세션 출석 현황 다이얼로그의 한 수강자 행.
 *
 * @param reservationId    예약 ID (출석 처리 대상)
 * @param userName         회원명
 * @param userPhone        전화번호
 * @param attendanceStatus 출석 상태 PENDING|ATTENDED|ABSENT
 * @param attendedAt       처리 일시 (yyyy-MM-dd HH:mm), 미처리 시 빈 문자열
 */
public record Attendee(
    Long   reservationId,
    String userName,
    String userPhone,
    String attendanceStatus,
    String attendedAt
) {}
