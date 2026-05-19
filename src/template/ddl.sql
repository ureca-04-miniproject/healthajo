CREATE TABLE IF NOT EXISTS users
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'PK 자동 증가',
    name            VARCHAR(100) NOT NULL COMMENT '사용자 이름',
    phone           VARCHAR(20)  NOT NULL COMMENT '로그인 식별자',
    email           VARCHAR(255) NULL COMMENT '이메일',
    role            VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT 'USER or ADMIN',
    login_code_hash VARCHAR(255) NULL COMMENT '관리자 코드 해시',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    deleted_at      DATETIME     NULL COMMENT '소프트 삭제',

    CONSTRAINT uq_users_phone UNIQUE (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS instructors
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'PK 자동 증가',
    name       VARCHAR(100) NOT NULL COMMENT '강사명',
    phone      VARCHAR(20)  NULL COMMENT '전화번호',
    specialty  VARCHAR(30)  NOT NULL COMMENT 'SPINNING·YOGA·PILATES·GOLF',
    status     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE·INACTIVE',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME     NULL COMMENT '소프트 삭제'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS programs
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'PK 자동 증가',
    name             VARCHAR(100) NOT NULL COMMENT '프로그램명',
    description      TEXT         NULL COMMENT '프로그램 설명',
    category         VARCHAR(30)  NOT NULL COMMENT 'SPINNING·YOGA·PILATES·GOLF',
    booking_open_at  DATETIME     NOT NULL COMMENT '예약 시작일시',
    booking_close_at DATETIME     NOT NULL COMMENT '예약 종료일시',
    cancel_open_at   DATETIME     NOT NULL COMMENT '취소 시작일시',
    cancel_close_at  DATETIME     NOT NULL COMMENT '취소 종료일시',
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at       DATETIME     NULL COMMENT '소프트 삭제',

    INDEX            idx_programs_category(category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS program_schedules
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'PK 자동 증가',
    program_id        BIGINT      NOT NULL COMMENT 'programs.id 참조',
    schedule_type     VARCHAR(30) NOT NULL COMMENT 'FIXED_WEEKLY·FREE_SLOT·EVENT',
    start_date        DATE        NOT NULL COMMENT '운영 시작일',
    end_date          DATE        NOT NULL COMMENT '운영 종료일',
    default_capacity  INT         NOT NULL COMMENT '기본 정원',
    slot_open_time    TIME        NULL COMMENT 'FREE_SLOT 전용',
    slot_close_time   TIME        NULL COMMENT 'FREE_SLOT 전용',
    slot_duration_min INT         NULL COMMENT 'FREE_SLOT 슬롯 단위 분',
    created_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX             idx_program_schedules_program(program_id),

    CONSTRAINT fk_program_schedules_program
        FOREIGN KEY (program_id)
            REFERENCES programs (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS schedule_weekdays
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'PK 자동 증가',
    schedule_id      BIGINT NOT NULL COMMENT 'program_schedules.id 참조',
    weekday          INT    NOT NULL COMMENT '0월~6일',
    class_start_time TIME   NOT NULL COMMENT '수업 시작',
    class_end_time   TIME   NOT NULL COMMENT '수업 종료',
    capacity         INT    NULL COMMENT 'NULL이면 default_capacity 사용',

    INDEX            idx_schedule_weekdays_schedule(schedule_id),

    CONSTRAINT fk_schedule_weekdays_schedule
        FOREIGN KEY (schedule_id)
            REFERENCES program_schedules (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS sessions
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'PK 자동 증가',
    program_id           BIGINT      NOT NULL COMMENT 'programs.id 참조',
    schedule_id          BIGINT      NOT NULL COMMENT 'program_schedules.id 참조',
    instructor_id        BIGINT      NULL COMMENT 'instructors.id 참조',
    session_type         VARCHAR(30) NOT NULL COMMENT 'FIXED·FREE_SLOT·EVENT',
    session_date         DATE        NOT NULL COMMENT '수업 날짜',
    start_time           TIME        NOT NULL COMMENT '시작 시각',
    end_time             TIME        NOT NULL COMMENT '종료 시각',
    capacity             INT         NOT NULL COMMENT '정원',
    booked_count         INT         NOT NULL DEFAULT 0 COMMENT '현재 예약 수',
    status               VARCHAR(20) NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN·CANCELLED',
    attendance_closed    TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '출석 마감 여부',
    attendance_closed_at DATETIME    NULL COMMENT '출석 마감 일시',
    created_at           DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX                idx_sessions_program_date(program_id, session_date),
    INDEX                idx_sessions_schedule(schedule_id),
    INDEX                idx_sessions_instructor(instructor_id),

    CONSTRAINT fk_sessions_program
        FOREIGN KEY (program_id)
            REFERENCES programs (id),

    CONSTRAINT fk_sessions_schedule
        FOREIGN KEY (schedule_id)
            REFERENCES program_schedules (id),

    CONSTRAINT fk_sessions_instructor
        FOREIGN KEY (instructor_id)
            REFERENCES instructors (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS memberships
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'PK 자동 증가',
    user_id         BIGINT       NOT NULL COMMENT 'users.id 참조',
    program_id      BIGINT       NOT NULL COMMENT 'programs.id 참조',
    name            VARCHAR(100) NOT NULL COMMENT '회원권명',
    total_count     INT          NOT NULL COMMENT '총 횟수',
    remaining_count INT          NOT NULL COMMENT '잔여 횟수',
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE·EXPIRED',
    issued_at       DATETIME     NOT NULL COMMENT '발급일시',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX           idx_memberships_user_program(user_id, program_id),

    CONSTRAINT chk_memberships_remaining_count
        CHECK (remaining_count >= 0),

    CONSTRAINT fk_memberships_user
        FOREIGN KEY (user_id)
            REFERENCES users (id),

    CONSTRAINT fk_memberships_program
        FOREIGN KEY (program_id)
            REFERENCES programs (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS reservations
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'PK 자동 증가',
    user_id           BIGINT      NOT NULL COMMENT 'users.id 참조',
    session_id        BIGINT      NOT NULL COMMENT 'sessions.id 참조',
    program_id        BIGINT      NOT NULL COMMENT '중복 신청 방지 역정규화',
    membership_id     BIGINT      NULL COMMENT '발급 전 NULL 허용',
    status            VARCHAR(30) NOT NULL COMMENT 'CONFIRMED·MEMBERSHIP_ISSUED·CANCELLED',
    cancelled_by      VARCHAR(20) NULL COMMENT 'USER·ADMIN',
    reserved_at       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cancelled_at      DATETIME    NULL,
    attendance_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING·ATTENDED·ABSENT',
    attended_at       DATETIME    NULL,
    attended_by       VARCHAR(20) NULL COMMENT 'USER·ADMIN',

    UNIQUE KEY uq_reservations_user_session (user_id, session_id),

    INDEX             idx_reservations_user(user_id),
    INDEX             idx_reservations_session(session_id),
    INDEX             idx_reservations_program(program_id),
    INDEX             idx_reservations_membership(membership_id),

    CONSTRAINT fk_reservations_user
        FOREIGN KEY (user_id)
            REFERENCES users (id),

    CONSTRAINT fk_reservations_session
        FOREIGN KEY (session_id)
            REFERENCES sessions (id),

    CONSTRAINT fk_reservations_program
        FOREIGN KEY (program_id)
            REFERENCES programs (id),

    CONSTRAINT fk_reservations_membership
        FOREIGN KEY (membership_id)
            REFERENCES memberships (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS membership_histories
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'PK 자동 증가',
    membership_id   BIGINT      NOT NULL COMMENT 'memberships.id 참조',
    session_id      BIGINT      NULL COMMENT 'sessions.id 참조',
    reservation_id  BIGINT      NULL COMMENT 'reservations.id 참조',
    change_type     VARCHAR(30) NOT NULL COMMENT 'USE·CANCEL_RESTORE·ADMIN_ADJUST',
    processed_by    VARCHAR(20) NOT NULL COMMENT 'USER·ADMIN',
    change_count    INT         NOT NULL COMMENT '차감 음수 복구 양수',
    remaining_after INT         NOT NULL COMMENT '처리 후 잔여 횟수',
    created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX           idx_membership_histories_membership(membership_id),
    INDEX           idx_membership_histories_session(session_id),
    INDEX           idx_membership_histories_reservation(reservation_id),

    CONSTRAINT fk_membership_histories_membership
        FOREIGN KEY (membership_id)
            REFERENCES memberships (id),

    CONSTRAINT fk_membership_histories_session
        FOREIGN KEY (session_id)
            REFERENCES sessions (id),

    CONSTRAINT fk_membership_histories_reservation
        FOREIGN KEY (reservation_id)
            REFERENCES reservations (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;