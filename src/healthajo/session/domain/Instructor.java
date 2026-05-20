package healthajo.session.domain;

/**
 * 강사 배정 콤보 등에서 사용하는 강사 최소 정보.
 */
public record Instructor(Long id, String name) {
    @Override
    public String toString() { return name; }
}
