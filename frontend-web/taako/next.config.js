/** @type {import('next').NextConfig} */
const APP_STAGE = process.env.APP_STAGE || 'dev'; // dev | prod

const DOMAINS = {
  dev:  { SITE: 'https://dev.tako.today',  API: 'https://dev-api.tako.today' },
  prod: { SITE: 'https://tako.today',       API: 'https://api.tako.today' },
};

const { SITE, API } = DOMAINS[APP_STAGE] || DOMAINS.dev;

module.exports = {
  reactStrictMode: true,
  swcMinify: true,

  // 이미지 도메인 설정
  images: {
    remotePatterns: [
      {
        protocol: 'https',
        hostname: 'bukadong-bucket.s3.ap-northeast-2.amazonaws.com',
        port: '',
        pathname: '/media/**',
      },
      {
        protocol: 'https',
        hostname: 'dev-api.tako.today',
        port: '',
        pathname: '/media/**',
      },
      {
        protocol: 'https',
        hostname: 'api.tako.today',
        port: '',
        pathname: '/media/**',
      },
    ],
  },

  // 클라이언트에 주입할 값
  env: {
    NEXT_PUBLIC_SITE_URL: SITE,
    NEXT_PUBLIC_API_BASE_URL: API,
    APP_STAGE,
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
