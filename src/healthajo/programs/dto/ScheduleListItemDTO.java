package healthajo.programs.dto;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;

public class ScheduleListItemDTO {
    private Long id;
    private String classStartDate;
    private String classEndDate;
    private Integer capacity;
    private Integer weekdayCount;

    public Long getId()                                  { return id; }
    public String getClassStartDate()                    { return classStartDate; }
    public String getClassEndDate()                      { return classEndDate; }
    public Integer getCapacity()                         { return capacity; }
    public Integer getWeekdayCount()                     { return weekdayCount; }

    public void setId(Long id)                           { this.id = id; }
    public void setClassStartDate(Date date)        { this.classStartDate = date.toString(); }
    public void setClassEndDate(Date date)          { this.classEndDate = date.toString(); }
    public void setCapacity(int capacity)                { this.capacity = capacity; }
    public void setWeekdayCount(int weekdayCount)        { this.weekdayCount = weekdayCount; }

}
