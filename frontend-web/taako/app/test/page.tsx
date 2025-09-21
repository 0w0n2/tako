'use client'

import { Button } from "@/components/ui/button";
import { useAuthStore } from "@/stores/useAuthStore";
import { useState, useEffect } from "react";

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

  const [now, handleNow] = useState<string>("")
  useEffect(() => {
    const interval = setInterval(() => {
      handleNow(new Date().toLocaleTimeString());
    }, 1000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="flex justify-center items-center">
      <Button variant="destructive" onClick={handleClick}>
        AccessToken 재요청
      </Button>
      <h2>현재 시간 : {now}</h2>
    </div>
  );
}
