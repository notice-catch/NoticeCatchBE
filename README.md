# 🚀 공지캐치 (Notice-Catch)

대학 공지사항을 효율적으로 모아 유저 맞춤형 피드와 스마트한 알림을 제공하는 안드로이드 앱 서비스입니다.
<br><br>

## 🛠️ Tech Stack

| Layer | Technology |
| :--- | :--- |
| **Backend** | ![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=flat-and-square&logo=java&logoColor=white) ![Spring Boot](https://img.shields.io/badge/spring%20boot-%236DB33F.svg?style=flat-and-square&logo=spring&logoColor=white) |
| **Infrastructure** | ![Docker](https://img.shields.io/badge/docker-%232496ED.svg?style=flat-and-square&logo=docker&logoColor=white) ![MariaDB](https://img.shields.io/badge/mariadb-%23003545.svg?style=flat-and-square&logo=mariadb&logoColor=white) |

<br>

## ✨ Key Features (서비스 기능 정의)

### 🔐 1. 회원가입 및 맞춤형 온보딩
* **소셜 로그인:** 카카오, 구글 계정을 이용해 간편하게 회원가입 및 로그인을 진행합니다.
* **소속 대학/학과 설정:** 내가 다니는 대학교와 학과를 검색하여 내 프로필에 등록합니다.
* **개인 프로필 완성:** 학년 정보와 함께 첫 온보딩 시 나만의 관심 키워드를 설정합니다.

### 📅 2. 홈 공지사항 피드 & 상세 조회
* **통합 공지 피드:** 카테고리 필터 칩과 키워드를 통해 수많은 학교 공지를 페이징 형태로 쾌적하게 조회합니다.
* **공지 상세 보기:** 공지의 본문 내용과 함께 원본 URL 링크, 첨부파일 유무, 그리고 마감까지 남은 `D-Day` 일수를 직관적으로 확인합니다.
* **키워드 검색:** 내가 원하는 단어를 입력하고 정렬 기준에 맞춰 공지사항을 빠르게 검색할 수 있습니다.

### 🔑 3. 맞춤형 관심 키워드 관리
* **추천 키워드 기반 등록:** 학교에서 가장 인기가 많은 추천 키워드(장학금, 비교과, 취업 등)를 한눈에 보고 선택할 수 있습니다.
* **나만의 커스텀 키워드:** 추천 목록에 없더라도 내가 상시 알림을 받고 싶은 단어(예: 공모전, 개발 등)를 직접 입력하여 자유롭게 추가·수정·삭제합니다.

### 🔔 4. 스마트 알림 센터 및 푸시 알림
* **푸시 토큰 관리:** 알림을 정상적으로 수신할 수 있도록 사용자 기기의 푸시 토큰(FCM)을 안전하게 등록합니다.
* **체계적인 알림함:** 도착한 알림들을 종류별(마감 임박 공지 `CLOSING`, 키워드 매칭 공지 `KEYWORD`)로 분류하고, 읽지 않은 알림은 빨간 점(Badge)으로 강조하여 보여줍니다.
* **알림 연동 이동:** 알림함을 통해 들어온 메시지를 클릭하면 해당 공지의 상세 화면으로 즉시 이동합니다.

### ⚙️ 5. 마이페이지 및 맞춤형 알림 설정
* **내 프로필 조회:** 내가 등록한 대학교, 학과, 학년 정보를 한눈에 확인합니다.
* **세부 알림 토글 제어:** 전체 알림 On/Off는 물론, 마감 임박 알림이나 특정 카테고리(장학, 비교과, 학사, 취업 등)별로 알림을 받을지 말지 유저가 입맛대로 켜고 끌 수 있습니다.

<br>

## 🚀 Getting Started (로컬 개발 환경 실행 방법)

공지캐치 백엔드 프로젝트를 로컬 컴퓨터에서 가장 빠르게 실행하는 방법입니다. 프로젝트는 **Docker**를 통해 데이터베이스 환경을 격리하여 관리합니다.

### 📋 1. 사전 요구사항 (Prerequisites)
프로젝트를 실행하기 전, 시스템에 아래 프로그램이 설치되어 있어야 합니다.
* **Java 21 JDK** (추천: Eclipse Temurin 21)
* **Docker** 및 **Docker Desktop** (실행 중이어야 함)

### 🛠️ 2. 소스코드 복사 및 실행 (Quick Start)

**Step 1. 저장소 복사하기**
```bash
git clone [git repository 주소]
cd notice-catch
```


**Step 2. 로컬 개발용 마리아DB(MariaDB) 컨테이너 구동하기**
```bash
docker compose -f infra/docker-compose-local.yml up -d
```


**Step 3. 환경변수(.env) 설정하기**

프로젝트 루트(`NoticeCatchBE/`)에 `.env` 파일을 만들고 아래 값을 채워주세요. Google/Kakao 값은 팀 채널에서 공유받은 값을 그대로 사용하면 됩니다.

```env
# 필수 — 없으면 부팅 자체가 실패합니다
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
GOOGLE_REDIRECT_URI=http://localhost:8080/login/oauth2/code/google
KAKAO_CLIENT_ID=
KAKAO_CLIENT_SECRET=
KAKAO_REDIRECT_URI=http://localhost:8080/login/oauth2/code/kakao

# 선택 — 비워둬도 부팅은 되고 해당 기능만 비활성화됩니다
GEMINI_API_KEY=
FIREBASE_CREDENTIALS_BASE64=
```

`.env`는 `.gitignore`에 포함되어 있어 커밋되지 않습니다. `FIREBASE_CREDENTIALS_BASE64`는 Firebase 콘솔의 서비스 계정 키(JSON)를 base64 한 줄로 인코딩한 값이며, 비워두면 FCM 푸시 발송만 비활성화되고 나머지 기능은 정상 동작합니다.

<br>

**Step 4. 스프링 부트 애플리케이션 실행하기**

인텔리제이(IntelliJ IDEA)로 프로젝트를 열고 NoticeCatchApplication.java 의 재생(▶️) 버튼을 누르거나, 터미널에서 아래 명령어를 입력합니다.

Windows (명령 프롬프트 / PowerShell)

```bash
./gradlew bootRun
```


Mac / Linux

```bash
chmod +x gradlew
./gradlew bootRun
```

<br>

### 🔗 3. 연결 정보 및 API 명세서 (Endpoints)

서버가 정상적으로 구동되면 브라우저를 통해 아래 주소들에 접근할 수 있습니다.

* **Swagger API 자동 명세서:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

**로컬 데이터베이스 접속 정보 (MariaDB):**
* **Host:** `localhost`
* **Port:** `3306`
* **Database:** `notice_catch`
* **Username / Password:** `catch_user` / `catch_password`

<br>

🤝 **프로젝트의 일관성을 위한 [공지캐치 SpringBoot 개발 컨벤션 규칙 보러가기](./docs/CONVENTION.md)**

