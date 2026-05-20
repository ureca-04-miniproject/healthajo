package healthajo.session.domain;

public record SessionSummary(
    Long   id,
    String date,
    String start,
    String end,
    int    remaining,
    String instructor
) {}
