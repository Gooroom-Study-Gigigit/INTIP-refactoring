use inu_portal;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE reply_like;
TRUNCATE TABLE post_like;
TRUNCATE TABLE folder_post;
TRUNCATE TABLE scrap;
TRUNCATE TABLE reply;
TRUNCATE TABLE report;
TRUNCATE TABLE post;
TRUNCATE TABLE member_roles;
TRUNCATE TABLE folder;
TRUNCATE TABLE fire;
TRUNCATE TABLE member;
TRUNCATE TABLE notice;
TRUNCATE TABLE schedule;
TRUNCATE TABLE category;

SET FOREIGN_KEY_CHECKS = 1;

-- Fire 더미 데이터
INSERT INTO fire (id, prompt, request_id, member_id, is_rated, good_count,  create_date, modified_date) VALUES
(1, '프롬프트1', 'REQ1', 1, false, 0, NOW(), NOW()),
(2, '프롬프트2', 'REQ2', 2, false, 0, NOW(), NOW()),
(3, '프롬프트3', 'REQ3', 3, false, 0, NOW(), NOW()),
(4, '프롬프트4', 'REQ4', 4, false, 0, NOW(), NOW()),
(5, '프롬프트5', 'REQ5', 5, false, 0, NOW(), NOW()),
(6, '프롬프트6', 'REQ6', 6, false, 0, NOW(), NOW()),
(7, '프롬프트7', 'REQ7', 7, false, 0, NOW(), NOW()),
(8, '프롬프트8', 'REQ8', 8, false, 0, NOW(), NOW()),
(9, '프롬프트9', 'REQ9', 9, false, 0, NOW(), NOW()),
(10, '프롬프트10', 'REQ10', 10, false, 0, NOW(), NOW());

-- Notice 더미 데이터
INSERT INTO notice (id, category, title, writer, create_date, view, url) VALUES
(1, '공지사항1', '제목1', '작성자1', '2023-01-01', 100, 'http://example1.com'),
(2, '공지사항2', '제목2', '작성자2', '2023-01-02', 200, 'http://example2.com'),
(3, '공지사항3', '제목3', '작성자3', '2023-01-03', 300, 'http://example3.com'),
(4, '공지사항4', '제목4', '작성자4', '2023-01-04', 400, 'http://example4.com'),
(5, '공지사항5', '제목5', '작성자5', '2023-01-05', 500, 'http://example5.com'),
(6, '공지사항6', '제목6', '작성자6', '2023-01-06', 600, 'http://example6.com'),
(7, '공지사항7', '제목7', '작성자7', '2023-01-07', 700, 'http://example7.com'),
(8, '공지사항8', '제목8', '작성자8', '2023-01-08', 800, 'http://example8.com'),
(9, '공지사항9', '제목9', '작성자9', '2023-01-09', 900, 'http://example9.com'),
(10, '공지사항10', '제목10', '작성자10', '2023-01-10', 1000, 'http://example10.com');

INSERT INTO category (id, category)
VALUES
    (1, '카테고리1'),
    (2, '카테고리2'),
    (3, '카테고리3'),
    (4, '카테고리4'),
    (5, '카테고리5'),
    (6, '카테고리6'),
    (7, '카테고리7'),
    (8, '카테고리8'),
    (9, '카테고리9'),
    (10, '카테고리10');


-- 멤버 테이블 더미 데이터 삽입
INSERT INTO member (student_id, nickname, fire_id)
VALUES
('20231001', 'member1', 1),
('20231002', 'member2', 1),
('20231003', 'member3', 1),
('20231004', 'member4', 1),
('20231005', 'member5', 1),
('20231006', 'member6', 1),
('20231007', 'member7', 1),
('20231008', 'member8', 1),
('20231009', 'member9', 1),
('20231010', 'member10', 1);

