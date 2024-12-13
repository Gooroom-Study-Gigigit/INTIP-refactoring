# inu-portal-server

---

**앱센터 겨울방학 프로젝트 INTIP의 서버 저장소입니다.**

# INTIP

---

인천대학교 학생들을 위한 정보, 편의성을 담은 웹사이트입니다.

https://inu-portal-web.pages.dev/

---
# 커밋 컨벤션
    - 예시
        - feat: todo-list 회원 API 엔티티 구현 - #2
        - fix: todo-list 회원 단건 조회 서비스 에러 수정 - #2
- feat: 새로운 기능 추가
- fix: 버그 수정
- docs: 문서 수정
- style: 코드 포맷팅, 세미콜론 누락, 코드 변경이 없는 경우
- refactor: 코드 리팩토링
- test: 테스트 코드, 리팩토링 테스트 코드 추가
- chore: 빌드 업무 수정, 패키지 매니저 수정

      
# 이슈 컨벤션
        - issue 제목
            - 예시: feat: 이슈 정리
        - issue 템플릿

            ```markdown
            ## 📋 이슈 내용
            
            ## ✅ 체크리스트
            
            ## 📚 레퍼런스
            
            ```
        - 제목 예시
            - add: UI button 구현

# branch 규칙
    - 각자 영어이름#이슈번호-이슈타입-이슈제목
    - 예시: hyungjun#12-feat-create-user-api
    
    - 종류: 메시지 - #이슈번호
    - 예시
        - feat: todo-list 회원 API 엔티티 구현 - #2
        - fix: todo-list 회원 단건 조회 서비스 에러 수정 - #2

# PR 템플릿
    - PR 템플릿

        ```markdown
        ## 📋 이슈 번호
        
        ## 🛠 구현 사항
        
        ## 📚 기타
        
        ```

# merge 컨벤션
    - merge: 브랜치 이름 - #Issue 번호 혹은 PR 번호
    - 예시
        - merge: main <-hyungjun#1-feat-user-controller
        

# ERD

---

![Untitled](https://github.com/user-attachments/assets/4f6865b2-eb38-4cb5-92fd-0894205c69dd)

# 🗺️아키텍처

---

![Untitled 1](https://github.com/user-attachments/assets/1406099f-98c8-4409-bf57-4c6d658e6721)


# 💡기능

---

### 회원

- 로그인 (인천대학교 포탈 아이디 연동)
- 회원 정보 수정
- 회원 탈퇴
- 회원이 작성한 글/댓글, 스크랩/좋아요한 글,댓글 조회
- 스크랩 폴더

### 게시글 스크랩 폴더

- 게시글 스크랩 폴더 생성/수정/삭제
- 스크랩 폴더에 게시글 담기/빼기

### 게시글

- 게시글 등록/수정/삭제
- 게시글의 이미지 등록/수정/삭제
- 게시글 좋아요/스크랩
- 카테고리별 게시글 리스트 제공
- 익명 게시글

### 댓글

- 댓글,대댓글 등록/수정/삭제
- 익명 댓글

### 검색

- 게시글 검색
- 스크랩 한 게시글 중 검색
- 스크랩 폴더 속 게시글 검색

### 인천대학교 공지사항

- 학교 공지사항 크롤링을 통해 제공

### 인천대학교 식단

- 학교 식단 크롤링을 통해 제공

### 인천대학교 학사일정

- 학교 학사일정 크롤링을 통해 제공

### 인천대학교(송도) 날씨

- 기상청 초단기예보API를 활용해 제공
- 한국천문연구원 출몰시각정보 API를 활용해 제공

### 횃불이 AI 이미지 생성 - AI 연구실 연계

- 텍스트로 횃불이 AI 이미지 생성

### 배네

- 각 학과의 홈페이지, 학교 관련 사이트로 이동

# 📌기술적 고민

---

- 교내 데이터베이스(Oracle)과 서버 데이터베이스(MySql) 동시 연결을 위한 Config 설정
- 지속적으로 버전이 업데이트 되는 Chrome, ChromeDriver 설치 자동화에 대한 docker 파일 설정 - https://velog.io/@hen715/docker-도커-컨테이너에서의-동적-웹-크롤링을-위한-도커파일
- 좋아요 와 같은 정확한 수치 계산이 필요한 데이터베이스 비관적 lock 설정에 대한 읽기 lock, 쓰기 lock 모드에 대한 성능 차이 - https://velog.io/@hen715/낙관적-락의-동시성과-비관적-락의-성능-테스트
- 검색 성능  향상을 위한 full text search 도입
- 메인 화면에 자주 바뀌는 인기글 유지 및 선정을 위한 Cache 전략
- 새로운 게시글의 작성으로 인한 페이징 밀림 현상에 대한 페이징 개선 - https://velog.io/@hen715/SpringBoot-Cursor-방식을-통한-페이징-성능-개선 
