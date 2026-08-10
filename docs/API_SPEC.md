# NoticeCatch API 명세서

> 이 문서는 팀에서 공유된 API 명세서(2026-07)를 정리한 것입니다. JSON 기반으로 작성되었습니다.

## 필독 사항

1. 이 API는 JSON을 기반으로 작성되었습니다.
2. 본 API를 읽고 생기는 의문점이나 수정해야 할 점들은 바로 알려주면 반영합니다.
   - 수정사항이나 의문점은 레몬/박세은에게 전달 (디스코드, 카톡 무관)
   - 최대한 빨리 수정 후 완료 시 안내
   - 수정 완료 안내 및 의문점 답변은 오전 10시 ~ 오후 10시 사이에 전달
3. 개발 중 API 변경이 있을 수 있으며, 변경 시 카톡/디스코드로 안내 후 본 명세서에 반영됩니다.

## 목차

- [변경 사항](#변경-사항)
- [공통 응답 객체](#공통-응답-객체)
- [회원가입 및 로그인 API (담당: 레몬)](#회원가입-및-로그인-api-담당-레몬)
- [홈 공지 피드 (담당: 레몬)](#홈-공지-피드-담당-레몬)
- [공지 상세 (담당: 베리)](#공지-상세-담당-베리)
- [검색 (담당: 베리)](#검색-담당-베리)
- [스크랩 캘린더 (담당: 베리)](#스크랩-캘린더-담당-베리)
- [알람 푸시 (담당: 베리)](#알람-푸시-담당-레스)
- [마이페이지 설정 (담당: 레스)](#마이페이지-설정-담당-레스)

---

## 변경 사항

- API 응답 객체를 구현하면서 모든 API에 공통 응답 객체가 적용되어, 모든 response API에 변경사항이 생겼습니다. (아래 [공통 응답 객체](#공통-응답-객체) 참고)
- 공지 상세 보기 API의 response에 AI 요약 데이터(`aiSummary`)를 받는 부분을 추가했습니다.
  ```json
  "aiSummary": {
    "eligibility": "직전 학기 12학점 이상 이수 및 이수 평점 3.5 이상인 자",
    "benefit": "등록금 전액 면제 및 도서비 분기별 20만 원 지급",
    "deadline": "2026년 7월 9일 18:00까지 (오프라인 제출 불가)"
  }
  ```
- 각 API response 아래에 예외 상황일 경우 어떻게 전달되는지 추가했습니다.
- 프로필 수정 API에서 endpoint와 request가 전반적으로 수정되었습니다.
- 공지 상세 보기 API에서 response의 `dday`가 없어지고 `deadlineAt`이 추가되었습니다.
- 마감일이 없는 마감 공지 목록 조회 API가 기존에는 선택한 날짜의 마감 공지 목록 조회와 동일한 endpoint를 썼으나, 별도의 endpoint로 분리되었습니다.
- 실제 백엔드 구현과 대조하여 문서 전반을 교정했습니다: 성공 메시지를 실제 값("성공적으로 요청을 처리했습니다.")으로 통일, 인증 실패 코드를 실제 값(`COMMON401`)으로 통일, 서버 오류 코드를 실제 값(`COMMON500`)으로 통일, 로그인/키워드/스펙 로그/공지 피드 섹션의 실존하지 않는 에러 코드·메시지·request 필드를 코드 기준으로 수정했습니다.

---

## 공통 응답 객체

### 단일 응답

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "isSuccess": true,
    "code": "COMMON200",
    "message": "성공적으로 요청을 처리했습니다.",
    "result": {
      // 단일 객체 결과물
    }
  }
}
```

### 리스트 응답

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "isSuccess": true,
    "code": "COMMON200",
    "message": "성공적으로 요청을 처리했습니다.",
    "result": {
      "content": [
        // 리스트 객체 결과물
      ],
      "totalCount": 0 // 리스트 개수(리스트 크기)
    }
  }
}
```

### 페이지 응답

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "isSuccess": true,
    "code": "COMMON200",
    "message": "성공적으로 요청을 처리했습니다.",
    "result": {
      "content": [
        // 페이지 객체 결과물
      ],
      "page": 0,
      "size": 20,
      "hasNext": true
    }
  }
}
```

---

## 회원가입 및 로그인 API (담당: 레몬)

### 회원가입/로그인

`POST /api/v1/auth/login`

**Request**

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "authorizationCode": "9yG_Xxxxxxx_sample_kakao_authorization_code_xxxxxx",
    "socialType": "KAKAO"
  }
}
```

**Response**

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "isSuccess": true,
    "code": "COMMON200",
    "message": "성공적으로 요청을 처리했습니다.",
    "result": {
      "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "nickname": "소셜유저",
      "isNewUser": true
    }
  }
}
```

**예외 상황**

1. 소셜 토큰이 유효하지 않거나 만료된 경우, 혹은 소셜 서버 응답을 읽을 수 없는 경우 (Unauthorized)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "USER4011",
       "message": "유효하지 않거나 만료된 소셜 토큰입니다.",
       "result": null
     }
   }
   ```
2. 지원하지 않는 소셜 로그인 제공자(`socialType`)로 요청한 경우 (Bad Request)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "USER4003",
       "message": "지원하지 않는 소셜 로그인 제공자입니다.",
       "result": null
     }
   }
   ```
3. 서버 내부 오류 (DB 또는 소셜 서버 통신 장애)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON500",
       "message": "서버 내부 오류가 발생했습니다.",
       "result": null
     }
   }
   ```

### 대학 목록 조회 (대학 선택 기능을 위해 선행되어야 함)

`GET /api/v1/universities`

**Request**

```json
{
  "headers": {
    "Content-Type": "application/json",
    "Authorization": "Bearer eyJhbGciOiJIUzI1..."
  },
  "body": null
}
```

**Response**

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "isSuccess": true,
    "code": "COMMON200",
    "message": "성공적으로 요청을 처리했습니다.",
    "result": {
      "content": [
        { "universityId": 1, "universityName": "동아대학교" },
        { "universityId": 2, "universityName": "영남대학교" },
        { "universityId": 3, "universityName": "인제대학교" }
      ],
      "totalCount": 3
    }
  }
}
```

**예외 상황**

1. 대학 정보가 없는 경우
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "UNIV4041",
       "message": "대학 정보가 시스템에 존재하지 않습니다.",
       "result": null
     }
   }
   ```
2. 서버 내부 오류
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON500",
       "message": "서버 내부 오류가 발생했습니다.",
       "result": null
     }
   }
   ```

### 대학 선택

`PATCH /api/v1/users/university/{universityId}`

**Request**

```json
{
  "headers": {
    "Content-Type": "application/json",
    "Authorization": "Bearer eyJhbGciOiJIUzI1..."
  },
  "body": null
}
```

**Response**

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "isSuccess": true,
    "code": "COMMON200",
    "message": "성공적으로 요청을 처리했습니다.",
    "result": null
  }
}
```

**예외 상황**

