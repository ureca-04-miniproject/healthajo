package healthajo.programs.dto;

import java.time.LocalDate;

public class ScheduleListItemDTO {
    private Long id;
    private String type;            // FIXED_WEEKLY | FREE_SLOT (서비스에서 판별)
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer defaultCapacity;
    private Integer weekdayCount;
    // getter/setter
}
