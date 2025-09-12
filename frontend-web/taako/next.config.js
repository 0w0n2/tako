/** @type {import('next').NextConfig} */
const APP_STAGE = process.env.APP_STAGE || 'dev';

const DOMAINS = {
  dev:  { SITE: 'https://dev.tako.today', API: 'https://dev-api.tako.today' },
  prod: { SITE: 'https://tako.today',     API: 'https://api.tako.today' },
};
const { SITE, API } = DOMAINS[APP_STAGE] || DOMAINS.dev;

module.exports = {
  reactStrictMode: true,
  swcMinify: true,

  // 정적 산출물
  output: 'export',
  // Next/Image 사용 중이면 정적 export 호환
  images: { unoptimized: true },

  env: {
    NEXT_PUBLIC_SITE_URL: SITE,
    NEXT_PUBLIC_API_BASE_URL: API,
    APP_STAGE,
  },

  // SPA 라우팅 필요 시 (Next export 시 404 방지)
  trailingSlash: false,
};
