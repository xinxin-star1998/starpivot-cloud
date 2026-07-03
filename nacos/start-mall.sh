#!/usr/bin/env bash
# 本地启动商城微服务（打印命令；使用 -run 时在后台启动各模块）
set -euo pipefail

IMPORT_CONFIG=false
SKIP_STATIC=false
RUN=false

while [[ $# -gt 0 ]]; do
  case "$1" in
    --import-config) IMPORT_CONFIG=true; shift ;;
    --skip-static)   SKIP_STATIC=true; shift ;;
    --run)           RUN=true; shift ;;
    *) echo "Unknown option: $1"; exit 1 ;;
  esac
done

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

if [[ -z "${JWT_SECRET:-}" || ${#JWT_SECRET} -lt 32 ]]; then
  echo "WARN: JWT_SECRET 未设置或不足 32 字符"
  echo '  export JWT_SECRET="dev-local-jwt-secret-must-be-at-least-32-chars"'
fi
if [[ -z "${INTERNAL_SERVICE_TOKEN:-}" ]]; then
  echo "WARN: 建议设置 INTERNAL_SERVICE_TOKEN"
  echo '  export INTERNAL_SERVICE_TOKEN="dev-internal-token"'
fi

if [[ "${IMPORT_CONFIG}" == "true" ]]; then
  bash "${SCRIPT_DIR}/import-mall-config.sh"
fi

declare -a MODULES=(
  "starpivot-mall/starpivot-mall-member:9206"
  "starpivot-mall/starpivot-mall-product:9207"
  "starpivot-mall/starpivot-mall-ware:9208"
  "starpivot-mall/starpivot-mall-promotion:9212"
  "starpivot-mall/starpivot-mall-order:9209"
)
if [[ "${SKIP_STATIC}" != "true" ]]; then
  MODULES+=("starpivot-mall/starpivot-mall-app:9205")
fi

echo ""
echo "商城微服务（repo: ${REPO_ROOT}）"
echo "网关请单独启动: mvn spring-boot:run -pl starpivot-gateway"
echo ""

for entry in "${MODULES[@]}"; do
  module="${entry%%:*}"
  port="${entry##*:}"
  cmd="cd \"${REPO_ROOT}\" && SERVER_PORT=${port} mvn spring-boot:run -pl ${module} -am"
  if [[ "${RUN}" == "true" ]]; then
    echo "Starting ${module} on :${port}..."
    ( eval "${cmd}" ) &
    sleep 3
  else
    echo "${module} (:${port})"
    echo "  ${cmd}"
  fi
done

if [[ "${RUN}" == "true" ]]; then
  echo ""
  echo "Services started in background. Verify via gateway :8080"
fi
