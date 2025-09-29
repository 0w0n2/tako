# Deployment Guide

## 개요

- 서비스명 : TAKO
- 팀명 :  부카동 (E104)
- 주요 구성
  - React(FE)
  - Springboot(BE)
  - FastAPI(AI)
  - MySQL, Redis(DB)
  - AWS EC2, Docker, Jenkins, Nginx, Grafana, Prometheus(Infra)

## 서버 환경

- OS : Ubuntu 22.04.5 LTS (Jammy)
- CPU : Intel(R) Xeon(R) CPU E5-2686 v4 @ 2.30GHz, 4 vCPU
- 메모리: 16 GiB RAM
- 가상화: Xen (AWS EC2)

### UFW 설정 및 포트

|포트|연결 서비스|비고|
|---|---|---|
|22|SSH||
|80|HTTP|Nginx Reverse Proxy|
|443|HTTPS|Nginx Reverse Proxy|
|8080|Springboot|Backend|
|8000|FastAPI|AI|
|3306|MySQL|DB|
|6379|Redis|DB|
|8080|Jenkins||
|3030|Grafana||
|9090|Prometheus||
|9100|node exporter||
|8080|cadvisor||
|9093|AlertManager||

> `sudo ufw deny <포트 번호>`를 통해 차단하거나 혹은 `sudo ufw delete allow <포트 번호>`로 규칙 삭제, 도커 컴포즈 파일 ports 부분 수정 가능

### 도메인 및 SSL 설정

- 도메인 : tako.today
- SSL 인증서 : Let's Encrypt (Certbot 활용)
  - `fullchain.pem`
  - `privkey.pem`
- 적용 범위 : Nginx Reverse Proxy를 통해 HTTPS(443) 적용, HTTP(80) 요청은 HTTPS로 리다이렉트
- 만료 주기 : 90일(3개월마다 재발급)

### 주요 외부 서비스

|서비스|용도|키 위치|연결 정보|발급|
|---|---|---|---|---|
|Alchemy|블록체인 이더리움 테스트넷 배포 플랫폼|`deploy/.env.dev`, `deploy/.env.prod`|-|-|
|AWS S3|파일 저장|`deploy/.env.dev`, `deploy/.env.prod`|버킷: `bukadong-bucket`, 리전:`ap-northeast-2`|AWS|

## 사전 설치 도구 및 버전

> docker compose 활용하므로 docker와 docker compose를 제외한 것들은 EC2 환경에 설치할 필요 없음

### Backend

- JVM : OpenJDK 17.0.2
- Framwork : Spring Boot 3.5.5

### Frontend

- Build : Node.js 220 (node:20-alpine)
- Package Manager : pnpm 10.13.1
- Framework : React 19.1.0

### AI

- Framework : FastAPI (Python 3.10.12)
- ASGI server : uvicorn

### Database

- MySQL : 8.0.x (mysql:8.0 tag)
- Redis : 8.x.x (redis:8 tag)

### Reverse Proxy

- Nginx : nginx:stable(docker container) - 1.27
- Configuration filepath : `S13P21E104/nginx/conf.d/nginx.conf`
- 80 -> HTTPS redirect
- 443에서 프록시:
  - `tako.today/` -> `tako_front:3000`
  - `tako.today/ai/` -> `tako_ai:8000`
  - `api.tako.today/` -> `tako_back:8080`
  - `dev.tako.today/` -> `tako_front_dev:3000`
  - `dev.tako.today/ai/` -> `tako_ai_dev:8000`
  - `dev-api.tako.today/` -> `tako_back_dev:8080`
- ssl 인증서 경로
  - `/etc/letsencrypt/live/tako.today/{fullchain.pem,privkey.pem}`

### Docker, Docker Compose

- Docker : 28.3.2 (build 578ccf6)
- Docker Compose : v2.24.4

```bash
# 1. 시스템 패키지 업데이트
sudo apt update && sudo apt upgrade -y

# 2. 필수 유틸리티 설치
sudo apt install -y git curl ca-certificates gnupg lsb-release

# 3. Docker 설치
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# 4. 현재 사용자에 Docker 권한 부여
sudo usermod -aG docker $USER
newgrp docker

# 5. Docker Compose 설치
DOCKER_CONFIG=${DOCKER_CONFIG:-$HOME/.docker}
mkdir -p $DOCKER_CONFIG/cli-plugins
curl -SL https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m) \
    -o $DOCKER_CONFIG/cli-plugins/docker-compose
chmod +x $DOCKER_CONFIG/cli-plugins/docker-compose

# 6. 설치 확인
docker --version
docker compose version
```

## 프로젝트 다운로드

```bash
# GitLab 프로젝트 클론
git clone https://lab.ssafy.com/s13-blochain-transaction-sub1/S13P21E104.git

# 디렉터리 이동
cd S13P21E104
```

## EC2 구조도

```
S13P21E104/
├─ backend-fastapi/
│  ├─ models/...
│  ├─ Dockerfile
│  └─ main.py
│
├─ backend-solidity/
│  ├─ contracts/...
│  ├─ scripts/...
│  └─ hardhat.config.js
│
├─ backend-spring/
│  └─ src/
│     ├─ Dockerfile
│     ├─ .dockerignore
│     ├─ package.json
│     └─ src/...
│
├─ frontend-web/
│  └─ taako/
│     ├─ Dockerfile
│     ├─ app/...
│     ├─ package.json
│     └─ components/...
│
└─ deploy/
   ├─ docker-compose.dev.yml
   ├─ docker-compose.prod.yml
   ├─ docker-compose.ai.yml
   └─ nginx/conf.d/
      └─ default.conf
```

## 환경 변수 설정

1. `.env.example`
    ```bash
    touch S13P21E104/deploy/.env.prod
    vi S13P21E104/deploy/.env.prod
    ```

    - `.env.example`을 보고 알맞은 값 입력 후 생성

## 빌드 및 실행 방법 및 배포

- 배포하기 전에 `.env` 파일과 docker, docker-compose 설치 필수
- 포트 80 / 443 / 22 포트는 개방
- ssl 인증서 생성 후 도커 볼륨 마운트 준비

```bash
# 1. Certbot 설치
sudo apt install -y certbot python3-cert-nginx

# 2. 인증서 발급
# -d 옵션 뒤에 발급받을 도메인 입력
sudo certbot --nginx -d <domain>

# 3. 발급 확인
sudo certbot certificates
```

- docker compose file 실행

```bash
# 배포 디렉터리 이동
cd deploy

# 네트워크 생성
docker network create tetonam-network || true

# 볼륨 생성(DB)
docker volume create mysql_prod_data
docker volume create redis_prod_data

# 배포(백그라운드 실행, 빌드 포함)
docker-compose --env-file .env.prod -f docker-compose.prod.yml -d --build

# 백엔드, 프론트엔드, mysql, redis, fastapi, nginx 띄워져있는지 확인 (UP 확인!)
docker ps -a
```

## 사연 시나리오

1. 회원가입 / 로그인
2. 메타마스크 카드 지갑 Web3 연결
3. 판매할 NFT 카드 Claim(NFT를 가진 카드로 경매 진행할 것이라면)
4. 경매등록
5. 경매진행
6. 구매자(최종 입찰자)와 판매자 간의 스마트 컨트랙트 진행