package healthajo.session.application;

import healthajo.jdbc.core.Page;
import healthajo.session.dao.SessionDao;
import healthajo.session.domain.AdminSession;
import healthajo.session.domain.Instructor;

import java.util.List;

/**
 * 세션 관련 응용 서비스 — 관리자 세션 관리 진입점.
 */
public class SessionApplication {

    private final SessionDao dao = new SessionDao();

    public Page<AdminSession> findAllForAdmin(int pageNumber, int pageSize) {
        return dao.findAllForAdmin(pageNumber, pageSize);
    }

    public List<Instructor> findActiveInstructors() {
        return dao.findActiveInstructors();
    }

    public void assignInstructor(Long sessionId, Long instructorId) {
        dao.assignInstructor(sessionId, instructorId);
    }

    public void cancelSession(Long sessionId) {
        dao.cancelSession(sessionId);
    }
}
