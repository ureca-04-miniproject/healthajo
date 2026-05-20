# JDBC 추상화 레이어 (`healthajo.jdbc`)

순수 JDBC 위에 올린 **타입 안전(type-safe) SQL DSL**입니다. jOOQ 스타일로, 자바 메서드 체이닝만으로 `SELECT / INSERT / UPDATE / DELETE`, JOIN, 서브쿼리, 집계·윈도우 함수, 페이징을 작성합니다. 문자열 SQL을 직접 쓰지 않고도 컬럼·조건을 컴파일 타임에 검증할 수 있고, 모든 값은 `PreparedStatement` 바인딩으로 처리되어 SQL 인젝션에 안전합니다.

```
RESERVATION.select(RESERVATION.ID, USER.NAME.as("user_name"))
    .join(USER).on(RESERVATION.USER_ID.eq(USER.ID))
    .where(RESERVATION.STATUS.eq("CONFIRMED"))
    .orderByDesc(RESERVATION.RESERVED_AT)
    .fetch();
// → SELECT reservations.id, users.name AS user_name
//     FROM reservations JOIN users ON reservations.user_id = users.id
//    WHERE reservations.status = ? ORDER BY reservations.reserved_at DESC
```

---

## 패키지 구조

```
jdbc/
├── core/      DSL 핵심 (쿼리 빌더, 조건, 결과 매핑)
├── factory/   커넥션 풀 · 설정 · 테이블 자동 초기화
└── table/     테이블 정의 (T* 클래스, 컬럼 메타데이터)
```

---

## 클래스 요약

### core — 테이블 / 컬럼
| 클래스 | 설명 |
|---|---|
| `TableBase` | 모든 테이블 정의의 부모. `select()`, `insertInto()`, `update()`, `delete()`, `as()`, `asterisk()` 제공. `FromSource` 구현. |
| `Column<T>` | 타입이 붙은 컬럼. 비교 연산자(`eq`, `ne`, `gt`, `lt`, `ge`, `le`, `like`, `isNull`, `in` …)로 `Condition`을 생성. `as()`로 별칭 부여. |
| `Field` | SELECT 절에 들어갈 수 있는 모든 것의 인터페이스(`Column`, `WindowFunction`, `ScalarSubquery`, 람다 raw 식). |
| `AliasedTable` | `TableBase.as()`가 반환하는 별칭 테이블. 셀프 조인 시 `col(원본컬럼)`으로 별칭 기준 컬럼 참조. |
| `TableWildcard` | `t.asterisk()` → `t.*` |

### core — 쿼리 빌더
| 클래스 | 설명 |
|---|---|
| `SelectStep` | SELECT 빌더. `join/leftJoin/rightJoin/crossJoin`, `where`, `groupBy`, `having`, `orderBy/orderByDesc`, `limit`, `offset`, `fetch`, `fetchOne`, `fetchCount`, `toSql`. |
| `JoinOnStep` | `join(...)` 직후 `.on(condition)`을 강제하는 중간 단계. |
| `JoinClause` | 생성된 JOIN 절(내부용). |
| `InsertStep` | INSERT 빌더. `set(col, val)` 체이닝 후 `execute()`(영향 행 수) 또는 `executeAndReturnKey()`(생성 PK). |
| `UpdateStep` | UPDATE 빌더. `set().where().execute()`. WHERE 없는 UPDATE는 예외. |
| `DeleteStep` | DELETE 빌더. `where().execute()`. |

