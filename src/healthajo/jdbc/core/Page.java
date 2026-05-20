package healthajo.jdbc.core;

import java.util.List;
import java.util.function.Function;

/**
 * 페이지네이션 결과 컨테이너.
 *
 * <pre>
 * // limit + offset(raw) 방식
 * Page&lt;Record&gt; page = Page.of(
 *     USER.select().where(...).orderBy(USER.ID),
 *     pageNumber,   // 0-based
 *     20            // page size
 * );
 *
 * page.getContent()    // 현재 페이지 데이터
 * page.getTotalCount() // 전체 행 수
 * page.hasNext()       // 다음 페이지 존재 여부
 * page.hasPrev()       // 이전 페이지 존재 여부
 * page.getTotalPages() // 전체 페이지 수
 * page.map(r -> r.get(USER.NAME))             // 타입 안전 변환
 * </pre>
 */
public class Page<T> {

    private final List<T> content;
    private final long    totalCount;
    private final int     pageNumber;   // 0-based
    private final int     pageSize;

    private Page(List<T> content, long totalCount, int pageNumber, int pageSize) {
        this.content    = content;
        this.totalCount = totalCount;
        this.pageNumber = pageNumber;
        this.pageSize   = pageSize;
    }

    // ── 팩토리 ────────────────────────────────────────────────────────────────

    /**
     * SelectStep에서 직접 Page를 생성.
     * 내부적으로 {@code fetchCount()} + {@code limit(size).offset(page * size).fetch()} 를 실행.
     *
     * @param step       ORDER BY까지 완성된 SelectStep (LIMIT/OFFSET 미지정 상태)
     * @param pageNumber 0-based 페이지 번호
     * @param pageSize   한 페이지당 행 수
     */
    public static Page<Record> of(SelectStep step, int pageNumber, int pageSize) {
        long         total   = step.fetchCount();
        List<Record> content = step.fetchWithRange(pageSize, pageNumber * pageSize);
        return new Page<>(content, total, pageNumber, pageSize);
    }

    /**
     * 이미 fetch한 결과와 totalCount를 직접 지정해 Page를 생성.
     * 직접 쿼리를 제어할 필요가 있을 때 사용.
     */
    public static <T> Page<T> of(List<T> content, long totalCount, int pageNumber, int pageSize) {
        return new Page<>(content, totalCount, pageNumber, pageSize);
    }

    // ── 접근자 ────────────────────────────────────────────────────────────────

    public List<T>  getContent()    { return content; }
    public long     getTotalCount() { return totalCount; }
    public int      getPageNumber() { return pageNumber; }
    public int      getPageSize()   { return pageSize; }

    /** 전체 페이지 수 (빈 결과면 0) */
    public int getTotalPages() {
        if (pageSize <= 0) return 0;
        return (int) Math.ceil((double) totalCount / pageSize);
    }

    /** 다음 페이지 존재 여부 */
    public boolean hasNext() { return pageNumber + 1 < getTotalPages(); }

    /** 이전 페이지 존재 여부 */
    public boolean hasPrev() { return pageNumber > 0; }

    /** 현재 페이지가 첫 번째 페이지인지 */
    public boolean isFirst() { return pageNumber == 0; }

    /** 현재 페이지가 마지막 페이지인지 */
    public boolean isLast() { return !hasNext(); }

    /** 현재 페이지에 데이터가 있는지 */
    public boolean hasContent() { return !content.isEmpty(); }

    // ── 변환 ─────────────────────────────────────────────────────────────────

    /**
     * content를 다른 타입으로 변환한 새 Page 반환.
     *
     * <pre>
     * Page&lt;String&gt; names = page.map(r -> r.get(USER.NAME));
 * Page&lt;String&gt; names = page.map(r -> r.get("name", String.class)); // 문자열 키 방식
     * </pre>
     */
    public <U> Page<U> map(Function<T, U> mapper) {
        List<U> mapped = content.stream().map(mapper).toList();
        return new Page<>(mapped, totalCount, pageNumber, pageSize);
    }

    @Override
    public String toString() {
        return "Page{page=" + pageNumber + ", size=" + pageSize
            + ", total=" + totalCount + ", pages=" + getTotalPages()
            + ", hasNext=" + hasNext() + "}";
    }
}
