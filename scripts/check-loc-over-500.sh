#!/usr/bin/env bash
# 全仓库 >500 行扫描（CI / Linux 用）
set -euo pipefail
root="$(cd "$(dirname "$0")/.." && pwd)"
found=0
while IFS= read -r -d '' file; do
  case "$file" in
    */build/*|*/.gradle/*|*/bin/*|*/.idea/*) continue ;;
  esac
  lines=$(wc -l < "$file" | tr -d ' ')
  if [ "$lines" -gt 500 ]; then
    rel="${file#"$root"/}"
    printf '%5d  %s\n' "$lines" "$rel"
    found=1
  fi
done < <(find "$root" \( -name '*.kt' -o -name '*.java' \) -type f -print0)
if [ "$found" -ne 0 ]; then
  echo "LOC gate failed: files over 500 lines listed above." >&2
  exit 1
fi
echo "LOC gate OK: no .kt/.java files over 500 lines."