### core — 조건 / 함수 / 결과
| 클래스 | 설명 |
|---|---|
| `Condition` | WHERE/ON/HAVING 조건. `and`/`or`로 결합(자동 괄호), `Condition.raw(sql, binds…)`, `exists`/`notExists`. |
| `WindowFunction` | 윈도우 함수(`ROW_NUMBER`, `RANK`, `SUM`, `LAG`/`LEAD` …) + `over().partitionBy().orderBy()`. |
| `ScalarSubquery` | SELECT 절에 들어가는 스칼라 서브쿼리 `(SELECT …) AS alias`. |
| `SubqueryTable` | FROM/JOIN에 쓰는 파생 테이블 `(SELECT …) AS alias`. |
| `Record` | 한 행의 결과. `get(Column<T>)`(타입 안전), `get("alias", Class)`, `get("name")`(Object). |
| `Page<T>` | 페이지네이션 결과 컨테이너. `Page.of(step, page, size)`, `getContent/getTotalCount/getTotalPages/hasNext`, `map()`. |
| `FromSource` | FROM 대상 추상화(테이블·별칭테이블·서브쿼리테이블 공통). |

### factory
| 클래스 | 설명 |
|---|---|
| `JdbcConnectionConfig` | 드라이버/URL/계정/풀 크기/타임아웃 설정 값 제공. |
| `JdbcConnectionFactory` | 싱글턴 커넥션 풀. `getInstance().getConnection()` → `AutoCloseable`인 `JdbcConnection`(`get()`으로 `java.sql.Connection`, `close()` 시 풀로 반환). |
| `JdbcTableAutoInit` | 시작 시 DDL로 테이블 자동 생성(`initIfNotExist()`). |

### table
`TUser`, `TReservation`, `TSessions`, `TPrograms`, `TProgramSchedules`, `TScheduleWeekdays`, `TMembership`, `TInstructors` — 각 테이블의 컬럼 메타데이터. 싱글턴 인스턴스(예: `TUser.USER`)를 `static import`해서 사용합니다.

---

## 테이블 정의 방법

`TableBase`를 상속하고 컬럼을 `Column<T>`로 선언, 싱글턴을 노출합니다. 컬럼 prefix는 `getPrefix()`를 써야 별칭(alias)이 올바르게 동작합니다.

```java
public final class TUser extends TableBase {
    public static final TUser USER = new TUser();

    public final Column<Long>   ID    = new Column<>(getPrefix(), "id",    Long.class);
    public final Column<String> NAME  = new Column<>(getPrefix(), "name",  String.class);
    public final Column<String> PHONE = new Column<>(getPrefix(), "phone", String.class);
    public final Column<Timestamp> DELETED_AT = new Column<>(getPrefix(), "deleted_at", Timestamp.class);

    private TUser()             { super("users"); }
    private TUser(String alias) { super("users", alias); }

    @Override public TUser as(String alias) { return new TUser(alias); } // covariant — 타입 컬럼 접근 유지
}
```

사용 시:
```java
import static healthajo.jdbc.table.TUser.USER;
```

---

## 사용 예시

### 1. 기본 SELECT
```java
// 전체 컬럼
List<Record> rows = USER.select().where(USER.DELETED_AT.isNull()).orderBy(USER.ID).fetch();

// 특정 컬럼
List<Record> rows = USER.select(USER.ID, USER.NAME, USER.PHONE).fetch();

// 단건 (없으면 null)
Record one = USER.select().where(USER.ID.eq(1L)).fetchOne();

// 개수
long total = USER.select().where(USER.DELETED_AT.isNull()).fetchCount();
```

### 2. 결과 읽기 (`Record`)
```java
Record r = USER.select().where(USER.ID.eq(1L)).fetchOne();
Long   id   = r.get(USER.ID);        // 타입 안전
String name = r.get(USER.NAME);
// 별칭/문자열 키로 읽기 (집계·조인 alias 결과)
String un = r.get("user_name", String.class);
Object raw = r.get("membership_count"); // 타입 모를 때 Object
```

### 3. WHERE 조건 (`Condition`)
```java
USER.select()
    .where(USER.DELETED_AT.isNull()
        .and(USER.NAME.like("%김%")
             .or(USER.PHONE.like("%1234%"))))   // (A AND (B OR C)) 자동 괄호
    .fetch();

USER.select().where(USER.ID.in(1L, 2L, 3L)).fetch();        // IN
USER.select().where(USER.ROLE.ne("ADMIN")).fetch();          // !=
```
DSL이 표현하지 못하는 식은 `Condition.raw`로 직접:
```java
.where(USER.DELETED_AT.isNull()
    .and(Condition.raw("DATE_FORMAT(reservations.reserved_at, '%Y-%m-%d') LIKE ?", "%2026-05%")))
```

