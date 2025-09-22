// lib/bc/wallet.ts
import api from '@/lib/api';

interface WalletAddressPayload {
  walletAddress: string;
}

export const sendWalletAddress = async (walletAddress: string) => {
  const payload: WalletAddressPayload = { walletAddress };
  const res = await api.post('/v1/members/me/wallet', payload);
  return res.data;
};