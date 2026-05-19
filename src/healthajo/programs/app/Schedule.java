package healthajo.programs.app;

import java.time.LocalDateTime;

public final class Schedule {
    private final Long id;
    private final Long programId;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final Integer defaultCapacity;
    private final LocalDateTime slotOpenTime;
    private final LocalDateTime slotCloseTime;
    private final Integer slotDurationTime;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public Schedule(Long id, Long programId,
                    LocalDateTime startDate, LocalDateTime endDate,
                    Integer defaultCapacity, LocalDateTime slotOpenTime, LocalDateTime slotCloseTime,
                    Integer slotDurationTime, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.programId = programId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.defaultCapacity = defaultCapacity;
        this.slotOpenTime = slotOpenTime;
        this.slotCloseTime = slotCloseTime;
        this.slotDurationTime = slotDurationTime;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId()                                  { return id; }
    public Long getProgramId()                           { return programId; }
    public LocalDateTime getStartDate()                  { return startDate; }
    public LocalDateTime getEndDate()                    { return endDate; }
    public Integer getDefaultCapacity()                  { return defaultCapacity; }
    public LocalDateTime getSlotOpenTime()               { return slotOpenTime; }
    public LocalDateTime getSlotCloseTime()              { return slotCloseTime; }
    public Integer getSlotDurationTime()                 { return slotDurationTime; }
    public LocalDateTime getCreatedAt()                  { return createdAt; }
    public LocalDateTime getUpdatedAt()                  { return updatedAt; }
}
