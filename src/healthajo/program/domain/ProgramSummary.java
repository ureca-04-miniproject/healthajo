package healthajo.program.domain;

public record ProgramSummary(
    Long   id,
    String name,
    String category,
    String reservationPeriod,
    int    remaining
) {}