1. 존재하지 않는 대학 ID를 보낸 경우 (Not Found)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "UNIV4042",
       "message": "유효하지 않거나 존재하지 않는 대학 식별자입니다.",
       "result": null
     }
   }
   ```
2. 이미 대학교가 등록되어 있는 경우 (Conflict)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "UNIV4091",
       "message": "이미 대학교 정보가 등록된 사용자입니다. 변경을 원하시면 프로필 수정을 이용해 주세요.",
       "result": null
     }
   }
   ```
3. 필수 인증 토큰이 누락되었거나 유효하지 않은 경우 (Unauthorized)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON401",
       "message": "인증되지 않았습니다.",
       "result": null
     }
   }
   ```
4. 서버 내부 오류
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON500",
       "message": "서버 내부 오류가 발생했습니다.",
       "result": null
     }
   }
   ```

### 학과 목록 조회 및 검색 (학과 선택을 위해 선행되어야 함)

`GET /api/v1/universities/{universityId}/departments?keyword='검색 단어'`

**Request**

```json
{
  "headers": {
    "Content-Type": "application/json",
    "Authorization": "Bearer eyJhbGciOiJIUzI1..."
  },
  "body": null
}
```

**Response**

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "isSuccess": true,
    "code": "COMMON200",
    "message": "성공적으로 요청을 처리했습니다.",
    "result": {
      "content": [
        { "departmentId": 1, "departmentName": "컴퓨터공학과" },
        { "departmentId": 2, "departmentName": "소프트웨어학과" }
      ],
      "totalCount": 2
    }
  }
}
```

**예외 상황**

1. 검색 결과에 해당하는 학과가 없는 경우
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "DEPT4041",
       "message": "해당 학과 정보가 시스템에 존재하지 않습니다. 입력한 이름을 다시 확인해 주세요.",
       "result": null
     }
   }
   ```
2. 특정 대학교에 종속된 학과가 존재하지 않는 경우 (Not Found)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "DEPT4042",
       "message": "선택하신 대학교에 개설된 학과 정보를 찾을 수 없습니다.",
       "result": null
     }
   }
   ```
3. 필수 인증 토큰이 누락되었거나 유효하지 않은 경우 (Unauthorized)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON401",
       "message": "인증되지 않았습니다.",
       "result": null
     }
   }
   ```
4. 서버 내부 오류
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON500",
       "message": "서버 내부 오류가 발생했습니다.",
       "result": null
     }
   }
   ```

### 온보딩 프로필 설정

`PATCH /api/v1/users/department/{departmentId}`

**Request**

```json
{
  "headers": {
    "Content-Type": "application/json",
    "Authorization": "Bearer eyJhbGciOiJIUzI1..."
  },
  "body": { "grade": 3 }
}
```

**Response**

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "isSuccess": true,
    "code": "COMMON200",
    "message": "성공적으로 요청을 처리했습니다.",
    "result": null
  }
}
```

**예외 상황**

1. 존재하지 않는 학과 ID를 보낸 경우 (Not Found)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "DEPT4043",
       "message": "유효하지 않거나 존재하지 않는 학과 식별자(ID)입니다.",
       "result": null
     }
   }
   ```
2. 필수 인증 토큰이 누락되었거나 유효하지 않은 경우 (Unauthorized)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON401",
       "message": "인증되지 않았습니다.",
       "result": null
     }
   }
   ```
3. 서버 내부 오류
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON500",
       "message": "서버 내부 오류가 발생했습니다.",
       "result": null
     }
   }
   ```

### 추천 키워드 목록 조회

`GET /api/v1/keywords/recommend`

**Request**

```json
{
  "headers": {
    "Content-Type": "application/json",
    "Authorization": "Bearer eyJhbGciOiJIUzI1..."
  },
  "body": null
}
```

**Response**

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "isSuccess": true,
    "code": "COMMON200",
    "message": "성공적으로 요청을 처리했습니다.",
    "result": {
      "content": [
        { "keyword": "장학금", "keywordType": "RECOMMEND" },
        { "keyword": "비교과", "keywordType": "RECOMMEND" },
        { "keyword": "학사", "keywordType": "RECOMMEND" },
        { "keyword": "취업", "keywordType": "RECOMMEND" },
        { "keyword": "대외활동", "keywordType": "RECOMMEND" },
        { "keyword": "교환학생", "keywordType": "RECOMMEND" }
      ],
      "totalCount": 6
    }
  }
}
```

**예외 상황**

1. 추천 키워드 데이터가 비어있는 경우 (데이터 부재)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": true,
       "code": "COMMON200",
       "message": "성공적으로 요청을 처리했습니다.",
       "result": { "content": [], "totalCount": 0 }
     }
   }
   ```
2. 필수 인증 토큰이 누락되었거나 유효하지 않은 경우 (Unauthorized)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON401",
       "message": "인증되지 않았습니다.",
       "result": null
     }
   }
   ```
3. 서버 내부 오류
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON500",
       "message": "서버 내부 오류가 발생했습니다.",
       "result": null
     }
   }
   ```

### 온보딩 키워드 설정

`PUT /api/v1/users/keywords`

**Request**

```json
{
  "headers": {
    "Content-Type": "application/json",
    "Authorization": "Bearer eyJhbGciOiJIUzI1..."
  },
  "body": {
    "keywords": [
      { "keyword": "장학금", "keywordType": "RECOMMEND" },
      { "keyword": "개발", "keywordType": "CUSTOM" }
    ]
  }
}
```

**Response**

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "isSuccess": true,
    "code": "COMMON200",
    "message": "성공적으로 요청을 처리했습니다.",
    "result": {
      "content": [
        { "keyword": "장학금", "keywordType": "RECOMMEND" },
        { "keyword": "개발", "keywordType": "CUSTOM" }
      ],
      "totalCount": 2
    }
  }
}
```

**예외 상황**

1. 등록 가능한 최대 키워드 개수를 초과한 경우 (Bad Request)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "KEYWORD4001",
       "message": "관심 키워드는 최대 10개까지만 등록할 수 있습니다.",
       "result": null
     }
   }
   ```
2. 직접 입력(CUSTOM) 키워드의 글자 수 제한을 초과한 경우 (Bad Request)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "KEYWORD4002",
       "message": "직접 입력 키워드는 공백 포함 최대 10자 이하여야 합니다.",
       "result": null
     }
   }
   ```
3. 요청 Body에 키워드 목록 데이터가 비어있는 경우 (Bad Request)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON400",
       "message": "잘못된 요청입니다.",
       "result": null
     }
   }
   ```
4. 필수 인증 토큰이 누락되었거나 유효하지 않은 경우 (Unauthorized)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON401",
       "message": "인증되지 않았습니다.",
       "result": null
     }
   }
   ```
5. 서버 내부 오류
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON500",
       "message": "서버 내부 오류가 발생했습니다.",
       "result": null
     }
   }
   ```

### 로그아웃

`POST /api/v1/auth/logout`

**Request**

```json
{
  "headers": {
    "Content-Type": "application/json",
    "Authorization": "Bearer eyJhbGciOiJIUzI1..."
  },
  "body": null
}
```

**Response**

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "isSuccess": true,
    "code": "COMMON200",
    "message": "성공적으로 요청을 처리했습니다.",
    "result": null
  }
}
```

