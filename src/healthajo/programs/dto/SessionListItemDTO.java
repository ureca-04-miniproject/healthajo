package healthajo.programs.dto;

public class SessionListItemDTO {
    private Long id;
    private String sessionDate;
    private String startTime;
    private String endTime;
    private Integer capacity;
    private Integer bookedCount;
    private String instructorName;
    private String status;

    public Long getId()                  { return id; }
    public String getSessionDate()       { return sessionDate; }
    public String getStartTime()         { return startTime; }
    public String getEndTime()           { return endTime; }
    public Integer getCapacity()         { return capacity; }
    public Integer getBookedCount()      { return bookedCount; }
    public String getInstructorName()    { return instructorName; }
    public String getStatus()            { return status; }

    public void setId(Long id)                       { this.id = id; }
    public void setSessionDate(String sessionDate)   { this.sessionDate = sessionDate; }
    public void setStartTime(String startTime)       { this.startTime = startTime; }
    public void setEndTime(String endTime)           { this.endTime = endTime; }
    public void setCapacity(Integer capacity)        { this.capacity = capacity; }
    public void setBookedCount(Integer bookedCount)  { this.bookedCount = bookedCount; }
    public void setInstructorName(String name)       { this.instructorName = name; }
    public void setStatus(String status)             { this.status = status; }
}
