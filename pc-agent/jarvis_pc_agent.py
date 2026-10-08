#!/usr/bin/env python3
"""Jarvis PC agent: a small local HTTP server that lets the Jarvis phone app
control this computer (run commands, read files, open apps/links).

Run it with:   python3 jarvis_pc_agent.py
Stop it with:  Ctrl+C

On first run it generates a random pairing token and prints it - copy that
token and this computer's address into Jarvis's Settings screen on your
phone. See README.md in this folder for networking options (same Wi-Fi vs.
Tailscale) and the security tradeoffs of running this at all.
"""

import argparse
import json
import os
import platform
import secrets
import subprocess
import sys
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path

CONFIG_DIR = Path.home() / ".jarvis-pc-agent"
TOKEN_FILE = CONFIG_DIR / "token"
COMMAND_TIMEOUT_SECONDS = 30
MAX_OUTPUT_CHARS = 8000
MAX_FILE_CHARS = 200_000


def load_or_create_token() -> str:
    CONFIG_DIR.mkdir(parents=True, exist_ok=True)
    if TOKEN_FILE.exists():
        return TOKEN_FILE.read_text().strip()
    token = secrets.token_urlsafe(32)
    TOKEN_FILE.write_text(token)
    try:
        os.chmod(TOKEN_FILE, 0o600)
    except OSError:
        pass
    return token


def open_path(path: str) -> None:
    system = platform.system()
    if system == "Windows":
        os.startfile(path)  # type: ignore[attr-defined]
    elif system == "Darwin":
        subprocess.run(["open", path], check=True)
    else:
        subprocess.run(["xdg-open", path], check=True)


def truncate(text: str, limit: int) -> str:
    if len(text) <= limit:
        return text
    return text[:limit] + f"\n... (tronqué, {len(text) - limit} caractères omis)"


class Handler(BaseHTTPRequestHandler):
    token: str = ""

    def _send_json(self, status: int, payload: dict) -> None:
        body = json.dumps(payload).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def _authorized(self) -> bool:
        expected = f"Bearer {Handler.token}"
        return self.headers.get("Authorization") == expected

    def _read_json_body(self) -> dict:
        length = int(self.headers.get("Content-Length", 0))
        if length == 0:
            return {}
        raw = self.rfile.read(length)
        return json.loads(raw.decode("utf-8")) if raw else {}

    def log_message(self, format: str, *args) -> None:  # noqa: A002 - stdlib signature
        sys.stderr.write(f"[jarvis-pc-agent] {self.address_string()} - {format % args}\n")

    def do_GET(self) -> None:
        if self.path == "/ping":
            if not self._authorized():
                self._send_json(401, {"error": "Jeton invalide ou manquant."})
                return
            self._send_json(200, {"status": "ok", "hostname": platform.node(), "os": platform.system()})
            return
        self._send_json(404, {"error": "Route inconnue."})

    def do_POST(self) -> None:
        if not self._authorized():
            self._send_json(401, {"error": "Jeton invalide ou manquant."})
            return

        try:
            body = self._read_json_body()
        except (ValueError, json.JSONDecodeError):
            self._send_json(400, {"error": "Corps JSON invalide."})
            return

        try:
            if self.path == "/run_command":
                self._handle_run_command(body)
            elif self.path == "/read_file":
                self._handle_read_file(body)
            elif self.path == "/list_files":
                self._handle_list_files(body)
            elif self.path == "/open_path":
                self._handle_open_path(body)
            else:
                self._send_json(404, {"error": "Route inconnue."})
        except Exception as exc:  # noqa: BLE001 - always report back as JSON, never crash the server
            self._send_json(500, {"error": str(exc)})

    def _handle_run_command(self, body: dict) -> None:
        command = body.get("command", "")
        if not command:
            self._send_json(400, {"error": "Paramètre 'command' manquant."})
            return
        try:
            result = subprocess.run(
                command,
                shell=True,
                capture_output=True,
                text=True,
                timeout=COMMAND_TIMEOUT_SECONDS,
            )
            self._send_json(200, {
                "exit_code": result.returncode,
                "stdout": truncate(result.stdout, MAX_OUTPUT_CHARS),
                "stderr": truncate(result.stderr, MAX_OUTPUT_CHARS),
            })
        except subprocess.TimeoutExpired:
            self._send_json(200, {
                "exit_code": -1,
                "stdout": "",
                "stderr": f"La commande a dépassé le délai de {COMMAND_TIMEOUT_SECONDS}s et a été interrompue.",
            })

    def _handle_read_file(self, body: dict) -> None:
        path = body.get("path", "")
        if not path:
            self._send_json(400, {"error": "Paramètre 'path' manquant."})
            return
        target = Path(path).expanduser()
        if not target.is_file():
            self._send_json(404, {"error": f"Fichier introuvable : {path}"})
            return
        content = target.read_text(errors="replace")
        self._send_json(200, {"path": str(target), "content": truncate(content, MAX_FILE_CHARS)})

    def _handle_list_files(self, body: dict) -> None:
        path = body.get("path") or str(Path.home())
        target = Path(path).expanduser()
        if not target.is_dir():
            self._send_json(404, {"error": f"Dossier introuvable : {path}"})
            return
        entries = sorted(
            [{"name": e.name, "type": "dir" if e.is_dir() else "file"} for e in target.iterdir()],
            key=lambda e: (e["type"] != "dir", e["name"].lower()),
        )
        self._send_json(200, {"path": str(target), "entries": entries})

    def _handle_open_path(self, body: dict) -> None:
        path = body.get("path", "")
        if not path:
            self._send_json(400, {"error": "Paramètre 'path' manquant."})
            return
        open_path(path)
        self._send_json(200, {"result": f"Ouvert : {path}"})


def main() -> None:
    parser = argparse.ArgumentParser(description="Serveur local pour contrôler ce PC depuis Jarvis.")
    parser.add_argument("--port", type=int, default=8765)
    parser.add_argument("--host", default="0.0.0.0", help="Interface d'écoute (0.0.0.0 = toutes, LAN inclus).")
    args = parser.parse_args()

    Handler.token = load_or_create_token()

    server = ThreadingHTTPServer((args.host, args.port), Handler)
    print("=== Jarvis PC agent ===")
    print(f"Écoute sur {args.host}:{args.port}")
    print(f"Jeton d'appairage (à coller dans Jarvis > Paramètres) : {Handler.token}")
    print(f"(stocké dans {TOKEN_FILE} - supprime ce fichier pour forcer un nouveau jeton)")
    print("Ctrl+C pour arrêter.")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        pass


if __name__ == "__main__":
    main()
