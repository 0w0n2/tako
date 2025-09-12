# ---- Build stage --------------------------------------------------------
FROM node:20-alpine AS builder
WORKDIR /app
ENV NODE_ENV=production NEXT_TELEMETRY_DISABLED=1
ARG APP_STAGE=dev
ENV APP_STAGE=${APP_STAGE}

RUN npm i -D @tailwindcss/postcss tailwindcss postcss autoprefixer
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY . .
# Next.js 정적 산출물(out/) 생성
RUN npm run build && npm run export

# ---- Runtime stage: Nginx ----------------------------------------------
FROM nginx:1.27-alpine

# 불필요한 기본 conf 제거
RUN rm -f /etc/nginx/conf.d/default.conf

# 정적 파일 배치
COPY --from=builder /app/out /usr/share/nginx/html

# 프론트 전용 vhost
COPY ./Nginx.conf /etc/nginx/conf.d/Nginx.conf

EXPOSE 80

CMD ["nginx","-g","daemon off;"]