### 4. INSERT
```java
// 영향 행 수 반환
RESERVATION.insertInto()
    .set(RESERVATION.USER_ID, userId)
    .set(RESERVATION.SESSION_ID, sessionId)
    .set(RESERVATION.STATUS, "CONFIRMED")
    .execute();

// 생성된 PK 반환
long newId = PROGRAMS.insertInto()
    .set(PROGRAMS.NAME, "스피닝 A")
    .set(PROGRAMS.CATEGORY, "SPINNING")
    .executeAndReturnKey();
```

### 5. UPDATE / DELETE
```java
RESERVATION.update()
    .set(RESERVATION.STATUS, "CANCELLED")
    .set(RESERVATION.CANCELLED_AT, LocalDateTime.now())
    .where(RESERVATION.ID.eq(reservationId))   // WHERE 필수
    .execute();

SCHEDULE_WEEKDAYS.delete()
    .where(SCHEDULE_WEEKDAYS.SCHEDULE_ID.eq(scheduleId))
    .execute();
```

### 6. JOIN
```java
RESERVATION.select(
        RESERVATION.ID,
        USER.NAME.as("user_name"),
        PROGRAMS.NAME.as("program_name"),
        SESSIONS.SESSION_DATE)
    .join(USER).on(RESERVATION.USER_ID.eq(USER.ID))            // INNER
    .leftJoin(PROGRAMS).on(RESERVATION.PROGRAM_ID.eq(PROGRAMS.ID))
    .join(SESSIONS).on(RESERVATION.SESSION_ID.eq(SESSIONS.ID))
    .where(USER.ID.eq(userId))
    .orderByDesc(SESSIONS.SESSION_DATE, RESERVATION.RESERVED_AT)
    .fetch();
```
`rightJoin`, `crossJoin`도 동일한 형태로 사용합니다.

### 7. 별칭 / 셀프 조인
```java
AliasedTable a1 = (AliasedTable) USER.as("a1");
AliasedTable a2 = (AliasedTable) USER.as("a2");
a1.select(a1.col(USER.ID), a2.col(USER.NAME).as("other_name"))
  .join(a2).on(a1.col(USER.ID).eq(a2.col(USER.ID)))
  .fetch();
```

### 8. 집계 + GROUP BY + HAVING
```java
SESSIONS.select(
        SESSIONS.PROGRAM_ID,
        rawField("COUNT(*) AS session_count"))
    .groupBy(SESSIONS.PROGRAM_ID)
    .having(Condition.raw("COUNT(*) > ?", 5))
    .fetch();

// raw Field 헬퍼 (Field는 함수형 인터페이스)
static Field rawField(String sql) { return () -> sql; }
```

### 9. 페이징
```java
// step은 ORDER BY까지만 작성 (LIMIT/OFFSET은 Page가 붙임), pageNumber는 0-based
SelectStep step = RESERVATION.select(/* ... */)
    .where(RESERVATION.USER_ID.eq(userId))
    .orderByDesc(RESERVATION.RESERVED_AT);

Page<Record> page = Page.of(step, pageNumber, pageSize);
page.getTotalCount();   // 전체 행 수 (COUNT 쿼리 자동 실행)
page.getTotalPages();
page.hasNext();

// 도메인 객체로 변환
Page<Reservation> result = Page.of(step, pageNumber, pageSize).map(this::toReservation);
```

### 10. 서브쿼리

**스칼라 서브쿼리 (SELECT 절):**
```java
PROGRAMS.select(
        PROGRAMS.ID,
        PROGRAMS.NAME,
        new ScalarSubquery(
            SESSIONS.select(rawField("COUNT(*)"))
                    .where(SESSIONS.PROGRAM_ID.eq(PROGRAMS.ID))
        ).as("session_count"))
    .fetch();
```

