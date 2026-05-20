package healthajo.attendance.application;

import healthajo.attendance.dao.AttendanceDao;
import healthajo.attendance.domain.AttendanceSession;
import healthajo.attendance.domain.Attendee;
import healthajo.jdbc.core.Page;

import java.time.LocalDate;
import java.util.List;

public class AttendanceApplication {

    private final AttendanceDao dao = new AttendanceDao();

    public Page<AttendanceSession> findSessionsByDate(LocalDate date, int pageNumber, int pageSize) {
        return dao.findSessionsByDate(date, pageNumber, pageSize);
    }

    public List<Attendee> findAttendees(Long sessionId) {
        return dao.findAttendees(sessionId);
    }

    public void markAttended(Long reservationId) {
        dao.markAttended(reservationId);
    }

    public void markAbsent(Long reservationId) {
        dao.markAbsent(reservationId);
    }

    public void revertToPending(Long reservationId) {
        dao.revertToPending(reservationId);
    }

    public void closeAttendance(Long sessionId) {
        dao.closeAttendance(sessionId);
    }
}
