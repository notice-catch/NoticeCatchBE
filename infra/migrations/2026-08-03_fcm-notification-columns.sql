-- PR #45 (feat/fcm-init) 배포 전 prod DB에 수동으로 실행할 것.
-- local/dev는 ddl-auto: update라 자동 반영되지만 prod는 ddl-auto: validate라
-- 컬럼이 미리 없으면 앱이 부팅 실패한다.
--
-- 실행 순서: 이 스크립트를 먼저 실행한 뒤에 새 버전을 배포할 것.
-- (컬럼만 미리 추가돼 있는 상태는 구버전 앱 동작에 영향 없음 — 안전)

-- 1. notices.notified — 신규 공지 키워드/카테고리 알림 배치가 처리 여부를 추적하는 플래그
ALTER TABLE notices ADD COLUMN notified TINYINT(1) NOT NULL DEFAULT 0;

-- 기존에 쌓여있던 공지를 전부 "새 공지"로 인식해서 알림이 한꺼번에 나가는 것을 막기 위해
-- 이 스크립트 실행 시점 기준 기존 행은 이미 처리된 것으로 표시한다.
UPDATE notices SET notified = 1;

-- 2. user_notices.closing_notified — 스크랩한 공지의 마감임박(D-3) 알림을 유저별로 한 번만 보내기 위한 플래그
-- 기존 행은 0(false)으로 남겨둔다 — 마감이 실제로 임박한 스크랩 건이 있다면 알림이 나가는 게 맞는 동작이라 백필 불필요.
ALTER TABLE user_notices ADD COLUMN closing_notified TINYINT(1) NOT NULL DEFAULT 0;
