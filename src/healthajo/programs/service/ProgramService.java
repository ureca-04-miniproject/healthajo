package healthajo.programs.service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import healthajo.jdbc.core.Field;
import healthajo.jdbc.core.Record;
import healthajo.jdbc.table.TProgramSchedules;
import healthajo.jdbc.table.TPrograms;
import healthajo.jdbc.table.TScheduleWeekdays;
import healthajo.jdbc.table.TSessions;
import healthajo.programs.dao.ProgramDAO;
import healthajo.programs.dao.ScheduleDAO;
import healthajo.programs.dto.ProgramResponseDTO;
import healthajo.programs.dto.ScheduleListItemDTO;
import healthajo.programs.entity.Schedule;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class ProgramService {
    private final ProgramDAO programDAO = new ProgramDAO();
    private final ScheduleDAO scheduleDAO = new ScheduleDAO();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final TPrograms P = TPrograms.PROGRAMS;
    private static final TSessions S = TSessions.SESSIONS;
    private static final TProgramSchedules PS = TProgramSchedules.PROGRAM_SCHEDULES;
    private static final TScheduleWeekdays SW = TScheduleWeekdays.SCHEDULE_WEEKDAYS;

    public List<ProgramResponseDTO> getProgramList() {
        return programDAO.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public Record getProgramWithSchedules(int id) {
        return programDAO.findByProgramID(id);
    }

    public List<ScheduleListItemDTO> findListItemsByProgramId(long programId) {
        Field weekdayCount = () ->
                "COUNT(" + SW.ID.getQualifiedName() + ") AS weekday_count";

        return PS.select(
                        PS.ID, PS.START_DATE, PS.END_DATE,
                        PS.DEFAULT_CAPACITY,
                        weekdayCount
                )
                .leftJoin(SW).on(SW.SCHEDULE_ID.eq(PS.ID))
                .where(PS.PROGRAM_ID.eq(programId))
                .groupBy(PS.ID)
                .orderByDesc(PS.CREATED_AT)
                .fetch().stream().map(this::toScheduleListItemDTO).toList();
    }

    private ScheduleListItemDTO toScheduleListItemDTO(Record record) {
//        private Long id;
//        private LocalDate classStartDate;
//        private LocalDate classEndDate;
//        private Integer capacity;
//        private Integer weekdayCount;

        Long id = record.get(PS.ID);
        LocalDateTime startDate  = record.get(PS.START_DATE);
        LocalDateTime endDate = record.get(PS.END_DATE);

        Integer capacity = record.get(PS.DEFAULT_CAPACITY);
        Integer weekdayCount = record.get()


    }

    private ProgramResponseDTO toResponseDTO(Record record) {
        Long id = record.get(P.ID);
        LocalDateTime openAt  = record.get(P.RESERVATION_OPEN_AT);
        LocalDateTime closeAt = record.get(P.RESERVATION_CLOSE_AT);

        Integer totalCapacity = record.get(S.CAPACITY);
        Integer sessionCount  = record.get(S.BOOKED_COUNT);

        ProgramResponseDTO dto = new ProgramResponseDTO();
        dto.setProgramId(record.get(P.ID));
        dto.setName(record.get(P.NAME));
        dto.setType(record.get(P.CATEGORY));
        dto.setReservationTime(formatPeriod(openAt, closeAt));
        dto.setStudentCount(totalCapacity == null ? 0 : totalCapacity);
        dto.setSessionCount(sessionCount == null ? 0 : sessionCount);
//        dto.setStatus(record.get(S.STATUS)
        dto.setStatus("-");
        return dto;
    }

    private String formatPeriod(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) return "-";
        return from.format(DATE_FMT) + " ~ " + to.format(DATE_FMT);
    }
}
