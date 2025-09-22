/** @type {import('next').NextConfig} */
const APP_STAGE = process.env.APP_STAGE || 'dev'; // dev | prod

const DOMAINS = {
  dev: { SITE: 'https://dev.tako.today', API: 'https://dev-api.tako.today' },
  prod: { SITE: 'https://tako.today', API: 'https://api.tako.today' },
};

const { SITE, API } = DOMAINS[APP_STAGE] || DOMAINS.dev;

module.exports = {
  images: {
    remotePatterns: [
      {
        protocol: 'https',
        hostname: 'bukadong-bucket.s3.ap-northeast-2.amazonaws.com',
        port: '',
        pathname: '/media/**',
        Unoptimized: true,
      },
    ],
    unoptimized: true,
  },

  reactStrictMode: true,
  swcMinify: true,

  // 클라이언트에 주입할 값
  env: {
    NEXT_PUBLIC_SITE_URL: SITE,
    NEXT_PUBLIC_API_BASE_URL: API,
    APP_STAGE, // 필요 시 클라이언트에서 참고 가능 (노출되어도 무방한 수준만!)
  },

  async redirects() {
    // prod일 때만 정규 도메인으로 정렬 (dev는 절대 리다이렉트 금지)
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
