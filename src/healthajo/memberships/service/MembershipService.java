package healthajo.memberships.service;

import healthajo.jdbc.core.Record;
import healthajo.memberships.dao.MembershipDAO;

import java.util.List;

import static healthajo.jdbc.table.TMembership.MEMBERSHIP;

public class MembershipService {

    private final MembershipDAO membershipDAO = new MembershipDAO();

    public void issueMembership(Long userId, Long programId, String name, int totalCount) {
        if (userId == null) {
            throw new IllegalArgumentException("회원을 선택하세요.");
        }

        if (programId == null) {
            throw new IllegalArgumentException("프로그램을 선택하세요.");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("회원권명을 입력하세요.");
        }

        if (totalCount <= 0) {
            throw new IllegalArgumentException("총 횟수는 1 이상이어야 합니다.");
        }

        membershipDAO.insertMembership(userId, programId, name.trim(), totalCount);
    }

    public List<Record> getAllMemberships() {
        return membershipDAO.findAllMemberships();
    }

    public List<Record> getAllMembershipsWithUserAndProgram(int limit, int offset) {
        return membershipDAO.findAllMembershipsWithUserAndProgram(limit, offset);
    }

    public List<Record> getAllMembershipsWithUserAndProgram(int limit, int offset, String keyword) {
        return membershipDAO.findAllMembershipsWithUserAndProgram(limit, offset, keyword);
    }

    public long countAllMemberships() {
        return membershipDAO.countAll();
    }

    public long countAllMemberships(String keyword) {
        return membershipDAO.countAll(keyword);
    }

    public List<Record> getMembershipsByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("회원 ID가 필요합니다.");
        }

        return membershipDAO.findMembershipsByUserId(userId);
    }

    public List<Record> getMembershipsWithProgramByUserId(Long userId) {
        return membershipDAO.findMembershipsWithProgramByUserId(userId);
    }

    public Record getUsableMembership(Long userId, Long programId) {
        if (userId == null || programId == null) {
            throw new IllegalArgumentException("회원 ID와 프로그램 ID가 필요합니다.");
        }

        return membershipDAO.findActiveByUserAndProgram(userId, programId);
    }

    public Long getUsableMembershipId(Long userId, Long programId) {
        Record membership = getUsableMembership(userId, programId);

        if (membership == null) {
            throw new IllegalStateException("사용 가능한 회원권이 없습니다.");
        }

        return membership.get(MEMBERSHIP.ID);
    }

    public boolean hasUsableMembership(Long userId, Long programId) {
        return membershipDAO.hasUsableMembership(userId, programId);
    }

    public void updateRemainingCount(Long membershipId, int remainingCount) {
        if (membershipId == null) {
            throw new IllegalArgumentException("회원권을 선택하세요.");
        }

        if (remainingCount < 0) {
            throw new IllegalArgumentException("잔여 횟수는 0보다 작을 수 없습니다.");
        }

        membershipDAO.updateRemainingCount(membershipId, remainingCount);
    }

    public void decreaseCount(Long membershipId) {
        if (membershipId == null) {
            throw new IllegalArgumentException("회원권 ID가 필요합니다.");
        }

        membershipDAO.decreaseCount(membershipId);
    }

    public void restoreCount(Long membershipId) {
        if (membershipId == null) {
            throw new IllegalArgumentException("회원권 ID가 필요합니다.");
        }

        membershipDAO.increaseCount(membershipId);
    }

    public void adjustCount(Long membershipId, int changeCount) {
        if (membershipId == null) {
            throw new IllegalArgumentException("회원권을 선택하세요.");
        }

        if (changeCount == 0) {
            throw new IllegalArgumentException("조정 횟수는 0일 수 없습니다.");
        }

        membershipDAO.adjustCount(membershipId, changeCount);
    }

    public void deleteMembership(Long membershipId) {
        if (membershipId == null) {
            throw new IllegalArgumentException("회원권을 선택하세요.");
        }

        membershipDAO.deleteMembershipById(membershipId);
    }
}
