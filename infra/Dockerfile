FROM eclipse-temurin:21-jre-alpine
WORKDIR /application

# 타임존 데이터 설치 및 한국 시간(Asia/Seoul) 세팅
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone

# 보안을 위한 non-root 유저 생성
RUN addgroup --system spring && adduser --system spring -G spring
USER spring:spring

COPY app.jar app.jar

EXPOSE 8080

# 기본 JAVA_OPTS 지정 (docker-compose 환경변수가 우선 적용됨)
ENV JAVA_OPTS="-Xms128m -Xmx256m"

# MaxRAMPercentage를 제거하여 JAVA_OPTS가 100% 우선 적용되도록 변경
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]