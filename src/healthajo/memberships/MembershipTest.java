package healthajo.memberships;

import healthajo.jdbc.core.Record;
import healthajo.memberships.service.MembershipService;

import java.util.List;

import static healthajo.jdbc.table.TMembership.MEMBERSHIP;

public class MembershipTest {

    public static void main(String[] args) {
        MembershipService service = new MembershipService();

        // 1. 회원권 발급 테스트
        service.issueMembership(
                1L,                 // user_id
                1L,                 // program_id
                "요가 10회권",       // 회원권명
                10                  // 총 횟수
        );

        System.out.println("회원권 발급 완료");

        // 2. 전체 회원권 조회 테스트
        List<Record> memberships = service.getAllMemberships();

        for (Record record : memberships) {
            Long id = record.get(MEMBERSHIP.ID);
            Long userId = record.get(MEMBERSHIP.USER_ID);
            Long programId = record.get(MEMBERSHIP.PROGRAM_ID);
            String name = record.get(MEMBERSHIP.NAME);
            Integer totalCount = record.get(MEMBERSHIP.TOTAL_COUNT);
            Integer remainingCount = record.get(MEMBERSHIP.REMAINING_COUNT);
            String status = record.get(MEMBERSHIP.STATUS);

            System.out.println(
                    "id=" + id +
                            ", userId=" + userId +
                            ", programId=" + programId +
                            ", name=" + name +
                            ", total=" + totalCount +
                            ", remaining=" + remainingCount +
                            ", status=" + status
            );
        }

        // 3. 횟수 차감 테스트
        Record first = memberships.get(0);
        Long membershipId = first.get(MEMBERSHIP.ID);

        service.decreaseCount(membershipId);
        System.out.println("회원권 1회 차감 완료");

        // 4. 횟수 복구 테스트
        service.restoreCount(membershipId);
        System.out.println("회원권 1회 복구 완료");

        // 5. 수동 조정 테스트
        service.adjustCount(membershipId, -3);
        System.out.println("회원권 3회 차감 완료");
    }
}