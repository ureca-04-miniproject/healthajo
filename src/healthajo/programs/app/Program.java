package healthajo.programs.app;

import java.time.LocalDateTime;

public final class Program {
    private final Long id;
    private final String name;
    private final String description;
    private final String category;
    private final LocalDateTime reservationOpenAt;
    private final LocalDateTime reservationCloseAt;
    private final LocalDateTime cancellationOpenAt;
    private final LocalDateTime cancellationCloseAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;

    public Program(Long id, String name, String description, String category,
                   LocalDateTime reservationOpenAt, LocalDateTime reservationCloseAt,
                   LocalDateTime cancellationOpenAt, LocalDateTime cancellationCloseAt,
                   LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.reservationOpenAt = reservationOpenAt;
        this.reservationCloseAt = reservationCloseAt;
        this.cancellationOpenAt = cancellationOpenAt;
        this.cancellationCloseAt = cancellationCloseAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    // 신규 생성용 — id, 감사 컬럼은 DB가 채움
    public static Program toCreate(String name, String description, String category,
                                   LocalDateTime reservationOpenAt, LocalDateTime reservationCloseAt,
                                   LocalDateTime cancellationOpenAt, LocalDateTime cancellationCloseAt) {
        return new Program(null, name, description, category,
                reservationOpenAt, reservationCloseAt,
                cancellationOpenAt, cancellationCloseAt,
                null, null, null);
    }

    public Long getId()                           { return id; }
    public String getName()                       { return name; }
    public String getDescription()                { return description; }
    public String getCategory()                   { return category; }
    public LocalDateTime getReservationOpenAt()   { return reservationOpenAt; }
    public LocalDateTime getReservationCloseAt()  { return reservationCloseAt; }
    public LocalDateTime getCancellationOpenAt()  { return cancellationOpenAt; }
    public LocalDateTime getCancellationCloseAt() { return cancellationCloseAt; }
    public LocalDateTime getCreatedAt()           { return createdAt; }
    public LocalDateTime getUpdatedAt()           { return updatedAt; }
    public LocalDateTime getDeletedAt()           { return deletedAt; }
}
