# ---- Build stage --------------------------------------------------------
FROM node:20-alpine AS builder
WORKDIR /app

# pnpm 사용 (corepack 활성화)
RUN corepack enable

# 종속성 메타 먼저 복사 → 캐시 최적화
COPY package.json pnpm-lock.yaml* ./
RUN pnpm fetch

# 실제 소스 복사 후 설치+빌드
COPY . .
RUN pnpm install --offline
RUN pnpm run build

# ---- Runtime (Nginx) ----------------------------------------------------
FROM nginx:1.27-alpine
# Nginx가 3000 포트로 서비스하도록 기본 사이트 설정 교체
RUN sed -i 's/listen       80;/listen       3000;/' /etc/nginx/conf.d/default.conf \
 && sed -i 's#/usr/share/nginx/html;#/usr/share/nginx/html;#' /etc/nginx/conf.d/default.conf

# 정적 파일 복사
COPY --from=builder /app/dist /usr/share/nginx/html
# (CRA이면 /app/build 경로일 수 있음 → 프레임워크에 맞춰 수정)

EXPOSE 3000
CMD ["nginx","-g","daemon off;"]
