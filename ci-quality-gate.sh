#!/usr/bin/env bash
# ──────────────────────────────────────────────────────────────────────
# TungTung CI Quality Gate — runs locally before every push.
# Usage:  bash ci-quality-gate.sh
# ──────────────────────────────────────────────────────────────────────
set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m'

echo -e "${YELLOW}═══════════════════════════════════════════${NC}"
echo -e "${YELLOW}  TungTung CI Quality Gate${NC}"
echo -e "${YELLOW}═══════════════════════════════════════════${NC}"

PASS=0
FAIL=0

# ── 1. Android Unit Tests ──
echo ""
echo -e "${YELLOW}[1/3] Android Unit Tests${NC}"
cd "$(dirname "$0")"
if ./gradlew testDebugUnitTest --quiet 2>&1; then
    echo -e "${GREEN}  ✅ Android tests passed${NC}"
    PASS=$((PASS + 1))
else
    echo -e "${RED}  ❌ Android tests FAILED${NC}"
    FAIL=$((FAIL + 1))
fi

# ── 2. ArchUnit compliance (included in Android tests) ──
echo ""
echo -e "${YELLOW}[2/3] ArchUnit Compliance${NC}"
echo -e "${GREEN}  ✅ Included in Android unit tests${NC}"
PASS=$((PASS + 1))

# ── 3. UI→DTO violation check ──
echo ""
echo -e "${YELLOW}[3/3] UI→DTO Violation Check${NC}"
DTO_IMPORTS=$(grep -r "import hcmute.edu.vn.nguyenthetan.data.remote.dto" \
    app/src/main/java/hcmute/edu/vn/nguyenthetan/ui/ 2>/dev/null | wc -l || true)

if [ "$DTO_IMPORTS" -eq 0 ]; then
    echo -e "${GREEN}  ✅ Zero DTO imports in UI layer${NC}"
    PASS=$((PASS + 1))
else
    echo -e "${RED}  ❌ Found $DTO_IMPORTS DTO import(s) in UI layer${NC}"
    grep -r "import hcmute.edu.vn.nguyenthetan.data.remote.dto" \
        app/src/main/java/hcmute/edu/vn/nguyenthetan/ui/ 2>/dev/null || true
    FAIL=$((FAIL + 1))
fi

# ── Summary ──
echo ""
echo -e "${YELLOW}═══════════════════════════════════════════${NC}"
if [ "$FAIL" -eq 0 ]; then
    echo -e "${GREEN}  ALL $PASS CHECKS PASSED ✅${NC}"
    exit 0
else
    echo -e "${RED}  $FAIL CHECK(S) FAILED ❌ ($PASS passed)${NC}"
    exit 1
fi
