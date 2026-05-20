package healthajo.reservation.application;

import healthajo.jdbc.core.Page;
import healthajo.jdbc.core.Record;
import healthajo.memberships.service.MembershipService;
import healthajo.reservation.dao.ReservationDao;
import healthajo.reservation.domain.Reservation;

import static healthajo.jdbc.table.TMembership.MEMBERSHIP;

public class ReservationApplication {

    private final ReservationDao dao = new ReservationDao();
    private final MembershipService membershipService = new MembershipService();

    // ── 관리자 ────────────────────────────────────────────────────────────────────

    public Page<Reservation> findAll(int pageNumber, int pageSize) {
        return dao.findAll(pageNumber, pageSize);
    }

    public Page<Reservation> findAll(int pageNumber, int pageSize, String keyword) {
        return dao.findAll(pageNumber, pageSize, keyword);
    }

    /**
     * 강제 취소 — 예약 상태를 CANCELLED로 변경하고 세션 예약 인원을 1 감소.
     * 회원권으로 발급된 예약(membership_id 존재)이면 잔여 횟수를 복구한다.
     */
    public void cancelByAdmin(Reservation reservation) {
        dao.cancel(reservation.id(), "ADMIN");
        if (reservation.sessionId() != null) {
            dao.decrementSessionBookedCount(reservation.sessionId());
        }
        restoreMembership(reservation);
    }

    // ── 사용자 ────────────────────────────────────────────────────────────────────

    public Page<Reservation> findByUserId(Long userId, int pageNumber, int pageSize, String keyword) {
        return dao.findByUserId(userId, pageNumber, pageSize, keyword);
    }

    public Page<Reservation> findAttendancesByUserId(Long userId, int pageNumber, int pageSize, String keyword) {
        return dao.findAttendancesByUserId(userId, pageNumber, pageSize, keyword);
    }

    /**
     * 예약 신청 — 해당 프로그램에 사용 가능한 회원권이 있으면 1회 차감하고
     * 예약을 MEMBERSHIP_ISSUED 상태로 생성한다. 없으면 CONFIRMED.
     */
    public long reserve(Long userId, Long sessionId, Long programId) {
        Record membership = membershipService.getUsableMembership(userId, programId);
        long id;
        if (membership != null) {
            Long membershipId = membership.get(MEMBERSHIP.ID);
            id = dao.insert(userId, sessionId, programId, membershipId, "MEMBERSHIP_ISSUED");
            membershipService.decreaseCount(membershipId);
        } else {
            id = dao.insert(userId, sessionId, programId, null, "CONFIRMED");
        }
        dao.incrementSessionBookedCount(sessionId);
        return id;
    }

    public void checkIn(Long reservationId) {
        dao.checkIn(reservationId);
    }

    /**
     * 사용자 예약 취소 — 세션 예약 인원 1 감소.
     * 회원권으로 발급된 예약이면 잔여 횟수를 복구한다.
     */
    public void cancelByUser(Reservation reservation) {
        dao.cancel(reservation.id(), "USER");
        if (reservation.sessionId() != null) {
            dao.decrementSessionBookedCount(reservation.sessionId());
        }
        restoreMembership(reservation);
    }

    private void restoreMembership(Reservation reservation) {
        if (reservation.membershipId() != null) {
            membershipService.restoreCount(reservation.membershipId());
        }
    }
}
