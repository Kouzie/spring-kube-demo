# User Service

Spring Boot 기반의 사용자 인증 서비스로, JWT 토큰 기반 인증 및 Vault를 사용한 자동 키 로테이션을 지원합니다.

## 주요 기능

1. **H2 Database**: 인메모리 데이터베이스를 사용한 사용자 관리
2. **JWT 인증**: Access Token 및 Refresh Token 기반 인증
3. **Spring Cloud Vault**: JWT 시크릿 키를 Vault에서 안전하게 관리
4. **자동 키 로테이션**: 30분마다 자동으로 JWT 키 로테이션 (설정 가능)
5. **멀티 키 지원**: 여러 키가 공존하면서 원활한 토큰 검증

## API 엔드포인트

### 인증 API

#### 회원가입
```bash
POST /api/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "password": "password123",
  "email": "test@example.com"
}
```

#### 로그인
```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**응답 예시:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 1800,
  "username": "admin"
}
```

#### Access Token 재발급
```bash
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "your-refresh-token"
}
```

#### 수동 키 로테이션 (관리자용)
```bash
POST /api/auth/rotate-keys
```

#### 헬스 체크
```bash
GET /api/auth/health
```

## 실행 방법

### Docker Compose 사용

```bash
# Vault 및 User Service 실행
cd docker
docker-compose -f docker-compose-vault.yaml up -d

# 로그 확인
docker-compose -f docker-compose-vault.yaml logs -f user-service
```

### 로컬 실행

1. Vault 실행:
```bash
docker run -d --name vault -p 8200:8200 \
  -e VAULT_DEV_ROOT_TOKEN_ID=root \
  vault:1.15
```

2. Gradle 빌드 및 실행:
```bash
cd api/user
../../gradlew bootRun
```

## 환경 변수

| 변수명 | 설명 | 기본값 |
|--------|------|--------|
| `SPRING_CLOUD_VAULT_HOST` | Vault 호스트 | localhost |
| `SPRING_CLOUD_VAULT_PORT` | Vault 포트 | 8200 |
| `SPRING_CLOUD_VAULT_TOKEN` | Vault 토큰 | root |
| `JWT_KEY_ROTATION_INTERVAL` | 키 로테이션 간격 (ms) | 1800000 (30분) |

## 키 로테이션 동작 방식

1. **자동 로테이션**: 30분마다 새로운 JWT 시크릿 키 생성
2. **멀티 키 관리**: 
   - 새 키 생성 시 기존 키는 60분간 유효 (30분 중복 기간)
   - 토큰 검증 시 모든 활성 키로 시도
   - Key ID (kid)를 JWT 헤더에 포함하여 효율적인 검증
3. **자동 정리**: 만료된 키는 자동으로 삭제 (만료 후 1시간 유예)

## 기본 계정

애플리케이션 시작 시 자동으로 생성되는 계정:

- **Admin**: `admin` / `admin123`
- **User**: `user` / `user123`

## 데이터베이스

- H2 인메모리 데이터베이스 사용
- H2 Console: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:userdb`
  - Username: `sa`
  - Password: (empty)

## 보안 고려사항

1. **프로덕션 환경**에서는:
   - Vault dev 모드 대신 프로덕션 모드 사용
   - H2 대신 PostgreSQL 등 영구 데이터베이스 사용
   - HTTPS/TLS 활성화
   - 강력한 Vault 토큰 및 접근 정책 설정

2. **키 로테이션 간격**:
   - 보안 요구사항에 따라 조정 가능
   - 너무 짧으면 성능 영향, 너무 길면 보안 위험

## 아키텍처

```
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│   Client    │─────▶│ User Service│─────▶│   Vault     │
└─────────────┘      └─────────────┘      └─────────────┘
                            │
                            ▼
                     ┌─────────────┐
                     │  H2 Database│
                     └─────────────┘

Scheduler (30분마다)
    ▼
JWT Key Rotation Service
    ▼
Vault Service (키 저장/조회)
    ▼
Vault (키 저장소)
```

## 테스트

```bash
# 빌드
./gradlew :api:user:build

# 테스트
./gradlew :api:user:test
```

