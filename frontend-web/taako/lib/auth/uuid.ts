// lib/user/uuid.ts
'use client';

const KEY = 'TAKO_USER_UUID';

function genUUID() {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID();
  }
  return 'u_' + Math.random().toString(36).slice(2) + Date.now().toString(36);
}

/** 브라우저 localStorage에 고정 저장(없으면 생성) */
export function getOrCreateUserUuid(): string {
  try {
    const existing = localStorage.getItem(KEY);
    if (existing) return existing;
    const id = genUUID();
    localStorage.setItem(KEY, id);
    return id;
  } catch {
    // SSR/프라이빗모드 등 예외 시에도 무조건 하나 반환
    return genUUID();
  }
}
