#!/usr/bin/env node
/**
 * Server statis sederhana untuk membuka pratinjau desain di browser.
 * Tanpa dependency — hanya memakai modul bawaan Node.
 *
 * Dipakai oleh Freebuff preview:  node design-preview/serve.js
 * Port diambil dari env PORT (di-inject Freebuff), fallback 4173.
 */
const http = require("http");
const fs = require("fs");
const path = require("path");

const ROOT = __dirname;
const PORT = Number(process.env.PORT || 4173);
const HOST = "0.0.0.0";

const TYPES = {
  ".html": "text/html; charset=utf-8",
  ".svg": "image/svg+xml",
  ".css": "text/css; charset=utf-8",
  ".js": "text/javascript; charset=utf-8",
  ".json": "application/json; charset=utf-8",
  ".png": "image/png",
  ".jpg": "image/jpeg",
  ".jpeg": "image/jpeg",
  ".webp": "image/webp",
  ".txt": "text/plain; charset=utf-8",
};

const server = http.createServer((req, res) => {
  let pathname;
  try {
    pathname = decodeURIComponent(new URL(req.url, `http://${req.headers.host || "localhost"}`).pathname);
  } catch {
    res.writeHead(400, { "Content-Type": "text/plain; charset=utf-8" }).end("400 Bad request");
    return;
  }

  if (pathname.endsWith("/")) pathname += "index.html";
  const file = path.join(ROOT, pathname);

  // Cegah keluar dari folder design-preview/
  if (file !== ROOT && !file.startsWith(ROOT + path.sep)) {
    res.writeHead(403, { "Content-Type": "text/plain; charset=utf-8" }).end("403 Forbidden");
    return;
  }

  fs.readFile(file, (err, buf) => {
    if (err) {
      res.writeHead(404, { "Content-Type": "text/plain; charset=utf-8" }).end(`404 ${pathname}`);
      return;
    }
    res.writeHead(200, {
      "Content-Type": TYPES[path.extname(file).toLowerCase()] || "application/octet-stream",
      "Cache-Control": "no-store",
    });
    res.end(buf);
  });
});

server.listen(PORT, HOST, () => {
  console.log(`Pratinjau desain: http://${HOST}:${PORT}/`);
  console.log(`Root: ${ROOT}`);
});
