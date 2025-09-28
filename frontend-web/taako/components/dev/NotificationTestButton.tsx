"use client";

import api from "@/lib/api";
import { useState } from "react";

export default function NotificationTestButton() {
	const [loading, setLoading] = useState(false);
	const [result, setResult] = useState<string | null>(null);

	const send = async () => {
		setLoading(true);
		setResult(null);
		try {
			const { data } = await api.post("/v1/notifications/test", undefined, { withCredentials: true });
			setResult(JSON.stringify(data));
		} catch (e: any) {
			setResult(e?.message ?? "error");
		} finally {
			setLoading(false);
		}
	};

	if (process.env.NEXT_PUBLIC_ENV === "prod") return null;

	return (
		<div className="fixed bottom-4 left-4 z-[1000]">
			<button className="px-3 py-1 text-xs rounded bg-gray-700 text-white hover:bg-gray-600" onClick={send} disabled={loading} title="/v1/notifications/test">
				{loading ? "Sending..." : "Test Noti"}
			</button>
			{result && <div className="mt-1 text-[10px] text-gray-400 max-w-[200px] break-all">{result}</div>}
		</div>
	);
}
