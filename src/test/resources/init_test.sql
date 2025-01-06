-- FOREIGN_KEY_CHECKS 비활성화
SET FOREIGN_KEY_CHECKS = 0;

-- 테이블 초기화
TRUNCATE TABLE reply_like;
TRUNCATE TABLE reply;
TRUNCATE TABLE post;
TRUNCATE TABLE member_roles;
TRUNCATE TABLE member;

-- FOREIGN_KEY_CHECKS 활성화
SET FOREIGN_KEY_CHECKS = 1;

-- Member 데이터 삽입
INSERT INTO member (id, student_id, nickname)
VALUES
    (1, '11111111', '게시글 작성 유저'),
    (2, '22222222', '요청 유저'),
    (3, '33333333', '댓글 작성 유저 1'),
    (4, '44444444', '댓글 작성 유저 2'),
    (5, '55555555', '댓글 작성 유저 3'),
    (6, '66666666', '댓글 좋아요 작성 유저 1'),
    (7, '77777777', '댓글 좋아요 작성 유저 2'),
    (8, '88888888', '댓글 좋아요 작성 유저 3'),
    (9, '99999999', '댓글 좋아요 작성 유저 4'),
    (10, '10101010', '댓글 좋아요 작성 유저 5'),
    (11, '11111110', '댓글 좋아요 작성 유저 6'),
    (12, '12121212', '댓글 좋아요 작성 유저 7');

INSERT INTO member_roles (member_id, roles)
VALUES
    (1, 'ROLE_USER'),
    (2, 'ROLE_USER'),
    (3, 'ROLE_USER'),
    (4, 'ROLE_USER'),
    (5, 'ROLE_USER'),
    (6, 'ROLE_USER'),
    (7, 'ROLE_USER'),
    (8, 'ROLE_USER'),
    (9, 'ROLE_USER'),
    (10, 'ROLE_USER'),
    (11, 'ROLE_USER'),
    (12, 'ROLE_USER');


-- Post 데이터 초기화
INSERT INTO post (id, content, title, anonymous, member_id, category, number, image_count, create_date, modified_date)
VALUES
    (1, '게시글 내용1', '게시글 제목1', true, 1, '카테고리', 24, 0, NOW(), NOW()),
    (2, '게시글 내용2', '게시글 제목2', true, 2, '카테고리', 0, 0, NOW(), NOW());


-- Reply 데이터 초기화 (parentReply가 NULL)
INSERT INTO reply (id, content, anonymous, is_deleted, number, likeCount, post_id, member_id, parent_reply_id, create_date, modified_date)
VALUES
    (1, '댓글 1', false, false, 1, 7, 1, 1, NULL, NOW(), NOW()), -- 6번~12번 유저 좋아요 (7개)
    (2, '댓글 2', false, false, 2, 7, 1, 1, NULL, NOW(), NOW()), -- 6번~12번 유저 좋아요 (7개)
    (3, '댓글 3', true, false, 3, 7, 1, 2, NULL, NOW(), NOW()), -- 6번~12번 유저 좋아요 (7개)
    (4, '댓글 4', false, false, 4, 7, 1, 2, NULL, NOW(), NOW()), -- 6번~12번 유저 좋아요 (7개)
    (5, '댓글 5', true, false, 5, 7, 1, 3, NULL, NOW(), NOW()), -- 6번~12번 유저 좋아요 (7개)
    (6, '댓글 6', false, false, 6, 7, 1, 3, NULL, NOW(), NOW()), -- 6번~12번 유저 좋아요 (7개)
    (7, '댓글 7', true, false, 7, 7, 1, 4, NULL, NOW(), NOW()), -- 6번~12번 유저 좋아요 (7개)
    (8, '댓글 8', false, false, 8, 7, 1, 4, NULL, NOW(), NOW()), -- 6번~12번 유저 좋아요 (7개)
    (9, '댓글 9', true, false, 9, 7, 1, 5, NULL, NOW(), NOW()), -- 6번~12번 유저 좋아요 (7개)
    (10, '댓글 10', false, false, 10, 7, 1, 5, NULL, NOW(), NOW()), -- 6번~12번 유저 좋아요 (7개)
    (21, 'delete 댓글1', true, true, 21, 2, 1, 2, NULL, NOW(), NOW()), -- 7번, 8번 유저 좋아요 (2개)
    (22, 'delete 댓글 2', false, true, 22, 2, 1, 2, NULL, NOW(), NOW()), -- 6번, 9번 유저 좋아요 (2개)
    (25, '게시글2 댓글', false, true, 22, 0, 2, 2, NULL, NOW(), NOW()); -- 6번, 9번 유저 좋아요 (2개)


