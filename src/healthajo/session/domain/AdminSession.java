package healthajo.session.domain;

/**
 * 관리자 세션 관리 화면용 세션 행 데이터.
 */
public record AdminSession(
    Long   id,
    String programName,
    String date,
    String start,
    String end,
    int    capacity,
    int    bookedCount,
    String instructorName,
    String status
) {}
