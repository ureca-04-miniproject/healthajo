package healthajo.programs.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class ScheduleListItemDTO {
    private Long id;
    private LocalDate classStartDate;
    private LocalDate classEndDate;
    private Integer capacity;
    private Integer weekdayCount;

    public Long getId()                                  { return id; }
    public String getClassStartDate()                    { return classStartDate.toString(); }
    public String getClassEndDate()                      { return classEndDate.toString(); }
    public Integer getCapacity()                         { return capacity; }
    public Integer getWeekdayCount()                     { return weekdayCount; }

    public void setId(Long id)                           { this.id = id; }
    public void setClassStartDate(LocalDate date)        { this.classStartDate = date; }
    public void setClassEndDate(LocalDate date)          { this.classEndDate = date; }
    public void setCapacity(int capacity)                { this.capacity = capacity; }
    public void setWeekdayCount(int weekdayCount)        { this.weekdayCount = weekdayCount; }

}