-- roles 테이블 더미 데이터 삽입
INSERT INTO member_roles (member_id, roles) VALUES (1, 'ROLE_ADMIN');
INSERT INTO member_roles (member_id, roles) VALUES (1, 'ROLE_ADMIN');
INSERT INTO member_roles (member_id, roles) VALUES (2, 'ROLE_USER');
INSERT INTO member_roles (member_id, roles) VALUES (3, 'ROLE_USER');
INSERT INTO member_roles (member_id, roles) VALUES (4, 'ROLE_ADMIN');
INSERT INTO member_roles (member_id, roles) VALUES (4, 'ROLE_USER');
INSERT INTO member_roles (member_id, roles) VALUES (5, 'ROLE_USER');
INSERT INTO member_roles (member_id, roles) VALUES (6, 'ROLE_USER');
INSERT INTO member_roles (member_id, roles) VALUES (7, 'ROLE_USER');
INSERT INTO member_roles (member_id, roles) VALUES (8, 'ROLE_ADMIN');
INSERT INTO member_roles (member_id, roles) VALUES (9, 'ROLE_ADMIN');


INSERT INTO member_roles (member_id, roles) VALUES (10, 'ROLE_ADMIN');

-- 폴더 테이블 더미 데이터 삽입
INSERT INTO folder (name, member_id)
VALUES
('Folder1', 1),
('Folder2', 2),
('Folder3', 3),
('Folder4', 4),
('Folder5', 5),
('Folder6', 6),
('Folder7', 7),
('Folder8', 8),
('Folder9', 9),
('Folder10', 10);

-- 포스트 테이블 더미 데이터 삽입
INSERT INTO post (title, content, category, anonymous, number, good, scrap, view, image_count, reply_count, member_id, create_date, modified_date)
VALUES
('제목 1', '내용 1입니다.', '카테고리 A', false, 0, 5, 2, 10, 3, 0, 1, CURRENT_DATE, CURRENT_DATE),
('제목 2', '내용 2입니다.', '카테고리 B', true, 0, 10, 3, 20, 2, 0, 2, CURRENT_DATE, CURRENT_DATE),
('제목 3', '내용 3입니다.', '카테고리 C', false, 0, 7, 4, 15, 1, 2, 3, CURRENT_DATE, CURRENT_DATE),
('제목 4', '내용 4입니다.', '카테고리 D', true, 0, 12, 5, 30, 4, 3, 4, CURRENT_DATE, CURRENT_DATE),
('제목 5', '내용 5입니다.', '카테고리 E', false, 0, 9, 1, 25, 0, 1, 5, CURRENT_DATE, CURRENT_DATE),
('제목 6', '내용 6입니다.', '카테고리 A', true, 0, 11, 2, 40, 3, 2, 6, CURRENT_DATE, CURRENT_DATE),
('제목 7', '내용 7입니다.', '카테고리 B', false, 0, 8, 0, 12, 2, 1, 7, CURRENT_DATE, CURRENT_DATE),
('제목 8', '내용 8입니다.', '카테고리 C', true, 0, 13, 3, 50, 1, 5, 8, CURRENT_DATE, CURRENT_DATE),
('제목 9', '내용 9입니다.', '카테고리 D', false, 0, 6, 4, 18, 4, 3, 9, CURRENT_DATE, CURRENT_DATE),
('제목 10', '내용 10입니다.', '카테고리 E', true, 0, 15, 5, 60, 5, 0, 10, CURRENT_DATE, CURRENT_DATE);


-- 폴더포스트 테이블 더미 데이터 삽입
INSERT INTO folder_post (post_id, folder_id, scrap)
VALUES
(1, 1, NULL),
(2, 2, NULL),
(3, 3, NULL),
(4, 4, NULL),
(5, 5, NULL),
(6, 6, NULL),
(7, 7, NULL),
(8, 8, NULL),
(9, 9, NULL),
(10, 10, NULL);


