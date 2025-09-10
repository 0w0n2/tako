#!/usr/bin/env node
/*
  cards.json에서 이미지 URL을 정규식으로 추출하여 다운로드합니다.
  - 사용법: node scripts/download-images-from-cards.js [--all | --lines N] [--out DIR]
  - 기본값: 전체 파일 읽기(--all), out=public/card-images
*/

const fs = require('fs');
const path = require('path');
const https = require('https');
const http = require('http');

function parseArgs() {
  const args = process.argv.slice(2);
  const options = { all: true, lines: null, out: path.join('public', 'card-images') };
  for (let i = 0; i < args.length; i++) {
    const arg = args[i];
    if (arg === '--all') {
      options.all = true;
      options.lines = null;
    } else if (arg === '--lines' && i + 1 < args.length) {
      options.all = false;
      options.lines = Number(args[++i]);
    } else if (arg.startsWith('--lines=')) {
      options.all = false;
      options.lines = Number(arg.split('=')[1]);
    } else if (arg === '--out' && i + 1 < args.length) {
      options.out = args[++i];
    } else if (arg.startsWith('--out=')) {
      options.out = arg.split('=')[1];
    }
  }
  if (options.all === false) {
    if (!Number.isFinite(options.lines) || options.lines <= 0) {
      console.error('유효하지 않은 --lines 값입니다. 양의 정수를 입력하세요.');
      process.exit(1);
    }
  }
  return options;
}

function readContent(filePath, numLinesOrNull) {
  const content = fs.readFileSync(filePath, 'utf8');
  if (numLinesOrNull == null) return content;
  const lines = content.split(/\r?\n/).slice(0, numLinesOrNull);
  return lines.join('\n');
}

function extractUrls(text) {
  // http(s)로 시작하고 공백/따옴표로 끝나는 단순 URL 정규식
  const urlRegex = /https?:\/\/[^"'\s,}\]]+/g;
  const urls = new Set();
  let match;
  while ((match = urlRegex.exec(text)) !== null) {
    urls.add(match[0]);
  }
  return Array.from(urls);
}

function ensureDirectory(dirPath) {
  if (!fs.existsSync(dirPath)) {
    fs.mkdirSync(dirPath, { recursive: true });
  }
}

function urlToFilePath(baseDir, urlStr) {
  const { pathname } = new URL(urlStr);
  // 예: https://images.pokemontcg.io/swsh12/160_hires.png
  // => out/swsh12/160_hires.png
  const parts = pathname.split('/').filter(Boolean);
  return path.join(baseDir, ...parts);
}

function downloadFile(urlStr, destPath) {
  return new Promise((resolve, reject) => {
    ensureDirectory(path.dirname(destPath));
    const file = fs.createWriteStream(destPath);
    const lib = urlStr.startsWith('https') ? https : http;
    const req = lib.get(urlStr, (res) => {
      if (res.statusCode && res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
        // redirect 따라가기
        const redirected = res.headers.location.startsWith('http')
          ? res.headers.location
          : new URL(res.headers.location, urlStr).toString();
        file.close(() => fs.unlink(destPath, () => {
          downloadFile(redirected, destPath).then(resolve).catch(reject);
        }));
        return;
      }
      if (res.statusCode !== 200) {
        file.close(() => fs.unlink(destPath, () => {
          reject(new Error(`다운로드 실패(${res.statusCode}): ${urlStr}`));
        }));
        return;
      }
      res.pipe(file);
      file.on('finish', () => file.close(resolve));
    });
    req.on('error', (err) => {
      file.close(() => fs.unlink(destPath, () => reject(err)));
    });
    req.setTimeout(30000, () => {
      req.destroy(new Error('요청 시간 초과'));
    });
  });
}

async function main() {
  try {
    const { lines, out } = parseArgs();
    const cardsJsonPath = path.join(process.cwd(), 'components', 'cards', 'cards.json');
    const text = readContent(cardsJsonPath, lines);
    const urls = extractUrls(text);
    if (urls.length === 0) {
      console.log('URL이 발견되지 않았습니다.');
      return;
    }
    console.log(`총 ${urls.length}개의 URL 검색됨. 저장 경로: ${out}`);
    ensureDirectory(out);
    const tasks = urls.map(async (urlStr) => {
      const dest = urlToFilePath(out, urlStr);
      if (fs.existsSync(dest)) {
        console.log(`이미 존재: ${dest}`);
        return;
      }
      try {
        await downloadFile(urlStr, dest);
        console.log(`저장 완료: ${dest}`);
      } catch (e) {
        console.warn(`실패: ${urlStr} -> ${e.message}`);
      }
    });
    await Promise.all(tasks);
    console.log('다운로드 작업 완료.');
  } catch (err) {
    console.error(err.message || err);
    process.exit(1);
  }
}

main();


