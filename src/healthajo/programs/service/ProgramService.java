package healthajo.programs.service;

import java.util.*;
import java.util.stream.Collectors;

import healthajo.jdbc.core.Record;
import healthajo.jdbc.table.TProgramSchedules;
import healthajo.jdbc.table.TPrograms;
import healthajo.jdbc.table.TSessions;
import healthajo.programs.dao.ProgramDAO;
import healthajo.programs.dto.ProgramResponseDTO;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class ProgramService {
    private final ProgramDAO programDAO = new ProgramDAO();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final TPrograms P = TPrograms.PROGRAMS;
    private static final TSessions S = TSessions.SESSIONS;
    private static final TProgramSchedules PS = TProgramSchedules.PROGRAM_SCHEDULES;

    public List<ProgramResponseDTO> getProgramList() {
        return programDAO.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public Record getProgramWithSchedules(int id) {
        return programDAO.findByProgramID(id);
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
