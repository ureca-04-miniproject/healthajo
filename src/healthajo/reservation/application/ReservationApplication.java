package healthajo.reservation.application;

import healthajo.jdbc.core.Page;
import healthajo.reservation.dao.ReservationDao;
import healthajo.reservation.domain.Reservation;

public class ReservationApplication {

    private final ReservationDao dao = new ReservationDao();

    // ── 관리자 ────────────────────────────────────────────────────────────────────

    public Page<Reservation> findAll(int pageNumber, int pageSize) {
        return dao.findAll(pageNumber, pageSize);
    }

    /**
     * 강제 취소 — 예약 상태를 CANCELLED로 변경하고 세션 예약 인원을 1 감소.
     * MEMBERSHIP_ISSUED 상태 예약 취소 시 회원권 잔여 횟수 복구는 TODO.
     */
    public void cancelByAdmin(Reservation reservation) {
        dao.cancel(reservation.id(), "ADMIN");
        if (reservation.sessionId() != null) {
            dao.decrementSessionBookedCount(reservation.sessionId());
        }
    }

    // ── 사용자 ────────────────────────────────────────────────────────────────────

    public Page<Reservation> findByUserId(Long userId, int pageNumber, int pageSize, String keyword) {
        return dao.findByUserId(userId, pageNumber, pageSize, keyword);
    }

    public Page<Reservation> findAttendancesByUserId(Long userId, int pageNumber, int pageSize, String keyword) {
        return dao.findAttendancesByUserId(userId, pageNumber, pageSize, keyword);
    }

    public long reserve(Long userId, Long sessionId, Long programId) {
        long id = dao.insert(userId, sessionId, programId);
        dao.incrementSessionBookedCount(sessionId);
        return id;
    }

    /**
     * 사용자 예약 취소 — CONFIRMED 상태 예약만 허용.
     * 세션 예약 인원 1 감소.
     */
    public void checkIn(Long reservationId) {
        dao.checkIn(reservationId);
    }

    public void cancelByUser(Reservation reservation) {
        dao.cancel(reservation.id(), "USER");
        if (reservation.sessionId() != null) {
            dao.decrementSessionBookedCount(reservation.sessionId());
        }
    }
}
