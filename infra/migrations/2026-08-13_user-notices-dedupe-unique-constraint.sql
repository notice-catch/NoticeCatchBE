-- PR (fix/notice-detail-dangling-department) 배포 전 prod DB에 수동으로 실행할 것.
-- local/dev는 ddl-auto: update라 UserNotice 엔티티의 @UniqueConstraint가 자동 반영되지만
-- prod는 ddl-auto: validate라 제약이 미리 없으면 앱이 부팅 실패한다.
--
-- 배경: user_notices(user_id, notice_id)에 유니크 제약이 없어서 스크랩 동시 요청 시
-- UserNoticeService.scrapNotice()의 "조회 후 없으면 생성" 로직이 레이스 컨디션으로 중복 행을 만들었다.
-- 이후 findByUserIdAndNoticeId(Optional 반환)를 호출하는 모든 경로(상세조회의 스크랩 여부 확인,
-- 스크랩 토글 자체)가 IncorrectResultSizeDataAccessException으로 500이 났다.
-- (실제 사례: user_id=4, notice_id=1에 중복 2건 — GET /api/v1/notices/1 500 에러로 발견)
--
-- 실행 순서: 이 스크립트를 먼저 실행한 뒤에 새 버전을 배포할 것.

-- 1. 중복 행이 있는 (user_id, notice_id) 조합마다, 남길 대표 행(가장 작은 id)에
--    다른 중복 행들의 상태를 병합한다. is_scrapped/is_read/closing_notified는
--    TINYINT(1)이라 MAX()가 사실상 OR 역할 — 어느 한 행이라도 true였다면 true로 유지.
UPDATE user_notices un
JOIN (
    SELECT user_id, notice_id, MIN(id) AS keep_id,
           MAX(is_scrapped) AS merged_is_scrapped,
           MAX(is_read) AS merged_is_read,
           MAX(closing_notified) AS merged_closing_notified
    FROM user_notices
    GROUP BY user_id, notice_id
    HAVING COUNT(*) > 1
) dup ON un.id = dup.keep_id
SET un.is_scrapped = dup.merged_is_scrapped,
    un.is_read = dup.merged_is_read,
    un.closing_notified = dup.merged_closing_notified;

-- 2. 대표 행을 제외한 나머지 중복 행 삭제
DELETE un FROM user_notices un
JOIN (
    SELECT user_id, notice_id, MIN(id) AS keep_id
    FROM user_notices
    GROUP BY user_id, notice_id
    HAVING COUNT(*) > 1
) dup ON un.user_id = dup.user_id AND un.notice_id = dup.notice_id
WHERE un.id <> dup.keep_id;

-- 3. 재발 방지 — 유니크 제약 추가 (엔티티의 @UniqueConstraint(name = "uk_user_notices_user_id_notice_id", ...)와 이름 일치시킴)
ALTER TABLE user_notices
    ADD CONSTRAINT uk_user_notices_user_id_notice_id UNIQUE (user_id, notice_id);