**예외 상황**

1. 인증 헤더가 없거나, 토큰이 만료·위조·블랙리스트 등록된 경우 (Unauthorized) — Spring Security 인증 진입점에서 컨트롤러 도달 전에 처리됩니다.
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON401",
       "message": "인증되지 않았습니다.",
       "result": null
     }
   }
   ```
2. 서버 내부 오류
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON500",
       "message": "서버 내부 오류가 발생했습니다.",
       "result": null
     }
   }
   ```

### 토큰 재발급

`POST /api/v1/auth/reissue`

> Access Token 만료 시, 만료된 Access Token 없이 Refresh Token만으로 호출합니다. 재발급 시마다 Refresh Token도 함께 새 값으로 교체(rotate)됩니다.

**Request**

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

**Response**

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "isSuccess": true,
    "code": "COMMON200",
    "message": "성공적으로 요청을 처리했습니다.",
    "result": {
      "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    }
  }
}
```

**예외 상황**

1. Refresh Token이 유효하지 않거나(서명 불일치), 만료되었거나, 이미 로그아웃/재발급되어 폐기된 경우 (Unauthorized)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "AUTH401",
       "message": "유효하지 않거나 만료된 리프레시 토큰입니다. 다시 로그인해 주세요.",
       "result": null
     }
   }
   ```
2. 서버 내부 오류
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON500",
       "message": "서버 내부 오류가 발생했습니다.",
       "result": null
     }
   }
   ```

---

## 홈 공지 피드 (담당: 레몬)

### 통합 공지 피드 / 카테고리 필터 칩

`GET /api/v1/notices?page='페이지 수'&size=20&keyword='카테고리명'`

> `page=0`이면 최신 공지로 동기화(새로 고침)
> `keyword`는 자유 텍스트 검색어가 아니라 공지 카테고리명(예: `학사`, `장학`)과 정확히 일치시키는 필터입니다. 일치하는 카테고리가 없으면 에러 없이 빈 목록이 반환됩니다.

**Request**

```json
{
  "headers": {
    "Content-Type": "application/json",
    "Authorization": "Bearer eyJhbGciOiJIUzI1..."
  },
  "body": null
}
```

**Response**

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "isSuccess": true,
    "code": "COMMON200",
    "message": "성공적으로 요청을 처리했습니다.",
    "result": {
      "content": [
        {
          "noticeId": 1,
          "categoryTag": "학사",
          "title": "2026학년도 2학기 수강신청 기간 안내",
          "source": "컴퓨터공학과 사무실",
          "createdAt": "2026-07-01T09:00:00",
          "deadlineAt": "2026-07-09T23:59:59"
        }
      ],
      "page": 0,
      "size": 20,
      "hasNext": true
    }
  }
}
```

**예외 상황**

1. 등록된 공지사항이 하나도 없는 경우 (데이터 부재)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": true,
       "code": "COMMON200",
       "message": "성공적으로 요청을 처리했습니다.",
       "result": { "content": [], "page": 0, "size": 20, "hasNext": false }
     }
   }
   ```
2. 일치하는 카테고리가 없는 `keyword`로 필터링한 경우 (`200 OK` 유지, 빈 목록 반환)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": true,
       "code": "COMMON200",
       "message": "성공적으로 요청을 처리했습니다.",
       "result": { "content": [], "page": 0, "size": 20, "hasNext": false }
     }
   }
   ```
3. 필수 인증 토큰이 누락되었거나 유효하지 않은 경우 (Unauthorized)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON401",
       "message": "인증되지 않았습니다.",
       "result": null
     }
   }
   ```
4. 서버 내부 오류
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON500",
       "message": "서버 내부 오류가 발생했습니다.",
       "result": null
     }
   }
   ```

---

## 공지 상세 (담당: 베리)

### 공지 상세 보기

`GET /api/v1/notices/{noticeId}`

**Request**

```json
{
  "headers": {
    "Content-Type": "application/json",
    "Authorization": "Bearer eyJhbGciOiJIUzI1..."
  },
  "body": null
}
```

**Response**

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "isSuccess": true,
    "code": "COMMON200",
    "message": "성공적으로 요청을 처리했습니다.",
    "result": {
      "noticeId": 42,
      "categoryTag": "학사",
      "title": "2026학년도 2학기 수강신청 기간 안내",
      "source": "컴퓨터공학과 사무실",
      "createdAt": "2026-07-01T09:00:00",
      "deadlineAt": "2026-07-09T23:59:59",
      "content": "안녕하세요. 컴퓨터공학과 사무실입니다. 수강신청 일정을 다음과 같이 안내하오니...",
      "hasFiles": true,
      "originalUrl": "https://www.university.ac.kr/notice/12345",
      "aiSummary": {
        "eligibility": "직전 학기 12학점 이상 이수 및 이수 평점 3.5 이상인 자",
        "benefit": "등록금 전액 면제 및 도서비 분기별 20만 원 지급",
        "deadline": "2026년 7월 9일 18:00까지 (오프라인 제출 불가)"
      }
    }
  }
}
```

**예외 상황**

1. 존재하지 않거나 삭제된 공지사항을 조회한 경우 (Not Found)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "NOTICE4041",
       "message": "해당 공지사항이 존재하지 않거나 삭제되었습니다.",
       "result": null
     }
   }
   ```
2. 아직 AI 요약이 생성되지 않은 경우 (Partial Success / NULL) — `aiSummary`는 `null`만 반환되며, 별도의 진행 상태(`status`) 객체 형태는 존재하지 않습니다.
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": true,
       "code": "COMMON200",
       "message": "성공적으로 요청을 처리했습니다.",
       "result": {
         "noticeId": 42,
         "categoryTag": "학사",
         "title": "2026학년도 2학기 수강신청 기간 안내",
         "content": "안녕하세요. 컴퓨터공학과 사무실입니다...",
         "aiSummary": null
       }
     }
   }
   ```
3. 필수 인증 토큰이 누락되었거나 유효하지 않은 경우 (Unauthorized)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON401",
       "message": "인증되지 않았습니다.",
       "result": null
     }
   }
   ```
4. 서버 내부 오류
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON500",
       "message": "서버 내부 오류가 발생했습니다.",
       "result": null
     }
   }
   ```

---

## 검색 (담당: 베리)

### 키워드 검색

`GET /api/v1/notices/search?searchWord='검색 키워드'&sort='정렬 기준'&page='페이지 수'&size=20`

**Request**

```json
{
  "headers": {
    "Content-Type": "application/json",
    "Authorization": "Bearer eyJhbGciOiJIUzI1..."
  },
  "body": null
}
```

**Response**

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "isSuccess": true,
    "code": "COMMON200",
    "message": "성공적으로 요청을 처리했습니다.",
    "result": {
      "content": [
        {
          "noticeId": 612,
          "categoryTag": "장학",
          "title": "이공계 국가우수장학금 추가 선발 안내",
          "source": "학생복지처",
          "createdAt": "2026-06-25T10:00:00",
          "deadlineAt": "2026-07-09T23:59:59"
        }
      ],
      "page": 0,
      "size": 20,
      "hasNext": false
    }
  }
}
```

