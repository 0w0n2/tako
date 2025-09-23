import type { AxiosError } from 'axios';

export type NormalizedHttpError = {
  name: string;
  message: string;
  status?: number;
  code?: string;
  data?: unknown;
  url?: string;
  canceled?: boolean; // ← 추가
};

export function normalizeAxiosError(err: unknown): NormalizedHttpError {
  const fallback = { name: 'Error', message: 'Unknown error' } as NormalizedHttpError;

  if (typeof err === 'object' && err !== null && 'isAxiosError' in err) {
    const e = err as AxiosError<any>;
    const isCanceled = e.code === 'ERR_CANCELED' || e.message === 'canceled';
    return {
      name: e.name || 'AxiosError',
      message:
        e.response?.data?.message ||
        e.message ||
        'Request failed',
      status: e.response?.status,
      code: e.code,
      data: e.response?.data,
      url: e.config?.baseURL
        ? `${e.config.baseURL}${e.config.url}`
        : e.config?.url,
      canceled: isCanceled, // ← 추가
    };
  }

  if (err instanceof DOMException && err.name === 'AbortError') {
    return { name: 'AbortError', message: '요청이 취소되었습니다.', canceled: true };
  }

  if (err instanceof Error) {
    return { name: err.name, message: err.message };
  }

  return fallback;
}