-- Reply 데이터 초기화 (parentReply 존재)
INSERT INTO reply (id, content, anonymous, is_deleted, number, likeCount, post_id, member_id, parent_reply_id, create_date, modified_date)
VALUES
    (11, '대댓글 1', false, false, 1, 2, 1, 5, 1, NOW(), NOW()), -- 6번, 7번 유저 좋아요 (2개)
    (12, '대댓글 2', true, false, 2, 2, 1, 5, 2, NOW(), NOW()), -- 6번, 8번 유저 좋아요 (2개)
    (13, '대댓글 3', false, false, 3, 2, 1, 4, 3, NOW(), NOW()), -- 9번, 10번 유저 좋아요 (2개)
    (14, '대댓글 4', true, false, 4, 2, 1, 4, 4, NOW(), NOW()), -- 11번, 12번 유저 좋아요 (2개)
    (15, '대댓글 5', false, false, 5, 2, 1, 3, 5, NOW(), NOW()), -- 6번, 9번 유저 좋아요 (2개)
    (16, '대댓글 6', true, false, 6, 2, 1, 3, 6, NOW(), NOW()), -- 7번, 8번 유저 좋아요 (2개)
    (17, '대댓글 7', false, false, 7, 2, 1, 2, 7, NOW(), NOW()), -- 10번, 12번 유저 좋아요 (2개)
    (18, '대댓글 8', true, false, 8, 2, 1, 2, 8, NOW(), NOW()), -- 6번, 7번 유저 좋아요 (2개)
    (19, '대댓글 9', false, false, 9, 2, 1, 1, 9, NOW(), NOW()), -- 8번, 9번 유저 좋아요 (2개)
    (20, '대댓글 10', true, false, 10, 2, 1, 1, 21, NOW(), NOW()), -- 10번, 11번 유저 좋아요 (2개)
    (23, 'delete 대댓글 1', true, true, 23, 2, 1, 2, 10, NOW(), NOW()), -- 12번, 11번 유저 좋아요 (2개)
    (24, 'delete 대댓글 2', false, true, 24, 2, 1, 2, 10, NOW(), NOW()); -- 6번, 10번 유저 좋아요 (2개)

-- ReplyLike 데이터 초기화 (1번부터 10번 댓글: 6번 ~ 12번 유저는 모두 좋아요)
INSERT INTO reply_like (reply_id, member_id)
VALUES
    (1, 6), (1, 7), (1, 8),
    (1, 9), (1, 10), (1, 11),
    (1, 12),
    (2, 6), (2, 7), (2, 8),
    (2, 9), (2, 10), (2, 11),
    (2, 12),
    (3, 6), (3, 7), (3, 8),
    (3, 9), (3, 10), (3, 11),
    (3, 12),
    (4, 6), (4, 7), (4, 8),
    (4, 9), (4, 10), (4, 11),
    (4, 12),
    (5, 6), (5, 7), (5, 8),
    (5, 9), (5, 10), (5, 11),
    (5, 12),
    (6, 6), (6, 7), (6, 8),
    (6, 9), (6, 10), (6, 11),
    (6, 12),
    (7, 6), (7, 7), (7, 8),
    (7, 9), (7, 10), (7, 11),
    (7, 12),
    (8, 6), (8, 7), (8, 8),
    (8, 9), (8, 10), (8, 11),
    (8, 12),
    (9, 6), (9, 7), (9, 8),
    (9, 9), (9, 10), (9, 11),
    (9, 12),
    (10, 6), (10, 7), (10, 8),
    (10, 9), (10, 10), (10, 11),
    (10, 12);

-- ReplyLike 데이터 초기화 (10번부터 24번 댓글: 랜덤으로 좋아요)
INSERT INTO reply_like (reply_id, member_id)
VALUES
    (11, 6), (11, 7),
    (12, 6), (12, 8),
    (13, 9), (13, 10),
    (14, 11), (14, 12),
    (15, 6), (15, 9),
    (16, 7), (16, 8),
    (17, 10), (17, 12),
    (18, 6), (18, 7),
    (19, 8), (19, 9),
    (20, 10), (20, 11),
    (21, 7), (21, 8),
    (22, 6), (22, 9),
    (23, 12), (23, 11),
    (24, 6), (24, 10);