**예외 상황**

1. 검색 결과가 없는 경우 (`200 OK` 유지)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": true,
       "code": "COMMON200",
       "message": "성공적으로 요청을 처리했습니다.",
       "result": { "content": [], "page": 0, "size": 20, "hasNext": false }
     }
   }
   ```
2. 검색어가 비어있는 경우 (Bad Request)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "SEARCH4001",
       "message": "검색 키워드는 공백을 제외한 최소 2글자 이상 입력해야 합니다.",
       "result": null
     }
   }
   ```
3. 필수 인증 토큰이 누락되었거나 유효하지 않은 경우 (Unauthorized)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON401",
       "message": "인증되지 않았습니다.",
       "result": null
     }
   }
   ```
4. 서버 내부 오류
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON500",
       "message": "서버 내부 오류가 발생했습니다.",
       "result": null
     }
   }
   ```

---

## 스크랩 캘린더 (담당: 베리)

### 공지 스크랩

`POST /api/v1/notices/{noticeId}/scrap`

**Request**

```json
{
  "headers": {
    "Content-Type": "application/json",
    "Authorization": "Bearer eyJhbGciOiJIUzI1..."
  },
  "body": null
}
```

**Response**

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "isSuccess": true,
    "code": "COMMON200",
    "message": "성공적으로 요청을 처리했습니다.",
    "result": { "noticeId": 42, "isScraped": true }
  }
}
```

**예외 상황**

1. 존재하지 않거나 삭제된 공지사항을 스크랩하려는 경우 (Not Found)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "NOTICE4041",
       "message": "해당 공지사항이 존재하지 않거나 삭제되었습니다.",
       "result": null
     }
   }
   ```
2. 필수 인증 토큰이 누락되었거나 유효하지 않은 경우 (Unauthorized)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON401",
       "message": "인증되지 않았습니다.",
       "result": null
     }
   }
   ```
3. 서버 내부 오류
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON500",
       "message": "서버 내부 오류가 발생했습니다.",
       "result": null
     }
   }
   ```

### 스크랩 목록 보기

`GET /api/v1/notices/scraps?categoryTag='카테고리명'&sort='정렬 기준'&page='페이지 수'&size=20`

**Request**

```json
{
  "headers": {
    "Content-Type": "application/json",
    "Authorization": "Bearer eyJhbGciOiJIUzI1..."
  },
  "body": null
}
```

**Response**

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "isSuccess": true,
    "code": "COMMON200",
    "message": "성공적으로 요청을 처리했습니다.",
    "result": {
      "categoryCounts": {
        "ALL": 12,
        "SCHOLARSHIP": 5,
        "ACADEMIC": 4,
        "EMPLOYMENT": 3
      },
      "content": [
        {
          "noticeId": 42,
          "categoryTag": "장학",
          "title": "[당일마감🔥] 2026학년도 2학기 교내 성적우수 장학금 신청",
          "source": "학생복지처",
          "createdAt": "2026-07-01T09:00:00",
          "deadlineAt": "2026-07-04T18:00:00"
        },
        {
          "noticeId": 105,
          "categoryTag": "학사",
          "title": "2026학년도 2학기 전과 신청 및 선발 일정 안내",
          "source": "교무처",
          "createdAt": "2026-07-02T14:00:00",
          "deadlineAt": "2026-07-10T23:59:59"
        }
      ],
      "page": 0,
      "size": 20,
      "hasNext": false
    }
  }
}
```

**예외 상황**

1. 스크랩한 공지가 하나도 없는 경우 (데이터 부재 - `200 OK` 유지)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": true,
       "code": "COMMON200",
       "message": "성공적으로 요청을 처리했습니다.",
       "result": {
         "categoryCounts": { "ALL": 0, "SCHOLARSHIP": 0, "ACADEMIC": 0, "EMPLOYMENT": 0 },
         "content": [],
         "page": 0,
         "size": 20,
         "hasNext": false
       }
     }
   }
   ```
2. 필수 인증 토큰이 누락되었거나 유효하지 않은 경우 (Unauthorized)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON401",
       "message": "인증되지 않았습니다.",
       "result": null
     }
   }
   ```
3. 서버 내부 오류
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON500",
       "message": "서버 내부 오류가 발생했습니다.",
       "result": null
     }
   }
   ```

### 캘린더 스크랩 마감 공지 날짜 조회

`GET /api/v1/notices/calendar/dates?year='년도'&month='월'`

**Request**

```json
{
  "headers": {
    "Content-Type": "application/json",
    "Authorization": "Bearer eyJhbGciOiJIUzI1..."
  },
  "body": null
}
```

**Response**

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "isSuccess": true,
    "code": "COMMON200",
    "message": "성공적으로 요청을 처리했습니다.",
    "result": {
      "content": ["2026-07-04", "2026-07-09", "2026-07-15"],
      "totalCount": 3
    }
  }
}
```

**예외 상황**

1. 해당 월(Month)에 마감되는 스크랩 공지가 하나도 없는 경우 (`200 OK` 유지)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": true,
       "code": "COMMON200",
       "message": "성공적으로 요청을 처리했습니다.",
       "result": { "content": [], "totalCount": 0 }
     }
   }
   ```
2. 필수 인증 토큰이 누락되었거나 유효하지 않은 경우 (Unauthorized)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON401",
       "message": "인증되지 않았습니다.",
       "result": null
     }
   }
   ```
3. 서버 내부 오류
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON500",
       "message": "서버 내부 오류가 발생했습니다.",
       "result": null
     }
   }
   ```

### 선택한 날짜의 마감 공지 목록 조회

`GET /api/v1/notices/calendar?date='날짜'&page='페이지 수'&size=20`

**Request**

```json
{
  "headers": {
    "Content-Type": "application/json",
    "Authorization": "Bearer eyJhbGciOiJIUzI1..."
  },
  "body": null
}
```

**Response**

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "isSuccess": true,
    "code": "COMMON200",
    "message": "성공적으로 요청을 처리했습니다.",
    "result": {
      "content": [
        {
          "noticeId": 42,
          "categoryTag": "장학",
          "title": "[당일마감🔥] 2026학년도 2학기 교내 성적우수 장학금 신청",
          "source": "학생복지처",
          "createdAt": "2026-07-01T09:00:00",
          "deadlineAt": "2026-07-04T18:00:00"
        }
      ],
      "page": 0,
      "size": 20,
      "hasNext": false
    }
  }
}
```

**예외 상황**

1. 선택한 날짜에 마감되는 공지가 없는 경우 (`200 OK` 유지)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": true,
       "code": "COMMON200",
       "message": "성공적으로 요청을 처리했습니다.",
       "result": { "content": [], "page": 0, "size": 20, "hasNext": false }
     }
   }
   ```
