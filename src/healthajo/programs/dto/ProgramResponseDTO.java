package healthajo.programs.dto;

import java.time.LocalDateTime;

public class ProgramResponseDTO {
    private Long programId;
    private String programName;
    private String programType;
    private String reservationTime;
    private Integer studentCount;
    private Integer sessionCount;
    private String status;

    public Long getProgramId()                           { return programId; }
    public String getName()                              { return programName; }
    public String getType()                              { return programType; }
    public String getReservationTime()                   { return reservationTime; }
    public Integer getStudentCount()                     { return studentCount; }
    public Integer getSessionCount()                     { return sessionCount; }
    public String getStatus()                            { return status; }

    public void setProgramId(Long id)                    { this.programId = id; }
    public void setName(String programName)              { this.programName = programName; }
    public void setType(String programType)              { this.programType = programType; }
    public void setReservationTime(String reservationTime) { this.reservationTime = reservationTime; }
    public void setStudentCount(Integer studentCount)    { this.studentCount = studentCount; }
    public void setSessionCount(Integer sessionCount)    { this.sessionCount = sessionCount; }
    public void setStatus(String status)                 { this.status = status; }
}
