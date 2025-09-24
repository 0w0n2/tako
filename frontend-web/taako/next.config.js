/** @type {import('next').NextConfig} */
const APP_STAGE = process.env.APP_STAGE || 'dev'; // dev | prod

const DOMAINS = {
  dev: {
    SITE: 'https://dev.tako.today',
    API: 'https://dev-api.tako.today',
    AI_API: 'https://dev-api.tako.today/ai'

  },
  prod: {
    SITE: 'https://tako.today',
    API: 'https://api.tako.today',
    AI_API: 'https://tako.today/ai'
  },
};

const { SITE, API, AI_API } = DOMAINS[APP_STAGE] || DOMAINS.dev;

// AI API가 로컬인 경우 프록시 URL 사용
const getAI_API_URL = () => {
  if (AI_API.includes('127.0.0.1') || AI_API.includes('localhost')) {
    return '/api/ai'; // 프록시 경로 사용
  }
  return AI_API; // 원본 URL 사용
};

module.exports = {
  reactStrictMode: true,
  swcMinify: true,

  // 이미지 도메인 설정
  images: {
    remotePatterns: [
      {
        protocol: 'https',
        hostname: 'bukadong-bucket.s3.ap-northeast-2.amazonaws.com',
      },
    ],
  },

  // 클라이언트에 주입할 값
  env: {
    NEXT_PUBLIC_SITE_URL: SITE,
    NEXT_PUBLIC_API_BASE_URL: API,
    NEXT_PUBLIC_AI_API_BASE_URL: getAI_API_URL(),
    APP_STAGE,
    NEXT_PUBLIC_TAKO_NFT,
    NEXT_PUBLIC_SPENDER_ADDRESS
  },

  async rewrites() {
    const rewrites = [];

    // AI API 프록시 설정 (CORS 해결)
    if (AI_API.includes('127.0.0.1') || AI_API.includes('localhost')) {
      rewrites.push({
        source: '/api/ai/:path*',
        destination: `${AI_API}/:path*`,
      });
    }

    return rewrites;
  },

  async redirects() {
    if (APP_STAGE === 'prod') {
      return [
        {
          source: '/:path*',
          has: [{ type: 'host', value: 'tako.today' }],
          destination: `${SITE}/:path*`,
          permanent: true,
        },
      ];
    }
    return [];
  },
};