2. 필수 인증 토큰이 누락되었거나 유효하지 않은 경우 (Unauthorized)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON401",
       "message": "인증되지 않았습니다.",
       "result": null
     }
   }
   ```
3. 서버 내부 오류
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON500",
       "message": "서버 내부 오류가 발생했습니다.",
       "result": null
     }
   }
   ```

### 마감일이 없는 마감 공지 목록 조회

`GET /api/v1/notices/calendar/no-deadline?page=0&size=20`

**Request**

```json
{
  "headers": {
    "Content-Type": "application/json",
    "Authorization": "Bearer eyJhbGciOiJIUzI1..."
  },
  "body": null
}
```

**Response**

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "isSuccess": true,
    "code": "COMMON200",
    "message": "성공적으로 요청을 처리했습니다.",
    "result": {
      "content": [
        {
          "noticeId": 99,
          "categoryTag": "일반",
          "title": "교내 흡연구역 부스 리모델링 공사 안내 (기한없음)",
          "source": "시설관리팀",
          "createdAt": "2026-07-02T11:00:00",
          "deadlineAt": null
        }
      ],
      "page": 0,
      "size": 20,
      "hasNext": false
    }
  }
}
```

**예외 상황**

1. 마감 기한이 없는 스크랩 공지가 하나도 없는 경우 (`200 OK` 유지)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": true,
       "code": "COMMON200",
       "message": "성공적으로 요청을 처리했습니다.",
       "result": { "content": [], "page": 0, "size": 20, "hasNext": false }
     }
   }
   ```
2. 필수 인증 토큰이 누락되었거나 유효하지 않은 경우 (Unauthorized)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON401",
       "message": "인증되지 않았습니다.",
       "result": null
     }
   }
   ```
3. 서버 내부 오류
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON500",
       "message": "서버 내부 오류가 발생했습니다.",
       "result": null
     }
   }
   ```

---

## 알람 푸시 (담당: 베리)

### 유저 기기 토큰 저장

`POST /api/v1/users/device`

**Request**

```json
{
  "headers": {
    "Content-Type": "application/json",
    "Authorization": "Bearer eyJhbGciOiJIUzI1..."
  },
  "body": { "pushToken": "fcm_token_string_asdf1234..." }
}
```

**Response**

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "isSuccess": true,
    "code": "COMMON200",
    "message": "성공적으로 요청을 처리했습니다.",
    "result": null
  }
}
```

**예외 상황**

1. 전송된 디바이스 토큰 값이 비어있거나 형식이 잘못된 경우 (Bad Request)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "PUSH4001",
       "message": "유효하지 않은 디바이스 토큰 형식입니다.",
       "result": null
     }
   }
   ```
2. 필수 인증 토큰이 누락되었거나 유효하지 않은 경우 (Unauthorized)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON401",
       "message": "인증되지 않았습니다.",
       "result": null
     }
   }
   ```
3. 서버 내부 오류
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON500",
       "message": "서버 내부 오류가 발생했습니다.",
       "result": null
     }
   }
   ```

### 알림함 목록 조회

`GET /api/v1/notifications?page='페이지 수'&size=20`

**Request**

```json
{
  "headers": {
    "Content-Type": "application/json",
    "Authorization": "Bearer eyJhbGciOiJIUzI1..."
  },
  "body": null
}
```

**Response**

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "isSuccess": true,
    "code": "COMMON200",
    "message": "성공적으로 요청을 처리했습니다.",
    "result": {
      "content": [
        {
          "notificationId": 1204,
          "noticeId": 542,
          "notificationType": "CLOSING",
          "title": "📌 곧 마감되는 공지가 있어요",
          "message": "[2026학년도 2학기 수강신청 기간 안내] D-3 · 2026-07-04 까지",
          "isRead": false,
          "createdAt": "2026-07-02T09:00:00"
        },
        {
          "notificationId": 198,
          "noticeId": 11,
          "notificationType": "KEYWORD",
          "title": "🏷️ 내 키워드와 관련된 공지가 등록되었어요",
          "message": "[산학협력단] 2026 대학생 IT 창업 아이디어 공모전 개최",
          "isRead": true,
          "createdAt": "2026-07-01T15:30:00"
        },
        {
          "notificationId": 97,
          "noticeId": 8,
          "notificationType": "CATEGORY",
          "title": "📢 관심 카테고리에 새 공지가 등록되었어요",
          "message": "2026학년도 2학기 수강신청 일정 안내",
          "isRead": true,
          "createdAt": "2026-06-30T09:00:00"
        }
      ],
      "page": 0,
      "size": 20,
      "hasNext": true
    }
  }
}
```

**예외 상황**

1. 수신된 알림이 하나도 없는 경우 (데이터 부재 - `200 OK` 유지)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": true,
       "code": "COMMON200",
       "message": "성공적으로 요청을 처리했습니다.",
       "result": { "content": [], "page": 0, "size": 20, "hasNext": false }
     }
   }
   ```
2. 필수 인증 토큰이 누락되었거나 유효하지 않은 경우 (Unauthorized)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON401",
       "message": "인증되지 않았습니다.",
       "result": null
     }
   }
   ```
3. 서버 내부 오류
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON500",
       "message": "서버 내부 오류가 발생했습니다.",
       "result": null
     }
   }
   ```

### 알림함 전체 읽기

`PATCH /api/v1/notifications/read`

**Request**

```json
{
  "headers": {
    "Content-Type": "application/json",
    "Authorization": "Bearer eyJhbGciOiJIUzI1..."
  },
  "body": null
}
```

**Response**

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "isSuccess": true,
    "code": "COMMON200",
    "message": "성공적으로 요청을 처리했습니다.",
    "result": null
  }
}
```

**예외 상황**

1. 필수 인증 토큰이 누락되었거나 유효하지 않은 경우 (Unauthorized)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON401",
       "message": "인증되지 않았습니다.",
       "result": null
     }
   }
   ```
2. 서버 내부 오류
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON500",
       "message": "서버 내부 오류가 발생했습니다.",
       "result": null
     }
   }
   ```

---

## 마이페이지 설정 (담당: 레스)

### 마이페이지 및 프로필 조회

`GET /api/v1/users/profile`

**Request**

```json
{
  "headers": {
    "Content-Type": "application/json",
    "Authorization": "Bearer eyJhbGciOiJIUzI1..."
  },
  "body": null
}
```

**Response**

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "isSuccess": true,
    "code": "COMMON200",
    "message": "성공적으로 요청을 처리했습니다.",
    "result": {
      "nickname": "소셜유저_a1b2c",
      "universityName": "인제대학교",
      "departmentName": "컴퓨터공학과",
      "grade": 3,
      "scrapCount": 12,
      "keywordCount": 5,
      "readCount": 47
    }
  }
}
```

**예외 상황**