-- PostLike 더미 데이터
INSERT INTO post_like (id, member_id, post_id) VALUES
(1, 1, 1),
(2, 2, 2),
(3, 3, 3),
(4, 4, 4),
(5, 5, 5),
(6, 6, 6),
(7, 7, 7),
(8, 8, 8),
(9, 9, 9),
(10, 10, 10);

-- Reply 더미 데이터
INSERT INTO reply (id, content, anonymous, is_deleted, number, likeCount, post_id, member_id, parent_reply_id,  create_date, modified_date) VALUES
(1, '댓글1', true, false, 1, 0, 1,1, NULL, NOW(), NOW()),
(2, '댓글2', false, false, 2, 0, 2,2, NULL, NOW(), NOW()),
(3, '댓글3', true, false, 3, 0, 3,3, NULL, NOW(), NOW()),
(4, '댓글4', false, false, 4, 0, 4,4, NULL, NOW(), NOW()),
(5, '댓글5', true, false, 5, 0, 5,5, NULL, NOW(), NOW()),
(6, '댓글6', false, false, 6, 0, 6,6, NULL, NOW(), NOW()),
(7, '댓글7', true, false, 7, 0, 7,7, NULL, NOW(), NOW()),
(8, '댓글8', false, false, 8, 0, 8,8, NULL, NOW(), NOW()),
(9, '댓글9', true, false, 9, 0, 9,9, NULL, NOW(), NOW()),
(10, '댓글10', false, false, 10, 0, 10,10, NULL, NOW(), NOW());

-- ReplyLike 더미 데이터
INSERT INTO reply_like (id, member_id, reply_id) VALUES
(1, 1, 1),
(2, 2, 2),
(3, 3, 3),
(4, 4, 4),
(5, 5, 5),
(6, 6, 6),
(7, 7, 7),
(8, 8, 8),
(9, 9, 9),
(10, 10, 10);

-- Report 더미 데이터
INSERT INTO report (id, reason, comment, member_id, post_id,  create_date, modified_date) VALUES
(1, '이유1', '코멘트1', 1, 1, NOW(), NOW()),
(2, '이유2', '코멘트2', 2, 2, NOW(), NOW()),
(3, '이유3', '코멘트3', 3, 3, NOW(), NOW()),
(4, '이유4', '코멘트4', 4, 4, NOW(), NOW()),
(5, '이유5', '코멘트5', 5, 5, NOW(), NOW()),
(6, '이유6', '코멘트6', 6, 6, NOW(), NOW()),
(7, '이유7', '코멘트7', 7, 7, NOW(), NOW()),
(8, '이유8', '코멘트8', 8, 8, NOW(), NOW()),
(9, '이유9', '코멘트9', 9, 9, NOW(), NOW()),
(10, '이유10', '코멘트10', 10, 10, NOW(), NOW());

-- Schedule 더미 데이터
INSERT INTO schedule (id, start_date, end_date, content) VALUES
(1, '2023-01-01', '2023-01-02', '스케줄1'),
(2, '2023-01-02', '2023-01-03', '스케줄2'),
(3, '2023-01-03', '2023-01-04', '스케줄3'),
(4, '2023-01-04', '2023-01-05', '스케줄4'),
(5, '2023-01-05', '2023-01-06', '스케줄5'),
(6, '2023-01-06', '2023-01-07', '스케줄6'),
(7, '2023-01-07', '2023-01-08', '스케줄7'),
(8, '2023-01-08', '2023-01-09', '스케줄8'),
(9, '2023-01-09', '2023-01-10', '스케줄9'),
(10, '2023-01-10', '2023-01-11', '스케줄10');

-- Scrap 더미 데이터
INSERT INTO scrap (id, member_id, post_id) VALUES
(1, 1, 1),
(2, 2, 2),
(3, 3, 3),
(4, 4, 4),
(5, 5, 5),
(6, 6, 6),
(7, 7, 7),
(8, 8, 8),
(9, 9, 9),
(10, 10, 10);