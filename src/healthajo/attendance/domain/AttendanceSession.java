package healthajo.attendance.domain;

/**
 * 출석 관리 목록의 한 세션 행.
 *
 * @param id               세션 ID
 * @param programName      프로그램명
 * @param sessionDate      세션 날짜 (yyyy-MM-dd)
 * @param startTime        시작 시각 (HH:mm)
 * @param endTime          종료 시각 (HH:mm)
 * @param bookedCount      예약 인원
 * @param capacity         정원
 * @param attendanceClosed 출석 마감 여부
 */
public record AttendanceSession(
    Long    id,
    String  programName,
    String  sessionDate,
    String  startTime,
    String  endTime,
    int     bookedCount,
    int     capacity,
    boolean attendanceClosed
) {}