1. 탈퇴했거나 시스템에서 찾을 수 없는 유저인 경우 (Not Found)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "USER4041",
       "message": "존재하지 않거나 이미 탈퇴한 회원의 프로필입니다.",
       "result": null
     }
   }
   ```
2. 필수 인증 토큰이 누락되었거나 유효하지 않은 경우 (Unauthorized)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON401",
       "message": "인증되지 않았습니다.",
       "result": null
     }
   }
   ```
3. 서버 내부 오류
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON500",
       "message": "서버 내부 오류가 발생했습니다.",
       "result": null
     }
   }
   ```

### 프로필 수정

`PATCH /api/v1/users/profile/universities/{universityId}/departments/{departmentId}`

**Request**

```json
{
  "headers": {
    "Content-Type": "application/json",
    "Authorization": "Bearer eyJhbGciOiJIUzI1..."
  },
  "body": { "grade": 2 }
}
```

**Response**

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "isSuccess": true,
    "code": "COMMON200",
    "message": "성공적으로 요청을 처리했습니다.",
    "result": {
      "nickname": "소셜유저_a1b2c",
      "universityName": "인제대학교",
      "departmentName": "컴퓨터공학과",
      "grade": 2
    }
  }
}
```

**예외 상황**

1. 존재하지 않는 학교 또는 학과 코드를 보낸 경우 (Bad Request / Not Found)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "USER4002",
       "message": "선택하신 학교 또는 학과 정보가 올바르지 않거나 존재하지 않습니다.",
       "result": null
     }
   }
   ```
2. 필수 인증 토큰이 누락되었거나 유효하지 않은 경우 (Unauthorized)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON401",
       "message": "인증되지 않았습니다.",
       "result": null
     }
   }
   ```
3. 서버 내부 오류
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON500",
       "message": "서버 내부 오류가 발생했습니다.",
       "result": null
     }
   }
   ```

### 알림 설정 조회

`GET /api/v1/users/alarm`

**Request**

```json
{
  "headers": {
    "Content-Type": "application/json",
    "Authorization": "Bearer eyJhbGciOiJIUzI1..."
  },
  "body": {}
}
```

**Response**

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "isSuccess": true,
    "code": "COMMON200",
    "message": "성공적으로 요청을 처리했습니다.",
    "result": {
      "isAll": true,
      "isClosing": true,
      "isKeyword": false,
      "scholarship": true,
      "extracurricular": true,
      "academic": false,
      "employment": false
    }
  }
}
```

**예외 상황**

1. 필수 인증 토큰이 누락되었거나 유효하지 않은 경우 (Unauthorized)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON401",
       "message": "인증되지 않았습니다.",
       "result": null
     }
   }
   ```
2. 서버 내부 오류
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON500",
       "message": "서버 내부 오류가 발생했습니다.",
       "result": null
     }
   }
   ```

### 알림 설정 수정

`PATCH /api/v1/users/alarm`

**Request**

```json
{
  "headers": {
    "Content-Type": "application/json",
    "Authorization": "Bearer eyJhbGciOiJIUzI1..."
  },
  "body": { "isKeyword": true, "academic": true }
}
```

**Response**

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "isSuccess": true,
    "code": "COMMON200",
    "message": "성공적으로 요청을 처리했습니다.",
    "result": {
      "isAll": true,
      "isClosing": true,
      "isKeyword": true,
      "scholarship": true,
      "extracurricular": true,
      "academic": true,
      "employment": false
    }
  }
}
```

**예외 상황**

1. 요청 본문(Body)에 필수 설정 필드가 누락된 경우 (Bad Request)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "ALARM4001",
       "message": "알림 설정 값은 필수입니다.",
       "result": null
     }
   }
   ```
2. 전체 알림(`isAll`) 비활성화 시 하위 설정 모순 오류 (Bad Request)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "ALARM4002",
       "message": "전체 알림이 꺼진 상태(isAll=false)에서는 하위 알림 설정을 활성화할 수 없습니다.",
       "result": null
     }
   }
   ```
3. 필수 인증 토큰이 누락되었거나 유효하지 않은 경우 (Unauthorized)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON401",
       "message": "인증되지 않았습니다.",
       "result": null
     }
   }
   ```
4. 서버 내부 오류
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON500",
       "message": "서버 내부 오류가 발생했습니다.",
       "result": null
     }
   }
   ```

### 관심 키워드 관리 (조회)

`GET /api/v1/users/keywords`

**Request**

```json
{
  "headers": {
    "Content-Type": "application/json",
    "Authorization": "Bearer eyJhbGciOiJIUzI1..."
  },
  "body": null
}
```

**Response**

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "isSuccess": true,
    "code": "COMMON200",
    "message": "성공적으로 요청을 처리했습니다.",
    "result": {
      "content": [
        { "keyword": "비교과", "keywordType": "RECOMMEND" },
        { "keyword": "취업", "keywordType": "RECOMMEND" },
        { "keyword": "개발", "keywordType": "CUSTOM" },
        { "keyword": "어학", "keywordType": "CUSTOM" }
      ],
      "totalCount": 4
    }
  }
}
```

**예외 상황**

1. 등록된 관심 키워드가 하나도 없는 경우 (`200 OK` 유지)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": true,
       "code": "COMMON200",
       "message": "성공적으로 요청을 처리했습니다.",
       "result": { "content": [], "totalCount": 0 }
     }
   }
   ```
2. 필수 인증 토큰이 누락되었거나 유효하지 않은 경우 (Unauthorized)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON401",
       "message": "인증되지 않았습니다.",
       "result": null
     }
   }
   ```
3. 서버 내부 오류
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON500",
       "message": "서버 내부 오류가 발생했습니다.",
       "result": null
     }
   }
   ```

### 관심 키워드 관리 (수정, 삭제, 추가)

`PUT /api/v1/users/keywords`

**Request**

```json
{
  "headers": {
    "Content-Type": "application/json",
    "Authorization": "Bearer eyJhbGciOiJIUzI1..."
  },
  "body": {
    "keywords": [
      { "keyword": "취업", "keywordType": "RECOMMEND" },
      { "keyword": "대외할동", "keywordType": "RECOMMEND" },
      { "keyword": "상담", "keywordType": "CUSTOM" },
      { "keyword": "공모전", "keywordType": "CUSTOM" }
    ]
  }
}
```

**Response**

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "isSuccess": true,
    "code": "COMMON200",
    "message": "성공적으로 요청을 처리했습니다.",
    "result": {
      "content": [
        { "keyword": "취업", "keywordType": "RECOMMEND" },
        { "keyword": "대외할동", "keywordType": "RECOMMEND" },
        { "keyword": "상담", "keywordType": "CUSTOM" },
        { "keyword": "공모전", "keywordType": "CUSTOM" }
      ],
      "totalCount": 4
    }
  }
}
```

**예외 상황**

1. 키워드 등록 개수 제한을 초과한 경우 (Bad Request)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "KEYWORD4001",
       "message": "관심 키워드는 최대 10개까지만 등록할 수 있습니다.",
       "result": null
     }
   }
   ```
2. 직접 입력(CUSTOM) 키워드의 글자 수 제한을 초과한 경우 (Bad Request)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "KEYWORD4002",
       "message": "직접 입력 키워드는 공백 포함 최대 10자 이하여야 합니다.",
       "result": null
     }
   }
   ```
