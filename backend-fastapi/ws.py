from typing import Dict, Set
from fastapi import WebSocket


class WSManager:
    def __init__(self):
        self._conns: Dict[str, Set[WebSocket]] = {}

    async def connect(self, job_id: str, ws: WebSocket):
        await ws.accept()
        self._conns.setdefault(job_id, set()).add(ws)

    def disconnect(self, job_id: str, ws: WebSocket):
        self._conns.get(job_id, set()).discard(ws)

    async def notify(self, job_id: str, payload: dict):
        for ws in list(self._conns.get(job_id, set())):
            try:
                await ws.send_json(payload)
            except Exception:
                self.disconnect(job_id, ws)


ws_manager = WSManager()
