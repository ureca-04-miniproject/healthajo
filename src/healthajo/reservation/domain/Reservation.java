package healthajo.reservation.domain;

import java.time.LocalDateTime;

public record Reservation(
    Long          id,
    Long          userId,
    String        userName,
    String        userPhone,
    Long          sessionId,
    LocalDateTime sessionDate,
    String        startTime,
    String        endTime,
    Long          programId,
    String        programName,
    Long          membershipId,
    String        status,
    String        cancelledBy,
    LocalDateTime reservedAt,
    LocalDateTime cancelledAt,
    String        attendanceStatus,
    LocalDateTime attendedAt
) {}
