# ---- Build stage --------------------------------------------------------
FROM node:20-alpine AS builder
WORKDIR /app

# 빌드 인자: dev | prod
ARG APP_STAGE=dev
ENV APP_STAGE=${APP_STAGE}
ENV NODE_ENV=production

# 의존성 설치 (캐시 최대화)
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund

# 소스 복사 & 빌드
COPY . .
RUN npm run build

# ---- Runtime stage ------------------------------------------------------
FROM node:20-alpine
WORKDIR /app

ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1

# 실행에 필요한 산출물만
COPY --from=builder /app/package.json .
COPY --from=builder /app/package-lock.json .
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public

# prod 의존성만 설치
RUN npm ci --omit=dev --no-audit --no-fund

# 런타임 환경변수로 API URL 재주입을 원하면 entrypoint 스크립트 사용 (옵션)
# COPY docker/entrypoint.sh /entrypoint.sh
# RUN chmod +x /entrypoint.sh
# ENTRYPOINT ["/entrypoint.sh"]

EXPOSE 3000
CMD ["npm", "start"]
