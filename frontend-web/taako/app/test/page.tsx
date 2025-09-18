'use client'

import { Button } from "@/components/ui/button";
import { useAuthStore } from "@/stores/useAuthStore";

export default function TestPage() {
  const refreshAccessToken = useAuthStore((state) => state.refreshAccessToken);

  const handleClick = async () => {
    const newToken = await refreshAccessToken();
    if (newToken) {
        console.log("성공")
    } else {
        console.log("실패")
    }
  };

  return (
    <div className="flex justify-center items-center">
      <Button variant="destructive" onClick={handleClick}>
        AccessToken 재요청
      </Button>
    </div>
  );
}
