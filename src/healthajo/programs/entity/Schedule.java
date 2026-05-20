package healthajo.programs.entity;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.*;

public final class Schedule {
    private final Long id;
    private final Long programId;
    private final Date startDate;
    private final Date endDate;
    private final Integer defaultCapacity;
    private final LocalTime slotOpenTime;
    private final LocalTime slotCloseTime;
    private final Integer slotDurationTime;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public Schedule(Long id, Long programId,
                    Date startDate, Date endDate,
                    Integer defaultCapacity, LocalTime slotOpenTime, LocalTime slotCloseTime,
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
    public Date getStartDate()                  { return startDate; }
    public Date getEndDate()                    { return endDate; }
    public Integer getDefaultCapacity()                  { return defaultCapacity; }
    public LocalTime getSlotOpenTime()               { return slotOpenTime; }
    public LocalTime getSlotCloseTime()              { return slotCloseTime; }
    public Integer getSlotDurationTime()                 { return slotDurationTime; }
    public LocalDateTime getCreatedAt()                  { return createdAt; }
    public LocalDateTime getUpdatedAt()                  { return updatedAt; }
}