3. 요청 Body에 키워드 목록 데이터가 비어있는 경우 (Bad Request)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON400",
       "message": "잘못된 요청입니다.",
       "result": null
     }
   }
   ```
4. 필수 인증 토큰이 누락되었거나 유효하지 않은 경우 (Unauthorized)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON401",
       "message": "인증되지 않았습니다.",
       "result": null
     }
   }
   ```
5. 서버 내부 오류
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON500",
       "message": "서버 내부 오류가 발생했습니다.",
       "result": null
     }
   }
   ```

### 운영팀 공지사항 목록 조회

`GET /api/v1/support/notices?page='페이지 수'&size=20`

**Request**

```json
{
  "headers": {
    "Content-Type": "application/json",
    "Authorization": "Bearer eyJhbGciOiJIUzI1..."
  },
  "body": null
}
```

**Response**

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "isSuccess": true,
    "code": "COMMON200",
    "message": "성공적으로 요청을 처리했습니다.",
    "result": {
      "content": [
        {
          "supportNoticeId": 5,
          "title": "[공지] 시스템 정기 점검 안내 (7/15)",
          "content": "안녕하세요. 서비스 안정화를 위한 시스템 점검이 진행될 예정입니다...",
          "createdAt": "2026-07-04T12:00:00"
        },
        {
          "supportNoticeId": 4,
          "title": "[업데이트] 버전 1.2.0 출시 안내 (캘린더 기능 추가)",
          "content": "드디어 많은 분들이 기다리시던 스크랩 캘린더 기능이 업데이트 되었습니다!",
          "createdAt": "2026-07-01T09:00:00"
        }
      ],
      "page": 0,
      "size": 20,
      "hasNext": false
    }
  }
}
```

**예외 상황**

1. 등록된 서비스 공지사항이 하나도 없는 경우 (`200 OK` 유지)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": true,
       "code": "COMMON200",
       "message": "성공적으로 요청을 처리했습니다.",
       "result": { "content": [], "page": 0, "size": 20, "hasNext": false }
     }
   }
   ```
2. 필수 인증 토큰이 누락되었거나 유효하지 않은 경우 (Unauthorized)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON401",
       "message": "인증되지 않았습니다.",
       "result": null
     }
   }
   ```
3. 서버 내부 오류
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON500",
       "message": "서버 내부 오류가 발생했습니다.",
       "result": null
     }
   }
   ```

### FAQ 목록 조회

`GET /api/v1/support/faqs?category='태그명'`

**Request**

```json
{
  "headers": {
    "Content-Type": "application/json",
    "Authorization": "Bearer eyJhbGciOiJIUzI1..."
  },
  "body": null
}
```

**Response**

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "isSuccess": true,
    "code": "COMMON200",
    "message": "성공적으로 요청을 처리했습니다.",
    "result": {
      "content": [
        {
          "faqId": 12,
          "category": "ACCOUNT",
          "question": "비밀번호를 분별하거나 변경하고 싶어요.",
          "answer": "로그인 화면 하단의 '비밀번호 찾기'를 이용하시거나, 마이페이지 > 프로필 수정에서 변경 가능합니다."
        },
        {
          "faqId": 15,
          "category": "ACCOUNT",
          "question": "회원 탈퇴는 어떻게 하나요?",
          "answer": "마이페이지 > 설정 > 회원 탈퇴 메뉴를 통해 탈퇴를 진행하실 수 있으며, 탈퇴 즉시 스크랩 데이터는 삭제됩니다."
        }
      ],
      "totalCount": 2
    }
  }
}
```

**예외 상황**

1. 해당 카테고리에 등록된 FAQ가 하나도 없는 경우 (`200 OK` 유지)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": true,
       "code": "COMMON200",
       "message": "성공적으로 요청을 처리했습니다.",
       "result": { "content": [], "totalCount": 0 }
     }
   }
   ```
2. 필수 인증 토큰이 누락되었거나 유효하지 않은 경우 (Unauthorized)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON401",
       "message": "인증되지 않았습니다.",
       "result": null
     }
   }
   ```
3. 서버 내부 오류
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON500",
       "message": "서버 내부 오류가 발생했습니다.",
       "result": null
     }
   }
   ```

### 스펙 로그 목록 조회

`GET /api/v1/specs?category='카테고리명'&sort='정렬 기준'&page='페이지 수'&size=20`

**Request**

```json
{
  "headers": {
    "Content-Type": "application/json",
    "Authorization": "Bearer eyJhbGciOiJIUzI1..."
  },
  "body": null
}
```

**Response**

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "isSuccess": true,
    "code": "COMMON200",
    "message": "성공적으로 요청을 처리했습니다.",
    "result": {
      "categoryCounts": {
        "allCount": 15,
        "licenseCount": 3,
        "awardCount": 1,
        "activityCount": 6,
        "languageCount": 2,
        "internCount": 1,
        "etcCount": 2
      },
      "content": [
        {
          "specId": 104,
          "category": "LICENSE",
          "categoryTag": "자격증",
          "title": "정보처리기사",
          "organization": "한국산업인력공단",
          "specDate": "2026-05-20"
        },
        {
          "specId": 89,
          "category": "AWARD",
          "categoryTag": "수상",
          "title": "2026 대학생 공공데이터 활용 창업경진대회 최우수상",
          "organization": "행정안전부",
          "specDate": "2026-04-15"
        }
      ],
      "page": 0,
      "size": 20,
      "hasNext": false
    }
  }
}
```

**예외 상황**

1. 기록된 스펙 로그가 하나도 없는 경우 (`200 OK` 유지)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": true,
       "code": "COMMON200",
       "message": "성공적으로 요청을 처리했습니다.",
       "result": {
         "categoryCounts": {
           "allCount": 0,
           "licenseCount": 0,
           "awardCount": 0,
           "activityCount": 0,
           "languageCount": 0,
           "internCount": 0,
           "etcCount": 0
         },
         "content": [],
         "page": 0,
         "size": 20,
         "hasNext": false
       }
     }
   }
   ```
2. `category` 필터 값이 `ALL` 또는 유효한 카테고리명(`LICENSE`/`AWARD`/`ACTIVITY`/`LANGUAGE`/`INTERN`/`ETC`)이 아닌 경우 (Bad Request)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "SPEC4002",
       "message": "스펙 제목, 카테고리, 취득일(활동일)은 필수 입력 항목입니다.",
       "result": null
     }
   }
   ```
3. 필수 인증 토큰이 누락되었거나 유효하지 않은 경우 (Unauthorized)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON401",
       "message": "인증되지 않았습니다.",
       "result": null
     }
   }
   ```
4. 서버 내부 오류
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON500",
       "message": "서버 내부 오류가 발생했습니다.",
       "result": null
     }
   }
   ```

### 스펙 로그 추가

`POST /api/v1/specs?page='페이지 수'&size=20`

**Request**

