# 1단계: 런타임 스테이지 (경량화된 Temurin JRE Alpine)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /application

# 타임존 데이터 설치 및 한국 시간(Asia/Seoul) 세팅
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone

# 보안을 위한 non-root 유저 생성
RUN addgroup --system spring && adduser --system spring -G spring
USER spring:spring

# 빌드되어 전송된 app.jar 파일 복사
COPY app.jar app.jar

EXPOSE 8080

# JAVA_OPTS가 있으면 적용하고, 없으면 기본 동적 메모리 옵션을 사용
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -XX:MaxRAMPercentage=75.0 -jar app.jar"]