**IN / EXISTS (WHERE 절):**
```java
// col IN (SELECT ...)
USER.select()
    .where(USER.ID.in(
        RESERVATION.select(RESERVATION.USER_ID).where(RESERVATION.STATUS.eq("CONFIRMED"))))
    .fetch();

// EXISTS / NOT EXISTS
USER.select()
    .where(Condition.exists(
        RESERVATION.select(RESERVATION.ID).where(RESERVATION.USER_ID.eq(USER.ID))))
    .fetch();
```

**파생 테이블 (FROM 절):**
```java
SelectStep sub = SESSIONS.select(SESSIONS.PROGRAM_ID, rawField("COUNT(*) AS cnt"))
                         .groupBy(SESSIONS.PROGRAM_ID);
SubqueryTable t = new SubqueryTable(sub, "s");
t.select(t.col("program_id", Long.class), t.col("cnt", Long.class))
 .where(Condition.raw("s.cnt > ?", 3))
 .fetch();
```

### 11. 윈도우 함수
```java
SESSIONS.select(
        SESSIONS.ID,
        SESSIONS.PROGRAM_ID,
        WindowFunction.rowNumber()
            .over().partitionBy(SESSIONS.PROGRAM_ID).orderByDesc(SESSIONS.SESSION_DATE)
            .as("rn"))
    .fetch();
// → ROW_NUMBER() OVER (PARTITION BY sessions.program_id ORDER BY sessions.session_date DESC) AS rn
```
`rank()`, `denseRank()`, `sum(col)`, `avg(col)`, `lag(col, 1)`, `lead(col)` 등도 동일하게 `over()` 체이닝.

### 12. 디버깅 / raw 커넥션
```java
String sql = step.toSql();   // 생성될 SQL 문자열 확인 (실행 로그에도 [SQL]/[BIND] 출력됨)
```
DSL로 표현이 어려운 다중 테이블 UPDATE 등은 커넥션을 직접 사용:
```java
try (JdbcConnectionFactory.JdbcConnection jc = JdbcConnectionFactory.getInstance().getConnection()) {
    Connection conn = jc.get();
    try (PreparedStatement ps = conn.prepareStatement(
            "UPDATE memberships m JOIN reservations r ON r.membership_id = m.id "
          + "SET m.remaining_count = m.remaining_count + 1 WHERE r.session_id = ?")) {
        ps.setLong(1, sessionId);
        ps.executeUpdate();
    }
} catch (SQLException e) { /* ... */ }
// try-with-resources 종료 시 커넥션은 풀로 반환됨
```

---

## 권장 레이어링

뷰(Swing) → **Application(서비스)** → **DAO** → JDBC DSL 순으로 사용합니다. DAO는 DSL로 쿼리하고 `Record`를 도메인 객체로 변환, Application은 트랜잭션 성격의 흐름·검증을 담당합니다. 대표 예시는 `healthajo.reservation.dao.ReservationDao`(JOIN·페이징·검색 조건)와 `healthajo.programs.dao.*`(CRUD·집계)를 참고하세요.

## 주의사항
- `Page.of`에 넘기는 `SelectStep`은 **`ORDER BY`까지만** 작성합니다(LIMIT/OFFSET은 `Page`가 부착).
- `pageNumber`는 **0-based**입니다(화면의 1-based 페이지는 `currentPage - 1`로 전달).
- `UPDATE`/`DELETE`는 `where()`가 없으면 실행 시 예외를 던집니다(전체 갱신/삭제 사고 방지).
- 컬럼 별칭(`.as("x")`)을 쓴 결과는 `record.get("x", Class)` 또는 문자열 키로 읽습니다.
- 모든 값 비교는 `PreparedStatement` 바인딩(`?`)으로 처리됩니다. 동적 식은 `Condition.raw` + 바인딩 인자를 사용하세요.
</content>
