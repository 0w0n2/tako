// lib/user/uuid.ts
'use client';

const KEY = 'TAKO_USER_UUID';
const TIMESTAMP_KEY = 'TAKO_USER_UUID_TIMESTAMP';
const RESET_INTERVAL = 10 * 1000; // 10초

function genUUID() {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID();
  }
  return 'u_' + Math.random().toString(36).slice(2) + Date.now().toString(36);
}

/** UUID 리셋 콜백 함수 타입 */
type UuidResetCallback = () => void;

/** UUID 리셋 콜백 함수들 */
const resetCallbacks = new Set<UuidResetCallback>();

/** UUID 리셋 콜백 등록 */
export function onUuidReset(callback: UuidResetCallback): () => void {
  resetCallbacks.add(callback);
  return () => resetCallbacks.delete(callback);
}

/** UUID 리셋 콜백 실행 */
function notifyUuidReset() {
  resetCallbacks.forEach(callback => callback());
}

/** 브라우저 localStorage에 고정 저장(없으면 생성), 10초마다 자동 리셋 */
export function getOrCreateUserUuid(): string {
  try {
    const now = Date.now();
    const existing = localStorage.getItem(KEY);
    const timestamp = localStorage.getItem(TIMESTAMP_KEY);

    // 기존 UUID가 있고 10초가 지나지 않았다면 기존 UUID 반환
    if (existing && timestamp) {
      const lastReset = parseInt(timestamp, 10);
      if (now - lastReset < RESET_INTERVAL) {
        return existing;
      }
    }

    // 10초가 지났거나 UUID가 없으면 새로 생성
    const newId = genUUID();
    localStorage.setItem(KEY, newId);
    localStorage.setItem(TIMESTAMP_KEY, now.toString());

    // UUID가 리셋되었음을 알림
    if (existing) {
      notifyUuidReset();
    }

    return newId;
  } catch {
    // SSR/프라이빗모드 등 예외 시에도 무조건 하나 반환
    return genUUID();
  }
}

/** 수동으로 UUID 리셋 */
export function resetUserUuid(): string {
  try {
    const newId = genUUID();
    localStorage.setItem(KEY, newId);
    localStorage.setItem(TIMESTAMP_KEY, Date.now().toString());
    notifyUuidReset();
    return newId;
  } catch {
    return genUUID();
  }
}