```json
{
  "headers": {
    "Content-Type": "application/json",
    "Authorization": "Bearer eyJhbGciOiJIUzI1..."
  },
  "body": {
    "category": "LANGUAGE",
    "title": "토익 (TOEIC)",
    "organization": "YBM 한국TOEIC위원회",
    "specDate": "2026-06-10",
    "scoreOrGrade": "920점",
    "memo": "유효기간 2년 확인 필요. 성적표 링크: https://..."
  }
}
```

> `scoreOrGrade`, `memo`는 선택 입력 항목입니다.

**Response**

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "isSuccess": true,
    "code": "COMMON200",
    "message": "성공적으로 요청을 처리했습니다.",
    "result": {
      "categoryCounts": {
        "allCount": 15,
        "licenseCount": 3,
        "awardCount": 1,
        "activityCount": 6,
        "languageCount": 2,
        "internCount": 1,
        "etcCount": 2
      },
      "content": [
        {
          "specId": 105,
          "category": "LANGUAGE",
          "categoryTag": "어학",
          "title": "토익 (TOEIC)",
          "organization": "YBM 한국TOEIC위원회",
          "specDate": "2026-06-10"
        },
        {
          "specId": 104,
          "category": "LICENSE",
          "categoryTag": "자격증",
          "title": "정보처리기사",
          "organization": "한국산업인력공단",
          "specDate": "2026-05-25"
        }
      ],
      "page": 0,
      "size": 20,
      "hasNext": false
    }
  }
}
```

**예외 상황**

1. 필수 입력 값이 누락된 경우 (Bad Request)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "SPEC4002",
       "message": "스펙 제목, 카테고리, 취득일(활동일)은 필수 입력 항목입니다.",
       "result": null
     }
   }
   ```
2. 필수 인증 토큰이 누락되었거나 유효하지 않은 경우 (Unauthorized)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON401",
       "message": "인증되지 않았습니다.",
       "result": null
     }
   }
   ```
3. 서버 내부 오류
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON500",
       "message": "서버 내부 오류가 발생했습니다.",
       "result": null
     }
   }
   ```

### 스펙 로그 수정

`PUT /api/v1/specs/{specId}`

**Request**

```json
{
  "headers": {
    "Content-Type": "application/json",
    "Authorization": "Bearer eyJhbGciOiJIUzI1..."
  },
  "body": {
    "category": "LANGUAGE",
    "title": "토익 (TOEIC) (수정)",
    "organization": "YBM 한국TOEIC위원회",
    "specDate": "2026-06-10",
    "scoreOrGrade": "950점",
    "memo": "유효기간 2년 재확인 완료. 성적표 링크 변경: https://..."
  }
}
```

> `scoreOrGrade`, `memo`는 선택 입력 항목입니다.

**Response**

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "isSuccess": true,
    "code": "COMMON200",
    "message": "성공적으로 요청을 처리했습니다.",
    "result": {
      "categoryCounts": {
        "allCount": 15,
        "licenseCount": 3,
        "awardCount": 1,
        "activityCount": 6,
        "languageCount": 2,
        "internCount": 1,
        "etcCount": 2
      },
      "content": [
        {
          "specId": 104,
          "category": "LICENSE",
          "categoryTag": "자격증",
          "title": "정보처리기사",
          "organization": "한국산업인력공단",
          "specDate": "2026-05-25"
        },
        {
          "specId": 89,
          "category": "AWARD",
          "categoryTag": "수상",
          "title": "2026 대학생 공공데이터 활용 창업경진대회 최우수상",
          "organization": "행정안전부",
          "specDate": "2026-04-15"
        },
        {
          "specId": 107,
          "category": "LANGUAGE",
          "categoryTag": "어학",
          "title": "토익 (TOEIC) (수정)",
          "organization": "YBM 한국TOEIC위원회",
          "specDate": "2026-06-10"
        }
      ],
      "page": 0,
      "size": 20,
      "hasNext": false
    }
  }
}
```

**예외 상황**

1. 수정하려는 스펙 로그 ID가 존재하지 않는 경우 (`404 Not Found`)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "SPEC4041",
       "message": "해당 스펙 로그 식별자(ID)를 찾을 수 없습니다.",
       "result": null
     }
   }
   ```
2. 타인의 스펙 로그를 수정하려고 시도하는 경우 (`403 Forbidden`)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "SPEC4031",
       "message": "해당 스펙 로그에 대한 권한이 없습니다.",
       "result": null
     }
   }
   ```
3. 필수 입력 값을 비워서 보낸 경우 (Bad Request)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "SPEC4002",
       "message": "스펙 제목, 카테고리, 취득일(활동일)은 필수 입력 항목입니다.",
       "result": null
     }
   }
   ```
4. 필수 인증 토큰이 누락되었거나 유효하지 않은 경우 (Unauthorized)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON401",
       "message": "인증되지 않았습니다.",
       "result": null
     }
   }
   ```
5. 서버 내부 오류
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON500",
       "message": "서버 내부 오류가 발생했습니다.",
       "result": null
     }
   }
   ```

### 스펙 로그 삭제

`DELETE /api/v1/specs/{specId}`

**Request**

```json
{
  "headers": {
    "Content-Type": "application/json",
    "Authorization": "Bearer eyJhbGciOiJIUzI1..."
  },
  "body": null
}
```

**Response**

```json
{
  "headers": { "Content-Type": "application/json" },
  "body": {
    "isSuccess": true,
    "code": "COMMON200",
    "message": "성공적으로 요청을 처리했습니다.",
    "result": {
      "categoryCounts": {
        "allCount": 15,
        "licenseCount": 3,
        "awardCount": 1,
        "activityCount": 6,
        "languageCount": 2,
        "internCount": 1,
        "etcCount": 2
      },
      "content": [
        {
          "specId": 104,
          "category": "LICENSE",
          "categoryTag": "자격증",
          "title": "정보처리기사",
          "organization": "한국산업인력공단",
          "specDate": "2026-05-20"
        }
      ],
      "page": 0,
      "size": 20,
      "hasNext": false
    }
  }
}
```

**예외 상황**

1. 삭제하려는 스펙 로그 ID가 존재하지 않는 경우 (`404 Not Found`)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "SPEC4041",
       "message": "해당 스펙 로그 식별자(ID)를 찾을 수 없습니다.",
       "result": null
     }
   }
   ```
2. 타인의 스펙 로그를 삭제하려고 시도하는 경우 (`403 Forbidden`)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "SPEC4031",
       "message": "해당 스펙 로그에 대한 권한이 없습니다.",
       "result": null
     }
   }
   ```
3. 필수 인증 토큰이 누락되었거나 유효하지 않은 경우 (Unauthorized)
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON401",
       "message": "인증되지 않았습니다.",
       "result": null
     }
   }
   ```
4. 서버 내부 오류
   ```json
   {
     "headers": { "Content-Type": "application/json" },
     "body": {
       "isSuccess": false,
       "code": "COMMON500",
       "message": "서버 내부 오류가 발생했습니다.",
       "result": null
     }
   }
   ```
